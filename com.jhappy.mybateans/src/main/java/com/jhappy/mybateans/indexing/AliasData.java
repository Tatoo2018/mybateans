package com.jhappy.mybateans.indexing;

/**
 *
 * @author th
 */
class AliasData {
    
    private String fqn;
    private int offset;

    AliasData(String fqn, int offset) {
        this.fqn = fqn;
        this.offset = offset;
    }

    /**
     * @return the fqn
     */
    public String getFqn() {
        return fqn;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
    
}
