/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.features.enlinkd.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;

/**
 * Force enlinkd to Discovery Bridge Topology via karaf command.
 * Log into console via: ssh -p 8101 admin@localhost
 * Install: feature:install opennms-enlinkd-shell
 * Usage: type 'opennms:enlinkd-discovery-bridge-domain' in karaf console
 */
@Command(scope = "opennms", name = "enlinkd-discovery-bridge-domain", description = "Execute bridge topology discovery algorithm")
@Service
public class DiscoveryBridgeTopologyCommand implements Action {


    @Reference
    private ReloadableTopologyDaemon reloadableTopologyDaemon;

    @Reference
    private EnhancedLinkdConfig enhancedLinkdConfig;

    @Override
    public Void execute() {
        if (enhancedLinkdConfig.useBridgeDiscovery()) {
            System.out.println("Run: Bridge Protocol Discovery, calling ");
            reloadableTopologyDaemon.runDiscoveryBridgeDomains();
            System.out.println("Run: Bridge Protocol Discovery, executed");
        } else {
            System.out.println("No run: Bridge Protocol Discovery not Enabled: set useBridgeDiscovery to true in enlinkd config file");
        }
        return null;
    }

}
