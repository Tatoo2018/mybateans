package com.jhappy.mybateans.indexing;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
                        docmapper.addPair("mapper_namespace", mapperData.namespace, true, true);
                        for (Map.Entry<String, Integer> entry : mapperData.idOffsets.entrySet()) {
                            String id = entry.getKey();
                            int offset = entry.getValue();
                            docmapper.addPair("mapper_id", id, true, true);
                            docmapper.addPair("id_pos_" + id, String.valueOf(offset), true, true);
                        }
                    }
                    support.removeDocuments(indexable);
                    support.addDocument(docmapper);
                    break;

                case "config":

                    MyBatisConfigData configData = parseTypeAliases(fo);

                    support.removeDocuments(indexable);
                    if (!configData.aliases.isEmpty()) {

                        for (Map.Entry<String, AliasData> entry : configData.aliases.entrySet()) {
                            IndexDocument docconfig = support.createDocument(indexable);

                            String alias = entry.getKey();
                            AliasData data = entry.getValue();

                            docconfig.addPair("typeAlias", alias, true, true);
                            docconfig.addPair("typeAlias_fqn", data.fqn, true, true);
                            docconfig.addPair("typeAlias_offset", String.valueOf(data.offset), true, true);

                            support.addDocument(docconfig);

                        }
                    }

                    if (!configData.packages.isEmpty()) {

                        for (PackageData entry : configData.packages) {
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

    // --- XML パース用 ---
    private static MyBatisData parseMyBatisXml(FileObject fo) {
        String namespace = null;
        Map<String, Integer> idOffsets = new HashMap<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        try (InputStream is = fo.getInputStream()) {
            XMLStreamReader reader = factory.createXMLStreamReader(is);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String tag = reader.getLocalName();

                    if ("mapper".equals(tag)) {
                        namespace = reader.getAttributeValue(null, "namespace");
                    } else if (SQL_TAGS.contains(tag)) {
                        String id = reader.getAttributeValue(null, "id");
                        if (id != null) {
                            int offset = reader.getLocation().getCharacterOffset();
                            idOffsets.put(id, offset);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return (namespace != null) ? new MyBatisData(namespace, idOffsets) : null;
    }

    private static class MyBatisData {

        final String namespace;
        final Map<String, Integer> idOffsets;

        MyBatisData(String ns, Map<String, Integer> offsets) {
            this.namespace = ns;
            this.idOffsets = offsets;
        }
    }

    // --- XML タイプ判定用 --- 
    public static boolean isMapperXml(FileObject fo) throws Exception {
        return hasRootElement(fo, "mapper");
    }

    public static boolean isConfigXml(FileObject fo) throws Exception {
        return hasRootElement(fo, "configuration");
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

        MyBatisConfigData configData = new MyBatisConfigData();
        configData.aliases = result;
        configData.packages = packages;

        return configData;
    }




    private static String getContentType(FileObject fo) {
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

}
