package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="operation")
public class SequenceOperation extends AbstractSequenceTransaction {
	@XmlAttribute(name="type", required=true)
	private String m_type;

	@XmlElement(name="value", required=false)
	private String m_value;
	
	@XmlElement(name="match", required=false)
	private SequenceOperationMatch m_match;

	@XmlElement(name="parameters", required=false)
	private SequenceParameterList m_parameters = new SequenceParameterList();

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
		m_parameters.addParameter(parameter);
	}

	public SequenceParameterList getParameters() {
		return m_parameters;
	}
	
	public void setParameters(SequenceParameterList parameters) {
		m_parameters=  parameters;
	}
}
