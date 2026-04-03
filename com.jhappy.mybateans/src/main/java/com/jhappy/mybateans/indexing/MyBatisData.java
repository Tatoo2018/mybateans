package com.jhappy.mybateans.indexing;
import com.jhappy.mybateans.util.xml.parser.TagData;
import com.jhappy.mybateans.util.xml.parser.XmlData;
import java.util.List;

public class MyBatisData {

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the namespaceOffset
     */
    public Integer getNamespaceOffset() {
        return namespaceOffset;
    }

    /**
     * @return the tags
     */
    public List<XmlData>  getTags() {
        return tags;
    }
    private final String namespace;
    private final Integer namespaceOffset;
    private final List<XmlData>  tags;

    public MyBatisData(String namespace, int namespaceOffset,List<XmlData>  tags) {
        this.namespace = namespace;
        this.namespaceOffset = namespaceOffset;
        this.tags = tags;
    }
}