package com.jhappy.mybateans.validators;

import com.jhappy.mybateans.hyperlink.MyBatisXmlHyperlinkProvider;
import com.jhappy.mybateans.util.xml.parser.AttributeData;
import com.jhappy.mybateans.indexing.MyBatisData;
import com.jhappy.mybateans.indexing.MyBatisIndexer;
import com.jhappy.mybateans.util.xml.parser.TagData;
import com.jhappy.mybateans.util.xml.parser.XmlData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
 * 1つのクラスでパースとエラー表示を完結させるタスク
 */
public class MyBatisValidationTask extends ParserResultTask {

    public static final Set<String> DEFAULT_ALIASES = Set.of(
            "_byte", "_char", "_character", "_long", "_short", "_int", "_integer", "_double", "_float", "_boolean",
            "string", "byte", "char", "character", "long", "short", "int", "integer", "double", "float", "boolean",
            "date", "decimal", "bigdecimal", "biginteger", "object",
            "date[]", "decimal[]", "bigdecimal[]", "biginteger[]", "object[]",
            "map", "hashmap", "list", "arraylist", "collection", "iterator"
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
                    boolean exists = MyBatisIndexer.existsJavaType(fo, alias);
                    if (!exists) {
                        ErrorDescription error = addError(alias,
                                fo,
                                offset);
                        errors.add(error);
                    }
                }

            }

            for (AttributeData packagedata : mybatisConfigResult.getPackagelist()) {
                if (packagedata != null) {
                    String packagename = packagedata.getValue();
                    int offset = packagedata.getValueoffset();

                    boolean exists = MyBatisXmlHyperlinkProvider.isExistPackage(packagename, fo);
                    if (!exists) {
                        ErrorDescription error = addError(packagename,
                                fo,
                                offset);
                        errors.add(error);
                    }
                }

            }

        } else if (result instanceof MyBatisMapperXmlParseResult mybatisdata) {

            MyBatisData mybatismapperdata = mybatisdata.getMyBatisData();
            if (mybatismapperdata == null) {
                return;
            }

            String namespace = mybatismapperdata.getNamespace();
            int offset = mybatismapperdata.getNamespaceOffset();

            if (namespace != null) {
                boolean exists = MyBatisIndexer.existsJavaType(fo, namespace);
                if (!exists) {
                    ErrorDescription error = addError(namespace,
                            fo,
                            offset);
                    errors.add(error);
                }

                if (exists) {

                    Set<String> javaMethods = getJavaMethods(fo, namespace);
                    for (XmlData tagData : mybatismapperdata.getTags()) {

                        AttributeData idAttr = tagData.getAttributes().get("id");
                        if (idAttr != null) {
                            String methodName = idAttr.getValue();
                            if (!javaMethods.contains(methodName)) {

                                ErrorDescription error = addError(methodName, fo, idAttr.getValueoffset());
                                errors.add(error);

                            }

                        }

                    }

                }
            }

            List<XmlData> idDatas = mybatismapperdata.getTags();

            for (XmlData tagData : idDatas) {

                AttributeData attrData = tagData.getAttributes().get("resultType");
                if (attrData != null) {

                    String resultType = attrData.getValue();

                    if (!DEFAULT_ALIASES.contains(resultType.toLowerCase(Locale.ROOT))) {

                        boolean exists = MyBatisXmlHyperlinkProvider.validateAlias(fo, resultType);

                        if (!exists) {
                            ErrorDescription error = addError(resultType, fo, attrData.getValueoffset());
                            errors.add(error);
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
     * @param offset
     * @return
     */
    public ErrorDescription addError(String value, FileObject fo, int offset) {
        ErrorDescription error = ErrorDescriptionFactory.createErrorDescription(
                Severity.ERROR,
                value + " not found.",
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
     * Java インターフェースからメソッド名の集合を取得する
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
