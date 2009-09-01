package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.Task;
import org.opennms.sms.monitor.NullOperationTask;
import org.opennms.sms.monitor.OperationTask;
import org.opennms.sms.monitor.ReceiveSmsOperationTask;
import org.opennms.sms.monitor.ReceiveUssdOperationTask;
import org.opennms.sms.monitor.SendSmsOperationTask;
import org.opennms.sms.monitor.SendUssdOperationTask;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="operation")
public class SequenceOperation extends BaseOperation {
	@XmlElement(name="value", required=false)
	private String m_value;

	@XmlElement(name="match", required=false)
	private SequenceOperationMatch m_match;

	@XmlElementWrapper(name="parameters", required=false)
	@XmlElement(name="parameter")
	private List<SequenceParameter> m_parameters;

	public SequenceOperation() {
		super();
	}

	public SequenceOperation(String type) {
		super();
		setType(type);
	}

	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}

	public SequenceOperationMatch getMatch() {
		return m_match;
	}

	public void setMatch(SequenceOperationMatch match) {
		m_match = match;
	}

	public void addParameter(SequenceParameter parameter) {
		if (m_parameters == null) {
			m_parameters = Collections.synchronizedList(new ArrayList<SequenceParameter>());
		}
		m_parameters.add(parameter);
	}

	public List<SequenceParameter> getParameters() {
		return m_parameters;
	}
	
	public void setParameters(List<SequenceParameter> parameters) {
		m_parameters = parameters;
	}

	public OperationTask createTask(DefaultTaskCoordinator coordinator, ContainerTask parent) {
		// FIXME: use the new service registry stuff to keep track of various task implementations
		OperationTask task;
		if (getType().equals("send-ussd")) {
			task = new SendUssdOperationTask(coordinator, parent, this);
		} else if (getType().equals("receive-ussd")) {
			task = new ReceiveUssdOperationTask(coordinator, parent, this);
		} else if (getType().equals("send-sms")) {
			task = new SendSmsOperationTask(coordinator, parent, this);
		} else if (getType().equals("receive-sms")) {
			task = new ReceiveSmsOperationTask(coordinator, parent, this);
		} else {
			task = new NullOperationTask(coordinator, parent, this);
		}
		parent.add(task);
		return task;
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("type", getType())
			.append("label", getLabel())
			.append("value", getValue())
			.append("match", getMatch())
			.append("parameters", getParameters())
			.toString();
	}
}
