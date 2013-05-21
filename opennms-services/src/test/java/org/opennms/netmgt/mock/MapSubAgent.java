/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
    
    @Override
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
    
    @Override
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
    
    @Override
    public VariableBinding get(OID requested) {
        if (!m_values.containsKey(requested)) {
            return null;
        }
        
        OID response = new OID(requested);
        Variable value = m_values.get(response);
        return new VariableBinding(response, value);
            
    }

}
