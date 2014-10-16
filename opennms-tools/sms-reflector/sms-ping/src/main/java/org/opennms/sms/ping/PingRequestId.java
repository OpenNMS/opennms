/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
