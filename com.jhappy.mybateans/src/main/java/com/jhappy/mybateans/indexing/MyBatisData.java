/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jhappy.mybateans.indexing;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyBatisData {
    public final String namespace;
    public final Map<String, Integer> idOffsets; 

    public MyBatisData(String namespace, Map<String, Integer> idOffsets) {
        this.namespace = namespace;
        this.idOffsets = idOffsets;
    }
}