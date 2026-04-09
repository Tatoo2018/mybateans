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

import java.awt.Component;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

public final class DbModelWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private int index;

    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {

        DbModelWizardPanel1 panel1 = new DbModelWizardPanel1();
        ConnectionManager cm = ConnectionManager.getDefault();
        DatabaseConnection[] connections = cm.getConnections();

        List<Map<String, String>> dbconnections = new ArrayList();

        Map<String, List<String>> schemas = new HashMap<>();

        for (int i = 0; i < connections.length; i++) {

            DatabaseConnection dbconn = connections[i];
            Map<String, Object> connection = new HashMap<>();
            String displayName = dbconn.getDisplayName();
            String databaseUrl = dbconn.getDatabaseURL();

            connection.put("KEY", i + "");
            connection.put("databaseName", displayName);
            connection.put("databaseUrl", databaseUrl);
            connection.put("connection", dbconn);
            panel1.addConnectionData(connection);

            Connection conn = dbconn.getJDBCConnection();
            if (conn == null) {
                connection.put("isConnected", false);
                continue;
            }
            connection.put("isConnected", true);

            try {
                DatabaseMetaData metadata = conn.getMetaData();

                try (ResultSet rs = metadata.getSchemas()) {
                    List<String> schemaList = new ArrayList<>();

                    while (rs.next()) {
                        String schemaName = rs.getString("TABLE_SCHEM");
                        schemaList.add(schemaName);
                    }
                    schemas.put(i + "", schemaList);
                }

            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        panel1.setSchemas(schemas);
        panel1.setSchemas(schemas);

        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
            panels.add(panel1);
            panels.add(new DbModelWizardPanel2());
            String[] steps = createSteps();
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    @Override
    public Set<?> instantiate() throws IOException {

        Set result = new HashSet();

        FileObject targetFolder = Templates.getTargetFolder(wizard);
        FileObject template = Templates.getTemplate(wizard);

        Map<String, List<Map<String, Object>>> tableData = (Map<String, List<Map<String, Object>>>) wizard.getProperty("tableData");
        List<String> selectedTables = (List<String>) wizard.getProperty("selectedTables");
        Boolean isUseJpaAnnotation = (Boolean) wizard.getProperty("isUseJpaAnnotation");

        String packageName = getPackageName(targetFolder);

        for (String table : selectedTables) {

            List<Map<String, Object>> cols = tableData.get(table);
            List<Map<String, Object>> formattedCols = new ArrayList<>();

            for (Map<String, Object> rawCol : cols) {

                Map<String, Object> col = new HashMap<>(rawCol);
                String rawColName = (String) col.get("COLUMN_NAME");

                col.put("COLUMN_NAME_LOWER_CAMEL_CASE", toLowerCamelCase(rawColName));
                formattedCols.add(col);
            }

            DataFolder folder = DataFolder.findFolder(targetFolder);
            DataObject templateDO = DataObject.find(template);
            Map<String, Object> param = new HashMap<>();
            param.put("cols", formattedCols);
            param.put("classname", toUpperCamelCase(table));
            param.put("package", packageName);
            param.put("isUseJpaAnnotation", isUseJpaAnnotation);

            DataObject created = templateDO.createFromTemplate(folder, toUpperCamelCase(table), param);

            result.add(created);

        }

        return result;
    }

    public String getPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        String packageName = "";
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

            for (SourceGroup group : groups) {
                FileObject root = group.getRootFolder();
                if (FileUtil.isParentOf(root, targetFolder)) {
                    String relativePath = FileUtil.getRelativePath(root, targetFolder);
                    if (relativePath != null) {
                        packageName = relativePath.replace('/', '.');
                    }
                    break;
                }
            }
        }
        return packageName;
    }

    /**
     * スネークケースをアッパーキャメルケースに変換 例: USER_INFO -> UserInfo, TABLE -> Table
     */
    private static String toUpperCamelCase(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = true;

        for (char c : s.toLowerCase().toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    /**
     */
    private static String toLowerCamelCase(String s) {
        String upper = toUpperCamelCase(s);
        if (upper.isEmpty()) {
            return upper;
        }
        return Character.toLowerCase(upper.charAt(0)) + upper.substring(1);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = (String[]) wizard.getProperty("WizardPanel_contentData");
        assert beforeSteps != null : "This wizard may only be used embedded in the template wizard";
        String[] res = new String[(beforeSteps.length - 1) + panels.size()];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels.get(i - beforeSteps.length + 1).getComponent().getName();
            }
        }
        return res;
    }

}
