package org.opennms.sms.monitor.internal.config;

import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.PropertiesUtils;
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
	public MobileMsgResponseMatcher getMatcher(Properties session) {
		USSDSessionStatus status;
		String text = PropertiesUtils.substitute(getText(), session);
		try {
			int statusVal = Integer.parseInt(text);
			status = USSDSessionStatus.getByNumeric(statusVal);
		} catch (NumberFormatException e) {
			status = USSDSessionStatus.valueOf(text);
		}
		return MobileMsgResponseMatchers.ussdStatusIs(status);
	}

}
