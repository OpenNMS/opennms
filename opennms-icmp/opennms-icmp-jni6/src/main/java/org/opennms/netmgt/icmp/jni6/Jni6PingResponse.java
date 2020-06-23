/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.jni6;

import java.net.Inet6Address;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.protocols.icmp6.ICMPv6EchoReply;
import org.opennms.protocols.rt.ResponseWithId;

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
public final class Jni6PingResponse implements ResponseWithId<Jni6PingRequestId>, EchoPacket {
    /**
     * The sender's address.
     */
    private final Inet6Address m_address;
    
    /**
     * The received packet.
     */
    private final ICMPv6EchoReply m_packet;

    /**
     * Constructs a new reply with the passed address and packet as the contents
     * of the reply.
     *
     * @param addr
     *            The address of the ICMP sender.
     * @param echoReply
     *            The received packet.
     */
    public Jni6PingResponse(Inet6Address addr, ICMPv6EchoReply echoReply) {
        m_packet = echoReply;
        m_address = addr;
    }

    /**
     * Returns the ICMP packet for the reply.
     *
     * @return a {@link org.opennms.protocols.icmp.ICMPEchoPacket} object.
     */
    private ICMPv6EchoReply getPacket() {
        return m_packet;
    }
    
    /**
     * Returns the internet address of the host that sent the reply.
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public Inet6Address getAddress() {
        return m_address;
    }

    /**
     * <p>getRequestId</p>
     *
     * @return a {@link org.opennms.netmgt.icmp.spi.PingRequestId} object.
     */
    @Override
    public Jni6PingRequestId getRequestId() {
        return new Jni6PingRequestId(this);
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
        return getPacket().getIdentifier();
    }

    @Override
    public int getSequenceNumber() {
        return getPacket().getSequenceNumber();
    }

    @Override
    public long getThreadId() {
        return getPacket().getThreadId();
    }

    @Override
    public long getSentTimeNanos() {
        return getPacket().getSentTime() * 1000000;
    }

    @Override
    public long getReceivedTimeNanos() {
        return getPacket().getReceiveTime() * 1000000;
    }

    @Override
    public double elapsedTime(TimeUnit timeUnit) {
        // {@link org.opennms.protocols.icmp.ICMPEchoPacket.getPingRTT()} returns microseconds.
        double nanosPerUnit = TimeUnit.NANOSECONDS.convert(1, timeUnit);
        return (getPacket().getRoundTripTime() * 1000) / nanosPerUnit;
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
