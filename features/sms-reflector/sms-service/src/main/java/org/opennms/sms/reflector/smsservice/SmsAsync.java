/**
 * 
 */
package org.opennms.sms.reflector.smsservice;

import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.smslib.OutboundMessage;

public class SmsAsync implements Async<MobileMsgResponse> {
	private final MobileMsgTracker m_tracker;
	private final MobileMsgSequence m_sequence;
	private final OutboundMessage m_message;
	private final MobileMsgResponseMatcher m_responseMatcher;
	private long m_timeout;
	private int m_retries;

	public SmsAsync(MobileMsgTracker tracker, MobileMsgSequence sequence, String gatewayId, long timeout, int retries, String recipient, String text, MobileMsgResponseMatcher responseMatcher) {
		this.m_tracker = tracker;
		this.m_sequence = sequence;
		this.m_message = new OutboundMessage(recipient, text);
		this.m_message.setGatewayId(gatewayId);
		this.m_responseMatcher = responseMatcher;
		this.m_timeout = timeout;
		this.m_retries = retries;
	}

	public void submit(final Callback<MobileMsgResponse> cb) {
		if (m_sequence.hasFailed()) {
			cb.complete(null);
		}
		
		MobileMsgResponseCallback mmrc = new MobileMsgCallbackAdapter(cb);

		try {
			m_tracker.sendSmsRequest(m_message, m_timeout, m_retries, mmrc, m_responseMatcher);
		} catch (Exception e) {
			cb.handleException(e);
		}
		
	}
}