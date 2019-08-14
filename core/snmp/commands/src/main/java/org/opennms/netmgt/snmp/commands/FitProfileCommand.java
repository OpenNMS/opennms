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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
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


@Command(scope = "opennms-snmp", name = "fit", description = "Fit a profile with an IPAddress")
@Service
public class FitProfileCommand implements Action {

    @Reference
    private SnmpProfileMapper snmpProfileMapper;

    @Reference
    private SnmpAgentConfigFactory agentConfigFactory;

    @Option(name = "-l", aliases = "--location", description = "location of IpAddress that needs fitting")
    String location;

    @Argument(name = "host", description = "IPAddress that needed to be fit", required = true)
    String ipAddress;

    @Option(name = "-s", aliases = "--save", description = "Save the resulting definition")
    boolean save = false;

    @Argument(index = 1, name = "label", description = "Label of the Snmp Profile used to fit")
    String label;

    @Argument(index = 2, name = "oid", description = "custom oid used to fit profile")
    String oid;



    @Override
    public Object execute() throws Exception {


        InetAddress inetAddress = InetAddress.getByName(ipAddress);
        CompletableFuture<SnmpAgentConfig> future = fitProfile(label, inetAddress, location, oid);
        while (true) {
            try {
                SnmpAgentConfig agentConfig = future.get(1, TimeUnit.SECONDS);
                if (agentConfig != null) {
                    System.out.println("Fitted with following agent config:");
                    System.out.println(agentConfig.toString());
                    if (save) {
                        agentConfigFactory.saveAgentConfigAsDefinition(agentConfig, location, "karaf-shell");
                        System.out.println("Saved above config in definitions");
                    }
                }
                break;
            } catch (TimeoutException e1) {
                // Ignore
            }
            System.out.print(".");
        }

        return null;
    }

    private CompletableFuture<SnmpAgentConfig> fitProfile(String profileLabel, InetAddress inetAddress, String location, String oid) {

        CompletableFuture<SnmpAgentConfig> future = new CompletableFuture<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Optional<SnmpAgentConfig> agentConfig = snmpProfileMapper.fitProfile(profileLabel, inetAddress, location, oid);
            if (agentConfig.isPresent()) {
                future.complete(agentConfig.get());
            } else {
                future.completeExceptionally(new NoSuchElementException("No matching profiles found for ipaddress : " + inetAddress.getHostAddress()));
            }
        });
        return future;
    }

}
