package com.jhappy.mybateans.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@ActionID(category = "Edit", id = "com.jhappy.mybateans.action.CreateMapperXmlAction")
@ActionRegistration(iconBase = "com/jhappy/mybateans/images/icon.png", displayName = "#CTL_CreateMapperXmlAction")
@ActionReferences({
    @ActionReference(path = "Loaders/text/x-java/Actions", position = 1274, separatorBefore = 1271),
    @ActionReference(path = "Editors/text/x-java/Popup", position = 9700)
})
public class CreateMapperXmlAction implements ActionListener {

    private final List<DataObject> context;

    public CreateMapperXmlAction(List<DataObject> context) {
        this.context = context;
    }

    private void createFile(DataFolder targetFolder, String fileName, String ext, String content) throws IOException {
        String fullFileName = fileName + "." + ext;
        FileObject folderFO = targetFolder.getPrimaryFile();
        FileObject existingFile = folderFO.getFileObject(fullFileName);

        if (existingFile != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(CreateMapperXmlAction.class, "MSG_FileAlreadyExists", fullFileName),
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        FileObject newFile = targetFolder.getPrimaryFile().createData(fullFileName);
        try (OutputStream os = newFile.getOutputStream()) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }

        SwingUtilities.invokeLater(() -> {
            try {
                DataObject dobj = DataObject.find(newFile);
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    ec.open();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);

            }
        });
    }

    public String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (DataObject dataObject : context) {
            FileObject javaFile = dataObject.getPrimaryFile();
            JavaSource js = JavaSource.forFileObject(javaFile);
            if (js == null) {
                continue;
            }

            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    Element topElement = cc.getTopLevelElements().get(0);
                    if (!(topElement instanceof TypeElement typeElement)) {
                        return;
                    }

                    String className = typeElement.getSimpleName().toString();
                    String qualifiedName = typeElement.getQualifiedName().toString();
                    String fqn = typeElement.getQualifiedName().toString();
                    String packageName = fqn.contains(".") ? fqn.substring(0, fqn.lastIndexOf('.')) : "";
                    String tableName = camelToSnake(className);

                    List<Map<String, String>> columns = new ArrayList<>();
                    for (Element enclosed : typeElement.getEnclosedElements()) {
                        if (enclosed.getKind() == ElementKind.FIELD
                                && !enclosed.getModifiers().contains(Modifier.STATIC)) {
                            String propName = enclosed.getSimpleName().toString();
                            Map<String, String> colMap = new HashMap<>();
                            colMap.put("property", propName);
                            colMap.put("column", camelToSnake(propName));
                            columns.add(colMap);
                        }
                    }

                    DataFolder targetFolder = DataFolder.findFolder(javaFile.getParent());

                    generateXml(targetFolder, className, fqn, tableName, columns);

                    generateJavaMapper(targetFolder, className, packageName, tableName);

                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void generateXml(DataFolder targetFolder, String className, String fqn, String tableName, List<Map<String, String>> columns) {
        try {

            FileObject templateFO = FileUtil.getConfigFile("Templates/MyBatis/MyBatisMapperTemplate.xml");
            String templateStr = templateFO.asText("UTF-8");

            Map<String, Object> params = new HashMap<>();
            params.put("namespace", fqn + "Mapper");
            params.put("className", className);
            params.put("classFqnName", fqn);
            params.put("tableName", tableName);
            params.put("columns", columns);

            String resultXml = applyTemplate(templateStr, params, "MyBatisMapperTemplate.xml");

            createFile(targetFolder, className + "Mapper", "xml", resultXml);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void generateJavaMapper(DataFolder targetFolder, String className, String packageName, String tableName) {
        try {

            FileObject templateFO = FileUtil.getConfigFile("Templates/MyBatis/MyBatisMapperJavaTemplate.ftl");
            String templateStr = templateFO.asText("UTF-8");

            Map<String, Object> params = new HashMap<>();
            params.put("packageName", packageName);
            params.put("className", className);
            params.put("variableName", className.substring(0, 1).toLowerCase() + className.substring(1));
            params.put("tableName", tableName);

            String resultJava = applyTemplate(templateStr, params, "MyBatisMapperJavaTemplate.ftl");

            createFile(targetFolder, className + "Mapper", "java", resultJava);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static String applyTemplate(String templateStr, Map<String, Object> params, String fileName) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("freemarker");
        engine.getContext().setAttribute("javax.script.filename", fileName, ScriptContext.ENGINE_SCOPE);
        Bindings bindings = engine.createBindings();
        bindings.putAll(params);
        StringWriter writer = new StringWriter();
        engine.getContext().setWriter(writer);
        engine.eval(templateStr, bindings);
        return writer.toString();
    }
}
