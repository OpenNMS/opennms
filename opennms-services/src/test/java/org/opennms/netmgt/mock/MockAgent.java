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
