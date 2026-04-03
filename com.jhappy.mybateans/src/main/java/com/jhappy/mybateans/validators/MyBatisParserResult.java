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
public class MyBatisParserResult extends Parser.Result {

    private final MyBatisData myBatisData;
        private List<AttributeData> packages = new ArrayList<>();
    private List<AttributeData> typeAliases = new ArrayList<>();

    public MyBatisParserResult(Snapshot snapshot, MyBatisData myBatisData) {
        super(snapshot);
        this.myBatisData = myBatisData;

    }

    @Override
    protected void invalidate() {

    }

    /**
     * @return the myBatisData
     */
    public MyBatisData getMyBatisData() {
        return myBatisData;
    }

}
