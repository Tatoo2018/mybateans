package com.jhappy.mybateans.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

@ActionID(category = "Database", id = "com.jhappy.mybateans.action.GenerateJavaBeanAction")
@ActionRegistration(displayName = "Generate JavaBean")
@ActionReferences({
    @ActionReference(path = "Databases/Explorer/Table/Actions", position = 600)
})
public final class GenerateJavaBeanAction implements ActionListener {

    private final Node context; // Nodeで受け取る

    public GenerateJavaBeanAction(Node context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ConnectionManager cm = ConnectionManager.getDefault();
        DatabaseConnection[] connections = cm.getConnections();

        // 2. 右クリックしたテーブル名と一致する接続を探す、
        // もしくは現在「アクティブ」な接続を探す
        for (DatabaseConnection dbconn : connections) {
            // ここで dbconn.getName() や、
            // 保持しているスキーマ情報と context.getDisplayName() を照合する
            System.out.println("Available Connection: " + dbconn.getDisplayName());

            try {
                DatabaseMetaData metadata = dbconn.getJDBCConnection().getMetaData();

                Collection<? extends Object> allItems = context.getLookup().lookupAll(Object.class);

                debug(allItems);

                String tableName = context.getDisplayName();

                // 1. カラム情報を取得（カタログ、スキーマ、テーブル名、カラム名パターン）
                try (java.sql.ResultSet rs = metadata.getColumns(null, null, tableName, null)) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("public class ").append(tableName).append(" {\n\n");

                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        int dataType = rs.getInt("DATA_TYPE"); // java.sql.Types の値

                        // 2. SQL型をJava型に変換
                        String javaType = convertSqlTypeToJavaType(dataType);

                        // 3. フィールド生成
                        sb.append("    private ").append(javaType).append(" ").append(columnName).append(";\n");
                    }

                    sb.append("\n}");

                    // 4. 結果をコンソールに出力（またはファイル生成へ！）
                    System.out.println(sb.toString());
                    StatusDisplayer.getDefault().setStatusText("JavaBean generated for " + tableName);
                }

                FilterNode fNode = context.getLookup().lookup(FilterNode.class);

                //   DatabaseConnection conn = context.getLookup().lookup(DatabaseConnection.class);
                //   NodeRegistry nodeRegistry = context.getLookup().lookup(NodeRegistry.class);
                debug(fNode.getLookup().lookupAll(Object.class));
                System.out.println(fNode);

            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

    }
    
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

    public void debug(Collection<? extends Object> allItems) {
        System.out.println("--- Lookup Contents Start ---");
        for (Object item : allItems) {
            // クラス名を表示
            System.out.println("Item: " + item.getClass().getName());
        }
        System.out.println("--- Lookup Contents End ---");
    }
}
