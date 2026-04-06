package com.jhappy.mybateans.validators;

import com.jhappy.mybateans.util.xml.parser.AttributeData;
import com.jhappy.mybateans.indexing.MyBatisMapperData;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * XML の解析結果（namespace とその位置）を保持するクラス
 */
public class MyBatisMapperXmlParseResult extends Parser.Result {
    
    private final MyBatisMapperData myBatisData;
    private List<AttributeData> packages = new ArrayList<>();
    private List<AttributeData> typeAliases = new ArrayList<>();

    /**
     * @return the packages
     */
    public List<AttributeData> getPackages() {
        return packages;
    }

    /**
     * @param packages the packages to set
     */
    public void setPackages(List<AttributeData> packages) {
        this.packages = packages;
    }

    /**
     * @return the typeAliases
     */
    public List<AttributeData> getTypeAliases() {
        return typeAliases;
    }

    /**
     * @param typeAliases the typeAliases to set
     */
    public void setTypeAliases(List<AttributeData> typeAliases) {
        this.typeAliases = typeAliases;
    }



    public MyBatisMapperXmlParseResult(Snapshot snapshot, MyBatisMapperData myBatisData) {
        super(snapshot);
        this.myBatisData = myBatisData;

    }

    @Override
    protected void invalidate() {

    }

    /**
     * @return the myBatisData
     */
    public MyBatisMapperData getMyBatisData() {
        return myBatisData;
    }

}
