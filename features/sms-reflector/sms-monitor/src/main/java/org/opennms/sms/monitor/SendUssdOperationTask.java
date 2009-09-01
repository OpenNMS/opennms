package org.opennms.sms.monitor;

import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.sms.monitor.internal.config.SequenceOperation;

public class SendUssdOperationTask extends OperationTask {

	public SendUssdOperationTask(DefaultTaskCoordinator coordinator, ContainerTask parent, SequenceOperation operation) {
		super(coordinator, parent, operation);
	}

	public void run() {
		super.run();
		
		
	}
}
