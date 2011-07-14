package org.opennms.netmgt.provision.support;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * An IoHandler that delegates all processing down to an attribute stored in
 * the individual IoSession. This permits us to have different classes of 
 * handler for each connection without having to create a new Connector each 
 * time.
 * 
 * @author Duncan Mackintosh
 *
 */
public class SessionDelegateIoHandler implements IoHandler {

	public void sessionCreated(IoSession session) throws Exception {
		if (session.getAttribute(IoHandler.class)==null) {
			return;
		}
		((IoHandler)session.getAttribute(IoHandler.class)).sessionCreated(session);			
	}

	public void sessionOpened(IoSession session) throws Exception {
		if (session.getAttribute(IoHandler.class)==null) {
			return;
		}
		((IoHandler)session.getAttribute(IoHandler.class)).sessionOpened(session);
	}

	public void sessionClosed(IoSession session) throws Exception {
		if (session.getAttribute(IoHandler.class)==null) {
			return;
		}
		((IoHandler)session.getAttribute(IoHandler.class)).sessionClosed(session);
	}

	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		if (session.getAttribute(IoHandler.class)==null) {
			return;
		}
		((IoHandler)session.getAttribute(IoHandler.class)).sessionIdle(session, status);
	}

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		if (session.getAttribute(IoHandler.class)==null) {
			return;
		}
		((IoHandler)session.getAttribute(IoHandler.class)).exceptionCaught(session, cause);
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if (session.getAttribute(IoHandler.class)==null) {
			return;
		}
		((IoHandler)session.getAttribute(IoHandler.class)).messageReceived(session, message);
	}

	public void messageSent(IoSession session, Object message)
			throws Exception {
		if (session.getAttribute(IoHandler.class)==null) {
			return;
		}
		((IoHandler)session.getAttribute(IoHandler.class)).messageSent(session, message);
	}

}
