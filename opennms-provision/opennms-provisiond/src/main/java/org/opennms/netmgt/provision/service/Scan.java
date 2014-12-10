package org.opennms.netmgt.provision.service;

import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.tasks.Task;

public interface Scan extends RunInBatch {
    public Task createTask();
}
