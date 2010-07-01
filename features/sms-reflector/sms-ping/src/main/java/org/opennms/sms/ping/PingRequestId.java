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
package org.opennms.sms.ping;

import org.springframework.util.Assert;

/**
 * PingRequestId
 *
 * @author brozow
 * @version $Id: $
 */
public class PingRequestId {
    
    private String m_destination;
    
    /**
     * <p>Constructor for PingRequestId.</p>
     *
     * @param destination a {@link java.lang.String} object.
     */
    public PingRequestId(String destination) {
        Assert.notNull(destination);
        m_destination = destination.startsWith("+") ? destination.substring(1) : destination;
    }
    
    /**
     * <p>getDestination</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDestination() {
        return m_destination;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PingRequestId) {
            PingRequestId o = (PingRequestId)obj;
            return m_destination.equals(o.m_destination);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return m_destination.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_destination;
    }


}
