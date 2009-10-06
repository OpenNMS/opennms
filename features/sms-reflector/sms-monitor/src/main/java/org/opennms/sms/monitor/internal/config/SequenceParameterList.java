package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="parameters")
public class SequenceParameterList {
	@XmlElement(name="parameter")
	private List<SequenceParameter> m_parameters = Collections.synchronizedList(new ArrayList<SequenceParameter>());

	public void addParameter(SequenceParameter parameter) {
		m_parameters.add(parameter);
	}

	public List<SequenceParameter> getParameters() {
		return m_parameters;
	}
	
	public void setParameters(List<SequenceParameter> parameters) {
		m_parameters = parameters;
	}
}
