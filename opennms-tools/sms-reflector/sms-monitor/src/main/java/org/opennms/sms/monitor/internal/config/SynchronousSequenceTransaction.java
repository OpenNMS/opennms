package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="transaction")
public class SynchronousSequenceTransaction extends BaseTransactionOperation {
	public SynchronousSequenceTransaction() {
		super();
		super.setType("synchronous");
	}

	public SynchronousSequenceTransaction(String label) {
		super();
		super.setType("synchronous");
		setLabel(label);
	}

	public Task createTask(DefaultTaskCoordinator coordinator) {
		throw new UnsupportedOperationException("must implement createTask");
	}
}
