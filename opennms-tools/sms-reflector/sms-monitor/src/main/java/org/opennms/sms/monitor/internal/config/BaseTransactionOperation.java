package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder={"sessionVariables", "operations"})
public abstract class BaseTransactionOperation extends BaseOperation implements TransactionOperation {
	private List<SequenceSessionVariable> m_sessionVariables;
	private List<Operation> m_sequenceTransactions;

	public void addSessionVariable(SequenceSessionVariable var) {
		if (m_sessionVariables == null) {
			m_sessionVariables = Collections.synchronizedList(new ArrayList<SequenceSessionVariable>());
		}
		m_sessionVariables.add(var);
	}

	@XmlElement(name="session-variable")
	public List<SequenceSessionVariable> getSessionVariables() {
		return m_sessionVariables;
	}

	public void setSessionVariables(List<SequenceSessionVariable> sessionVariables) {
		m_sessionVariables = sessionVariables;
	}

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
			.append("session-variables", getSessionVariables())
			.append("operations", getOperations())
			.toString();
	}

	public abstract Task createTask(DefaultTaskCoordinator coordinator, ContainerTask parent);
}
