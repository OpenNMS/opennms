package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers;

@XmlRootElement(name="matches")
public class TextResponseMatcher extends SequenceResponseMatcher {

	private String m_text;

	public TextResponseMatcher() {
	}

	public TextResponseMatcher(String text) {
		this();
		setText(text);
	}

	@XmlElement(name="")
	public String getValue() {
		return m_text;
	}
	
	public void setValue(String value) {
		m_text = value;
	}
	
	@XmlTransient
	public String getText() {
		return m_text;
	}
	
	public void setText(String text) {
		m_text = text;
	}
	
	@Override
	public MobileMsgResponseMatcher getMatcher() {
		return MobileMsgResponseMatchers.textMatches(m_text);
	}

}
