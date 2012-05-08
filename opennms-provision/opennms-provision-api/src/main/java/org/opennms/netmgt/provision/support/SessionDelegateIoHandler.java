/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
