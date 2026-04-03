package com.jhappy.mybateans.util.xml.parser;

import java.util.HashMap;
import java.util.Map;

public class TagData {

    /**
     * @return the tagName
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @return the attributes
     */
    public Map<String, AttributeData> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Map<String, AttributeData> attributes) {
        this.attributes = attributes;
    }
    
    private final String tagName;
    
    private Map<String, AttributeData> attributes = new HashMap<>();

    public TagData(String tagName) {
        this.tagName = tagName;
    }
}