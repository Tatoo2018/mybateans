package com.jhappy.mybateans.util;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;

/**
 *
 * @author th
 */
public class NbUtil {

    public static List<FileObject> getRootsForSearch(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        List<FileObject> roots = new ArrayList<>();
        // Java
        for (SourceGroup g : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            roots.add(g.getRootFolder());
        }
        // Resources（Maven）
        for (SourceGroup g : sources.getSourceGroups("resources")) {
            roots.add(g.getRootFolder());
        }
        // Generic（保険）
        for (SourceGroup g : sources.getSourceGroups(Sources.TYPE_GENERIC)) {
            roots.add(g.getRootFolder());
        }
        return roots;
    }

}
