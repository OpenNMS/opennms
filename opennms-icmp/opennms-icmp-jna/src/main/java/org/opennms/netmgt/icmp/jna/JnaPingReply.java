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
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.EchoPacket;
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
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya </a>
 */
public final class JnaPingReply implements ResponseWithId<JnaPingRequestId>, EchoPacket {
    /**
     * The sender's address.
     */
    private final InetAddress m_address;

    /**
     * The received packet.
     */
    private final EchoPacket m_packet;

    /**
     * Constructs a new reply with the passed address and packet as the contents
     * of the reply.
     *
     * @param addr
     *            The address of the ICMP sender.
     * @param pkt
     *            The received packet.
     */
    public JnaPingReply(InetAddress addr, EchoPacket pkt) {
        m_address = addr;
        m_packet = pkt;
    }
    
    /**
     * <p>getRequestId</p>
     *
     * @return a {@link org.opennms.netmgt.icmp.spi.PingRequestId} object.
     */
    @Override
    public JnaPingRequestId getRequestId() {
        return new JnaPingRequestId(getAddress(), getIdentifier(), getSequenceNumber(), getThreadId());
    }
    
    /**
     * Returns the internet address of the host that sent the reply.
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public final InetAddress getAddress() {
        return m_address;
    }

    /**
     * Returns true if the recovered packet is an echo reply.
     *
     * @return a boolean.
     */
    @Override
    public boolean isEchoReply() {
        return m_packet.isEchoReply();
    }

    @Override
    public int getIdentifier() {
        return m_packet.getIdentifier();
    }

    @Override
    public int getSequenceNumber() {
        return m_packet.getSequenceNumber();
    }

    @Override
    public long getThreadId() {
        return m_packet.getThreadId();
    }

    @Override
    public long getReceivedTimeNanos() {
        return m_packet.getReceivedTimeNanos();
    }

    @Override
    public long getSentTimeNanos() {
        return m_packet.getSentTimeNanos();
    }

    @Override
    public double elapsedTime(TimeUnit timeUnit) {
        return m_packet.elapsedTime(timeUnit);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append('[');
        buf.append("Address = ").append(getAddress());
        buf.append(", ");
        buf.append("JnaPingRequestId = ").append(getRequestId().toString());
        buf.append(']');
        return buf.toString();
    }

}
