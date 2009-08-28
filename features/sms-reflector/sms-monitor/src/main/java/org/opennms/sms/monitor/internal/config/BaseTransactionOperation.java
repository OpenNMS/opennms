package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;

@XmlAccessorType(XmlAccessType.PROPERTY)
public abstract class BaseTransactionOperation extends BaseOperation implements TransactionOperation {
	private List<Operation> m_sequenceTransactions;

	public void addOperation(Operation operation) {
		if (m_sequenceTransactions == null) {
			m_sequenceTransactions = Collections.synchronizedList(new ArrayList<Operation>());
		}
		m_sequenceTransactions.add(operation);
	}

	@XmlElementRef
	public List<Operation> getOperations() {
		return m_sequenceTransactions;
	}

	public void setOperations(List<Operation> operations) {
		m_sequenceTransactions = operations;
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("type", getType())
			.append("label", getLabel())
			.append("operations", getOperations())
			.toString();
	}

	public abstract Task createTask(DefaultTaskCoordinator coordinator, ContainerTask parent);
}
