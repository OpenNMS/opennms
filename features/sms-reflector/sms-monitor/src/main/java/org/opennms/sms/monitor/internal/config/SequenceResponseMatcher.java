package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;

/**
 * <p>Abstract SequenceResponseMatcher class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class SequenceResponseMatcher {
	// Forces this to be an XSD complexType instead of simpleType
	@SuppressWarnings("unused")
	@XmlAttribute(name="dummy", required=false)
	private String m_dummy;
	
	private String m_text;

	/**
	 * <p>Constructor for SequenceResponseMatcher.</p>
	 */
	public SequenceResponseMatcher() {
	}
	
	/**
	 * <p>Constructor for SequenceResponseMatcher.</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public SequenceResponseMatcher(String text) {
		setText(text);
	}

	/**
	 * <p>getText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlValue
	public String getText() {
		return m_text;
	}
	
	/**
	 * <p>setText</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public void setText(String text) {
		m_text = text;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("text", getText())
			.toString();
	}

	/**
	 * <p>matches</p>
	 *
	 * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
	 * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
	 * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
	 * @return a boolean.
	 */
	public abstract boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response);

}
