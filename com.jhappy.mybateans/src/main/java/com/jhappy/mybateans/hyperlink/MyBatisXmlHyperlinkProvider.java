package com.jhappy.mybateans.hyperlink;

import com.jhappy.mybateans.indexing.MyBatisIndexerFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
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
import org.netbeans.api.java.project.JavaProjectConstants;

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
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
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

        return "namespace".equals(attr.name)
                || "id".equals(attr.name) || TYPE_REF_ATTRS.contains(attr.name);
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

        if ("namespace".equals(attr.name)) {

            jumpToClass(attr.value, doc);

        } else if ("id".equals(attr.name)) {

            String namespace = findNamespace(doc);

            jumpToMethod(namespace, attr.value, xmlfile);

        } else if (TYPE_REF_ATTRS.contains(attr.name)) {

            String alias = attr.value;

            TargetLocation configFo = findTypeAliasFQN(xmlfile, alias);

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
        }

    }

    private String resolveFromPackages(FileObject xmlfile, String simpleName) {
        Set<String> packages = findTypeAliasPackages(xmlfile);

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
    private TargetLocation findTypeAliasFQN(FileObject xmlfile, String alias) {

        Project project = FileOwnerQuery.getOwner(xmlfile);
        if (project == null) {
            return null;
        }

        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject[] roots = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            roots[i] = groups[i].getRootFolder();
        }

        try {

            QuerySupport querySupport = QuerySupport.forRoots(MyBatisIndexerFactory.INDEXER_NAME, MyBatisIndexerFactory.version, roots);

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

        if ("namespace".equals(attr.name)) {
            return "Go to Java Mapper: " + attr.value;
        } else if ("id".equals(attr.name)) {
            return "Go to Method: " + attr.value;
        }

        return null;
    }

    private Set<String> findTypeAliasPackages(FileObject xmlfile) {
        Set<String> packages = new HashSet<>();

        Project project = FileOwnerQuery.getOwner(xmlfile);
        if (project == null) {
            return packages;
        }

        SourceGroup[] groups = ProjectUtils.getSources(project)
                .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        FileObject[] roots = new FileObject[groups.length];

        for (int i = 0; i < groups.length; i++) {
            roots[i] = groups[i].getRootFolder();
        }

        try {
            QuerySupport qs = QuerySupport.forRoots(
                    MyBatisIndexerFactory.INDEXER_NAME,
                    MyBatisIndexerFactory.version,
                    roots
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

    /**
     *
     * @param doc
     * @param offset
     * @return
     */
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

            Token<XMLTokenId> prev;
            while (ts.movePrevious()) {
                prev = ts.token();
                if (prev.id() == XMLTokenId.ARGUMENT) {
                    String name = prev.text().toString();
                    result[0] = new AttributeInfo(name, value);
                    return;
                }
                // OPERATOR や空白はスキップ
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
    private void jumpToClass(String fqn, Document doc) {

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

        final String name;
        final String value;

        AttributeInfo(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     *
     * @param location
     * @throws DataObjectNotFoundException
     * @throws IOException
     */
    private void openAtEditor(TargetLocation location) throws DataObjectNotFoundException, IOException {

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

}
