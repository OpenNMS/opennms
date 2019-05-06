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
 * Usage: type 'enlinkd:generate-topology' in karaf console
 */
@Command(scope = "enlinkd", name = "generate-topology", description = "Creates a linkd topology")
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
