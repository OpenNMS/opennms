/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

@Command(scope = "snmp", name = "show-config", description = "Display the effective SNMP agent configuration.")
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

        System.out.println("Address: " + InetAddrUtils.str(agent.getAddress()));
        System.out.println("ProxyForAddress: " + InetAddrUtils.str(agent.getProxyFor()));
        System.out.println("Port: " + agent.getPort());
        System.out.println("Timeout: " + agent.getTimeout());
        System.out.println("Retries: " + agent.getRetries());
        System.out.println("MaxVarsPerPdu: " + agent.getMaxVarsPerPdu());
        System.out.println("MaxRepetitions: " + agent.getMaxRepetitions());
        System.out.println("MaxRequestSize: " + agent.getMaxRequestSize());
        System.out.println("Version: " + agent.getVersionAsString());
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
        System.out.println();
        return null;
    }
}
