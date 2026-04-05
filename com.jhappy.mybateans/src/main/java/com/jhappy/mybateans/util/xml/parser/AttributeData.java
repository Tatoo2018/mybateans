package com.jhappy.mybateans.util.xml.parser;

/**
 *
 * @author th
 */
public class AttributeData {

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

    private final String name;
    private final int nameoffset;
    private final String value;
    private final int valueoffset;

    public AttributeData(String name, int nameoffset, String value, int valueoffset) {
        this.name = name;
        this.value = value;
        this.nameoffset = nameoffset;
        this.valueoffset = valueoffset;
    }

}
