/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.jicmp;

import java.net.InetAddress;
import java.util.Queue;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.protocols.rt.Messenger;


/**
 * @author brozow
 */
public class JnaIcmpMessenger implements Messenger<JnaPingRequest, JnaPingReply>, PingReplyListener {

	private V4Pinger m_v4;
	private V6Pinger m_v6;
    private Queue<JnaPingReply> pendingReplies = null;

	public JnaIcmpMessenger() throws Exception {

	    m_v4 = new V4Pinger();
		m_v4.addPingReplyListener(this);

		m_v6 = new V6Pinger();
		m_v6.addPingReplyListener(this);
	}
	
	@Override
	public void sendRequest(JnaPingRequest request) {
		request.send(m_v4, m_v6);
	}

	@Override
	public void start(Queue<JnaPingReply> replyQueue) {
        pendingReplies = replyQueue;
        m_v4.start();
        m_v6.start();
	}

	public void onPingReply(InetAddress address, EchoPacket packet) {
		pendingReplies.offer(new JnaPingReply(address, packet));
	}
}
