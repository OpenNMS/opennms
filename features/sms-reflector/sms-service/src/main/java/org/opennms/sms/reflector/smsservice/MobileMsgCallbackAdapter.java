/**
 * 
 */
package org.opennms.sms.reflector.smsservice;

import java.net.SocketTimeoutException;

import org.opennms.core.tasks.Callback;

public class MobileMsgCallbackAdapter implements MobileMsgResponseCallback {
	private final Callback<MobileMsgResponse> cb;

	MobileMsgCallbackAdapter(Callback<MobileMsgResponse> cb) {
		this.cb = cb;
	}

	public void handleError(MobileMsgRequest request, Throwable t) {
		cb.handleException(t);
	}

	public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse packet) {
		cb.complete(packet);
		return true;
	}

	public void handleTimeout(MobileMsgRequest request) {
		cb.handleException(new SocketTimeoutException("timed out processing request " + request));
	}
}