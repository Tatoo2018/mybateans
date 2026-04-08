package com.jhappy.mybateans.wizard.configxml;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class MyBatisConfigXmlWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private MyBatisConfigXmlVisualPanel component;

    @Override
    public MyBatisConfigXmlVisualPanel getComponent() {
        if (component == null) {
            component = new MyBatisConfigXmlVisualPanel();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {

        return HelpCtx.DEFAULT_HELP;

    }

    @Override
    public boolean isValid() {
        MyBatisConfigXmlVisualPanel c = getComponent();
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

    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        MyBatisConfigXmlVisualPanel c = getComponent();

        wiz.putProperty("dataSourceType", c.getDataSourceType());
        wiz.putProperty("transactionManagerType", c.getTManagerType());

    }

}
