package com.jhappy.mybateans.action;

import com.google.common.base.CaseFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

@ActionID(category = "Database", id = "com.jhappy.mybateans.action.GenerateJavaBeanAction")
@ActionRegistration(iconBase = "com/jhappy/mybateans/images/icon.png", displayName = "#GenerateJavaBean")
@ActionReferences({
    @ActionReference(path = "Databases/Explorer/Table/Actions", position = 600)
})
public final class GenerateJavaBeanAction implements ActionListener {

    /**
     *
     */
    private final Node context;

    /**
     *
     * @param context
     */
    public GenerateJavaBeanAction(Node context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        ConnectionManager cm = ConnectionManager.getDefault();
        DatabaseConnection[] connections = cm.getConnections();

        org.netbeans.modules.db.explorer.DatabaseConnection conn = context.getLookup().lookup(org.netbeans.modules.db.explorer.DatabaseConnection.class);

        MetadataElementHandle handle = context.getLookup().lookup(MetadataElementHandle.class);

        new Thread(() -> {
            MetadataModel metadataModel = conn.getMetadataModel();
            try {
                metadataModel.runReadAction(new Action<Metadata>() {
                    @Override
                    public void run(Metadata metadata) {

                        MetadataElement m = handle.resolve(metadata);
                        if (m instanceof Table table) {

                            String tableName = table.getName();

                            String tableNameUpperCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);

                            StringBuilder sb = new StringBuilder();

                            sb.append("public class ").append(tableNameUpperCamel).append(" {\n\n");

                            for (Column col : table.getColumns()) {

                                String columnName = col.getName();

                                String columnNameLowerCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);

                                SQLType dataType = col.getType();

                                String typeName = col.getTypeName();

                                int length = col.getLength();

                                String javaType = convertSqlTypeToJavaType(dataType.ordinal());

                                sb.append("    private ").append(javaType).append(" ").append(columnNameLowerCamel).append(";\n");

                            }

                            sb.append("\n");

                            PrimaryKey pk = table.getPrimaryKey();

                            Set<String> pkColumns = new HashSet<>();

                            if (pk != null) {
                                for (Column pkCol : pk.getColumns()) {
                                    pkColumns.add(pkCol.getName());
                                }
                            }

                            for (Column col : table.getColumns()) {

                                String columnName = col.getName();

                                String columnNameLowerCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);

                                SQLType dataType = col.getType();

                                String typeName = col.getTypeName();
                                String javaType = convertSqlTypeToJavaType(dataType.ordinal());

                                int length = col.getLength();
                                sb.append("    public ").append(javaType).append(" get").append(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, columnNameLowerCamel)).append("() {\n");
                                sb.append("        return this.").append(columnNameLowerCamel).append(";\n");
                                sb.append("    }\n");

                            }

                            sb.append("\n}");

                            List<Map<String, Object>> formattedCols = new ArrayList<>();

                            for (Column col : table.getColumns()) {

                                Map<String, Object> coldata = new HashMap<>();
                                String rawColName = col.getName();
                                SQLType dataType = col.getType();
                                int colLength = col.getLength();
                                String typeName = col.getTypeName();
                                String javaType = convertSqlTypeToJavaType(dataType.ordinal());
                                String columnNameLowerCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, rawColName);
                                boolean isKey = pkColumns.contains(rawColName);
                                  

                                coldata.put("COLUMN_NAME", rawColName);
                                coldata.put("COLUMN_SIZE", colLength);
                                coldata.put("COLUMN_NAME_LOWER_CAMEL_CASE", columnNameLowerCamel);
                                coldata.put("TYPE_NAME", typeName);
                                coldata.put("IS_KEY", isKey);

                                formattedCols.add(coldata);

                            }

                            String result = generateJavaBean(null, tableName, formattedCols, false);

                            javax.swing.SwingUtilities.invokeLater(() -> {
                                JTextArea textArea = new JTextArea(result);
                                textArea.setEditable(false);
                                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                                JScrollPane scrollPane = new JScrollPane(textArea);
                                scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));

                                String title = "Generated JavaBean: " + tableNameUpperCamel;
                                DialogDescriptor dd = new DialogDescriptor(scrollPane, title);
                                DialogDisplayer.getDefault().notify(dd);
                            });

                        }

                    }

                });
            } catch (MetadataModelException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();

    }

    private String generateJavaBean(String packageName, String tableName, List<Map<String, Object>> formattedCols, boolean isUseJpaAnnotation) {
        try {

            FileObject templateFO = FileUtil.getConfigFile("Templates/MyBatis/JavaBean.java");
            String templateStr = templateFO.asText("UTF-8");

            Map<String, Object> param = new HashMap<>();
            param.put("cols", formattedCols);
            param.put("tableName", tableName);
            param.put("classname", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName));
            param.put("package", packageName);
            param.put("isUseJpaAnnotation", isUseJpaAnnotation);

            String resultJava = CreateMapperXmlAction.applyTemplate(templateStr, param, "JavaBean.java");

            return resultJava;

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     *
     * @param sqlType
     * @return
     */
    private String convertSqlTypeToJavaType(int sqlType) {
        switch (sqlType) {
            case java.sql.Types.VARCHAR:
            case java.sql.Types.CHAR:
            case java.sql.Types.LONGVARCHAR:
                return "String";
            case java.sql.Types.INTEGER:
                return "Integer";
            case java.sql.Types.BIGINT:
                return "Long";
            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                return "java.math.BigDecimal";
            case java.sql.Types.DATE:
                return "java.util.Date";
            case java.sql.Types.TIMESTAMP:
                return "java.sql.Timestamp";
            default:
                return "Object";
        }
    }

    /**
     *
     * @param allItems
     */
    public void debug(Collection<? extends Object> allItems) {
        System.out.println("--- Lookup Contents Start ---");
        for (Object item : allItems) {
            // クラス名を表示
            System.out.println("Item: " + item.getClass().getName());
        }
        System.out.println("--- Lookup Contents End ---");
    }
}
