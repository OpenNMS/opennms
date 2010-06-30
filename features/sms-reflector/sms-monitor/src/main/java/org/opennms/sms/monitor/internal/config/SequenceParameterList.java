package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>SequenceParameterList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="parameters")
public class SequenceParameterList {
	@XmlElement(name="parameter")
	private List<SequenceParameter> m_parameters = Collections.synchronizedList(new ArrayList<SequenceParameter>());

	/**
	 * <p>addParameter</p>
	 *
	 * @param parameter a {@link org.opennms.sms.monitor.internal.config.SequenceParameter} object.
	 */
	public void addParameter(SequenceParameter parameter) {
		m_parameters.add(parameter);
	}

	/**
	 * <p>getParameters</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<SequenceParameter> getParameters() {
		return m_parameters;
	}
	
	/**
	 * <p>setParameters</p>
	 *
	 * @param parameters a {@link java.util.List} object.
	 */
	public void setParameters(List<SequenceParameter> parameters) {
		m_parameters = parameters;
	}
}
