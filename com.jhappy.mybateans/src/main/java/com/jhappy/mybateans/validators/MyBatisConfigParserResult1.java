package com.jhappy.mybateans.validators;

import com.jhappy.mybateans.indexing.AttributeData;
import com.jhappy.mybateans.indexing.MyBatisData;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * XML の解析結果（namespace とその位置）を保持するクラス
 */
public class MyBatisConfigParserResult1 extends Parser.Result {

    /**
     * @return the packagelist
     */
    public List<AttributeData> getPackagelist() {
        return packagelist;
    }

    /**
     * @return the typeAliases
     */
    public List<AttributeData> getTypeAliases() {
        return typeAliases;
    }

    private List<AttributeData> packagelist = new ArrayList<>();
    private List<AttributeData> typeAliases = new ArrayList<>();

    public MyBatisConfigParserResult1(Snapshot snapshot, List<AttributeData> packagelist, List<AttributeData> typeAliases) {
        super(snapshot);
        this.packagelist = packagelist;
        this.typeAliases = typeAliases;

    }

    @Override
    protected void invalidate() {

    }

}
