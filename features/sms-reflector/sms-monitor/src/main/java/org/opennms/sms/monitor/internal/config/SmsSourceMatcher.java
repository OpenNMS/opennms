package org.opennms.sms.monitor.internal.config;

import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers;

@XmlRootElement(name="validate-source")
public class SmsSourceMatcher extends SequenceResponseMatcher {

	public SmsSourceMatcher() {
		super();
	}
	
	public SmsSourceMatcher(String originator) {
		super(originator);
	}

	@Override
	public MobileMsgResponseMatcher getMatcher(Properties session) {
		return MobileMsgResponseMatchers.smsFrom(PropertiesUtils.substitute(getText(), session));
	}

}
