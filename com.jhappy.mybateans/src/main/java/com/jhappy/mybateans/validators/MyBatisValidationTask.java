package com.jhappy.mybateans.validators;

import com.jhappy.mybateans.hyperlink.MyBatisXmlHyperlinkProvider;
import com.jhappy.mybateans.util.xml.parser.AttributeData;
import com.jhappy.mybateans.indexing.MyBatisMapperData;
import com.jhappy.mybateans.indexing.MyBatisIndexer;
import com.jhappy.mybateans.util.xml.parser.TagData;
import com.jhappy.mybateans.util.xml.parser.XmlData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author th
 */
public class MyBatisValidationTask extends ParserResultTask {

    public static final Set<String> DEFAULT_ALIASES = Set.of(
            "_byte", "_char", "_character", "_long", "_short", "_int", "_integer", "_double", "_float", "_boolean",
            "string", "byte", "char", "character", "long", "short", "int", "integer", "double", "float", "boolean",
            "date", "decimal", "bigdecimal", "biginteger", "object",
            "date[]", "decimal[]", "bigdecimal[]", "biginteger[]", "object[]",
            "map", "hashmap", "list", "arraylist", "collection", "iterator"
    );

    public static final Set<String> INCLUDE_JAVA_METHOD_ON_ID_TAG = Set.of(
            "select", "insert", "delete", "update"
    );

    public static final Set<String> INCLUDE_ID_TAG = Set.of(
            "select", "insert", "delete", "update", "sql", "resultMap"
    );

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {

        Snapshot snapshot = result.getSnapshot();
        FileObject fo = snapshot.getSource().getFileObject();
        Document doc = snapshot.getSource().getDocument(false);

        List<ErrorDescription> errors = new ArrayList<>();

        if (result instanceof MyBatisConfigXmlParserResult mybatisConfigResult) {

            for (AttributeData attr : mybatisConfigResult.getTypeAliases()) {
                if (attr != null) {
                    String alias = attr.getValue();
                    int offset = attr.getValueoffset();
                    if (!MyBatisIndexer.existsJavaType(fo, alias)) {
                        String message = alias + " is not found as Alias or JavaType";
                        errors.add(createError(alias, fo, offset, message));
                    }
                }
            }

            for (AttributeData packagedata : mybatisConfigResult.getPackagelist()) {
                if (packagedata != null) {
                    String packagename = packagedata.getValue();
                    int offset = packagedata.getValueoffset();
                    if (!MyBatisXmlHyperlinkProvider.isExistPackage(packagename, fo)) {
                        String message = "this package is not found";
                        errors.add(createError(packagename, fo, offset, message));
                    }
                }
            }

        } else if (result instanceof MyBatisMapperXmlParseResult mapperXmlParseResult) {

            MyBatisMapperData mapperData = mapperXmlParseResult.getMyBatisData();

            if (mapperData == null) {
                return;
            }

            String namespace = mapperData.getNamespace();

            if (namespace != null) {

                if (!MyBatisIndexer.existsJavaType(fo, namespace)) {

                    int offset = mapperData.getNamespaceOffset();
                    errors.add(createError(namespace, fo, offset, "not found"));

                } else {

                    Set<String> javaMethods = getJavaMethods(fo, namespace);
                    Map<String, AttributeData> ids = new HashMap<>();
                    Set<String> duplicatedIds = new HashSet<>();

                    for (XmlData tagData : mapperData.getTags()) {

                        String tagName = tagData.getTagName();

                        for (String attrName : List.of("resultType", "parameterType", "type")) {

                            AttributeData attr = tagData.getAttributes().get(attrName);

                            if (attr != null) {
                                String value = attr.getValue();
                                if (isInvalidAlias(value, fo)) {
                                    String message = value + " is not found as Alias or JavaType";
                                    errors.add(createError(value, fo, attr.getValueoffset(), message));
                                }

                            }
                        }

                        if (INCLUDE_JAVA_METHOD_ON_ID_TAG.contains(tagName)) {

                            AttributeData idAttr = tagData.getAttributes().get("id");

                            if (idAttr != null) {

                                String methodName = idAttr.getValue();

                                if (!javaMethods.contains(methodName)) {
                                    String message = namespace + "." + methodName + " is not found on class";
                                    errors.add(createError(methodName, fo, idAttr.getValueoffset(), message));

                                }

                            }
                        }
                        if (INCLUDE_ID_TAG.contains(tagName)) {

                            AttributeData idAttr = tagData.getAttributes().get("id");
                            if (idAttr != null) {

                                String id = idAttr.getValue();

                                if (ids.keySet().contains(id)) {

                                    String message = "id is dupulicated";
                                    errors.add(createError(id, fo, idAttr.getValueoffset(), message));

                                    if (!duplicatedIds.contains(id)) {
                                        AttributeData reportedAttr = ids.get(id);
                                        errors.add(createError(reportedAttr.getValue(), fo, reportedAttr.getValueoffset(), message));
                                    }

                                    duplicatedIds.add(id);

                                } else {
                                    ids.put(id, idAttr);
                                }

                            }

                        }

                    }

                }

            }

        }

        HintsController.setErrors(doc, "MyBatis", errors);
    }

    /**
     *
     * @param value
     * @param fo
     * @return
     */
    private boolean isInvalidAlias(String value, FileObject fo) {

        if (DEFAULT_ALIASES.contains(value.toLowerCase(Locale.ROOT))) {
            return false;
        }

        boolean exists = MyBatisXmlHyperlinkProvider.validateAlias(fo, value);

        return !exists;
    }

    /**
     *
     * @param value
     * @param fo
     * @param offset
     * @return
     */
    private static ErrorDescription createError(String value, FileObject fo, int offset, String message) {
        ErrorDescription error = ErrorDescriptionFactory.createErrorDescription(
                Severity.ERROR,
                message,
                fo,
                offset,
                offset + value.length()
        );
        return error;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

    /**
     *
     */
    private Set<String> getJavaMethods(FileObject fo, String fqn) {

        final Set<String> methods = new HashSet<>();

        JavaSource js = MyBatisIndexer.getJavaSource(fo, fqn);

        if (js == null) {
            return methods;
        }

        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement te = cc.getElements().getTypeElement(fqn);
                if (te != null) {
                    for (Element e : te.getEnclosedElements()) {
                        if (e instanceof ExecutableElement) {
                            methods.add(e.getSimpleName().toString());
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return methods;
    }

}
