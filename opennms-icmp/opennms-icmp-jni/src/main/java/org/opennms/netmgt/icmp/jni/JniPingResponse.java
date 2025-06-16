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
package org.opennms.netmgt.icmp.jni;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.core.tracker.ResponseWithId;

/**
 * <p>
 * This class is use to encapsulate an ICMP reply that conforms to the
 * {@link EchoPacket packet}class. The reply must be of type ICMP Echo Reply and be
 * the correct length.
 * </p>
 *
 * <p>
 * When constructed by the <code>create</code> method the returned reply
 * encapsulates the sender's address and the received packet as final,
 * non-mutable values for the instance.
 * </p>
 *
 */
public final class JniPingResponse implements ResponseWithId<JniPingRequestId>, EchoPacket {
    /**
     * The sender's address.
     */
    private final InetAddress m_address;
    
    /**
     * The received packet.
     */
    private final ICMPEchoPacket m_packet;

    /**
     * Constructs a new reply with the passed address and packet as the contents
     * of the reply.
     *
     * @param addr
     *            The address of the ICMP sender.
     * @param pkt
     *            The received packet.
     */
    public JniPingResponse(InetAddress addr, ICMPEchoPacket pkt) {
        m_packet = pkt;
        m_address = addr;
    }

    /**
     * Returns the ICMP packet for the reply.
     *
     * @return a {@link org.opennms.protocols.icmp.ICMPEchoPacket} object.
     */
    private ICMPEchoPacket getPacket() {
        return m_packet;
    }
    
    /**
     * Returns the internet address of the host that sent the reply.
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * <p>getRequestId</p>
     *
     * @return a {@link org.opennms.netmgt.icmp.spi.PingRequestId} object.
     */
    @Override
    public JniPingRequestId getRequestId() {
        return new JniPingRequestId(this);
    }

    /**
     * Returns true if the recovered packet is an echo reply.
     *
     * @return a boolean.
     */
    @Override
    public boolean isEchoReply() {
        return getPacket().isEchoReply();
    }


    @Override
    public int getIdentifier() {
        return getPacket().getIdentity();
    }

    @Override
    public int getSequenceNumber() {
        return getPacket().getSequenceId();
    }

    @Override
    public long getThreadId() {
        return getPacket().getTID();
    }

    @Override
    public long getSentTimeNanos() {
        return getPacket().getSentTime();
    }

    @Override
    public long getReceivedTimeNanos() {
        return getPacket().getReceivedTime();
    }

    @Override
    public double elapsedTime(TimeUnit timeUnit) {
        // {@link org.opennms.protocols.icmp.ICMPEchoPacket.getPingRTT()} returns microseconds.
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, timeUnit);
        return (getPacket().getPingRTT() * 1000) / nanosPerUnit;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append('[');
        buf.append("Address = ").append(getAddress());
        buf.append(", ");
        buf.append("JniPingRequestId = ").append(getRequestId().toString());
        buf.append(']');
        return buf.toString();
    }

}
