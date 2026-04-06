package com.jhappy.mybateans.hyperlink;

import com.jhappy.mybateans.indexing.MyBatisIndexerFactory;
import com.jhappy.mybateans.indexing.MyBatisIndexer;
import com.jhappy.mybateans.util.NbUtil;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

@MimeRegistration(
        mimeType = "text/x-java",
        service = HyperlinkProviderExt.class,
        position = 10
)
public class MyBatisMapperHyperlinkProvider implements HyperlinkProviderExt {

    private String targetNamespace;
    private String targetId;

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType ht) {

        JavaSource js = JavaSource.forDocument(doc);
        if (js == null) {
            return false;
        }

        final String[] namespaceHolder = new String[1];
        final String[] idHolder = new String[1];

        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                TreePath path = cc.getTreeUtilities().pathFor(offset);
                var el = cc.getTrees().getElement(path);

                if (el instanceof ExecutableElement method) {
                    TypeElement clazz = (TypeElement) method.getEnclosingElement();
                    namespaceHolder[0] = clazz.getQualifiedName().toString();
                    idHolder[0] = method.getSimpleName().toString();
                } else if (el instanceof TypeElement clazz) {
                    namespaceHolder[0] = clazz.getQualifiedName().toString();
                    idHolder[0] = null;
                }
            }, true);
        } catch (IOException e) {
            return false;
        }

        if (namespaceHolder[0] == null) {
            return false;
        }

        try {
            DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
            if (dobj == null) {
                return false;
            }

            FileObject javaFO = dobj.getPrimaryFile();

            TargetLocation loc = findMapperLocation(javaFO, namespaceHolder[0], idHolder[0]);

            if (loc != null) {
                targetNamespace = namespaceHolder[0];
                targetId = idHolder[0];
                return true;
            }

        } catch (IOException ex) {
            return false;
        }

        return false;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType ht) {

        return new int[]{offset, offset};
    }

    @Override
    public void performClickAction(Document doc, int i, HyperlinkType ht) {

        if (targetNamespace == null && targetId == null) {
            return;
        }

        DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        if (dobj == null) {
            return;
        }

        try {

            FileObject javaFO = dobj.getPrimaryFile();

            TargetLocation location = findMapperLocation(javaFO, targetNamespace, targetId);

            if (location != null && location.file != null) {

                DataObject xmlDobj = DataObject.find(location.file);
                EditorCookie ec = xmlDobj.getLookup().lookup(EditorCookie.class);

                if (ec != null) {

                    ec.open();

                    if (location.offset != -1) {
                        final int jumpPos = location.offset;

                        NbDocument.openDocument(location.file, jumpPos, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                    }
                }
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     *
     * @param javaFO
     * @param namespace
     * @param id
     * @return
     * @throws IOException
     */
    public static TargetLocation findMapperLocation(FileObject javaFO, String namespace, String id) throws IOException {

        Project project = FileOwnerQuery.getOwner(javaFO);
        if (project == null) {
            return null;
        }

        Collection<? extends IndexResult> results = findMapperData(project, namespace);

        for (IndexResult result : results) {

            if (id == null) {

                FileObject xmlFile = result.getFile();

                if (xmlFile != null) {
                    String namespaceOffsetStr = result.getValue(MyBatisIndexer.INDEX_KEY_MAPPER_NAMESPACE_OFFSET);
                    int namespaceOffset = (namespaceOffsetStr != null) ? Integer.parseInt(namespaceOffsetStr) : -1;
                    return new TargetLocation(xmlFile, namespaceOffset);
                }
            } else {

                String[] ids = result.getValues(MyBatisIndexer.INDEX_KEY_MAPPER_ID);
                for (String currentId : ids) {
                    if (id.equals(currentId)) {

                        String offsetStr = result.getValue(MyBatisIndexer.INDEX_KEY_MAPPER_ID_OFFSET);
                        int offset = (offsetStr != null) ? Integer.parseInt(offsetStr) : -1;

                        FileObject xmlFile = result.getFile();

                        if (xmlFile != null) {
                            return new TargetLocation(xmlFile, offset);
                        }

                    }
                }

            }

        }
        return null;
    }

    public static FileObject findMapperXmlFile(Project project, String namespace) throws IOException {
        Collection<? extends IndexResult> results = findMapperData(project, namespace);

        if (0 < results.size()) {
            IndexResult[] result = new IndexResult[results.size()];
            return results.toArray(result)[0].getFile();
        }

        return null;
    }

    private static Collection<? extends IndexResult> findMapperData(Project project, String namespace) throws IOException {
        List<FileObject> roots = NbUtil.getRootsForSearch(project);
        QuerySupport querySupport = QuerySupport.forRoots(MyBatisIndexerFactory.INDEXER_NAME, MyBatisIndexerFactory.version, roots.toArray(new FileObject[0]));
        Collection<? extends IndexResult> results = querySupport.query(MyBatisIndexer.INDEX_KEY_MAPPER_NAMESPACE, namespace, QuerySupport.Kind.EXACT);
        return results;
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public String getTooltipText(Document dcmnt, int i, HyperlinkType ht) {
        return "Go to MyBatis Mapper XML";
    }

}
