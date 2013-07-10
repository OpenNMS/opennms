/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;


/**
 * <p>LocationMonitorIpInterface class.</p>
 */
public class LocationMonitorIpInterface {
    private final OnmsLocationMonitor m_locationMonitor;
    private final OnmsIpInterface m_ipInterface;

    /**
     * <p>Constructor for LocationMonitorIpInterface.</p>
     *
     * @param locationMonitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @param ipInterface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public LocationMonitorIpInterface(final OnmsLocationMonitor locationMonitor, final OnmsIpInterface ipInterface) {
        Assert.notNull(locationMonitor);
        Assert.notNull(ipInterface);
        m_locationMonitor = locationMonitor;
        m_ipInterface = ipInterface;
    }

    /**
     * <p>getIpInterface</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public OnmsIpInterface getIpInterface() {
        return m_ipInterface;
    }

    /**
     * <p>getLocationMonitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     */
    public OnmsLocationMonitor getLocationMonitor() {
        return m_locationMonitor;
    }
    
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("locationMonitor", m_locationMonitor)
            .append("ipInterface", m_ipInterface)
            .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_ipInterface.getId().hashCode();
        result = prime * result + m_locationMonitor.getId().hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof LocationMonitorIpInterface))
            return false;
        LocationMonitorIpInterface other = (LocationMonitorIpInterface) obj;
        if (m_ipInterface.getId() == null) {
            if (other.m_ipInterface.getId() != null)
                return false;
        } else if (!m_ipInterface.getId().equals(other.m_ipInterface.getId()))
            return false;
        if (m_locationMonitor.getId() == null) {
            if (other.m_locationMonitor.getId() != null)
                return false;
        } else if (!m_locationMonitor.getId().equals(other.m_locationMonitor.getId()))
            return false;
        return true;
    }
}
