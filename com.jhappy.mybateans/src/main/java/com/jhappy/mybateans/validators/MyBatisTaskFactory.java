package com.jhappy.mybateans.validators;

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.TaskFactory;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.spi.SchedulerTask;

@MimeRegistration(mimeType = "", service = TaskFactory.class)
public class MyBatisTaskFactory extends TaskFactory {

    @Override
    public Collection<? extends SchedulerTask> create(Snapshot snapshot) {

        String mime = snapshot.getMimeType();

        if (mime != null && mime.endsWith("xml")) {
            return Collections.singleton(new MyBatisValidationTask());
        }
        return Collections.emptyList();
    }

}
