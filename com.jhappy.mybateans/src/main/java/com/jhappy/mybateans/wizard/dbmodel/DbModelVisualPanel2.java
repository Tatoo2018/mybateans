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
package com.jhappy.mybateans.wizard.dbmodel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.Exceptions;

public final class DbModelVisualPanel2 extends JPanel {

    /**
     * @return the connection
     */
    public DatabaseConnection getConnection() {
        return connection;
    }

    /**
     * @param connection the connection to set
     */
    public void setData(DatabaseConnection connection, String schema) {
        this.connection = connection;
        this.schema = schema;
        txtSchema.setText(schema);

        Connection conn = connection.getJDBCConnection();
        if (conn != null) {
            try {
                DatabaseMetaData metadata = conn.getMetaData();
                String[] types = {"TABLE"};
                try (ResultSet rs = metadata.getTables(null, schema, "%", types)) {
                    DefaultListModel<String> tableModel = new DefaultListModel<>();

                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        tableModel.addElement(tableName);
                        loadTableData(tableName);
                    }

                    listTable.setModel(tableModel);
                }

            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }
    
    public List<String> getSelectedTables() {
        return listTable.getSelectedValuesList();        
    }
    
    public Map<String, List<Map<String, Object>>> getTableData(){
        
        return tableData;
    }
    
    public boolean isUsedJpaAnnotation(){
        return chkJpaAnnotation.isSelected();
    }
    

    Map<String, List<Map<String, Object>>> tableData = new HashMap();

    void loadTableData(String tableName) {

        try {
            DatabaseMetaData metadata = connection.getJDBCConnection().getMetaData();

            List<Map<String, Object>> cols = new ArrayList<>();

            Set<String> keys = new HashSet<>();
            try (ResultSet rs = metadata.getPrimaryKeys(null, schema, tableName)) {
                while (rs.next()) {
                    String pkColumn = rs.getString("COLUMN_NAME");
                    keys.add(pkColumn);
                }
            }

            try (ResultSet rs = metadata.getColumns(null, schema, tableName, "%")) {

                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String typeName = rs.getString("TYPE_NAME");
                    int size = rs.getInt("COLUMN_SIZE");
                    boolean isKey = keys.contains(colName);
                    Map<String, Object> col = new HashMap<>();
                    col.put("COLUMN_NAME", colName);
                    col.put("TYPE_NAME", typeName);
                    col.put("COLUMN_SIZE", size);
                    col.put("IS_KEY", isKey);

                    cols.add(col);

                }
            }

            tableData.put(tableName, cols);

        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    private DatabaseConnection connection = null;
    private String schema = null;

    /**
     * Creates new form DbModelVisualPanel2
     */
    public DbModelVisualPanel2() {
        initComponents();

        listTable.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { 
                String selectedTable = listTable.getSelectedValue();
                if (selectedTable != null) {
                    showTableDetails(selectedTable);
                }
            }
        });
    }

    private void showTableDetails(String tableName) {

        DefaultTableModel model = (DefaultTableModel) tbl.getModel();
        model.setRowCount(0);
        List<Map<String, Object>> cols = tableData.get(tableName);
        for (Map<String, Object> col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            String typeName = (String) col.get("TYPE_NAME");
            Integer size = (Integer) col.get("COLUMN_SIZE");
            Boolean isKey = (Boolean) col.get("IS_KEY");
            model.addRow(new Object[]{colName, typeName, size, isKey});
        }

    }

    @Override
    public String getName() {
        return "Step #2";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtSchema = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        listTable = new javax.swing.JList<>();
        scrollPane1 = new javax.swing.JScrollPane();
        tbl = new javax.swing.JTable();
        chkJpaAnnotation = new javax.swing.JCheckBox();

        txtSchema.setText(org.openide.util.NbBundle.getMessage(DbModelVisualPanel2.class, "DbModelVisualPanel2.txtSchema.text")); // NOI18N
        txtSchema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSchemaActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(listTable);

        tbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "COLUMN", "TYPE", "SIZE", "KEY"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollPane1.setViewportView(tbl);

        org.openide.awt.Mnemonics.setLocalizedText(chkJpaAnnotation, org.openide.util.NbBundle.getMessage(DbModelVisualPanel2.class, "DbModelVisualPanel2.chkJpaAnnotation.text")); // NOI18N
        chkJpaAnnotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkJpaAnnotationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                    .addComponent(txtSchema)
                    .addComponent(chkJpaAnnotation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkJpaAnnotation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(scrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtSchemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSchemaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSchemaActionPerformed

    private void chkJpaAnnotationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkJpaAnnotationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkJpaAnnotationActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkJpaAnnotation;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> listTable;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JTable tbl;
    private javax.swing.JTextField txtSchema;
    // End of variables declaration//GEN-END:variables

    
}
