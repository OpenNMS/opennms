package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;

/**
 * <p>TextResponseMatcher class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="matches")
public class TextResponseMatcher extends SequenceResponseMatcher {

	/**
	 * <p>Constructor for TextResponseMatcher.</p>
	 */
	public TextResponseMatcher() {
	}

	/**
	 * <p>Constructor for TextResponseMatcher.</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public TextResponseMatcher(String text) {
		this();
		setText(text);
	}

	/** {@inheritDoc} */
	@Override
    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        LogUtils.tracef(this, "textMatches(%s, %s, %s)", session.substitute(getText()), request, response);
        String responseText = response.getText() == null ? "" : response.getText();
        return session.matches(getText(), responseText);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "textMatches(\"" + getText() + "\")";
    }

}
