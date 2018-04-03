/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2016 The OpenNMS Group, Inc.
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "snmp", name = "walk", description = "Walk the agent on the specified host and print the results.")
@Service
public class WalkCommand implements Action {

    private static final Logger LOG = LoggerFactory.getLogger(WalkCommand.class);

    @Reference
    public SnmpAgentConfigFactory snmpAgentConfigFactory;

    @Reference
    public LocationAwareSnmpClient locationAwareSnmpClient;

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String m_location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String m_systemId = null;

    @Argument(index = 0, name = "host", description = "Hostname or IP Address of the system to walk", required = true, multiValued = false)
    String m_host;

    @Argument(index = 1, name = "oids", description = "List of OIDs to retrieve from the agent", required = true, multiValued = true)
    List<String> m_oids;

    @Override
    public Object execute() throws Exception {
        LOG.debug("snmp:walk {} {} {}", m_location != null ? "-l " + m_location : "", m_host, m_oids);
        final List<SnmpObjId> snmpObjIds = m_oids.stream()
                    .map(SnmpObjId::get)
                    .collect(Collectors.toList());
        final SnmpAgentConfig agent = snmpAgentConfigFactory.getAgentConfig(InetAddress.getByName(m_host), m_location);
        final CompletableFuture<List<SnmpResult>> future = locationAwareSnmpClient.walk(agent, snmpObjIds)
            .withDescription("snmp:walk")
            .withLocation(m_location)
            .withSystemId(m_systemId)
            .execute();

        while (true) {
            try {
                future.get(1, TimeUnit.SECONDS).stream()
                    .forEach(res -> {
                        System.out.printf("[%s].[%s] = %s%n", res.getBase(), res.getInstance(), res.getValue());
                    });
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
        }
        return null;
    }
}
