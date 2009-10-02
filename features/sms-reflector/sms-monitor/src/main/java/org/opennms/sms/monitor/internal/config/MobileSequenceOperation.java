package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public abstract class MobileSequenceOperation {
	public MobileSequenceOperation() {
	}

	public MobileSequenceOperation(String label) {
		setLabel(label);
	}
	
	public MobileSequenceOperation(String gatewayId, String label) {
		setGatewayId(gatewayId);
		setLabel(label);
	}

	private String m_gatewayId;
	private String m_label;
	
	@XmlAttribute(name="gatewayId")
	public String getGatewayId() {
		return m_gatewayId;
	}
	
	public void setGatewayId(String gatewayId) {
		m_gatewayId = gatewayId;
	}
	
	@XmlAttribute(name="label")
	public String getLabel() {
		return m_label;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	public Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("gatewayId", getGatewayId())
			.append("label", getLabel())
			.toString();
	}
}
