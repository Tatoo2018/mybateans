package com.jhappy.mybateans.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;

public class MyBatisIndexer extends CustomIndexer {

    private static final Set<String> SQL_TAGS = new HashSet<>(
            Arrays.asList("select", "insert", "update", "delete", "sql")
    );

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
                    IndexDocument docmapper = support.createDocument(indexable);

                    MyBatisData mapperData = parseMyBatisXml(fo);
                    if (mapperData != null) {

                        String namespace = mapperData.getNamespace();

                        boolean exists = existsJavaType(fo, namespace);
                        docmapper.addPair("namespace_exists", String.valueOf(exists), true, true);

                        docmapper.addPair("mapper_namespace", namespace, true, true);
                        for (TagData tagData : mapperData.getTags()) {

                            Map<String, AttributeData> attr = tagData.getAttributes();
                            if (attr != null) {

                                AttributeData namespaceData = attr.get("id");
                                if (namespaceData != null) {
                                    String id = namespaceData.getValue();
                                    int offset = namespaceData.getValueoffset();

                                    docmapper.addPair("mapper_id", id, true, true);
                                    docmapper.addPair("id_pos_" + id, String.valueOf(offset), true, true);
                                }

                            }

                        }
                    }
                    support.removeDocuments(indexable);
                    support.addDocument(docmapper);
                    break;

                case "config":

                    MyBatisConfigData configData = parseTypeAliases(fo);

                    support.removeDocuments(indexable);
                    if (!configData.getAliases().isEmpty()) {

                        for (Map.Entry<String, AliasData> entry : configData.getAliases().entrySet()) {
                            IndexDocument docconfig = support.createDocument(indexable);

                            String alias = entry.getKey();
                            AliasData data = entry.getValue();

                            docconfig.addPair("typeAlias", alias, true, true);
                            docconfig.addPair("typeAlias_fqn", data.getFqn(), true, true);
                            docconfig.addPair("typeAlias_offset", String.valueOf(data.getOffset()), true, true);

                            support.addDocument(docconfig);

                        }
                    }

                    if (!configData.getPackages().isEmpty()) {

                        for (PackageData entry : configData.getPackages()) {
                            IndexDocument docconfig = support.createDocument(indexable);

                            String name = entry.name;

                            docconfig.addPair("typeAlias_package", name, true, true);
                            docconfig.addPair("typeAlias_package_offset", String.valueOf(entry.offset), true, true);

                            support.addDocument(docconfig);

                        }
                    }
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

    public static MyBatisData parseMyBatisXml(FileObject fo) {
        try {

            String text = fo.asText();
            TokenHierarchy<String> th = TokenHierarchy.create(text, XMLTokenId.language());
            TokenSequence<XMLTokenId> ts = th.tokenSequence(XMLTokenId.language());

            if (ts == null) {
                return null;
            }

            if (!isMapperXml(fo)) {
                return null;
            }

            String namespace = null;
            int namespaceOffset = -1;
            List<TagData> tags = new ArrayList<>();
            TagData currentTagData = null;

            while (ts.moveNext()) {
                Token<XMLTokenId> token = ts.token();
                XMLTokenId id = token.id();

                // 1. タグの開始 (<mapper, <select など)
                if (id == XMLTokenId.TAG) {
                    String tagText = token.text().toString();
                    if (tagText.startsWith("<") && !tagText.startsWith("</")) {
                        String tagName = tagText.substring(1).trim();
                        currentTagData = new TagData(tagName);
                        tags.add(currentTagData);
                    }
                }

                // 2. 属性の抽出
                if (id == XMLTokenId.ARGUMENT && currentTagData != null) {
                    String attrName = token.text().toString();
                    int attrNameOffset = ts.offset();

                    // 属性値 (VALUE) までトークンを進める
                    while (ts.moveNext() && ts.token().id() != XMLTokenId.VALUE) {
                        // スキップ (OPERATOR "=" など)
                    }

                    Token<XMLTokenId> valToken = ts.token();
                    if (valToken != null && valToken.id() == XMLTokenId.VALUE) {
                        String fullVal = valToken.text().toString();
                        if (fullVal.length() >= 2) {
                            String pureValue = fullVal.substring(1, fullVal.length() - 1);
                            int valOffset = ts.offset() + 1;

                            // 属性データを生成して現在のタグに追加
                            AttributeData attr = new AttributeData(attrName, attrNameOffset, pureValue, valOffset);
                            currentTagData.getAttributes().put(attrName, attr);

                            // namespace だけは特別に MyBatisData 直下で管理すると便利
                            if ("mapper".equals(currentTagData.getTagName()) && "namespace".equals(attrName)) {
                                namespace = pureValue;
                                namespaceOffset = valOffset;
                            }
                        }
                    }
                }
            }

            return (namespace != null) ? new MyBatisData(namespace, namespaceOffset, tags) : null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static boolean isMapperXml(FileObject fo){
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

    private static MyBatisConfigData parseTypeAliases(FileObject fo) {

        Map<String, AliasData> result = new HashMap<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        Set<PackageData> packages = new HashSet<>();

        try (InputStream is = fo.getInputStream()) {
            XMLStreamReader reader = factory.createXMLStreamReader(is);
            boolean inTypeAliases = false;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    String tag = reader.getLocalName();

                    if ("typeAliases".equals(tag)) {
                        inTypeAliases = true;

                    } else if (inTypeAliases && "typeAlias".equals(tag)) {
                        String alias = reader.getAttributeValue(null, "alias");
                        String type = reader.getAttributeValue(null, "type");

                        if (alias != null && type != null) {
                            int offset = reader.getLocation().getCharacterOffset();

                            result.put(alias, new AliasData(type, offset));
                        }
                    } else if (inTypeAliases && "package".equals(tag)) {
                        String name = reader.getAttributeValue(null, "name");

                        if (name != null) {
                            int offset = reader.getLocation().getCharacterOffset();
                            packages.add(new PackageData(name, offset));

                        }

                    }

                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("typeAliases".equals(reader.getLocalName())) {
                        inTypeAliases = false;
                    }
                }
            }
        } catch (Exception e) {
            // 無視
        }

        MyBatisConfigData configData = new MyBatisConfigData(result, packages);

        return configData;
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
