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
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.topology.PairGenerator;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.model.OnmsNode;

public class CdpProtocol extends Protocol {
    private final TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.cdp;

    public CdpProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {
        List<CdpElement> cdpElements = createCdpElements(nodes);
        context.getTopologyPersister().persist(cdpElements);
        List<CdpLink> links = createLinks(cdpElements);
        context.getTopologyPersister().persist(links);
    }

    private List<CdpElement> createCdpElements(List<OnmsNode> nodes) {
        ArrayList<CdpElement> cdpElements = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountElements(); i++) {
            OnmsNode node = nodes.get(i);
            cdpElements.add(createCdpElement(node));
        }
        return cdpElements;
    }

    private CdpElement createCdpElement(OnmsNode node) {
        CdpElement cdpElement = new CdpElement();
        cdpElement.setNode(node);
        cdpElement.setCdpGlobalDeviceId("CdpElementForNode" + node.getId());
        cdpElement.setCdpGlobalRun(OspfElement.TruthValue.FALSE);
        cdpElement.setCdpNodeLastPollTime(new Date());
        return cdpElement;
    }

    private List<CdpLink> createLinks(List<CdpElement> cdpElements) {
        PairGenerator<CdpElement> pairs = createPairGenerator(cdpElements);
        List<CdpLink> links = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountLinks() / 2; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.matchCdpLinks()
            Pair<CdpElement, CdpElement> pair = pairs.next();
            CdpElement sourceElement = pair.getLeft();
            CdpElement targetElement = pair.getRight();

            int sourceIfIndex = nodeIfIndexes.get(sourceElement.getNode().getId());
            int targetIfIndex = nodeIfIndexes.get(targetElement.getNode().getId());

            CdpLink sourceLink = createCdpLink(sourceElement.getNode(), UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(), targetElement.getCdpGlobalDeviceId(), sourceIfIndex);
            links.add(sourceLink);

            String targetCdpCacheDevicePort = sourceLink.getCdpInterfaceName();
            String targetCdpInterfaceName = sourceLink.getCdpCacheDevicePort();
            String targetCdpGlobalDeviceId = sourceElement.getCdpGlobalDeviceId();
            CdpLink targetLink = createCdpLink(targetElement.getNode(), targetCdpInterfaceName,
                    targetCdpCacheDevicePort, targetCdpGlobalDeviceId, targetIfIndex);
            links.add(targetLink);
            context.currentProgress(String.format("Linked node %s with node %s", sourceElement.getNode().getLabel(),
                    targetElement.getNode().getLabel()));
        }
        return links;
    }

    private CdpLink createCdpLink(OnmsNode node, String cdpInterfaceName, String cdpCacheDevicePort,
                                  String cdpCacheDeviceId, int ifIndex) {
        CdpLink link = new CdpLink();
        link.setCdpCacheDeviceId(cdpCacheDeviceId);
        link.setCdpInterfaceName(cdpInterfaceName);
        link.setCdpCacheDevicePort(cdpCacheDevicePort);
        link.setNode(node);
        link.setCdpCacheAddressType(CdpLink.CiscoNetworkProtocolType.chaos);
        link.setCdpCacheAddress("CdpCacheAddress");
        link.setCdpCacheDeviceIndex(33);
        link.setCdpCacheDevicePlatform("CdpCacheDevicePlatform");
        link.setCdpCacheIfIndex(ifIndex);
        link.setCdpCacheVersion("CdpCacheVersion");
        link.setCdpLinkLastPollTime(new Date());
        return link;
    }

    public TopologyGenerator.Protocol getProtocol() {
        return this.protocol;
    }
}
