package com.jhappy.mybateans.indexing;
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
    public List<TagData> getTags() {
        return tags;
    }
    private final String namespace;
    private final Integer namespaceOffset;
    private final List<TagData> tags;

    public MyBatisData(String namespace, int namespaceOffset,List<TagData> tags) {
        this.namespace = namespace;
        this.namespaceOffset = namespaceOffset;
        this.tags = tags;
    }
}