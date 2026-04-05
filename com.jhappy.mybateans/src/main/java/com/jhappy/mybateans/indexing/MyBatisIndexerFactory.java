package com.jhappy.mybateans.indexing;

import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.Context;

/**
 * CustomIndexerFactory for XML files.
 * * NOTE ON MIME TYPE:
 * We use an empty string for mimeType ("") and set position = 1 to bypass 
 * the strict MIME filtering and caching logic of the NetBeans Indexing Engine. 
 * Explicitly using "text/xml" often causes this indexer to be skipped if the 
 * internal indexing system determines the file has already been processed by 
 * the standard XML indexer or if there is a MIME type mismatch.
 * * By using an empty mimeType, we ensure the 'index' method is reliably called 
 * for all files, allowing us to manually filter for MyBatis-specific XML files 
 * within the indexer implementation.
 * * @author th
 */
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CustomIndexerFactory.class)
public class MyBatisIndexerFactory extends CustomIndexerFactory {

    public static int version = 8;
    public static String INDEXER_NAME = "MyBatisIndexer10";

    @Override
    public CustomIndexer createIndexer() {
        return new MyBatisIndexer();

    }

    @Override
    public boolean scanStarted(Context context) {
        return true;
    }

    @Override
    public String getIndexerName() {
        return INDEXER_NAME; 
    }

    @Override
    public int getIndexVersion() {
        return version;
    }

    @Override
    public boolean supportsEmbeddedIndexers() {
        return true;
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> itrbl, Context cntxt) {

    }

    @Override
    public void filesDirty(Iterable<? extends Indexable> itrbl, Context cntxt) {


    }
}
