package com.jhappy.mybateans.indexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author th
 */
class MyBatisConfigData {
    
    Map<String, AliasData> aliases = new HashMap<>();
    Set<PackageData> packages = new HashSet<>();
    
}
