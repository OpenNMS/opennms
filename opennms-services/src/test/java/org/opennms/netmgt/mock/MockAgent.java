//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 09: Java 5 generics and loops. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.mock;

import java.util.SortedMap;
import java.util.TreeMap;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

/**
 * Represents a MockAgent 
 *
 * @author brozow
 */
public class MockAgent {
    
    private SortedMap<OID, SubAgent> m_subAgents = new TreeMap<OID, SubAgent>();

    public void addSubAgent(SubAgent subAgent) {
        m_subAgents.put(subAgent.getBaseOID(), subAgent);
        
    }

    /**
     * @param oid
     * @return
     */
    public VariableBinding getNext(OID oid) {
        VariableBinding result = null;
        for (SubAgent subAgent : m_subAgents.values()) {
            result = subAgent.getNext(oid);
            if (result != null) {
                return result;
            }
        }
        
        return result;
    }

    /**
     * @param oid
     * @return
     */
    public VariableBinding get(OID oid) {
        for (OID agentKey : m_subAgents.keySet()) {
            if (oid.startsWith(agentKey)) {
                SubAgent subAgent = m_subAgents.get(agentKey);
                return subAgent.get(oid);
            }
        }
        return null;
    }


}
