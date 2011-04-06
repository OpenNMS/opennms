/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ping;

import java.net.DatagramPacket;
import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.icmp.AbstractPingRequest;
import org.opennms.netmgt.icmp.ICMPEchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.protocols.icmp.IcmpSocket;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
final public class PingRequest extends AbstractPingRequest<IcmpSocket> {
    public PingRequest(InetAddress addr, long tid, int sequenceId, long timeout, int retries, ThreadCategory logger, PingResponseCallback cb) {
        super(addr, tid, sequenceId, timeout, retries, logger, cb);
    }
    
    PingRequest(InetAddress addr, long tid, short sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(addr, tid, sequenceId, timeout, retries, ThreadCategory.getInstance(PingRequest.class), cb);
    }
    
    PingRequest(InetAddress addr, short sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(addr, s_nextTid++, sequenceId, timeout, retries, cb);
    }
    
    PingRequest(InetAddress addr, long timeout, int retries, PingResponseCallback cb) {
        this(addr, DEFAULT_SEQUENCE_ID, timeout, retries, cb);
    }
    

    /**
     * Send this PingRequest through the given icmpSocket
     *
     * @param icmpSocket a {@link org.opennms.protocols.icmp.IcmpSocket} object.
     */
    public void send(IcmpSocket icmpSocket, InetAddress addr) {
        try {
            m_request = createRequestPacket();

            m_log.debug(System.currentTimeMillis()+": Sending Ping Request: "+this);
            byte[] data = m_request.toBytes();
            icmpSocket.send(new DatagramPacket(data, data.length, m_id.getAddress(), 0));
        } catch (Throwable t) {
            m_callback.handleError(m_id.getAddress(), m_request, t);
        }
    }

    /**
     * <p>createRequestPacket</p>
     */
    private ICMPEchoPacket createRequestPacket() {
        m_expiration = System.currentTimeMillis() + m_timeout;
        org.opennms.protocols.icmp.ICMPEchoPacket iPkt = new org.opennms.protocols.icmp.ICMPEchoPacket(m_id.getTid());
        iPkt.setIdentity(FILTER_ID);
        iPkt.setSequenceId((short) m_id.getSequenceId());
        iPkt.computeChecksum();
        return new JICMPEchoPacket(iPkt);
    }
    

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("ID=").append(m_id).append(',');
        sb.append("Retries=").append(m_retries).append(",");
        sb.append("Timeout=").append(m_timeout).append(",");
        sb.append("Expiration=").append(m_expiration).append(',');
        sb.append("Callback=").append(m_callback);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public PingRequest constructNewRequest(InetAddress inetAddress, long tid, int sequenceId, long timeout, int retries, ThreadCategory log, PingResponseCallback callback) {
        return new PingRequest(inetAddress, tid, sequenceId, timeout, retries, log, callback);
    }
}
