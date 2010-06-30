package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>Abstract MobileSequenceOperation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class MobileSequenceOperation {
	/**
	 * <p>Constructor for MobileSequenceOperation.</p>
	 */
	public MobileSequenceOperation() {
	}

	/**
	 * <p>Constructor for MobileSequenceOperation.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceOperation(String label) {
		setLabel(label);
	}
	
	/**
	 * <p>Constructor for MobileSequenceOperation.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceOperation(String gatewayId, String label) {
		setGatewayId(gatewayId);
		setLabel(label);
	}

	private String m_gatewayId;
	private String m_label;
	
	/**
	 * <p>getGatewayId</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="gatewayId")
	public String getGatewayId() {
		return m_gatewayId;
	}
	
	/**
	 * <p>setGatewayId</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 */
	public void setGatewayId(String gatewayId) {
		m_gatewayId = gatewayId;
	}
	
	/**
	 * <p>getLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="label")
	public String getLabel() {
		return m_label;
	}

	/**
	 * <p>setLabel</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public void setLabel(String label) {
		m_label = label;
	}

	/**
	 * <p>log</p>
	 *
	 * @return a {@link org.opennms.core.utils.ThreadCategory} object.
	 */
	public ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("gatewayId", getGatewayId())
			.append("label", getLabel())
			.toString();
	}
}
