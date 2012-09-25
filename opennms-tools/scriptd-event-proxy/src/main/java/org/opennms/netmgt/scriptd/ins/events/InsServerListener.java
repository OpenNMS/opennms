/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scriptd.ins.events;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.event.Event;




/**
 * The InsServerListener will accept input from a socket, create a InsSession in
 * which communicate with an InsClient for the alarm synchronization.
 * 
 * @see main method for usage example 
 */

public class InsServerListener extends InsServerAbstractListener {
	private ServerSocket m_listener;
	private final Set<InsSession> m_activeSessions = new HashSet<InsSession>();

	/**
	 * listens for incoming connection on defined port (default is 8154)
	 */
	public void run() {
		if(criteriaRestriction == null) {
			throw new IllegalStateException("The property criteriaRestriction cannot be null!");
		}
		LogUtils.infof(this, "InsServerListener started: listening on port %d", listeningPort);
		try {
			m_listener = new ServerSocket(listeningPort);
			Socket server;

			while (true) {
				// when accepts an incoming connection, create an InsSession for
				// alarms exchange
				server = m_listener.accept();
				final InsSession session = new InsSession(server);

				//only if the sharedASCIIString is valorized, requires authentication
				if(getSharedASCIIString() != null) {
					session.setSharedASCIIString(getSharedASCIIString());
				}
				session.setCriteriaRestriction(criteriaRestriction);
				session.start();
				m_activeSessions.add(session);
			}
		} catch (final IOException ioe) {
			LogUtils.infof(this, "Socket closed." );
		}
	}

	@Override
	/**
	 * Stops the m_listener
	 */
	public void interrupt() {
		try {
			m_listener.close();
		} catch (final IOException e) {
		    LogUtils.errorf(this, e, "Error while closing listener.");
		}
		super.interrupt();
		LogUtils.infof(this, "InsServerListener Interrupted!");
	}
	
	private synchronized void cleanActiveSessions() {
		synchronized (m_activeSessions) {
			final Iterator<InsSession> it = m_activeSessions.iterator();
			while(it.hasNext()) {
				final InsSession insSession = it.next();
				if (insSession == null || !insSession.isAlive()) {
					LogUtils.debugf(this, "removing session %s", insSession);
					it.remove();
				}
			}
		}
		LogUtils.debugf(this, "active sessions are: %s", m_activeSessions);
	}

	/**
	 * Flushes the event in input to all active sessions with clients
	 * @param event
	 */
	public void flushEvent(final Event event) {
		LogUtils.debugf(this, "Flushing uei: %s", event.getUei());
		LogUtils.debugf(this, "Flushing ifindex: %s", event.getIfIndex());
		LogUtils.debugf(this, "Flushing ifAlias: %s", event.getIfAlias());
	      
		synchronized (m_activeSessions) {
			cleanActiveSessions();
			final Iterator<InsSession> it = m_activeSessions.iterator();
			while (it.hasNext()) {
				final InsSession insSession = it.next();
				final PrintStream ps = insSession.getStreamToClient();
				synchronized (ps) {
					if(ps!=null) {
						try {
							JaxbUtils.marshal(event, new PrintWriter(ps));
						} catch (final Throwable e) {
							LogUtils.errorf(this, e, "Error while sending current event to client");
						}
					}
				}

			}
		}
	}

	public static void main(final String[] args) {
		final InsServerListener isl = new InsServerListener();
		isl.setListeningPort(8155);
		//optional (if not set, no authentication is required)
		isl.setSharedASCIIString("1234567890");

		isl.setCriteriaRestriction("eventuei is not null");
		//required properties
		
		
		isl.start();

	}

}


