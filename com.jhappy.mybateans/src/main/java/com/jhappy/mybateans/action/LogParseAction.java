/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/actionListener.java to edit this template
 */
package com.jhappy.mybateans.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.JTextComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Utilities;

@ActionID(
        category = "Window",
        id = "action.LogParseAction"
)
@ActionRegistration(
        displayName = "#CTL_LogParseAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 100)

})
public final class LogParseAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        JTextComponent pane = Utilities.actionsGlobalContext().lookup(JTextComponent.class);

        if (pane == null) {
            java.awt.Component focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner instanceof JTextComponent) {
                pane = (JTextComponent) focusOwner;
            }
        }

        if (pane != null) {
            String selectedText = pane.getSelectedText();
            if (selectedText != null && !selectedText.isEmpty()) {
                showPrettySql(MybatisLogFormatter.format(selectedText));
            }
        } else {

            NotifyDescriptor nd = new NotifyDescriptor.Message("テキストコンポーネントが見つかりません");
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private void showPrettySql(String rawLog) {

        NotifyDescriptor nd = new NotifyDescriptor.Message(rawLog);
        DialogDisplayer.getDefault().notify(nd);
    }

}
