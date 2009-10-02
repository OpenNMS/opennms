package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="SequenceSessionVariableType", propOrder={"m_name", "m_className", "m_parameters"})
@XmlRootElement(name="session-variable")
public class SequenceSessionVariable {
	@XmlAttribute(name="name")
	private String m_name;

	@XmlAttribute(name="class")
	private String m_className;

	@XmlElementWrapper(name="parameters", required=false)
	@XmlElement(name="parameter")
	private List<SequenceParameter> m_parameters;
	
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
		if (m_parameters == null) {
			m_parameters = Collections.synchronizedList(new ArrayList<SequenceParameter>());
		}
		m_parameters.add(new SequenceParameter(key, value));
	}
	
	public List<SequenceParameter> getParameters() {
		return m_parameters;
	}

	public Map<String,String> getParametersAsMap() {
		Map<String,String> m = new HashMap<String,String>();
		if (m_parameters != null) {
			for (SequenceParameter p : m_parameters) {
				m.put(p.getKey(), p.getValue());
			}
		}
		return m;
	}
	
	public void setParameters(List<SequenceParameter> parameters) {
		m_parameters = parameters;
	}
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("name", getName())
			.append("class", getClassName())
			.append("parameters", getParameters())
			.toString();
	}
}
