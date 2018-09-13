/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import org.opennms.netmgt.model.topology.Topology;

public class OnmsTopologyProtocol {

    public static OnmsTopologyProtocol createFromTopologySupportedProtocol(Topology.ProtocolSupported protocol) {
        return new OnmsTopologyProtocol(protocol.name());
    }

    public static OnmsTopologyProtocol createFromString(String protocol) {
        return new OnmsTopologyProtocol(protocol);
    }

    
    final private String m_protocol;
    private OnmsTopologyProtocol(String protocol) {
        m_protocol = protocol;
    }
    
    public String getProtocol() {
        return m_protocol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_protocol == null) ? 0 : m_protocol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OnmsTopologyProtocol other = (OnmsTopologyProtocol) obj;
        if (m_protocol == null) {
            if (other.m_protocol != null)
                return false;
        } else if (!m_protocol.equals(other.m_protocol))
            return false;
        return true;
    }
    
    
    

}
