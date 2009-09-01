/**
 * 
 */
package org.opennms.sms.monitor;

import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.sms.monitor.internal.config.SequenceOperation;

public class NullOperationTask extends OperationTask {
	private SequenceOperation m_operation;

	public NullOperationTask(DefaultTaskCoordinator coordinator, ContainerTask parent, SequenceOperation operation) {
		super(coordinator, parent, null);
		m_operation = operation;
	}

	public void run() {
		log().warn("no concrete task defined for SequenceOperation: " + m_operation);
	}
}