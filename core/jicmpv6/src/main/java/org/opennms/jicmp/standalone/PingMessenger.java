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
package org.opennms.jicmp.standalone;

import java.net.InetAddress;
import java.util.Queue;

import org.opennms.jicmp.jna.NativeDatagramSocket;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.spi.PingReply;
import org.opennms.netmgt.icmp.spi.PingRequest;
import org.opennms.protocols.rt.Messenger;


/**
 * @author brozow
 */
public class PingMessenger implements Messenger<PingRequest<NativeDatagramSocket>, PingReply>, PingReplyListener {

	private Queue<PingReply> pendingReplies = null;

	public PingMessenger(V4Pinger v4, V6Pinger v6) {
		v4.addPingReplyListener(this);
		v6.addPingReplyListener(this);
	}

	@Override
	public void sendRequest(PingRequest<NativeDatagramSocket> request) {
		// Don't need to send a socket here, the sockets are managed by the V4Pinger and V6Pinger classes
		request.send(null, request.getId().getAddress());
	}

	@Override
	public void start(Queue<PingReply> replyQueue) {
		pendingReplies = replyQueue;
	}

	public void onPingReply(InetAddress address, EchoPacket packet) {
		pendingReplies.offer(new PingReply(address, packet));
	}
}
