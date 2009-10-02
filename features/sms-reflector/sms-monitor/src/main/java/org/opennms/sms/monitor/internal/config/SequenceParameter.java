package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"m_key", "m_value"})
public class SequenceParameter {
	@XmlAttribute(name="key")
	private String m_key;

	@XmlAttribute(name="value")
	private String m_value;
	
	public SequenceParameter() {
	}
	
	public SequenceParameter(String key, String value) {
		m_key = key;
		m_value = value;
	}
	
	public String getKey() {
		return m_key;
	}
	
	public String getValue() {
		return m_value;
	}
}
