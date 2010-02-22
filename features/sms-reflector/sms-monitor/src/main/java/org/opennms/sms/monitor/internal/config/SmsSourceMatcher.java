package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.SmsResponse;

@XmlRootElement(name="validate-source")
public class SmsSourceMatcher extends SequenceResponseMatcher {

	public SmsSourceMatcher() {
		super();
	}
	
	public SmsSourceMatcher(String originator) {
		super(originator);
	}

	@Override
    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        LogUtils.tracef(this, "smsFrom.matches(%s, %s, %s)", session.substitute(getText()), request, response);
        return response instanceof SmsResponse && session.eqOrMatches(getText(), ((SmsResponse)response).getOriginator());
    }

    @Override
    public String toString() {
        return "smsSourceMatches(" + getText() +")";
    }

}
