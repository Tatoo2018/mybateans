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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.ImageUtilities;

public final class DbModelVisualPanel1 extends JPanel {

    List<Map<String, Object>> dbconnections = new ArrayList();
    private Map<String, List<String>> schemas = new HashMap<>();

    private DbModelWizardPanel1 controller;

    public void setController(DbModelWizardPanel1 controller) {
        this.controller = controller;
    }



    void setSchemas(Map<String, List<String>> schemas) {
        this.schemas = schemas;
    }

    void setConnectionData(List<Map<String, Object>> dbconnections) {

        lblMessage3.setVisible(false);
        lblImage.setVisible(false);
        lblMessage2.setVisible(false);
        lblImage2.setVisible(false);
        lblMessage1.setVisible(false);
        if (dbconnections.isEmpty()) {
            lblMessage2.setVisible(true);
            lblImage2.setVisible(true);
        }
        this.dbconnections.addAll(dbconnections);
        for (Map<String, Object> d : dbconnections) {
            d.put("NAME", d.get("databaseName") + " : " + d.get("databaseUrl"));
            jHComboBox1.addItem(d);
        }
    }

    public String getSelectedSchema() {

        return listSchema.getSelectedValue();

    }

    public DatabaseConnection getSelectedConnectioon() {

        Map<String, Object> connData = (Map<String, Object>) jHComboBox1.getSelectedItem();

        if (connData != null) {
            return (DatabaseConnection) connData.get("connection");
        } else {
            return null;
        }

    }

    /**
     * Creates new form DbModelVisualPanel1
     */
    public DbModelVisualPanel1() {
        initComponents();
        lblMessage1.setVisible(false);
        jHComboBox1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                lblMessage1.setVisible(true);

                Object item = e.getItem();

                if (item instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) item;

                    updateSchemaTable(data);

                }

            }

            public void updateSchemaTable(Map<String, Object> data) {

                DefaultListModel model = (DefaultListModel) listSchema.getModel();
                model.removeAllElements();
                Boolean isConnected = (Boolean) data.get("isConnected");
                if (!isConnected) {
                    lblMessage3.setVisible(true);
                    lblImage.setVisible(true);
                    lblMessage1.setVisible(false);
                } else {
                    lblMessage3.setVisible(false);
                    lblImage.setVisible(false);
                    lblMessage1.setVisible(true);

                    String key = (String) data.get("KEY");
                    List<String> schemaList = schemas.get(key);

                    if (schemaList != null) {
                        lblMessage1.setVisible(false);
                        for (String schema : schemaList) {
                            model.addElement(schema);
                        }
                    }
                }
            }

        });
        
        listSchema.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                if(controller!=null){
                    controller.fireChangeEvent();
                }
                
            }
        });
        
        
    }

    @Override
    public String getName() {
        return "Step #1";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myBatisSampleTemplateProjectWizardPanel1 = new com.jhappy.mybateans.template.MyBatisSampleTemplateProjectWizardPanel();
        myBatisSampleTemplateProjectWizardIterator1 = new com.jhappy.mybateans.template.MyBatisSampleTemplateProjectWizardIterator();
        jLabel1 = new javax.swing.JLabel();
        jHComboBox1 = new com.jhappy.mybateans.wizard.dbmodel.JHComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        listSchema = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        lblMessage1 = new javax.swing.JLabel();
        lblMessage2 = new javax.swing.JLabel();
        lblMessage3 = new javax.swing.JLabel();
        lblImage = new javax.swing.JLabel();
        lblImage2 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DbModelVisualPanel1.class, "DbModelVisualPanel1.jLabel1.text")); // NOI18N

        listSchema.setModel(new DefaultListModel<String>());
        jScrollPane1.setViewportView(listSchema);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DbModelVisualPanel1.class, "DbModelVisualPanel1.jLabel2.text")); // NOI18N

        lblMessage1.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(lblMessage1, org.openide.util.NbBundle.getMessage(DbModelVisualPanel1.class, "DbModelVisualPanel1.lblMessage1.text")); // NOI18N

        lblMessage2.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(lblMessage2, org.openide.util.NbBundle.getMessage(DbModelVisualPanel1.class, "DbModelVisualPanel1.lblMessage2.text")); // NOI18N

        lblMessage3.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(lblMessage3, org.openide.util.NbBundle.getMessage(DbModelVisualPanel1.class, "DbModelVisualPanel1.lblMessage3.text")); // NOI18N

        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jhappy/mybateans/wizard/dbmodel/descriptionconnection.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblImage, org.openide.util.NbBundle.getMessage(DbModelVisualPanel1.class, "DbModelVisualPanel1.lblImage.text")); // NOI18N

        lblImage2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jhappy/mybateans/wizard/dbmodel/descriptionconnection2.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblImage2, org.openide.util.NbBundle.getMessage(DbModelVisualPanel1.class, "DbModelVisualPanel1.lblImage2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblMessage3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblImage2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jHComboBox1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(lblMessage1, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblMessage2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jHComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMessage2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblImage2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMessage3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblMessage1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jhappy.mybateans.wizard.dbmodel.JHComboBox jHComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblImage;
    private javax.swing.JLabel lblImage2;
    private javax.swing.JLabel lblMessage1;
    private javax.swing.JLabel lblMessage2;
    private javax.swing.JLabel lblMessage3;
    private javax.swing.JList<String> listSchema;
    private com.jhappy.mybateans.template.MyBatisSampleTemplateProjectWizardIterator myBatisSampleTemplateProjectWizardIterator1;
    private com.jhappy.mybateans.template.MyBatisSampleTemplateProjectWizardPanel myBatisSampleTemplateProjectWizardPanel1;
    // End of variables declaration//GEN-END:variables

}
