/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.topogen.protocol;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.opennms.enlinkd.topogen.TopologyGenerator;
import org.opennms.enlinkd.topogen.TopologyPersisterDao;
import org.opennms.enlinkd.topogen.topology.PairGenerator;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdpProtocol extends Protocol<CdpElement> {
    private final static Logger LOG = LoggerFactory.getLogger(CdpProtocol.class);
    private TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.cdp;

    public CdpProtocol(TopologyGenerator.Topology topology, int amountNodes, int amountLinks,
                       int amountElements, int amountSnmpInterfaces, int amountIpInterfaces, TopologyPersisterDao persister){
        super(topology, amountNodes, amountLinks, amountElements, amountSnmpInterfaces, amountIpInterfaces, persister);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) throws SQLException {
        List<CdpElement> cdpElements = createCdpElements(nodes);
        persister.persistCdpElements(cdpElements);
        List<CdpLink> links = createCdpLinks(cdpElements);
        persister.persistCdpLinks(links);
    }

    private List<CdpElement> createCdpElements(List<OnmsNode> nodes) {
        ArrayList<CdpElement> cdpElements = new ArrayList<>();
        for (int i = 0; i < amountElements; i++) {
            OnmsNode node = nodes.get(i);
            cdpElements.add(createCdpElement(node));
        }
        return cdpElements;
    }

    private CdpElement createCdpElement(OnmsNode node) {
        CdpElement cdpElement = new CdpElement();
        cdpElement.setId(node.getId()); // we use the same id for simplicity
        cdpElement.setNode(node);
        cdpElement.setCdpGlobalDeviceId("CdpElementForNode" + node.getId());
        cdpElement.setCdpGlobalRun(OspfElement.TruthValue.FALSE);
        cdpElement.setCdpNodeLastPollTime(new Date());
        return cdpElement;
    }

    private List<CdpLink> createCdpLinks(List<CdpElement> cdpElements) {
        PairGenerator<CdpElement> pairs = createPairGenerator(cdpElements);
        List<CdpLink> links = new ArrayList<>();
        for (int i = 0; i < amountLinks; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.matchCdpLinks()
            Pair<CdpElement, CdpElement> pair = pairs.next();
            CdpElement sourceCdpElement = pair.getLeft();
            CdpElement targetCdpElement = pair.getRight();
            CdpLink sourceLink = createCdpLink(i,
                    sourceCdpElement.getNode(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    targetCdpElement.getCdpGlobalDeviceId()
            );
            links.add(sourceLink);

            String targetCdpCacheDevicePort = sourceLink.getCdpInterfaceName();
            String targetCdpInterfaceName = sourceLink.getCdpCacheDevicePort();
            String targetCdpGlobalDeviceId = sourceCdpElement.getCdpGlobalDeviceId();
            CdpLink targetLink = createCdpLink(++i,
                    targetCdpElement.getNode(),
                    targetCdpInterfaceName,
                    targetCdpCacheDevicePort,
                    targetCdpGlobalDeviceId
            );
            links.add(targetLink);
            LOG.debug("Linked node {} with node {}", sourceCdpElement.getNode().getLabel(), targetCdpElement.getNode().getLabel());
        }
        return links;
    }

    private CdpLink createCdpLink(int id, OnmsNode node, String cdpInterfaceName, String cdpCacheDevicePort,
                                  String cdpCacheDeviceId) {
        CdpLink link = new CdpLink();
        link.setId(id);
        link.setCdpCacheDeviceId(cdpCacheDeviceId);
        link.setCdpInterfaceName(cdpInterfaceName);
        link.setCdpCacheDevicePort(cdpCacheDevicePort);
        link.setNode(node);
        link.setCdpCacheAddressType(CdpLink.CiscoNetworkProtocolType.chaos);
        link.setCdpCacheAddress("CdpCacheAddress");
        link.setCdpCacheDeviceIndex(33);
        link.setCdpCacheDevicePlatform("CdpCacheDevicePlatform");
        link.setCdpCacheIfIndex(33);
        link.setCdpCacheVersion("CdpCacheVersion");
        link.setCdpLinkLastPollTime(new Date());
        return link;
    }

    public TopologyGenerator.Protocol getProtocol() {
        return this.protocol;
    }
}
