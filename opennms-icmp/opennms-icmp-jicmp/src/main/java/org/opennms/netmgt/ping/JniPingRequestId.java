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
    int m_identifier;
    int m_sequenceNumber;
    long m_threadId;

    /**
     * <p>Constructor for JniPingRequestId.</p>
     *
     * @param addr a {@link java.net.InetAddress} object.
     * @param sequenceNumber a short.
     * @param threadId a long.
     */
    public JniPingRequestId(InetAddress addr, int identifier, int sequenceNumber, long threadId) {
        m_addr = addr;
        m_identifier = identifier;
        m_sequenceNumber = sequenceNumber;
        m_threadId = threadId;
    }
    
    /**
     * <p>Constructor for JniPingRequestId.</p>
     *
     * @param reply a {@link org.opennms.netmgt.icmp.spi.JniPingResponse.PingReply} object.
     */
    public JniPingRequestId(JniPingResponse reply) {
        this(reply.getAddress(), reply.getIdentifier(), reply.getSequenceNumber(), reply.getThreadId());
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_addr;
    }
    
    public int getIdentifier() {
        return m_identifier;
    }

    /**
     * <p>getSequenceId</p>
     *
     * @return a int.
     */
    public int getSequenceNumber() {
        return m_sequenceNumber;
    }

    /**
     * <p>getTid</p>
     *
     * @return a long.
     */
    public long getThreadId() {
        return m_threadId;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JniPingRequestId) {
            JniPingRequestId id = (JniPingRequestId)obj;
            return (new InetAddressComparator().compare(getAddress(), id.getAddress()) == 0)
                && getIdentifier() == id.getIdentifier()
                && getSequenceNumber() == id.getSequenceNumber()
                && getThreadId() == id.getThreadId(); 
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + m_addr.hashCode();
        hash = hash * 31 + m_identifier;
        hash = hash * 31 + m_sequenceNumber;
        hash = hash * 31 + (int)(m_threadId >>> 32);
        hash = hash * 31 + (int)(m_threadId);
        return hash;
    }


    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName());
        buf.append('[');
        buf.append("addr = ").append(m_addr);
        buf.append(", ");
        buf.append("ident = ").append(m_identifier);
        buf.append(", ");
        buf.append("seqNum = ").append(m_sequenceNumber);
        buf.append(", ");
        buf.append("tId = ").append(m_threadId);
        buf.append(']');
        return buf.toString();
    }
    

}
