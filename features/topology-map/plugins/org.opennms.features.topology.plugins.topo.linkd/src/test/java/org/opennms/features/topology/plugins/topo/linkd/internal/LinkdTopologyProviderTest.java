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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.MockLogger;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.net.InetAddresses;

public class LinkdTopologyProviderTest {

    private final static int AMOUNT_ELEMENTS = 20;
    private final static int AMOUNT_NODES = 5;

    private final static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProviderTest.class);

    private Random random;
    private LinkdTopologyProvider provider;

    @Before
    public void setUp() {
        random = new Random(42);
        // We don't want the debug messages to take time:
        System.setProperty(MockLogger.LOG_KEY_PREFIX + LinkdTopologyProvider.class.getName(), "INFO");
        MetricRegistry registry = Mockito.mock(MetricRegistry.class);
        provider = new LinkdTopologyProvider(registry);
    }

    @Test
    public void isIsLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match

        List<OnmsNode> nodes = createNodes(6);
        List<IsIsElement> elements = Arrays.asList(
                createIsIsElement(nodes.get(0), "nomatch0"),
                createIsIsElement(nodes.get(1), "1.3"),
                createIsIsElement(nodes.get(2), "nomatch1"),
                createIsIsElement(nodes.get(3), "1.2"),
                createIsIsElement(nodes.get(4), "2.3"),
                createIsIsElement(nodes.get(5), "2.2")
            );

        List<IsIsLink> allLinks = Arrays.asList(
                createIsIsLink(0, "nomatch2", 100, nodes.get(0)),
                createIsIsLink(1, "1.2", 11, nodes.get(1)),
                createIsIsLink(2, "nomatch3", 101, nodes.get(2)),
                createIsIsLink(3, "1.3", 11, nodes.get(3)),
                createIsIsLink(4, "2.2", 22, nodes.get(4)),
                createIsIsLink(5, "2.3", 22, nodes.get(5))
        );

        List<Pair<IsIsLink, IsIsLink>> matchedLinks = provider.matchIsIsLinks(elements, allLinks);

        assertMatching(allLinks, matchedLinks);
    }

    @Test
    public void cdpLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match

        List<OnmsNode> nodes = createNodes(6);
        List<CdpElement> elements = Arrays.asList(
                createCdpElement(nodes.get(0), "Element0"),
                createCdpElement(nodes.get(1), "match1.4"),
                createCdpElement(nodes.get(2), "Element2"),
                createCdpElement(nodes.get(3), "match1.3"),
                createCdpElement(nodes.get(4), "match2.4"),
                createCdpElement(nodes.get(5), "match2.3")
        );

        List<CdpLinkTopologyEntity> allLinks = Arrays.asList(
                createCdpLinkTopologyEntity(0, nodes.get(0), "nomatch1", "nomatch2", "nomatch3"),
                createCdpLinkTopologyEntity(1, nodes.get(1), "match1.3", "match1.1", "match1.2"),
                createCdpLinkTopologyEntity(2, nodes.get(2), "nomatch4", "nomatch5", "nomatch6"),
                createCdpLinkTopologyEntity(3, nodes.get(3), "match1.4", "match1.2", "match1.1"),
                createCdpLinkTopologyEntity(4, nodes.get(4), "match2.3", "match2.1", "match2.2"),
                createCdpLinkTopologyEntity(5, nodes.get(5), "match2.4", "match2.2", "match2.1")
        );

        List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> matchedLinks = provider.matchCdpLinks(elements, allLinks);

        assertMatching(allLinks, matchedLinks);

    }

    @Test
    public void ospfLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match

        List<OnmsNode> nodes = createNodes(6);
        List<InetAddress> addresses = createInetAddresses(6);

        List<OspfLink> allLinks = Arrays.asList(
                createOspfLink(0, nodes.get(0), addresses.get(0), addresses.get(5)),
                createOspfLink(1, nodes.get(1), addresses.get(1), addresses.get(3)),
                createOspfLink(2, nodes.get(2), addresses.get(2), addresses.get(3)),
                createOspfLink(3, nodes.get(3), addresses.get(3), addresses.get(1)),
                createOspfLink(4, nodes.get(4), addresses.get(4), addresses.get(5)),
                createOspfLink(5, nodes.get(5), addresses.get(5), addresses.get(4))
        );

        List<Pair<OspfLink, OspfLink>> matchedLinks = provider.matchOspfLinks(allLinks);
        assertMatching(allLinks, matchedLinks);
    }

    @Test
    public void lldpLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match

        List<OnmsNode> nodes = createNodes(6);
        Map<Integer, LldpElement> elements = new HashMap<>();
        elements.put(0, createLldpElement(nodes.get(0), "Element0"));
        elements.put(1, createLldpElement(nodes.get(1), "match1.1"));
        elements.put(2, createLldpElement(nodes.get(2), "Element2"));
        elements.put(3, createLldpElement(nodes.get(3), "match1.2"));
        elements.put(4, createLldpElement(nodes.get(4), "match2.1"));
        elements.put(5, createLldpElement(nodes.get(5), "match2.2"));

        List<LldpLink> allLinks = Arrays.asList(
           createLldpLink(0, nodes.get(0), "nomatch1", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT,  "nomatch2", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT, "nomatch3"),
                createLldpLink(1, nodes.get(1), "match1.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME,  "match1.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, "match1.2"),
                createLldpLink(2, nodes.get(2), "nomatch4", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT,  "nomatch5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT, "nomatch6"),
                createLldpLink(3, nodes.get(3), "match1.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS,  "match1.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, "match1.1"),
                createLldpLink(4, nodes.get(4), "match2.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID,  "match2.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS, "match2.2"),
                createLldpLink(5, nodes.get(5), "match2.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS,  "match2.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID, "match2.1")
        );
        List<Pair<LldpLink, LldpLink>> matchedLinks = provider.matchLldpLinks(elements, allLinks);
        assertMatching(allLinks, matchedLinks);
    }

    private <E extends Topology> void sortAndAssertEquals(List<Pair<E, E>> matchesOld, List<Pair<E, E>> matchesNew){
        Comparator<Pair<E, E>> comparator = Comparator.comparing(pair -> pair.getKey().printTopology() + pair.getRight().printTopology());
        matchesOld.sort(comparator );
        matchesNew.sort(comparator );
        assertEquals(matchesOld, matchesNew);
    }

    private <Link> void assertMatching(List<Link> allLinks, List<Pair<Link, Link>> matchedLinks){
        // we expect:
        // 1 and 3 will match
        // 4 and 5 will match
        assertEquals(2, matchedLinks.size());
        assertEquals(allLinks.get(1), matchedLinks.get(0).getLeft());
        assertEquals(allLinks.get(3), matchedLinks.get(0).getRight());
        assertEquals(allLinks.get(4), matchedLinks.get(1).getLeft());
        assertEquals(allLinks.get(5), matchedLinks.get(1).getRight());
    }

    private Map<Integer, LldpElement> createLldpElements(List<OnmsNode> nodes) {
        Map<Integer, LldpElement> elements = new HashMap<>();
        for (int i = 0; i < AMOUNT_ELEMENTS; i++) {
            LldpElement element = createLldpElement(getRandom(nodes), "Element"+i);
            elements.put(element.getNode().getId(), element);
        }
        return elements;
    }

    private LldpElement createLldpElement(OnmsNode node, String lLdpChassisId) {
        LldpElement element = new LldpElement();
        element.setNode(node);
        element.setLldpChassisId(lLdpChassisId);
        element.setLldpChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT);
        return element;
    }

    private LldpLink createLldpLink(int id, OnmsNode node, String portId, LldpUtils.LldpPortIdSubType portIdSubType
            , String remotePortId, LldpUtils.LldpPortIdSubType remotePortIdSubType, String remoteChassisId) {
          LldpLink link = new LldpLink();
            link.setId(id);
            link.setLldpPortId(portId);
            link.setLldpPortIdSubType(portIdSubType);
            link.setLldpRemPortId(remotePortId);
            link.setLldpRemPortIdSubType(remotePortIdSubType);
            link.setLldpRemChassisId(remoteChassisId);
            link.setLldpRemChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT); // shouldn't be relevant for match => set it fixed
            link.setNode(node);
        return link;
    }

    private OspfLink createOspfLink(int id, OnmsNode node, InetAddress ipAddress, InetAddress remoteAddress) {
        OspfLink link = new OspfLink();
        link.setId(id);
        link.setNode(node);
        link.setOspfIpAddr(ipAddress);
        link.setOspfRemIpAddr(remoteAddress);
        return link;
    }

    private List<InetAddress> createInetAddresses(int amount) {
        List<InetAddress> addresses = new ArrayList<>();
        InetAddress address = InetAddresses.forString("0.0.0.0");
        addresses.add(address);
        for (int i = 1; i < amount; i++) {
            address = InetAddresses.increment(address);
            addresses.add(address);
        }
        return addresses;
    }

    private List<OnmsNode> createNodes() {
        return createNodes(AMOUNT_NODES);
    }

    private List<OnmsNode> createNodes(int amount) {
        ArrayList<OnmsNode> nodes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            OnmsNode node = new OnmsNode();
            node.setId(i);
            nodes.add(node);
        }
        return nodes;
    }

    private List<IsIsElement> createIsIsElements(List<OnmsNode> nodes) {
        ArrayList<IsIsElement> elements = new ArrayList<>();
        for (int i = 0; i < AMOUNT_ELEMENTS; i++) {
            IsIsElement element = createIsIsElement(getRandom(nodes), "Element"+i);
            elements.add(element);
        }
        return elements;
    }

    private IsIsElement createIsIsElement(OnmsNode node, String isisSysID) {
        IsIsElement element = new IsIsElement();
        element.setNode(node);
        element.setIsisSysID(isisSysID);
        return element;
    }

    private IsIsLink createIsIsLink(int id, String isisISAdjNeighSysID, int isisISAdjIndex, OnmsNode node) {
        IsIsLink link = new IsIsLink();
        link.setId(id);
        link.setIsisCircAdminState(IsIsElement.IsisAdminState.off);
        link.setIsisISAdjState(IsIsLink.IsisISAdjState.up);
        link.setIsisISAdjNeighSysID(isisISAdjNeighSysID);
        link.setIsisISAdjNeighSysType(IsIsLink.IsisISAdjNeighSysType.unknown);
        link.setIsisISAdjIndex(isisISAdjIndex);
        link.setNode(node);
        return link;
    }

    private CdpElement createCdpElement(OnmsNode node, String globalDeviceId) {
        CdpElement cdpElement = new CdpElement();
        cdpElement.setNode(node);
        cdpElement.setCdpGlobalDeviceId(globalDeviceId);
        return cdpElement;
    }

    private CdpLinkTopologyEntity createCdpLinkTopologyEntity(int id, OnmsNode node, String cdpCacheDeviceId, String cdpInterfaceName,
                                                              String cdpCacheDevicePort) {
        CdpLinkTopologyEntity link = new CdpLinkTopologyEntity(id, node.getId(), 123, cdpInterfaceName,
                "cdpCacheAddress", cdpCacheDeviceId, cdpCacheDevicePort);
        return link;
    }

    private <E> E getRandom(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }

}