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
package org.opennms.enlinkd.generator.protocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
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

public class LldpProtocol extends Protocol {
    private final TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.lldp;
    private final Random ifindexRandomGenerator = new Random();

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
        link.setLldpRemLocalPortNum(ifindexRandomGenerator.nextInt(100000));
        link.setLldpRemIndex(ifindexRandomGenerator.nextInt(100000));
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
