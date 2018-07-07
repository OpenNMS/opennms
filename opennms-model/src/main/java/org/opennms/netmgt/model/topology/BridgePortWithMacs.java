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

package org.opennms.netmgt.model.topology;

import java.util.Set;

public class BridgePortWithMacs implements Topology {

    public static BridgePortWithMacs create(BridgePort port, Set<String> macs) throws BridgeTopologyException {
        if (port == null) {
            throw new BridgeTopologyException("cannot create BridgePortWithMacs bridge port is null");
        }
        if (macs == null) {
            throw new BridgeTopologyException("cannot create BridgePortWithMacs macs is null");
        }
        return new BridgePortWithMacs(port,macs);
        
    }

    private final BridgePort m_port;
    private final Set<String> m_macs;
    
 
    private BridgePortWithMacs(BridgePort port, Set<String> macs) {
        m_port=port;
        m_macs=macs;
    }

    public BridgePort getPort() {
        return m_port;
    }

    public Set<String> getMacs() {
        return m_macs;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_port == null) ? 0 : m_port.hashCode());
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
        BridgePortWithMacs other = (BridgePortWithMacs) obj;
        if (m_port == null) {
            if (other.m_port != null)
                return false;
        } else if (!m_port.equals(other.m_port))
            return false;
        return true;
    }

    @Override
    public String printTopology() {
        StringBuffer strbfr = new StringBuffer();
        strbfr.append(m_port.printTopology());
        strbfr.append(" macs:");
        strbfr.append(m_macs);
        
        return strbfr.toString();
    }


    
}
