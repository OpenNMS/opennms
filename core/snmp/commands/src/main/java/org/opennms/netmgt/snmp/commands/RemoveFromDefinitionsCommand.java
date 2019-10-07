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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;

import com.google.common.base.Strings;

@Command(scope = "opennms-snmp", name = "remove-from-definition", description = "Remove an IP address from a definition")
@Service
public class RemoveFromDefinitionsCommand implements Action {

    @Reference
    private SnmpAgentConfigFactory snmpAgentConfigFactory;

    @Option(name = "-l", aliases = "--location", description = "Location")
    String location;

    @Argument(name = "ipAddress", description = "IP address to remove from definition", required = true)
    String ipAddress;

    @Override
    public Object execute() throws Exception {
        boolean succeeded = snmpAgentConfigFactory.removeFromDefinition(InetAddress.getByName(ipAddress), location, "karaf-shell");
        if (Strings.isNullOrEmpty(location)) {
            location = "Default";
        }
        if (succeeded) {
            System.out.printf("IP address '%s' at location '%s' removed from SNMP Definitions \n", ipAddress, location);
        } else {
            System.out.printf("Failed to remove IP address '%s' at location '%s' from SNMP Definitions \n ", ipAddress, location);
        }
        return null;
    }
}
