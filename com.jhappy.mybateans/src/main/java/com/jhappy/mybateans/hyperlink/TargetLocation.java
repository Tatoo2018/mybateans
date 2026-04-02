
package com.jhappy.mybateans.hyperlink;

import org.openide.filesystems.FileObject;

/**
 *
 * @author th
 */
public class TargetLocation {
    
    FileObject file;
    int offset;

    TargetLocation(FileObject f, int o) {
        this.file = f;
        this.offset = o;
    }
    
}
