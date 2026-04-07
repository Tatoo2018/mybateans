/*
 * The MIT License
 *
 * Copyright 2026 th.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jhappy.mybateans.hint;

import com.jhappy.mybateans.hyperlink.MyBatisMapperHyperlinkProvider;
import com.jhappy.mybateans.hyperlink.TargetLocation;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_MapperMethodHind", description = "#DESC_MapperMethodHind", category = "general")
@Messages({
    "DN_MapperMethodHind=MyBatis Mapper Check",
    "DESC_MapperMethodHind="
})
public class MapperMethodHint {

    @TriggerPattern("$mods$ $retType $name($args$)")
    @Messages("ERR_MapperMethodHind=This method doesn't exisit at Mapper XML")
    public static ErrorDescription computeWarning(HintContext ctx) {

        CompilationInfo compilationInfo = ctx.getInfo();

        javax.lang.model.element.Element element = compilationInfo.getTrees().getElement(ctx.getPath());

        if (element != null && element.getKind() == ElementKind.METHOD) {
            try {
                ExecutableElement method = (ExecutableElement) element;

                String methodName = method.getSimpleName().toString();

                TypeElement typeElement = (TypeElement) method.getEnclosingElement();
                if (typeElement != null && typeElement.getKind() == ElementKind.INTERFACE) {

                    String interfaceFqn = typeElement.getQualifiedName().toString();

                    TargetLocation location = MyBatisMapperHyperlinkProvider.findMapperLocation(ctx.getInfo().getFileObject(), interfaceFqn, methodName);

                    if (location == null) {
                        String[] possibleTags = {"select", "insert", "update", "delete"};
                        List<Fix> fixes = new java.util.ArrayList<>();

                        for (String tag : possibleTags) {
                            fixes.add(new FixImpl(ctx.getInfo(), ctx.getPath(), tag).toEditorFix());
                        }

                        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(),
                                Bundle.ERR_MapperMethodHind(), fixes.toArray(new Fix[0]));
                    }
                }

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        return null;

    }

    private static final class FixImpl extends JavaFix {

        private final String forcedTagName;

        public FixImpl(CompilationInfo info, TreePath tp, String tagName) {
            super(info, tp);
            this.forcedTagName = tagName;
        }

        @Override
        @Messages({
            "FIX_Generate_select=Generate select tag",
            "FIX_Generate_insert=Generate insert tag",
            "FIX_Generate_update=Generate update tag",
            "FIX_Generate_delete=Generate delete tag"
        })
        protected String getText() {
            return switch (forcedTagName) {
                case "select" ->
                    Bundle.FIX_Generate_select();
                case "insert" ->
                    Bundle.FIX_Generate_insert();
                case "update" ->
                    Bundle.FIX_Generate_update();
                case "delete" ->
                    Bundle.FIX_Generate_delete();
                default ->
                    "Generate " + forcedTagName;
            };
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {

            WorkingCopy wc = ctx.getWorkingCopy();
            try {
                wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }

            Element element = wc.getTrees().getElement(ctx.getPath());

            if (element != null && element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                String methodName = method.getSimpleName().toString();

                TypeElement typeElement = (TypeElement) method.getEnclosingElement();
                String interfaceFqn = typeElement.getQualifiedName().toString();
                Project project = FileOwnerQuery.getOwner(wc.getFileObject());

                try {
                    FileObject xmlFile = MyBatisMapperHyperlinkProvider.findMapperXmlFile(project, interfaceFqn);
                    if (xmlFile != null) {
                        insertTagToXml(xmlFile, methodName, method, wc, forcedTagName);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        }

        private static void insertTagToXml(FileObject xmlFile, String methodName, ExecutableElement method, WorkingCopy wc, String tagName) {
            try {

                DataObject dao = DataObject.find(xmlFile);
                EditorCookie editor = dao.getLookup().lookup(EditorCookie.class);
                if (editor == null) {
                    return;
                }

                StyledDocument doc = editor.openDocument();

                TypeMirror returnTypeMirror = method.getReturnType();

                String resultType = "";
                String updateSql = """
                                           UPDATE
                                               table_name
                                           SET
                                   """;
                String selectSql = """
                                           SELECT
                                               column
                                           FROM
                                               table_name
                                   """;

                String deleteSql = """
                                           DELETE
                                           FROM
                                               table_name
                                   """;

                if ("select".equals(tagName)) {
                    resultType = resolveResultType(returnTypeMirror);
                }

                List<String> paramNames = method.getParameters().stream()
                        .map(p -> p.getSimpleName().toString())
                        .collect(Collectors.toList());

                if (method.getParameters().size() == 1) {
                    VariableElement param = method.getParameters().get(0);
                    TypeMirror type = param.asType();

                    if (isJavaBean(type, wc)) { 
                        paramNames = getProperties(type, wc);

                    }
                }

                String sql = "";

                StringBuilder whereSql = new StringBuilder();
                StringBuilder paramsSql = new StringBuilder();
                if (!paramNames.isEmpty()) {

                    for (int i = 0; i < paramNames.size(); i++) {
                        String paramName = paramNames.get(i);

                        if (i == 0) {
                            whereSql.append("        where 1=1\n");
                        }
                        whereSql.append("            and ").append(paramName).append(" = #{").append(paramName).append("}\n");

                        paramsSql.append("            " + paramName).append(" = #{").append(paramName).append("}");
                        if (i != paramNames.size() - 1) {
                            paramsSql.append(",\n");
                        } else {
                            paramsSql.append("\n");
                        }

                    }
                }

                if ("select".equals(tagName)) {
                    sql = selectSql + whereSql.toString();
                } else if ("update".equals(tagName)) {
                    sql = updateSql + paramsSql.toString() + whereSql.toString();
                } else if ("delete".equals(tagName)) {
                    sql = deleteSql + whereSql.toString();
                } else if ("insert".equals(tagName)) {
                    StringBuilder columns = new StringBuilder();
                    StringBuilder values = new StringBuilder();
                    String indent = "            ";
                    for (int i = 0; i < paramNames.size(); i++) {
                        String name = paramNames.get(i);

                        columns.append(name);
                        values.append("#{").append(name).append("}");

                        if (i != paramNames.size() - 1) {
                            columns.append(", ");
                            if ((i + 1) % 4 == 0) {
                                columns.append("\n").append(indent);
                                values.append("\n").append(indent);
                            } else {
                                values.append(", ");
                            }
                        }
                    }
                    sql = """
                    INSERT INTO
                    table_name
                    (
            """
                            + indent + columns.toString() + "\n"
                            + """
                    )
                    VALUES
                    (
            """
                            + indent + values.toString() + "\n"
                            + """
                    )
            """;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("\n    <").append(tagName).append(" id=\"").append(methodName).append("\"");
                if ("select".equals(tagName)) {
                    sb.append(" resultType=\"").append(resultType).append("\"");
                }
                sb.append(">\n").append(sql).append("    </").append(tagName).append(">\n\n");

                String text = doc.getText(0, doc.getLength());
                int lastIndex = text.lastIndexOf("</mapper>");

                if (lastIndex != -1) {
                    doc.insertString(lastIndex, sb.toString(), null);
                    final int newCaretPos = lastIndex + sb.toString().indexOf(">") + 2;

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        NbDocument.openDocument(xmlFile, newCaretPos,
                                Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                    });
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * 戻り値の型から MyBatis の resultType にふさわしい文字列を返す
     */
    private static String resolveResultType(TypeMirror type) {

        String typeStr = type.toString();

        if (typeStr.contains("<") && typeStr.contains(">")) {
            typeStr = typeStr.substring(typeStr.indexOf("<") + 1, typeStr.lastIndexOf(">"));
        }

        if (typeStr.equals("java.lang.String")) {
            return "string";
        }
        if (typeStr.equals("java.lang.Integer")) {
            return "int";
        }
        if (typeStr.equals("java.lang.Long")) {
            return "long";
        }
        if (typeStr.equals("java.util.Map") || typeStr.startsWith("java.util.Map<")) {
            return "map";
        }

        return typeStr;
    }

    private static boolean isJavaBean(TypeMirror type, WorkingCopy wc) {
        String ts = type.toString();
        return !ts.startsWith("java.lang.") && !ts.startsWith("java.util.") && !type.getKind().isPrimitive();
    }

    private static List<String> getProperties(TypeMirror type, WorkingCopy wc) {
        List<String> props = new java.util.ArrayList<>();
        Element typeElement = wc.getTypes().asElement(type);

        if (typeElement != null) {
            for (Element encl : typeElement.getEnclosedElements()) {
                if (encl.getKind() == ElementKind.FIELD) {
                    Set<Modifier> mods = encl.getModifiers();
                    if (!mods.contains(Modifier.STATIC) && !mods.contains(Modifier.TRANSIENT)) {
                        props.add(encl.getSimpleName().toString());
                    }
                }
            }
        }
        return props;
    }
}
