/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
