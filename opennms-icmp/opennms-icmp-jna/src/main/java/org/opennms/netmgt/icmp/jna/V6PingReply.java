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
