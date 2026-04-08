package com.jhappy.mybateans.wizard.mapperxml;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class MyBatisMapperXmlWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor> {

    private MyBatisMapperXmlVisualPanel1 component;

    @Override
    public MyBatisMapperXmlVisualPanel1 getComponent() {
        if (component == null) {
            component = new MyBatisMapperXmlVisualPanel1();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {

        return HelpCtx.DEFAULT_HELP;

    }

    @Override
    public boolean isValid() {
        MyBatisMapperXmlVisualPanel1 c = getComponent();
        return !c.getTableName().isEmpty();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        

    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        MyBatisMapperXmlVisualPanel1 c = getComponent();

        wiz.putProperty("tableName", c.getTableName());
        wiz.putProperty("namespace", c.getNamespace());

    }

}
