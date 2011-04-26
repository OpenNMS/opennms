/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.ping;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressComparator;

/**
 * <p>JniPingRequestId class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JniPingRequestId {
    InetAddress m_addr;
    long m_tid;
    int m_seqId;

    /**
     * <p>Constructor for JniPingRequestId.</p>
     *
     * @param addr a {@link java.net.InetAddress} object.
     * @param tid a long.
     * @param seqId a short.
     */
    public JniPingRequestId(InetAddress addr, long tid, int seqId) {
        m_addr = addr;
        m_tid = tid;
        m_seqId = seqId;
    }
    
    /**
     * <p>Constructor for JniPingRequestId.</p>
     *
     * @param reply a {@link org.opennms.netmgt.icmp.spi.JniPingResponse.PingReply} object.
     */
    public JniPingRequestId(JniPingResponse reply) {
        this(reply.getAddress(), reply.getIdentifier(), reply.getSequenceNumber());
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_addr;
    }

    /**
     * <p>getTid</p>
     *
     * @return a long.
     */
    public long getTid() {
        return m_tid;
    }

    /**
     * <p>getSequenceId</p>
     *
     * @return a short.
     */
    public int getSequenceId() {
        return m_seqId;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JniPingRequestId) {
            JniPingRequestId id = (JniPingRequestId)obj;
            return (new InetAddressComparator().compare(getAddress(), id.getAddress()) == 0) && getTid() == id.getTid() && getSequenceId() == id.getSequenceId();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + m_addr.hashCode();
        hash = hash * 31 + (int)(m_tid >>> 32);
        hash = hash * 31 + (int)(m_tid);
        hash = hash * 31 + m_seqId;
        return hash;
    }


    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName());
        buf.append('[');
        buf.append("addr = ").append(m_addr);
        buf.append(", ");
        buf.append("tid = ").append(m_tid);
        buf.append(", ");
        buf.append("seqId = ").append(m_seqId);
        buf.append(']');
        return buf.toString();
    }
    

}
