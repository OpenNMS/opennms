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
// 2008 Feb 09: Java 5 generics. - dj@opennms.org
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
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 * Represents a MapSubAgent 
 *
 * @author brozow
 */
public class MapSubAgent implements SubAgent {

    private SortedMap<OID, Variable> m_values = new TreeMap<OID, Variable>();
    private OID m_base;

    /**
     * @param string
     */
    public MapSubAgent(String baseOID) {
        m_base = new OID(baseOID);
    }
    
    public OID getBaseOID() {
        return new OID(m_base);
    }
    
    public void put(String oid, Variable value) {
        OID oidKey = new OID(m_base);
        oidKey.append(oid);
        m_values.put(oidKey, value);
    }
    
    private OID nextOID(OID requested) {
        OID next = new OID(requested);
        next.append(0);
        return next;
    }
    
    public VariableBinding getNext(OID requested) {
        OID successor = nextOID(requested);
        SortedMap<OID, Variable> tailMap = m_values.tailMap(successor);
        if (tailMap.isEmpty()) {
            return null;
        }
        
        OID next = tailMap.firstKey();
        Variable value = tailMap.get(next);
        return new VariableBinding(next, value);
        
    }
    
    public VariableBinding get(OID requested) {
        if (!m_values.containsKey(requested)) {
            return null;
        }
        
        OID response = new OID(requested);
        Variable value = m_values.get(response);
        return new VariableBinding(response, value);
            
    }

}
