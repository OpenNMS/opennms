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
package org.opennms.netmgt.snmp.snmp4j;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class Snmp4JV2TrapBuilder implements SnmpTrapBuilder {
    private Snmp4JStrategy m_strategy;
    private PDU m_pdu;
    
    protected Snmp4JV2TrapBuilder(Snmp4JStrategy strategy, PDU pdu, int type) {
        m_strategy = strategy;
        m_pdu = pdu;
        m_pdu.setType(type);
    }
    
    protected Snmp4JV2TrapBuilder(Snmp4JStrategy strategy) {
        this(strategy, new PDU(), PDU.TRAP);
    }
    
    protected PDU getPDU() {
        return m_pdu;
    }

    @Override
    public void send(String destAddr, int destPort, String community) throws Exception {
        SnmpAgentConfig snmpAgentConfig = m_strategy.buildAgentConfig(destAddr, destPort, community, m_pdu);
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(snmpAgentConfig);
        
        m_strategy.send(agentConfig, m_pdu, false);
    }

	public SnmpValue[] sendInform(String destAddr, int destPort, int timeout,
			int retries, String community) throws Exception {
        SnmpAgentConfig snmpAgentConfig = m_strategy.buildAgentConfig(destAddr, destPort, timeout, retries, community, m_pdu);
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(snmpAgentConfig);
        
        return m_strategy.send(agentConfig, m_pdu, true);
	}

	public void send(String destAddr, int destPort, int securityLevel,
			String securityName, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privProtocol) throws Exception {
        SnmpAgentConfig snmpAgentConfig = 
        	m_strategy.buildAgentConfig(destAddr, destPort, securityLevel, securityName, authPassPhrase, authProtocol, privPassPhrase, privProtocol, m_pdu);
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(snmpAgentConfig);
        
        m_strategy.send(agentConfig, m_pdu, false);
		
	}

	public SnmpValue[] sendInform(String destAddr, int destPort, int timeout,
			int retries, int securityLevel, String securityName,
			String authPassPhrase, String authProtocol, String privPassPhrase,
			String privProtocol) throws Exception {
        SnmpAgentConfig snmpAgentConfig = 
        	m_strategy.buildAgentConfig(destAddr, destPort, timeout, retries, securityLevel, securityName, authPassPhrase, authProtocol, privPassPhrase, privProtocol, m_pdu);
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(snmpAgentConfig);
        
        return m_strategy.send(agentConfig, m_pdu, true);
	}

    @Override
    public void addVarBind(SnmpObjId name, SnmpValue value) {
        OID oid = new OID(name.getIds());
        Variable val = ((Snmp4JValue) value).getVariable();
        m_pdu.add(new VariableBinding(oid, val));
    }

    @Override
    public void sendTest(String destAddr, int destPort, String community) throws Exception {
        m_strategy.sendTest(destAddr, destPort, community, m_pdu);
    }


}
