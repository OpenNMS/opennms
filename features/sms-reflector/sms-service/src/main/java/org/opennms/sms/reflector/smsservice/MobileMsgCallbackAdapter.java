/**
 * 
 */
package org.opennms.sms.reflector.smsservice;

import java.net.SocketTimeoutException;

import org.opennms.core.tasks.Callback;

public class MobileMsgCallbackAdapter implements MobileMsgResponseCallback {
	private final Callback<MobileMsgResponse> cb;

	public MobileMsgCallbackAdapter(Callback<MobileMsgResponse> cb) {
		this.cb = cb;
	}
	
	public Callback<MobileMsgResponse> getCb() {
	    return cb;
	}

	public void handleError(MobileMsgRequest request, Throwable t) {
		getCb().handleException(t);
	}

	public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse packet) {
		getCb().complete(packet);
		return true;
	}

	public void handleTimeout(MobileMsgRequest request) {
		getCb().handleException(new SocketTimeoutException("timed out processing request " + request));
	}
}