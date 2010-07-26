package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.UssdResponse;
import org.smslib.USSDSessionStatus;

/**
 * <p>UssdSessionStatusMatcher class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="session-status")
public class UssdSessionStatusMatcher extends SequenceResponseMatcher {

	/**
	 * <p>Constructor for UssdSessionStatusMatcher.</p>
	 */
	public UssdSessionStatusMatcher() {
	}

	/**
	 * <p>Constructor for UssdSessionStatusMatcher.</p>
	 *
	 * @param text a {@link java.lang.String} object.
	 */
	public UssdSessionStatusMatcher(String text) {
		setText(text);
	}
	
	/**
	 * <p>Constructor for UssdSessionStatusMatcher.</p>
	 *
	 * @param status a {@link org.smslib.USSDSessionStatus} object.
	 */
	public UssdSessionStatusMatcher(USSDSessionStatus status) {
		setText(status.name());
	}

	/** {@inheritDoc} */
	@Override
    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        LogUtils.tracef(this, "ussdStatusIs(%s, %s)", getText(), request, response);
        return response instanceof UssdResponse && session.ussdStatusMatches(getText(), ((UssdResponse)response).getSessionStatus());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ussdStatusIs(" + getText() + ")";
    }

}
