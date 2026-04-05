package com.jhappy.mybateans.util.xml.parser;

/**
 *
 * @author th
 */
public class AttributeData {
    
    private String name;
    
    private int nameoffset;
    
    private String value;
    
    private int valueoffset;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the nameoffset
     */
    public int getNameoffset() {
        return nameoffset;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the valueoffset
     */
    public int getValueoffset() {
        return valueoffset;
    }


    public AttributeData(String name, int nameoffset, String value, int valueoffset) {
        this.name = name;
        this.value = value;
        this.nameoffset = nameoffset;
        this.valueoffset = valueoffset;
    }

}
