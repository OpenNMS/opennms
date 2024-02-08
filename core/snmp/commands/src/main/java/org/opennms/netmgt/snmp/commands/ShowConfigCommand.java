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
package org.opennms.netmgt.snmp.commands;

import java.net.InetAddress;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

@Command(scope = "opennms", name = "snmp-show-config", description = "Display the effective SNMP agent configuration.")
@Service
public class ShowConfigCommand implements Action {

    @Reference
    public SnmpAgentConfigFactory snmpAgentConfigFactory;

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String m_location = null;

    @Argument(index = 0, name = "host", description = "Hostname or IP Address of the system to walk", required = true, multiValued = false)
    String m_host;

    @Override
    public Object execute() throws Exception {
        final InetAddress agentAdress = InetAddress.getByName(m_host);
        final SnmpAgentConfig agent = snmpAgentConfigFactory.getAgentConfig(agentAdress, m_location);
        prettyPrint(agent);
        return null;
    }

    public static void prettyPrint(SnmpAgentConfig agent) {
        System.out.println("Address: " + InetAddrUtils.str(agent.getAddress()));
        System.out.println("ProxyForAddress: " + InetAddrUtils.str(agent.getProxyFor()));
        System.out.println("Port: " + agent.getPort());
        System.out.println("Timeout: " + agent.getTimeout());
        System.out.println("Retries: " + agent.getRetries());
        System.out.println("MaxVarsPerPdu: " + agent.getMaxVarsPerPdu());
        System.out.println("MaxRepetitions: " + agent.getMaxRepetitions());
        System.out.println("MaxRequestSize: " + agent.getMaxRequestSize());
        System.out.println("Version: " + agent.getVersionAsString());
        if(agent.getTTL() != null) {
            System.out.println("TTL: " + agent.getTTL());
        }
        // The Karaf shell requires the ADMIN role, so we can safely display
        // the credentials in this context
        if (agent.isVersion3()) {
            System.out.println("SecurityLevel: " + agent.getSecurityLevel());
            System.out.println("SecurityName: " + agent.getSecurityName());
            System.out.println("AuthPassPhrase: " + agent.getAuthPassPhrase());
            System.out.println("AuthProtocol: " + agent.getAuthProtocol());
            System.out.println("PrivPassphrase: " + agent.getPrivPassPhrase());
            System.out.println("PrivProtocol: " + agent.getPrivProtocol());
            System.out.println("ContextName: " + agent.getContextName());
            System.out.println("EngineId: " + agent.getEngineId());
            System.out.println("ContextEngineId: " + agent.getContextEngineId());
            System.out.println("EnterpriseId: " + agent.getEnterpriseId());
        } else {
            System.out.println("ReadCommunity: " + agent.getReadCommunity());
            System.out.println("WriteCommunity: " + agent.getWriteCommunity());
        }
        if (agent.getProfileLabel() != null) {
            System.out.println("ProfileLabel : " + agent.getProfileLabel());
        }
        System.out.println();
    }
}
