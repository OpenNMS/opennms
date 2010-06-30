package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>SequenceOperationMatch class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="match")
public class SequenceOperationMatch {
	@XmlAttribute(name="type")
	private String m_type = "success";
	
	@XmlValue
	private String m_value;
	
	/**
	 * <p>Constructor for SequenceOperationMatch.</p>
	 */
	public SequenceOperationMatch() {
	}
	
	/**
	 * <p>Constructor for SequenceOperationMatch.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public SequenceOperationMatch(String type) {
		setType(type);
	}
	
	/**
	 * <p>Constructor for SequenceOperationMatch.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public SequenceOperationMatch(String type, String value) {
		setType(type);
		setValue(value);
	}

	/**
	 * <p>getType</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getType() {
		return m_type;
	}
	
	/**
	 * <p>setType</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		m_type = type;
	}

	/**
	 * <p>getValue</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue() {
		return m_value;
	}
	
	/**
	 * <p>setValue</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 */
	public void setValue(String value) {
		m_value = value;
	}
}
