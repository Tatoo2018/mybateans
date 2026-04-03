package com.jhappy.mybateans.hyperlink;

import com.jhappy.mybateans.indexing.MyBatisIndexer;
import com.jhappy.mybateans.indexing.MyBatisIndexerFactory;
import com.jhappy.mybateans.util.NbUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;

import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;

import org.netbeans.api.java.source.JavaSource;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import org.netbeans.api.java.source.ClasspathInfo;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.cookies.EditorCookie;
import org.openide.text.Line;

import org.netbeans.modules.editor.NbEditorUtilities;

import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;

import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

@MimeRegistration(
        mimeType = "text/xml",
        service = HyperlinkProviderExt.class
)
public class MyBatisXmlHyperlinkProvider implements HyperlinkProviderExt {

    //Properties available for class FQN names or aliases
    private static final Set<String> TYPE_REF_ATTRS = Set.of(
            "resultType",
            "parameterType",
            "ofType",
            "javaType",
            "type"
    );

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        AttributeInfo attr = getAttributeAt(doc, offset);
        if (attr == null) {
            return false;
        }

        return "namespace".equals(attr.attrName)
                || "id".equals(attr.attrName) || TYPE_REF_ATTRS.contains(attr.attrName)
                || ("package".equals(attr.tabName) && "name".equals(attr.attrName));
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        AttributeInfo attr = getAttributeAt(doc, offset);

        if (attr == null) {
            return null;
        }

        //
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<XMLTokenId> ts = th.tokenSequence(XMLTokenId.language());

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token<XMLTokenId> token = ts.token();

        if (token.id() == XMLTokenId.VALUE) {
            int start = ts.offset();
            int end = start + token.length();
            return new int[]{start, end};
        }

        return null;
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {

        AttributeInfo attr = getAttributeAt(doc, offset);
        if (attr == null) {
            return;
        }

        FileObject xmlfile = NbEditorUtilities.getFileObject(doc);

        Project project = FileOwnerQuery.getOwner(xmlfile);

        if ("namespace".equals(attr.attrName)) {

            jumpToClass(attr.attrValue, doc);

        } else if ("id".equals(attr.attrName)) {

            String namespace = findNamespace(doc);

            jumpToMethod(namespace, attr.attrValue, xmlfile);

        } else if (TYPE_REF_ATTRS.contains(attr.attrName)) {

            String alias = attr.attrValue;

            jumpByClassOrAlias(project, alias, xmlfile, doc);
        } else if ("name".equals(attr.attrName) && "package".equals(attr.tabName)) {

            String packagename = attr.attrValue;
            jumpToPackage(packagename, xmlfile);

        }

    }

    public static boolean validateAlias(FileObject xmlfile, String alias) {

        Project project = FileOwnerQuery.getOwner(xmlfile);

        TargetLocation configFo = findTypeAliasFQN(project, alias);
        if (configFo != null) {
            return true;
        }
        String resolved = resolveFromPackages(xmlfile, alias);
        if (resolved != null) {
            return true;
        }

        boolean exists = MyBatisIndexer.existsJavaType(xmlfile, alias);

        return exists;
    }

    public static void jumpByClassOrAlias(Project project, String alias, FileObject xmlfile, Document doc) {

        TargetLocation configFo = findTypeAliasFQN(project, alias);
        if (configFo != null) {
            try {
                openAtEditor(configFo);
                return;
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            String resolved = resolveFromPackages(xmlfile, alias);
            if (resolved != null) {
                jumpToClass(resolved, doc);
                return;
            }
        }

        jumpToClass(alias, doc);
        return;
    }

    private static String resolveFromPackages(FileObject xmlfile, String simpleName) {

        Project project = FileOwnerQuery.getOwner(xmlfile);

        Set<String> packages = findTypeAliasPackages(project);

        try {
            ClasspathInfo cpInfo = ClasspathInfo.create(xmlfile);
            JavaSource js = JavaSource.create(cpInfo);

            final String[] result = new String[1];

            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                for (String pkg : packages) {
                    String fqn = pkg + "." + simpleName;

                    TypeElement clazz = cc.getElements().getTypeElement(fqn);
                    if (clazz != null) {
                        result[0] = fqn;
                        return;
                    }
                }
            }, true);

            return result[0];

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        return null;
    }

    /**
     *
     * @param xmlfile
     * @param alias
     * @return
     */
    public static TargetLocation findTypeAliasFQN(Project project, String alias) {

        if (project == null) {
            return null;
        }

        List<FileObject> roots = NbUtil.getRootsForSearch(project);

        try {

            QuerySupport querySupport = QuerySupport.forRoots(MyBatisIndexerFactory.INDEXER_NAME, MyBatisIndexerFactory.version, roots.toArray(new FileObject[0]));

            //
            Collection<? extends IndexResult> results;

            results = querySupport.query("typeAlias", alias, QuerySupport.Kind.EXACT, "typeAlias", "typeAlias_offset", "typeAlias_fqn");

            for (IndexResult r : results) {

                FileObject xmlFile = r.getFile();

                String offsetStr = r.getValue("typeAlias_offset");

                String fqn = r.getValue("typeAlias_fqn");

                int offset = (offsetStr != null) ? Integer.parseInt(offsetStr) : 0;

                if (xmlFile != null) {
                    return new TargetLocation(xmlFile, offset);
                }

            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        AttributeInfo attr = getAttributeAt(doc, offset);
        if (attr == null) {
            return null;
        }

        if ("namespace".equals(attr.attrName)) {
            return "Go to Java Mapper: " + attr.attrValue;
        } else if ("id".equals(attr.attrName)) {
            return "Go to Method: " + attr.attrValue;
        }

        return null;
    }

    private static Set<String> findTypeAliasPackages(Project project) {
        Set<String> packages = new HashSet<>();

        if (project == null) {
            return packages;
        }

        List<FileObject> roots = NbUtil.getRootsForSearch(project);

        try {
            QuerySupport qs = QuerySupport.forRoots(
                    MyBatisIndexerFactory.INDEXER_NAME,
                    MyBatisIndexerFactory.version,
                    roots.toArray(new FileObject[0])
            );

            Collection<? extends IndexResult> results
                    = qs.query("typeAlias_package", "", QuerySupport.Kind.PREFIX,
                            "typeAlias_package");

            for (IndexResult r : results) {
                String pkg = r.getValue("typeAlias_package");
                if (pkg != null) {
                    packages.add(pkg);
                }
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return packages;
    }

    private AttributeInfo getAttributeAt(Document doc, int offset) {
        final AttributeInfo[] result = new AttributeInfo[1];

        doc.render(() -> {
            TokenHierarchy<?> th = TokenHierarchy.get(doc);
            if (th == null) {
                return;
            }

            TokenSequence<XMLTokenId> ts = th.tokenSequence(XMLTokenId.language());
            if (ts == null) {
                return;
            }

            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<XMLTokenId> token = ts.token();
            if (token.id() != XMLTokenId.VALUE) {
                return;
            }

            String value = token.text().toString();
            if (value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }

            String attrName = null;
            String tagName = null;

            // 1. 属性名 (ARGUMENT) を求めて遡る
            while (ts.movePrevious()) {
                Token<XMLTokenId> t = ts.token();
                if (t.id() == XMLTokenId.ARGUMENT) {
                    attrName = t.text().toString();
                    break; // 属性名が見つかったらタグ名探しへ
                }
            }

            // 2. タグ名 (TAG) を求めてさらに遡る
            if (attrName != null) {
                while (ts.movePrevious()) {
                    Token<XMLTokenId> t = ts.token();
                    // 開始タグ (<select, <mapper など) を探す
                    if (t.id() == XMLTokenId.TAG && t.text().toString().startsWith("<")) {
                        // "<" を除いた純粋なタグ名を取得
                        tagName = t.text().toString().substring(1).trim();
                        break;
                    }
                }
            }

            if (tagName != null && attrName != null) {
                // タグ名、属性名、値をセットで返す
                result[0] = new AttributeInfo(tagName, attrName, value);
            }
        });

        return result[0];
    }

    /**
     *
     * @param doc
     * @return
     */
    private String findNamespace(Document doc) {

        final String[] result = new String[1];

        doc.render(() -> {

            TokenHierarchy<?> th = TokenHierarchy.get(doc);
            TokenSequence<XMLTokenId> ts = th.tokenSequence(XMLTokenId.language());

            while (ts.moveNext()) {

                Token<XMLTokenId> token = ts.token();

                if (token.id() == XMLTokenId.ARGUMENT
                        && "namespace".contentEquals(token.text())) {

                    if (!ts.moveNext()) {
                        return;
                    }
                    if (!ts.moveNext()) {
                        return;
                    }

                    Token<XMLTokenId> valueToken = ts.token();

                    if (valueToken.id() == XMLTokenId.VALUE) {
                        String val = valueToken.text().toString();
                        result[0] = val.substring(1, val.length() - 1);
                        return;
                    }
                }
            }
        });

        return result[0];
    }

    /**
     *
     * @param fqn
     * @param doc
     */
    private static void jumpToClass(String fqn, Document doc) {

        try {

            FileObject fo = NbEditorUtilities.getFileObject(doc);
            if (fo == null) {
                return;
            }

            ClasspathInfo cpInfo = ClasspathInfo.create(fo);
            JavaSource js = JavaSource.create(cpInfo);

            js.runUserActionTask(cc -> {

                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                TypeElement clazz = cc.getElements().getTypeElement(fqn);
                if (clazz == null) {
                    return;
                }

                ElementOpen.open(cpInfo, clazz);

            }, true);

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    /**
     *
     * @param namespace
     * @param id
     * @param xmlfile
     */
    private void jumpToMethod(String namespace, String id, FileObject xmlfile) {

        try {
            ClasspathInfo cpInfo = ClasspathInfo.create(xmlfile);
            JavaSource js = JavaSource.create(cpInfo);

            js.runUserActionTask(cc -> {

                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                TypeElement clazz = cc.getElements().getTypeElement(namespace);

                if (clazz == null) {
                    return;
                }

                for (Element e : clazz.getEnclosedElements()) {

                    if (e instanceof ExecutableElement method) {

                        if (method.getSimpleName().toString().equals(id)) {
                            ElementOpen.open(cpInfo, method);

                            return;
                        }
                    }
                }

            }, true);

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    // =========================
    // DTO
    // =========================
    private static class AttributeInfo {

        final String attrName;
        final String attrValue;
        final String tabName;

        AttributeInfo(String tabName, String attrName, String attrValue) {
            this.tabName = tabName;
            this.attrName = attrName;
            this.attrValue = attrValue;
        }
    }

    /**
     *
     * @param location
     * @throws DataObjectNotFoundException
     * @throws IOException
     */
    private static void openAtEditor(TargetLocation location) throws DataObjectNotFoundException, IOException {

        if (location != null && location.file != null) {

            DataObject xmlDobj = DataObject.find(location.file);
            EditorCookie ec = xmlDobj.getLookup().lookup(EditorCookie.class);

            if (ec != null) {

                //
                //StyledDocument xmlDoc = ec.openDocument();
                ec.open();

                //
                if (location.offset != -1) {
                    final int jumpPos = location.offset;

                    NbDocument.openDocument(location.file, jumpPos, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                }
            }
        }
    }

    public static boolean isExistPackage(String packageName, FileObject xmlfile) {

        // 1. ソースパスからフォルダを探す
        org.netbeans.api.java.classpath.ClassPath cp
                = org.netbeans.api.java.classpath.ClassPath.getClassPath(xmlfile, org.netbeans.api.java.classpath.ClassPath.SOURCE);

        if (cp == null) {
            return false;
        }

        String resourcePath = packageName.replace('.', '/');
        FileObject pkgFolder = cp.findResource(resourcePath);

        return pkgFolder != null && pkgFolder.isFolder();

    }

    private static void jumpToPackage(String packageName, FileObject xmlfile) {
        if (packageName == null || packageName.isEmpty()) {
            return;
        }

        // 1. ソースパスからフォルダを探す
        org.netbeans.api.java.classpath.ClassPath cp
                = org.netbeans.api.java.classpath.ClassPath.getClassPath(xmlfile, org.netbeans.api.java.classpath.ClassPath.SOURCE);

        if (cp == null) {
            return;
        }

        String resourcePath = packageName.replace('.', '/');
        FileObject pkgFolder = cp.findResource(resourcePath);

        if (pkgFolder != null && pkgFolder.isFolder()) {
            try {
                DataObject dobj = DataObject.find(pkgFolder);

                // 2. フォルダを開くための Cookie を取得
                // プロジェクトツリーでその場所を選択・展開させるには OpenCookie が最適
                org.openide.cookies.OpenCookie oc = dobj.getLookup().lookup(org.openide.cookies.OpenCookie.class);

                if (oc != null) {
                    oc.open(); // これでプロジェクトウィンドウが開く
                } else {
                    // OpenCookie がない場合の予備：EditCookie を試す
                    org.openide.cookies.EditCookie ec = dobj.getLookup().lookup(org.openide.cookies.EditCookie.class);
                    if (ec != null) {
                        ec.edit();
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
