package com.jhappy.mybateans.indexing;

import java.io.IOException;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;

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

    public static int version = 6;
    public static String INDEXER_NAME = "MyBatisIndexer10";

    @Override
    public CustomIndexer createIndexer() {
        System.out.println("createIndexer");
        return new MyBatisIndexer();

    }

    @Override
    public boolean scanStarted(Context context) {
        System.out.println("scanStarted");
        return true;
    }

    @Override
    public String getIndexerName() {
        System.out.println("getIndexerName");
        return INDEXER_NAME; // 識別名
    }

    @Override
    public int getIndexVersion() {
        System.out.println("getIndexVersion");
        return version; // 構造を変えたら上げる
    }

    @Override
    public boolean supportsEmbeddedIndexers() {
        System.out.println("supportsEmbeddedIndexers");
        return true;
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> itrbl, Context cntxt) {

        System.out.println("filesDeleted called");
        for (Indexable i : itrbl) {
            System.out.println("Dirty file candidate: " + i.getRelativePath());
        }

    }

    @Override
    public void filesDirty(Iterable<? extends Indexable> itrbl, Context cntxt) {
        System.out.println("filesDirty called");
        for (Indexable i : itrbl) {
            FileObject fo = cntxt.getRoot().getFileObject(i.getRelativePath());
            if (fo != null) {
                // これが "text/xml" と一致するか、大文字小文字含めてチェック
                System.out.println("CRITICAL DEBUG: Path=" + i.getRelativePath());
                System.out.println("CRITICAL DEBUG: MIME=" + fo.getMIMEType());
                System.out.println("CRITICAL DEBUG: Name=" + fo.getName());
            }
        }

        try {
            // IndexingSupport を使って、無理やり「このドキュメントを削除」扱いにする
            // これにより、NetBeans は「データが消えたから index() を呼んで作り直さなきゃ」と考えます
            IndexingSupport support = IndexingSupport.getInstance(cntxt);
            for (Indexable i : itrbl) {
                support.removeDocuments(i);
            }
        } catch (IOException ex) {
            // ignore
        }

    }
}
