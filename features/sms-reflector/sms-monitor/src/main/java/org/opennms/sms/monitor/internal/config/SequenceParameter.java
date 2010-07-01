package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>SequenceParameter class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"m_key", "m_value"})
public class SequenceParameter {
	@XmlAttribute(name="key")
	private String m_key;

	@XmlAttribute(name="value")
	private String m_value;
	
	/**
	 * <p>Constructor for SequenceParameter.</p>
	 */
	public SequenceParameter() {
	}
	
	/**
	 * <p>Constructor for SequenceParameter.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public SequenceParameter(String key, String value) {
		m_key = key;
		m_value = value;
	}
	
	/**
	 * <p>getKey</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getKey() {
		return m_key;
	}
	
	/**
	 * <p>getValue</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue() {
		return m_value;
	}
}
