package org.opennms.sms.monitor.internal.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="operation")
public class SequenceOperation extends BaseTransactionOperation {
	@XmlAttribute(name="type", required=true)
	private String m_type;

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

	public String getType() {
		return m_type;
	}
	
	public void setType(String type) {
		m_type = type;
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
}
