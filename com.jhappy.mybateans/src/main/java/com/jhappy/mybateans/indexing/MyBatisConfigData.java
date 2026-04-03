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
    
    MyBatisConfigData(Map<String, AliasData> aliases,Set<PackageData> packages){
        this.aliases = aliases;
         this.packages = packages;
    }

    /**
     * @param aliases the aliases to set
     */
    public void setAliases(Map<String, AliasData> aliases) {
        this.aliases = aliases;
    }

    /**
     * @param packages the packages to set
     */
    public void setPackages(Set<PackageData> packages) {
        this.packages = packages;
    }
    
    private Map<String, AliasData> aliases = new HashMap<>();
    private Set<PackageData> packages = new HashSet<>();

    /**
     * @return the aliases
     */
    public Map<String, AliasData> getAliases() {
        return aliases;
    }

    /**
     * @return the packages
     */
    public Set<PackageData> getPackages() {
        return packages;
    }
    
}
