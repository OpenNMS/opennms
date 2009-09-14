package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers;
import org.smslib.USSDSessionStatus;

@XmlRootElement(name="session-status")
public class UssdSessionStatusMatcher extends SequenceResponseMatcher {

	public UssdSessionStatusMatcher() {
	}

	public UssdSessionStatusMatcher(String text) {
		this();
		setText(text);
	}

	@Override
	public MobileMsgResponseMatcher getMatcher() {
		USSDSessionStatus status;
		try {
			int statusVal = Integer.parseInt(getText());
			status = USSDSessionStatus.getByNumeric(statusVal);
		} catch (NumberFormatException e) {
			status = USSDSessionStatus.valueOf(getText());
		}
		return MobileMsgResponseMatchers.ussdStatusIs(status);
	}

}
