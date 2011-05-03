//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Mar 05: Cleaned up some ICMP related code.
// 2002 Nov 13: Added response time stats for ICMP requests.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// Reply.java,v 1.1.1.1 2001/11/11 17:34:37 ben Exp

package org.opennms.netmgt.icmp.jni;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.protocols.icmp.ICMPEchoPacket;
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
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append('[');
        buf.append("Address = ").append(getAddress());
        buf.append(", ");
        buf.append("JniPingRequestId = ").append(getRequestId().toString());
        buf.append(']');
        return buf.toString();
    }

}
