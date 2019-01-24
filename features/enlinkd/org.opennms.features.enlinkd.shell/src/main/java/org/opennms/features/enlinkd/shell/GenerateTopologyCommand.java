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

import java.io.IOException;
import java.sql.SQLException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;

@Command(scope = "enlinkd", name = "generate-topology", description = "Creates a linkd topology")
@Service
public class GenerateTopologyCommand implements Action {

    @Option(name = "--nodes", description = "generate <N> OmnsNodes.")
    private Integer amountNodes;

    @Option(name = "--elements", description = "generate <N> CdpElements")
    private Integer amountElements;

    @Option(name = "--links", description = "generate <N> CdpLinks")
    private Integer amountLinks;

    @Option(name = "--snmpinterfaces", description = "generate <N> SnmpInterfaces but not more than amount nodes")
    private Integer amountSnmpInterfaces;

    @Option(name = "--ipinterfaces", description = "generate <N> IpInterfaces but not more than amount snmp interfaces")
    private Integer amountIpInterfaces;

    @Option(name = "--topology", description = "type of topology (complete | ring | random)")
    private String topology;

    @Option(name = "--protocol", description = "type of protocol (cdp | isis | lldp | ospf)")
    private String protocol;

    @Option(name = "--delete", description = "delete existing toplogogy (all OnmsNodes, CdpElements and CdpLinks)")
    private Boolean deleteExistingTolology;

    @Reference
    private GenericPersistenceAccessor genericPersistenceAccessor;

    private void invokeGenerator() throws SQLException, IOException {

        // We print directly to the system out so it will appear in the console
        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(){

            @Override
            public void currentProgress(String progress) {
                System.out.println(progress);
            }
        };

        TopologyGenerator generator = TopologyGenerator.builder()
                .amountElements(this.amountElements)
                .amountIpInterfaces(this.amountIpInterfaces)
                .amountLinks(this.amountLinks)
                .amountNodes(this.amountNodes)
                .amountElements(this.amountElements)
                .amountSnmpInterfaces(amountSnmpInterfaces)
                .deleteExistingTolology(this.deleteExistingTolology)
                .protocol(toEnumOrNull(TopologyGenerator.Protocol.class, this.protocol))
                .topology(toEnumOrNull(TopologyGenerator.Topology.class, this.topology))
                .persister(new TopologyPersister(genericPersistenceAccessor, progressCallback))
                .progressCallback(progressCallback)
                .build();
        generator.generateTopology();
    }

    private <E extends Enum> E toEnumOrNull(Class<E> enumClass, String s) {
        return s == null ? null : (E) Enum.valueOf(enumClass, s);
    }

    /**
     * Execute via Karaf.
     */
    @Override
    public Object execute() throws Exception {
        invokeGenerator();
        return null;
    }
}
