package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers;

@XmlRootElement(name="from-recipient")
public class SmsFromRecipientResponseMatcher extends SequenceResponseMatcher {

	@Override
	public MobileMsgResponseMatcher getMatcher() {
		return MobileMsgResponseMatchers.smsFromRecipient();
	}

}
