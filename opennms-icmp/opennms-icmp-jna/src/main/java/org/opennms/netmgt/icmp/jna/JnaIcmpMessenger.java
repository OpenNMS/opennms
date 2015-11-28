/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.icmp.jna;

import java.net.InetAddress;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.IcmpMessengerIOException;
import org.opennms.protocols.rt.Messenger;
import org.opennms.protocols.rt.ReplyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author brozow
 */
public class JnaIcmpMessenger implements Messenger<JnaPingRequest, JnaPingReply>, PingReplyListener {
	
	
	private static final Logger LOG = LoggerFactory
			.getLogger(JnaIcmpMessenger.class);
	
	private V4Pinger m_v4;
	private V6Pinger m_v6;
    private ReplyHandler<JnaPingReply> m_callback = null;

	public JnaIcmpMessenger(final int pingerId) throws Exception {
	    Throwable error = null;
	    try {
	        m_v4 = new V4Pinger(pingerId);
	        m_v4.addPingReplyListener(this);
	    } catch (final Throwable t) {
	        LOG.debug("Unable to initialize IPv4 Pinger.", t);
	        error = t;
	        m_v4 = null;
	    }
	    
	    try {
	        m_v6 = new V6Pinger(pingerId);
	        m_v6.addPingReplyListener(this);
	    } catch (final Throwable t) {
	        LOG.debug("Unable to initialize IPv6 Pinger.", t);
	        if (error == null) error = t;
	        m_v6 = null;
	    }
	    
	    if (m_v4 == null && m_v6 == null) {
	        final IcmpMessengerIOException exception = new IcmpMessengerIOException("IPv4 and IPv6 are not available.", error);
	        LOG.warn("Unable to initialize JNA ICMP messenger", exception);
	        throw exception;
	    }
	}
	
    public boolean isV4Available() {
        if (m_v4 != null) {
            return true;
        }
        return false;
    }

    public boolean isV6Available() {
        if (m_v6 != null) {
            return true;
        }
        return false;
    }

        @Override
	public void sendRequest(final JnaPingRequest request) {
		request.send(m_v4, m_v6);
	}

        @Override
	public void start(ReplyHandler<JnaPingReply> callback) {
        m_callback = callback;
        m_v4.start();
        m_v6.start();
	}

        @Override
	public void onPingReply(final InetAddress address, final EchoPacket packet) {
        m_callback.handleReply(new JnaPingReply(address, packet));
	}

}
