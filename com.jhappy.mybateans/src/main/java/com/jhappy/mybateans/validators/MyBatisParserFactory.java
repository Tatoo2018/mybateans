package com.jhappy.mybateans.validators;

import java.util.Collection;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.api.editor.mimelookup.MimeRegistration;

@MimeRegistration(mimeType = "text/xml", service = ParserFactory.class)
public class MyBatisParserFactory extends ParserFactory {

    @Override
    public Parser createParser(Collection<Snapshot> snapshots) {
        return new MyBatisParser();
    }
}