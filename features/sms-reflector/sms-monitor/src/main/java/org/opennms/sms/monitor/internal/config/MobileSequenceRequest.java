package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="request")
public abstract class MobileSequenceRequest extends MobileSequenceOperation {
	private String m_text;

	public MobileSequenceRequest() {
		super();
	}
	
	public MobileSequenceRequest(String label, String text) {
		super(label);
		setText(text);
	}

	public MobileSequenceRequest(String gatewayId, String label, String text) {
		super(gatewayId, label);
		setText(text);
	}

	@XmlAttribute(name="text")
	public String getText() {
		return m_text;
	}
	
	public void setText(String text) {
		m_text = text;
	}
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("gatewayId", getGatewayId())
			.append("label", getLabel())
			.append("text", getText())
			.toString();
	}
}
