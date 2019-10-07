/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpProfileMapper;

import com.google.common.base.Strings;


@Command(scope = "opennms-snmp", name = "fit", description = "Fit a profile for a given IP address")
@Service
public class FitProfileCommand implements Action {

    @Reference
    private SnmpProfileMapper snmpProfileMapper;

    @Reference
    private SnmpAgentConfigFactory agentConfigFactory;

    @Option(name = "-l", aliases = "--location", description = "Location of IP address that needs fitting")
    String location;

    @Option(name = "-s", aliases = "--save", description = "Save the resulting definition")
    boolean save = false;

    @Option(name = "-o", aliases = "--oid", description = "Custom OID used to fit profile")
    String oid;

    @Argument(name = "host", description = "IP address to fit", required = true)
    String ipAddress;

    @Argument(index = 1, name = "label", description = "Label of the Snmp Profile used to fit")
    String label;


    @Override
    public Object execute() throws Exception {

        InetAddress inetAddress = InetAddress.getByName(ipAddress);
        CompletableFuture<Optional<SnmpAgentConfig>> future = snmpProfileMapper.fitProfile(label, inetAddress, location, oid);
        while (true) {
            try {
                Optional<SnmpAgentConfig> agentConfig = future.get(1, TimeUnit.SECONDS);
                if (agentConfig.isPresent()) {
                    System.out.printf("Fitted IP address '%s' with profile '%s', agent config: \n", ipAddress, agentConfig.get().getProfileLabel());
                    ShowConfigCommand.prettyPrint(agentConfig.get());
                    if (save) {
                        agentConfigFactory.saveAgentConfigAsDefinition(agentConfig.get(), location, "karaf-shell");
                        System.out.println("*** Saved above config in definitions ***");
                    }
                    break;
                } else {
                    if (Strings.isNullOrEmpty(label)) {
                        System.out.printf("\nDidn't find any matching profile for IP address %s \n", ipAddress);
                    } else {
                        System.out.printf("\nProfile with label '%s' didn't fit for IP address '%s'\n", label, ipAddress);
                    }
                    break;
                }

            } catch (TimeoutException e1) {
                // Ignore
            }
            System.out.print(".");
        }

        return null;
    }


}
