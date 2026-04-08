package com.jhappy.mybateans.wizard.configxml;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class MyBatisConfigXmlWizardPanel2 implements WizardDescriptor.Panel<WizardDescriptor> {

    private MyBatisConfigXmlVisualPanel2 component;

    @Override
    public MyBatisConfigXmlVisualPanel2 getComponent() {
        if (component == null) {
            component = new MyBatisConfigXmlVisualPanel2();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {

        return HelpCtx.DEFAULT_HELP;

    }

    @Override
    public boolean isValid() {
        MyBatisConfigXmlVisualPanel2 c = getComponent();
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {

        MyBatisConfigXmlVisualPanel2 c = getComponent();

        String dataSourceType = (String) wiz.getProperty("dataSourceType");

        if (dataSourceType != null) {
            c.updateLayout(dataSourceType);
        }

    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        MyBatisConfigXmlVisualPanel2 c = getComponent();

        wiz.putProperty("jdbcUrl", c.getJdbcUrl());
        wiz.putProperty("jdbcDriver", c.getJdbcDriver());
        wiz.putProperty("jndiName", c.getJndi());
        wiz.putProperty("user", c.getUser());
        wiz.putProperty("password", c.getPassword());

    }

}
