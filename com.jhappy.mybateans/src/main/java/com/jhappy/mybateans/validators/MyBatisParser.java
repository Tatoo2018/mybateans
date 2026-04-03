package com.jhappy.mybateans.validators;

import com.jhappy.mybateans.indexing.AttributeData;
import com.jhappy.mybateans.indexing.MyBatisData;
import com.jhappy.mybateans.indexing.MyBatisIndexer;
import com.jhappy.mybateans.indexing.XmlData;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;

public class MyBatisParser extends Parser {

    private Parser.Result lastResult;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {

        FileObject fo = snapshot.getSource().getFileObject();

        List<AttributeData> packages = new ArrayList<>();
        List<AttributeData> typeAliases = new ArrayList<>();

        if (MyBatisIndexer.isConfigXml(fo)) {
            XmlData xmldata = XmlData.parseFullXml(fo);
            List<XmlData> packageList = xmldata.select("configuration>typeAliases>package");

            for (XmlData data : packageList) {

                AttributeData attr = data.attributes.get("name");
                if (attr != null) {
                    packages.add(attr);
                }

            }

            List<XmlData> typeAliaslist = xmldata.select("configuration>typeAliases>typeAlias");

            for (XmlData data : typeAliaslist) {

                AttributeData attr = data.attributes.get("type");
                if (attr != null) {
                    typeAliases.add(attr);
                }

            }

            lastResult = new MyBatisConfigParserResult1(snapshot, packages, typeAliases);

        } else if (MyBatisIndexer.isMapperXml(fo)) {

            MyBatisData mybatisData = MyBatisIndexer.parseMyBatisXml(fo);

            if (mybatisData != null) {
                lastResult = new MyBatisParserResult(snapshot, mybatisData);
            }
        }

    }

    @Override
    public Result getResult(Task task) {

        return lastResult;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {

        System.out.println("called parse addChangeListener");
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        System.out.println("called parse removeChangeListener");
    }

}
