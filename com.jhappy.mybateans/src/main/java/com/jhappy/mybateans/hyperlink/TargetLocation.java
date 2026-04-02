/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
