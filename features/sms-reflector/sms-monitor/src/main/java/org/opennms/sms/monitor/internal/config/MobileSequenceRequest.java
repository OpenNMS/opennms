package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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

	@XmlAttribute(name="text")
	public String getText() {
		return m_text;
	}
	
	public void setText(String text) {
		m_text = text;
	}
}
