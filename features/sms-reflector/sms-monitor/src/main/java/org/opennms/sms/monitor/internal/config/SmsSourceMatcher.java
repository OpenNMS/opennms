package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.SmsResponse;

/**
 * <p>SmsSourceMatcher class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="validate-source")
public class SmsSourceMatcher extends SequenceResponseMatcher {

	/**
	 * <p>Constructor for SmsSourceMatcher.</p>
	 */
	public SmsSourceMatcher() {
		super();
	}
	
	/**
	 * <p>Constructor for SmsSourceMatcher.</p>
	 *
	 * @param originator a {@link java.lang.String} object.
	 */
	public SmsSourceMatcher(String originator) {
		super(originator);
	}

	/** {@inheritDoc} */
	@Override
    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        LogUtils.tracef(this, "smsFrom.matches(%s, %s, %s)", session.substitute(getText()), request, response);
        return response instanceof SmsResponse && session.eqOrMatches(getText(), ((SmsResponse)response).getOriginator());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "smsSourceMatches(" + getText() +")";
    }

}
