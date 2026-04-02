package com.jhappy.mybateans.indexing;
import java.util.Map;

public class MyBatisData {
    public final String namespace;
    public final Map<String, Integer> idOffsets; 

    public MyBatisData(String namespace, Map<String, Integer> idOffsets) {
        this.namespace = namespace;
        this.idOffsets = idOffsets;
    }
}