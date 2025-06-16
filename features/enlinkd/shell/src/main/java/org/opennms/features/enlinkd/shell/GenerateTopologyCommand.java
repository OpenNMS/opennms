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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;

/**
 * Generate a enlinkd topology via karaf command.
 * Log into console via: ssh -p 8101 admin@localhost
 * Install: feature:install opennms-enlinkd-shell
 * Usage: type 'opennms:generate-topology' in karaf console
 */
@Command(scope = "opennms", name = "generate-topology", description = "Creates a linkd topology")
@Service
public class GenerateTopologyCommand implements Action {

    @Option(name = "--nodes", description = "generate <N> OmnsNodes. Default: 10")
    private Integer amountNodes;

    @Option(name = "--elements", description = "generate <N> (Cdp | IsIs | Lldp | Ospf ) Elements, depending on protocol. Default: amount nodes.")
    private Integer amountElements;

    @Option(name = "--links", description = "generate <N> (Cdp | IsIs | Lldp | Ospf ) Links, depending on protocol. Default: amount elements.")
    private Integer amountLinks;

    @Option(name = "--snmpinterfaces", description = "generate <N> SnmpInterfaces but not more than amount nodes. Default: amount nodes * 18.")
    private Integer amountSnmpInterfaces;

    @Option(name = "--ipinterfaces", description = "generate <N> IpInterfaces but not more than amount snmp interfaces. Default: amount nodes * 2.")
    private Integer amountIpInterfaces;

    @Option(name = "--topology", description = "type of topology (complete | ring | random). Default: random.")
    private String topology;

    @Option(name = "--protocol", description = "type of protocol (cdp | isis | lldp | ospf | bridge | userdefined). Default: cdp.")
    private String protocol;

    @Reference
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Reference
    private ReloadableTopologyDaemon reloadableTopologyDaemon;

    @Override
    public Object execute() {

        // We print directly to System.out so it will appear in the console
        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(System.out::println);

        TopologyGenerator generator = TopologyGenerator.builder()
                .persister(new TopologyPersister(genericPersistenceAccessor, progressCallback))
                .progressCallback(progressCallback)
                .build();
        TopologySettings settings = TopologySettings.builder()
                .amountElements(this.amountElements)
                .amountIpInterfaces(this.amountIpInterfaces)
                .amountLinks(this.amountLinks)
                .amountNodes(this.amountNodes)
                .amountSnmpInterfaces(amountSnmpInterfaces)
                .protocol(toEnumOrNull(TopologyGenerator.Protocol.class, this.protocol))
                .topology(toEnumOrNull(TopologyGenerator.Topology.class, this.topology))
                .build();
        generator.generateTopology(settings);
        reloadableTopologyDaemon.reloadTopology();
        return null;
    }

    private <E extends Enum> E toEnumOrNull(Class<E> enumClass, String s) {
        return s == null ? null : (E) Enum.valueOf(enumClass, s);
    }
}
