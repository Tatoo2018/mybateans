package com.jhappy.mybateans.indexing;
import com.jhappy.mybateans.util.xml.parser.XmlData;
import java.util.List;

public class MyBatisMapperData {

   
    private final String namespace;
    private final Integer namespaceOffset;
    private final List<XmlData>  tags;

    /**
     * 
     * @param namespace
     * @param namespaceOffset
     * @param tags 
     */
    public MyBatisMapperData(String namespace, int namespaceOffset,List<XmlData>  tags) {
        this.namespace = namespace;
        this.namespaceOffset = namespaceOffset;
        this.tags = tags;
    }
    
     /**
     * @return the tags
     */
    public List<XmlData>  getTags() {
        return tags;
    }
    
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

}