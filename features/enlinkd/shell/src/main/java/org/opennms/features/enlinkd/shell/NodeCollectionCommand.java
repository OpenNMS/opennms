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
package org.opennms.features.enlinkd.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;

/**
 * Force enlinkd to runSimpleSnmpCollection for specified node and protocol via karaf command.
 * Log into console via: ssh -p 8101 admin@localhost
 * Install: feature:install opennms-enlinkd-shell
 * Usage: type 'opennms:enlinkd-run-collection -n {nodeid} cdp' in karaf console
 */
@Command(scope = "opennms", name = "enlinkd-run-collection", description = "Execute a linkd snmp data collection and persist")
@Service
public class NodeCollectionCommand implements Action {

    @Option(name = "-n", aliases = "--node", description = "Node ID or FS:FID", required = true)
    private String node;

    @Argument(name = "protocol", description = "type of protocol (cdp | isis | lldp | ospf | bridge ).", required = true)
    @Completion(ProtocolCompleter.class)
    private String protocol;

    @Reference
    private ReloadableTopologyDaemon reloadableTopologyDaemon;

    @Reference
    private EnhancedLinkdConfig enhancedLinkdConfig;

    @Override
    public Void execute() {
        boolean protocolNotEnabled = true;
        boolean protocolNotSupported = false;
        switch (protocol) {
            case "cdp":
                if (enhancedLinkdConfig.useCdpDiscovery()) {
                    protocolNotEnabled = false;
                }
                break;
            case "lldp":
                if (enhancedLinkdConfig.useLldpDiscovery()) {
                    protocolNotEnabled = false;
                }
                break;
            case "bridge":
                if (enhancedLinkdConfig.useBridgeDiscovery()) {
                    protocolNotEnabled = false;
                }
                break;
            case "ospf":
                if (enhancedLinkdConfig.useOspfDiscovery()) {
                    protocolNotEnabled = false;
                }
                break;
            case "isis":
                if (enhancedLinkdConfig.useIsisDiscovery()) {
                    protocolNotEnabled = false;
                }
                break;
            default:
                protocolNotSupported=true;
                break;
        }
        if (protocolNotSupported) {
            System.out.printf("protocol %s, not known or supported by enlinkd", protocol);
            return null;
        }
        if (protocolNotEnabled) {
            System.out.printf("protocol %s, not enabled in enlinkd config file", protocol);
            return null;
        }
        boolean run = reloadableTopologyDaemon.runSingleSnmpCollection(node, protocol);
        if (run) {
            System.out.printf("protocol %s, node %s found and collection has been runned and persisted", protocol, node);
        } else {
            System.out.printf("node %s not found or snmp not supported", node);
        }
        return null;
    }

}
