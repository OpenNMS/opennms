/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
