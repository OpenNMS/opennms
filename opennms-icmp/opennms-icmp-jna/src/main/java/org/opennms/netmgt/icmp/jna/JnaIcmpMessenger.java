/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.jna;

import java.net.InetAddress;
import java.util.Queue;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.IcmpMessengerIOException;
import org.opennms.protocols.rt.Messenger;


/**
 * @author brozow
 */
public class JnaIcmpMessenger implements Messenger<JnaPingRequest, JnaPingReply>, PingReplyListener {

	private V4Pinger m_v4;
	private V6Pinger m_v6;
    private Queue<JnaPingReply> pendingReplies = null;

	public JnaIcmpMessenger(final int pingerId) throws Exception {
	    Throwable error = null;
	    try {
	        m_v4 = new V4Pinger(pingerId);
	        m_v4.addPingReplyListener(this);
	    } catch (final Throwable t) {
	        LogUtils.debugf(this, t, "Unable to initialize IPv4 Pinger.");
	        error = t;
	        m_v4 = null;
	    }
	    
	    try {
	        m_v6 = new V6Pinger(pingerId);
	        m_v6.addPingReplyListener(this);
	    } catch (final Throwable t) {
	        LogUtils.debugf(this, t, "Unable to initialize IPv6 Pinger.");
	        if (error == null) error = t;
	        m_v6 = null;
	    }
	    
	    if (m_v4 == null && m_v6 == null) {
	        final IcmpMessengerIOException exception = new IcmpMessengerIOException("IPv4 and IPv6 are not available.", error);
	        LogUtils.warnf(this, exception, "Unable to initialize JNA ICMP messenger");
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
	public void start(final Queue<JnaPingReply> replyQueue) {
        pendingReplies = replyQueue;
        m_v4.start();
        m_v6.start();
	}

        @Override
	public void onPingReply(final InetAddress address, final EchoPacket packet) {
		pendingReplies.offer(new JnaPingReply(address, packet));
	}

}
