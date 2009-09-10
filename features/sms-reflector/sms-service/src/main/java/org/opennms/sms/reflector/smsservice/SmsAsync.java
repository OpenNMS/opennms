/**
 * 
 */
package org.opennms.sms.reflector.smsservice;

import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.smslib.OutboundMessage;

public class SmsAsync implements Async<MobileMsgResponse> {
	private MobileMsgTracker m_tracker;
	private final OutboundMessage m_message;
	private final MobileMsgResponseMatcher m_responseMatcher;

	SmsAsync(MobileMsgTracker tracker, String gatewayId, long timeout, int retries, String recipient, String text, MobileMsgResponseMatcher responseMatcher) {
		this.m_tracker = tracker;
		this.m_message = new OutboundMessage(recipient, text);
		this.m_message.setGatewayId(gatewayId);
		this.m_responseMatcher = responseMatcher;
	}

	SmsAsync(MobileMsgTracker tracker, OutboundMessage msg, MobileMsgResponseMatcher responseMatcher) {
		this.m_tracker = tracker;
		this.m_message = msg;
		this.m_responseMatcher = responseMatcher;
	}

	public void submit(final Callback<MobileMsgResponse> cb) {
		MobileMsgResponseCallback mmrc = new MobileMsgCallbackAdapter(cb);

		try {
			m_tracker.sendSmsRequest(m_message, 3000L, 0, mmrc, m_responseMatcher);
		} catch (Exception e) {
			cb.handleException(e);
		}
		
	}
}