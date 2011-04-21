/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
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

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.opennms.jicmp.ipv6.ICMPv6EchoPacket;
import org.opennms.jicmp.ipv6.ICMPv6Packet;

class V6PingReply extends ICMPv6EchoPacket implements org.opennms.netmgt.icmp.EchoPacket {
    
    // The below long is equivalent to the next line and is more efficient than
    // manipulation as a string
    // Charset.forName("US-ASCII").encode("OpenNMS!").getLong(0);
    public static final long COOKIE = 0x4F70656E4E4D5321L;
    
    private long m_receivedTimeNanos;

    public V6PingReply(ICMPv6Packet icmpPacket, long receivedTimeNanos) {
        super(icmpPacket);
        m_receivedTimeNanos = receivedTimeNanos;
    }

    public boolean isValid() {
        ByteBuffer content = getContentBuffer();
        /* we ensure the length can contain 2 longs (cookie and sent time)
           and that the cookie matches */
        return content.limit() >= 16 && COOKIE == content.getLong(0);
    }

    @Override
    public boolean isEchoReply() {
    	return Type.EchoReply.equals(getType());
    }

    @Override
    public long getSentTimeNanos() {
        return getContentBuffer().getLong(8);
    }
    
    @Override
    public long getReceivedTimeNanos() {
        return m_receivedTimeNanos;
    }
    
    @Override
    public float elapsedTime(TimeUnit unit) {
        return unit.convert(elapsedTimeNanos(), TimeUnit.NANOSECONDS);
    }

    long elapsedTimeNanos() {
        return getReceivedTimeNanos() - getSentTimeNanos();
    }

    @Override
    public long getIdentity() {
    	return getIdentifier();
    }
}
