/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.opennms.jicmp.ipv6.ICMPv6EchoPacket;
import org.opennms.jicmp.ipv6.ICMPv6Packet;
import org.opennms.netmgt.icmp.EchoPacket;

class V6PingReply extends ICMPv6EchoPacket implements EchoPacket {
    
    private long m_receivedTimeNanos;

    public V6PingReply(ICMPv6Packet icmpPacket, long receivedTimeNanos) {
        super(icmpPacket);
        m_receivedTimeNanos = receivedTimeNanos;
    }

    public boolean isValid() {
        ByteBuffer content = getContentBuffer();
        return content.limit() >= V6PingRequest.DATA_LENGTH && V6PingRequest.COOKIE == content.getLong(V6PingRequest.OFFSET_COOKIE);
    }
    
    @Override
    public boolean isEchoReply() {
        return Type.EchoReply.equals(getType());
    }

    @Override
    public int getIdentifier() {
        // this is here just for EchoPacket interface completeness
        return super.getIdentifier();
    }
    
    @Override
    public int getSequenceNumber() {
        // this is here just for EchoPacket interface completeness
        return super.getSequenceNumber();
    }

    @Override
    public long getThreadId() {
        return getContentBuffer().getLong(V6PingRequest.OFFSET_THREAD_ID);
    }

    @Override
    public long getSentTimeNanos() {
        return getContentBuffer().getLong(V6PingRequest.OFFSET_TIMESTAMP);
    }
    
    @Override
    public long getReceivedTimeNanos() {
        return m_receivedTimeNanos;
    }
    
    @Override
    public double elapsedTime(TimeUnit unit) {
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, unit);
        return elapsedTimeNanos() / nanosPerUnit;
    }

    protected long elapsedTimeNanos() {
        return getReceivedTimeNanos() - getSentTimeNanos();
    }

}
