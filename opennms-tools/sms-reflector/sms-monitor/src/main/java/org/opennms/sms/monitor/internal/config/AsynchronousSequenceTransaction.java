package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="transaction")
public class AsynchronousSequenceTransaction extends BaseTransactionOperation {
	public AsynchronousSequenceTransaction() {
		super();
		super.setType("asynchronous");
	}

	public AsynchronousSequenceTransaction(String label) {
		super();
		super.setType("asynchronous");
		setLabel(label);
	}

	public Task createTask(DefaultTaskCoordinator coordinator, ContainerTask parent) {
		BatchTask task = coordinator.createBatch(parent);
		parent.add(task);
		for (Operation op : getOperations()) {
			op.createTask(coordinator, task);
		}
		return task;
	}
}
