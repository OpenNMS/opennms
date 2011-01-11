
/**
 * <p>MobileMsgCallbackAdapter class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.sms.reflector.smsservice;

import java.net.SocketTimeoutException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.Callback;
public class MobileMsgCallbackAdapter implements MobileMsgResponseCallback {
	private final Callback<MobileMsgResponse> cb;

	/**
	 * <p>Constructor for MobileMsgCallbackAdapter.</p>
	 *
	 * @param cb a {@link org.opennms.core.tasks.Callback} object.
	 */
	public MobileMsgCallbackAdapter(Callback<MobileMsgResponse> cb) {
		this.cb = cb;
	}
	
	/**
	 * <p>Getter for the field <code>cb</code>.</p>
	 *
	 * @return a {@link org.opennms.core.tasks.Callback} object.
	 */
	public Callback<MobileMsgResponse> getCb() {
	    return cb;
	}

	/** {@inheritDoc} */
	public void handleError(final MobileMsgRequest request, final Throwable t) {
		getCb().handleException(t);
	}

	/** {@inheritDoc} */
	public boolean handleResponse(final MobileMsgRequest request, final MobileMsgResponse packet) {
		getCb().complete(packet);
		return true;
	}

	/** {@inheritDoc} */
	public void handleTimeout(final MobileMsgRequest request) {
		getCb().handleException(new SocketTimeoutException("timed out processing request " + request));
	}
	
	public String toString() {
	    return new ToStringBuilder(this)
	        .append("callback", cb)
	        .toString();
	}
}
