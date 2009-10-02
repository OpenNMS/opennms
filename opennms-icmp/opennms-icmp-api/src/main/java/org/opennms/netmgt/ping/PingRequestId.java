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

public class PingRequestId {
    InetAddress m_addr;
    long m_tid;
    short m_seqId;

    public PingRequestId(InetAddress addr, long tid, short seqId) {
        m_addr = addr;
        m_tid = tid;
        m_seqId = seqId;
    }
    
    public PingRequestId(PingReply reply) {
        this(reply.getAddress(), reply.getPacket().getTID(), reply.getPacket().getSequenceId());
    }

    public InetAddress getAddress() {
        return m_addr;
    }

    public long getTid() {
        return m_tid;
    }

    public short getSequenceId() {
        return m_seqId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PingRequestId) {
            PingRequestId id = (PingRequestId)obj;
            return getAddress().equals(id.getAddress()) && getTid() == id.getTid() && getSequenceId() == id.getSequenceId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + m_addr.hashCode();
        hash = hash * 31 + (int)(m_tid >>> 32);
        hash = hash * 31 + (int)(m_tid);
        hash = hash * 31 + m_seqId;
        return hash;
    }


    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
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