package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="session-variable")
public class SequenceSessionVariable {
	@XmlAttribute(name="name")
	private String m_name;
	
	@XmlAttribute(name="class")
	private String m_className;

	@XmlElement(name="parameter")
	private List<SequenceParameter> m_parameters = Collections.synchronizedList(new ArrayList<SequenceParameter>());
	
	public SequenceSessionVariable() {
	}

	public SequenceSessionVariable(String name, String className) {
		setName(name);
		setClassName(className);
	}
	
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name;
	}
	
	public String getClassName() {
		return m_className;
	}
	public void setClassName(String name) {
		m_className = name;
	}

	public void addParameter(String key, String value) {
		m_parameters.add(new SequenceParameter(key, value));
	}
	
	public List<SequenceParameter> getParameters() {
		return m_parameters;
	}
	
	public void setParameters(List<SequenceParameter> parameters) {
		m_parameters = parameters;
	}
}
