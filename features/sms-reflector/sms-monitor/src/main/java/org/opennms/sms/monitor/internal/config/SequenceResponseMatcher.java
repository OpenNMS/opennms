package org.opennms.sms.monitor.internal.config;

import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;

public abstract class SequenceResponseMatcher {
	public abstract MobileMsgResponseMatcher getMatcher();
}
