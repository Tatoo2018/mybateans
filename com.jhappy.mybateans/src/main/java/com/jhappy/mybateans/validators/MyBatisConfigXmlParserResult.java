package com.jhappy.mybateans.validators;

import com.jhappy.mybateans.util.xml.parser.AttributeData;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * XML の解析結果（namespace とその位置）を保持するクラス
 */
public class MyBatisConfigXmlParserResult extends Parser.Result {

    private List<AttributeData> packagelist = new ArrayList<>();
    
    private List<AttributeData> typeAliases = new ArrayList<>();

    /**
     * 
     * @param snapshot
     * @param packagelist
     * @param typeAliases 
     */
    public MyBatisConfigXmlParserResult(Snapshot snapshot, List<AttributeData> packagelist, List<AttributeData> typeAliases) {
        super(snapshot);
        this.packagelist = packagelist;
        this.typeAliases = typeAliases;

    }

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

    @Override
    protected void invalidate() {

    }

}
