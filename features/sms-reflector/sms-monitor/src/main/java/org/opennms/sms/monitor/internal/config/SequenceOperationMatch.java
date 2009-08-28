package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="match")
public class SequenceOperationMatch {
	@XmlAttribute(name="type")
	private String m_type = "success";
	
	@XmlValue
	private String m_value;
	
	public SequenceOperationMatch() {
	}
	
	public SequenceOperationMatch(String type) {
		setType(type);
	}
	
	public SequenceOperationMatch(String type, String value) {
		setType(type);
		setValue(value);
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
}
