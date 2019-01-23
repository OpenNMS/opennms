/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.shell;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.action.Option;
import org.opennms.features.topology.shell.topogen.TopologyPersisterDao;
import org.opennms.features.topology.shell.topogen.TopologyGenerator;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;

@Command(scope="enlinkd", name="generate-topology", description="Creates a linkd topology")
@Service
public class GenerateTopologyCommand implements Action {

    @Option(name="nodes", description="generate <N> OmnsNodes.")
    private Integer amountNodes;

    @Option(name="elements", description="generate <N> CdpElements")
    private Integer amountElements;

    @Option(name="links", description="generate <N> CdpLinks")
    private Integer amountLinks;

    @Option(name="snmpinterfaces", description="generate <N> SnmpInterfaces but not more than amount nodes")
    private Integer amountSnmpInterfaces;

    @Option(name="ipinterfaces", description="generate <N> IpInterfaces but not more than amount snmp interfaces")
    private Integer amountIpInterfaces;

    @Option(name="topology", description="type of topology (complete | ring | random)")
    private String topology;

    @Option(name="protocol", description="type of protocol (cdp | isis | lldp | ospf)")
    private String protocol;

    @Option(name="delete", description = "delete existing toplogogy (all OnmsNodes, CdpElements and CdpLinks)")
    private Boolean deleteExistingTolology;

    @Reference
    private NodeDao nodeDao;

    @Reference
    private CdpElementDao cdpElementDao;

    @Reference
    private IsIsElementDao isIsElementDao;

    @Reference
    private LldpElementDao lldpElementDao;

    @Reference
    private OspfElementDao ospfElementDao;

    @Reference
    private CdpLinkDao cdpLinkDao;

    @Reference
    private IsIsLinkDao isIsLinkDao;

    @Reference
    private LldpLinkDao lldpLinkDao;

    @Reference
    private OspfLinkDao ospfLinkDao;

    @Reference
    private IpInterfaceDao ipInterfaceDao;

    @Reference
    private SnmpInterfaceDao snmpInterfaceDao;

    private void invokeGenerator() throws SQLException, IOException {
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
            .persister(createTopologyPersisterDao())
            .build();
        generator.createNetwork();
    }

    private TopologyPersisterDao createTopologyPersisterDao() throws IOException {
        return new TopologyPersisterDao(
            nodeDao,
            cdpElementDao,
            isIsElementDao,
            lldpElementDao,
            ospfElementDao,
            cdpLinkDao,
            isIsLinkDao,
            lldpLinkDao,
            ospfLinkDao,
            ipInterfaceDao,
            snmpInterfaceDao
      );
    }

    private <E extends Enum> E toEnumOrNull(Class<E> enumClass, String s ) {
        return s == null ? null : (E) Enum.valueOf(enumClass, s);
    }

    /** Execute via Karaf. */
    @Override
    public Object execute() throws Exception {
        GenerateTopologyCommand main = new GenerateTopologyCommand();
        main.invokeGenerator();
        return null;
    }
}
