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
package org.opennms.jicmp;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.jicmp.jna.NativeDatagramSocket;
import org.opennms.jicmp.v6.V6Pinger;
import org.opennms.netmgt.icmp.AbstractPingRequest;
import org.opennms.netmgt.icmp.PingResponseCallback;

/**
 * This class is used to encapsulate a ping request. A request consist of
 * the pingable address and a signaled state.
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
final public class JnaPingRequest extends AbstractPingRequest<NativeDatagramSocket> {
    private final V4Pinger v4;
    private final V6Pinger v6;
    public JnaPingRequest(V4Pinger v4, V6Pinger v6, InetAddress addr, long tid, int sequenceId, long timeout, int retries, ThreadCategory logger, PingResponseCallback cb) {
        super(addr, tid, sequenceId, timeout, retries, logger, cb);
        this.v4 = v4;
        this.v6 = v6;
    }

    JnaPingRequest(V4Pinger v4, V6Pinger v6, InetAddress addr, long tid, int sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(v4, v6, addr, tid, sequenceId, timeout, retries, ThreadCategory.getInstance(JnaPingRequest.class), cb);
    }

    JnaPingRequest(V4Pinger v4, V6Pinger v6, InetAddress addr, int sequenceId, long timeout, int retries, PingResponseCallback cb) {
        this(v4, v6, addr, s_nextTid++, sequenceId, timeout, retries, cb);
    }
    
    /**
     * Send this PingRequest through the given icmpSocket
     *
     * @param icmpSocket a {@link org.opennms.protocols.icmp.IcmpSocket} object.
     */
    public void send(NativeDatagramSocket icmpSocket, InetAddress addr) {
        try {
            m_log.debug(System.currentTimeMillis()+": Sending Ping Request: " + this);

            if (addr instanceof Inet4Address) {
        		v4.ping((Inet4Address)addr, m_id.getSequenceId(), 1, m_timeout);
        	} else if (addr instanceof Inet6Address) {
        		v6.ping((Inet6Address)addr, m_id.getSequenceId(), 1, m_timeout);
        	}
        } catch (Throwable t) {
            m_callback.handleError(m_id.getAddress(), m_request, t);
        }
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
    public JnaPingRequest constructNewRequest(InetAddress inetAddress, long tid, int sequenceId, long timeout, int retries, ThreadCategory log, PingResponseCallback callback) {
        return new JnaPingRequest(v4, v6, inetAddress, tid, sequenceId, timeout, retries, log, callback);
    }
}
