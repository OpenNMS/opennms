package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;

//@XmlSeeAlso({AsynchronousSequenceTransaction.class, SynchronousSequenceTransaction.class, SequenceOperation.class})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class AbstractSequenceTransaction implements SequenceTransaction {
	private String m_label;
	private List<SequenceSessionVariable> m_sessionVariables = Collections.synchronizedList(new ArrayList<SequenceSessionVariable>());
	private List<SequenceTransaction> m_sequenceTransactions = Collections.synchronizedList(new ArrayList<SequenceTransaction>());

	@XmlTransient
	public String getType() {
		throw new UnsupportedOperationException("getType must be overridden in the concrete class!");
	}

	@XmlAttribute(name="label")
	public String getLabel() {
		return m_label;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	public void addSessionVariable(SequenceSessionVariable var) {
		m_sessionVariables.add(var);
	}

	@XmlElement(name="session-variable")
	public List<SequenceSessionVariable> getSessionVariables() {
		return m_sessionVariables;
	}

	public void setSessionVariables(List<SequenceSessionVariable> sessionVariables) {
		m_sessionVariables = sessionVariables;
	}

	public void addOperation(SequenceTransaction operation) {
		m_sequenceTransactions.add(operation);
	}

	@XmlElementRef
	public List<SequenceTransaction> getOperations() {
		return m_sequenceTransactions;
	}

	public void setOperations(List<SequenceTransaction> operations) {
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
}
