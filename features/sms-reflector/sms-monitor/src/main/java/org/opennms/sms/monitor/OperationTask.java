package org.opennms.sms.monitor;

import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.SyncTask;
import org.opennms.sms.monitor.internal.config.SequenceOperation;

public class OperationTask extends SyncTask {
	private SequenceOperation m_operation;

	public OperationTask(DefaultTaskCoordinator coordinator, ContainerTask parent, SequenceOperation operation) {
		super(coordinator, parent, null);
		m_operation = operation;
	}

	protected SequenceOperation getSequenceOperation() {
		return m_operation;
	}

	public void run() {
		log().warn("no concrete task defined for SequenceOperation: " + m_operation);
	}
}
