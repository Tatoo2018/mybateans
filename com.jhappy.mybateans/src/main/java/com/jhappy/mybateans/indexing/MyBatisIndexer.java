package com.jhappy.mybateans.indexing;

import com.jhappy.mybateans.util.xml.parser.AttributeData;
import com.jhappy.mybateans.util.xml.parser.XmlData;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.util.Exceptions;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public class MyBatisIndexer extends CustomIndexer {

    private static final Set<String> SQL_TAGS = new HashSet<>(
            Arrays.asList("select", "insert", "update", "delete")
    );

    public static final String INDEX_KEY_MAPPER_ID = "mapper_id";
    public static final String INDEX_KEY_MAPPER_ID_OFFSET = INDEX_KEY_MAPPER_ID + "_offset";
    public static final String INDEX_KEY_MAPPER_NAMESPACE = "mapper_namespace";
    public static final String INDEX_KEY_MAPPER_NAMESPACE_OFFSET = INDEX_KEY_MAPPER_NAMESPACE + "_offset";

    @Override
    protected void index(Iterable<? extends Indexable> indexables, Context context) {
        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return;
        }

        FileObject root = context.getRoot();
        if (root == null) {
            return;
        }

        for (Indexable indexable : indexables) {
            FileObject fo = root.getFileObject(indexable.getRelativePath());
            if (fo == null || !fo.isData()) {
                continue;
            }

            String type = getContentType(fo);
            if (type == null) {
                continue;
            }

            switch (type) {
                case "mapper":

                    support.removeDocuments(indexable);

                    XmlData mapperRoot = XmlData.parseFullXml(fo);
                    if (mapperRoot != null) {

                        Map<String, String> headerData = getMapperTagData(mapperRoot);

                        try {
                            DataObject dobj = DataObject.find(fo);
                            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);

                            // エディタが開いていればそのDocument、閉じていれば仮想的なDocumentをロード
                            Document doc = ec.openDocument();

                            String namespace = headerData.get("mapper_namespace");
                            String mapperNamespaceOffsetStr = headerData.get("mapper_namespace_offset");
                            int namespaceOffset = (mapperNamespaceOffsetStr != null) ? Integer.parseInt(mapperNamespaceOffsetStr) : -1;

                            if (!existsJavaType(fo, namespace)) {
                                List<ErrorDescription> errors = new ArrayList<>();
                                errors.add(ErrorDescriptionFactory.createErrorDescription(
                                        Severity.ERROR,
                                        "Namespace '" + namespace + "' not found.",
                                        fo,
                                        namespaceOffset,
                                        namespaceOffset + namespace.length()
                                ));
                                HintsController.setErrors(doc, "MyBatis", errors);
                                fo.setAttribute("mybatis_error_status", "error");

                            } else {
                                HintsController.setErrors(doc, "MyBatis", Collections.emptyList());
                            }

                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        List<XmlData> sqlNodes = getSqlTagData(mapperRoot);

                        List<String[]> mapperConfs = new ArrayList<>();
                        mapperConfs.add(new String[]{"id", INDEX_KEY_MAPPER_ID});
                        saveIndex(sqlNodes, support, indexable, mapperConfs, headerData);

                    }
                    break;

                case "config":

                    support.removeDocuments(indexable);

                    XmlData xmldata = XmlData.parseFullXml(fo);

                    List<XmlData> typeAliasList = xmldata.select("configuration", "typeAliases", "typeAlias");
                    List<String[]> confs1 = new ArrayList<>();
                    confs1.add(new String[]{"alias", "typeAlias"});
                    confs1.add(new String[]{"type", "typeAlias_fqn"});
                    saveIndex(typeAliasList, support, indexable, confs1, null);

                    List<XmlData> packageList = xmldata.select("configuration", "typeAliases", "package");
                    List<String[]> confs2 = new ArrayList<>();
                    confs2.add(new String[]{"name", "typeAlias_package"});
                    saveIndex(packageList, support, indexable, confs2, null);

                    break;

                case "springConfig":

                    IndexDocument docspring = support.createDocument(indexable);
                    docspring.addPair("springConfig", "true", true, true);

                    support.removeDocuments(indexable);
                    support.addDocument(docspring);
                    break;
            }

        }
    }

    public static List<XmlData> getSqlTagData(XmlData mapperRoot) {
        List<XmlData> sqlNodes = new ArrayList<>();
        for (String tagName : SQL_TAGS) {
            sqlNodes.addAll(mapperRoot.select("mapper", tagName));
        }
        return sqlNodes;
    }

    public static Map<String, String> getMapperTagData(XmlData mapperRoot) {

        Map<String, String> headerData = new HashMap<>();

        List<XmlData> nsAttr = mapperRoot.select("mapper");
        if (!nsAttr.isEmpty()) {

            XmlData data = nsAttr.get(0);
            Map<String, AttributeData> ns = data.getAttributes();

            AttributeData namespaceData = ns.get("namespace");
            if (namespaceData != null) {

                String namespace = namespaceData.getValue();
                String namespaceOffset = String.valueOf(namespaceData.getValueoffset());

                headerData.put("mapper_namespace", namespace);
                headerData.put("mapper_namespace_offset", namespaceOffset);
                return headerData;
            }

        }
        return null;
    }

    public void saveIndex(List<XmlData> packageList, IndexingSupport support, Indexable indexable, List<String[]> confs, Map<String, String> headerData) {
        for (XmlData data : packageList) {

            Map<String, AttributeData> attrs = data.getAttributes();
            if (attrs != null) {

                IndexDocument docconfig = support.createDocument(indexable);

                if (headerData != null) {
                    for (String key : headerData.keySet()) {
                        docconfig.addPair(key, headerData.get(key), true, true);
                    }

                }

                for (String[] conf : confs) {

                    String attrName = conf[0];
                    String indexKeyForAttrValue = conf[1];
                    String indexKeyForAttrValueOffset = indexKeyForAttrValue + "_offset";

                    AttributeData aliasdata = attrs.get(attrName);
                    if (aliasdata != null) {
                        String value = aliasdata.getValue();
                        int offset = aliasdata.getValueoffset();
                        docconfig.addPair(indexKeyForAttrValue, value, true, true);
                        docconfig.addPair(indexKeyForAttrValueOffset, String.valueOf(offset), true, true);
                    }
                }

                support.addDocument(docconfig);

            }

        }
    }

    public static boolean existsJavaType(FileObject fo, String fqn) {
        try {
            org.netbeans.api.java.classpath.ClassPath cp
                    = org.netbeans.api.java.classpath.ClassPath.getClassPath(fo, org.netbeans.api.java.classpath.ClassPath.SOURCE);

            if (cp == null) {
                return false;
            }

            FileObject javaFile = cp.findResource(fqn.replace('.', '/') + ".java");
            if (javaFile != null) {
                return true;
            }

            // classも見る（compile後）
            org.netbeans.api.java.classpath.ClassPath compileCp
                    = org.netbeans.api.java.classpath.ClassPath.getClassPath(fo, org.netbeans.api.java.classpath.ClassPath.COMPILE);

            if (compileCp != null) {
                FileObject classFile = compileCp.findResource(fqn.replace('.', '/') + ".class");
                return classFile != null;
            }

        } catch (Exception e) {
            // 無視
        }
        return false;
    }

    public static MyBatisData parseMyBatisConfigXml(FileObject fo) {

        try {
            String text = fo.asText();
            TokenHierarchy<String> th = TokenHierarchy.create(text, XMLTokenId.language());
            TokenSequence<XMLTokenId> ts = th.tokenSequence(XMLTokenId.language());

            if (ts == null) {
                return null;
            }

            if (!isConfigXml(fo)) {
                return null;
            }

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        return null;

    }

    public static boolean isMapperXml(FileObject fo) {
        try {
            return hasRootElement(fo, "mapper");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public static boolean isConfigXml(FileObject fo) {
        try {
            return hasRootElement(fo, "configuration");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public static boolean isSpringConfigXml(FileObject fo) throws Exception {
        return hasRootElement(fo, "beans");
    }

    private static boolean hasRootElement(FileObject fo, String expectedRoot) throws Exception {
        if (!"xml".equals(fo.getExt())) {
            return false;
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        try (InputStream is = fo.getInputStream()) {
            XMLStreamReader reader = factory.createXMLStreamReader(is);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    return expectedRoot.equals(reader.getLocalName());
                }
            }
        }
        return false;
    }

    public static String getContentType(FileObject fo) {
        if (!"xml".equals(fo.getExt())) {
            return null;
        }
        try {

            if (MyBatisIndexer.isMapperXml(fo)) {
                return "mapper";
            }
            if (MyBatisIndexer.isConfigXml(fo)) {
                return "config";
            }
            if (MyBatisIndexer.isSpringConfigXml(fo)) {
                return "springConfig";
            }

        } catch (Exception e) {
            // 無視して続行
        }
        return null;
    }

    /**
     * 指定された FQN に対応する JavaSource を取得する
     *
     * @param fo コンテキストとなるファイル（XMLファイルなど）
     * @param fqn 探したい Java クラスの完全修飾名 (例: com.jhappy.mapper.CustomerMapper)
     * @return 見つかった場合は JavaSource、見つからない場合は null
     */
    public static JavaSource getJavaSource(FileObject fo, String fqn) {
        if (fo == null || fqn == null || fqn.isEmpty()) {
            return null;
        }

        // 1. プロジェクトのソースパスを取得
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }

        // 2. FQN をファイルパス形式に変換して、実際のファイル（FileObject）を探す
        // 例: com.jhappy.Test -> com/jhappy/Test.java
        String resourcePath = fqn.replace('.', '/') + ".java";
        FileObject javaFile = cp.findResource(resourcePath);

        if (javaFile != null) {

            return JavaSource.forFileObject(javaFile);
        }

        return null;
    }

}
