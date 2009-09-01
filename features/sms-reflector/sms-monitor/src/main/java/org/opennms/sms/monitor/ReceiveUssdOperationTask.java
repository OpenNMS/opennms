package org.opennms.sms.monitor;

import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.sms.monitor.internal.config.SequenceOperation;

public class ReceiveUssdOperationTask extends OperationTask {

	public ReceiveUssdOperationTask(DefaultTaskCoordinator coordinator, ContainerTask parent, SequenceOperation operation) {
		super(coordinator, parent, operation);
	}

	public void run() {
		super.run();
		
		
	}
}
