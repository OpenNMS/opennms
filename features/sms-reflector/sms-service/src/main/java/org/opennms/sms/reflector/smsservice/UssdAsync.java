/**
 * 
 */
package org.opennms.sms.reflector.smsservice;

import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.smslib.USSDRequest;

final class UssdAsync implements Async<MobileMsgResponse> {
	private final MobileMsgTracker m_tracker;
	private final USSDRequest m_request;
	private final String m_gatewayId;
	private final MobileMsgResponseMatcher m_matcher;

	UssdAsync(MobileMsgTracker tracker, String gatewayId, USSDRequest req, MobileMsgResponseMatcher matcher) {
		this.m_tracker = tracker;
		this.m_gatewayId = gatewayId;
		this.m_request = req;
		this.m_matcher = matcher;
	}

	public void submit(final Callback<MobileMsgResponse> cb) {
		MobileMsgResponseCallback mmrc = new MobileMsgCallbackAdapter(cb);

		try {
			m_tracker.sendUssdRequest(m_gatewayId, m_request, 3000L, 0, mmrc, m_matcher);
		} catch (Exception e) {
			cb.handleException(e);
		}
		
	}
}