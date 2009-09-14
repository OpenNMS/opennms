package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlValue;

import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;

public abstract class SequenceResponseMatcher {
	private String m_text;

	public SequenceResponseMatcher() {
	}
	
	public SequenceResponseMatcher(String text) {
		setText(text);
	}

	@XmlValue
	public String getText() {
		return m_text;
	}
	
	public void setText(String text) {
		m_text = text;
	}
	
	public abstract MobileMsgResponseMatcher getMatcher();
}
