package com.jhappy.mybateans.validators;

import com.jhappy.mybateans.util.xml.parser.AttributeData;
import com.jhappy.mybateans.indexing.MyBatisData;
import com.jhappy.mybateans.indexing.MyBatisIndexer;
import com.jhappy.mybateans.util.xml.parser.XmlData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

            List<XmlData> packageList = xmldata.select("configuration", "typeAliases", "package");

            for (XmlData data : packageList) {

                AttributeData attr = data.getAttributes().get("name");
                if (attr != null) {
                    packages.add(attr);
                }

            }

            List<XmlData> typeAliaslist = xmldata.select("configuration", "typeAliases", "typeAlias");

            for (XmlData data : typeAliaslist) {

                AttributeData attr = data.getAttributes().get("type");
                if (attr != null) {
                    typeAliases.add(attr);
                }

            }

            lastResult = new MyBatisConfigXmlParserResult(snapshot, packages, typeAliases);

        } else if (MyBatisIndexer.isMapperXml(fo)) {

            
            XmlData mapperRoot = XmlData.parseFullXml(fo);

            Map<String, String> mapperTagData = MyBatisIndexer.getMapperTagData(mapperRoot);
            String mapperNamespace = mapperTagData.get("mapper_namespace");
            String mapperNamespaceOffsetStr = mapperTagData.get("mapper_namespace_offset");

            if (mapperNamespace != null) {
                int mapperNamespaceOffset = (mapperNamespaceOffsetStr != null) ? Integer.parseInt(mapperNamespaceOffsetStr) : -1;
                List<XmlData> sqlTagList = MyBatisIndexer.getSqlTagData(mapperRoot);

                MyBatisData mybatisData = new MyBatisData(mapperNamespace, mapperNamespaceOffset, sqlTagList);

                lastResult = new MyBatisMapperXmlParseResult(snapshot, mybatisData);
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
