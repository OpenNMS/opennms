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

package org.opennms.enlinkd.generator.protocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.core.utils.LldpUtils;
import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.topology.PairGenerator;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LldpProtocol extends Protocol<LldpElement> {
    private final static Logger LOG = LoggerFactory.getLogger(IsIsProtocol.class);
    private TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.lldp;

    public LldpProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {
        List<LldpElement> elements = createElements(nodes);
        context.getTopologyPersister().persist(elements);
        List<LldpLink> links = createLinks(elements);
        context.getTopologyPersister().persist(links);
    }

    private List<LldpElement> createElements(List<OnmsNode> nodes) {
        ArrayList<LldpElement> elements = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountElements(); i++) {
            OnmsNode node = nodes.get(i);
            String lLdpChassisId = "lLdpChassisId" + UUID.randomUUID();
            elements.add(createElement(node, lLdpChassisId));
        }
        return elements;
    }

    private LldpElement createElement(OnmsNode node, String lLdpChassisId) {
        LldpElement element = new LldpElement();
        element.setNode(node);
        element.setLldpChassisId(lLdpChassisId);
        element.setLldpChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT);
        element.setLldpNodeLastPollTime(new Date());
        element.setLldpSysname("LldpSysname");
        return element;
    }

    private List<LldpLink> createLinks(List<LldpElement> elements) {
        PairGenerator<LldpElement> pairs = createPairGenerator(elements);
        List<LldpLink> links = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountLinks() / 2; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.match...Links()
            Pair<LldpElement, LldpElement> pair = pairs.next();
            LldpElement sourceElement = pair.getLeft();
            LldpElement targetElement = pair.getRight();

            LldpUtils.LldpPortIdSubType portIdSubType = LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS;
            LldpUtils.LldpPortIdSubType portIdSubTypeRemote =
                    LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS;

            int sourceIfIndex = nodeIfIndexes.get(sourceElement.getNode().getId());
            int targetIfIndex = nodeIfIndexes.get(targetElement.getNode().getId());
            String portId = sourceElement.getNode().getId() + "-" + sourceIfIndex;
            String portIdRemote = sourceElement.getNode().getId() + "-" + sourceIfIndex;

            LldpLink sourceLink = createLink(sourceElement.getNode(), portId, portIdSubType, portIdRemote,
                    portIdSubTypeRemote, targetElement.getLldpChassisId(), sourceIfIndex);
            links.add(sourceLink);

            LldpLink targetLink = createLink(targetElement.getNode(), portIdRemote, portIdSubTypeRemote, portId,
                    portIdSubType, sourceElement.getLldpChassisId(), targetIfIndex);
            links.add(targetLink);

            context.currentProgress(String.format("Linked node %s with node %s", sourceElement.getNode().getLabel(),
                    targetElement.getNode().getLabel()));
        }
        return links;
    }


    private LldpLink createLink(OnmsNode node, String portId, LldpUtils.LldpPortIdSubType portIdSubType,
                                String remotePortId, LldpUtils.LldpPortIdSubType remotePortIdSubType,
                                String remoteChassisId, int ifIndex) {
        LldpLink link = new LldpLink();
        link.setLldpPortId(portId);
        link.setLldpPortIdSubType(portIdSubType);
        link.setLldpRemPortId(remotePortId);
        link.setLldpRemPortIdSubType(remotePortIdSubType);
        link.setLldpRemChassisId(remoteChassisId);
        link.setNode(node);
        link.setLldpPortIfindex(ifIndex);

        // static attributes:
        link.setLldpRemChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT); // shouldn't be relevant for match => set it fixed
        link.setLldpLocalPortNum(123);
        link.setLldpLinkLastPollTime(new Date());
        link.setLldpPortDescr("lldpportdescr");
        link.setLldpRemSysname("lldpRemSysname");
        link.setLldpPortDescr("lldpPortDescr");
        link.setLldpRemPortDescr("lldpRemPortDescr");

        return link;
    }

    public TopologyGenerator.Protocol getProtocol() {
        return this.protocol;
    }
}
