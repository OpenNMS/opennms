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

package org.opennms.jicmp.standalone;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.ip.ICMPPacket;

class V4PingReply extends ICMPEchoPacket implements PingReply {
    
    // The below long is equivalent to the next line and is more efficient than
    // manipulation as a string
    // StandardCharsets.US_ASCII.encode("OpenNMS!").getLong(0);
    public static final long COOKIE = 0x4F70656E4E4D5321L; 
    
    private long m_receivedTimeNanos;

    public V4PingReply(ICMPPacket icmpPacket, long receivedTimeNanos) {
        super(icmpPacket);
        m_receivedTimeNanos = receivedTimeNanos;
    }
    
    public boolean isValid() {
        ByteBuffer content = getContentBuffer();
        /* we ensure the length can contain 2 longs (cookie and sent time)
           and that the cookie matches */
        return content.limit() >= 16 && COOKIE == content.getLong(0);
    }

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
    public double elapsedTime(TimeUnit unit) {
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, unit);
        return getElapsedTimeNanos() / nanosPerUnit;
    }

    @Override
    public long getElapsedTimeNanos() {
        return getReceivedTimeNanos() - getSentTimeNanos();
    }

    @Override
    public long getThreadId() {
    	return getIdentifier();
    }
}
