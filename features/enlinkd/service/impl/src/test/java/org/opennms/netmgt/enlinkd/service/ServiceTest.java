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

package org.opennms.netmgt.enlinkd.service;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.net.InetAddresses;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-enlinkdservice-mock.xml"
})
public class ServiceTest {

    @Autowired
    private IsisTopologyService isisTopologyService;
    @Autowired
    private CdpTopologyService cdpTopologyService;
    @Autowired
    private LldpTopologyService lldpTopologyService;
    @Autowired
    private OspfTopologyService ospfTopologyService;

    @Autowired
    private TopologyEntityCache topologyEntityCache;

    List<OnmsNode> nodes;
    List<IsIsElementTopologyEntity> isiselements;
    List<IsIsLinkTopologyEntity> isisLinks;
    
    List<CdpElementTopologyEntity> cdpelements;
    List<CdpLinkTopologyEntity> cdpLinks;

    List<OspfLinkTopologyEntity> ospfLinks;

    List<LldpElementTopologyEntity> lldpelements;
    List<LldpLinkTopologyEntity> lldpLinks;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        nodes = createNodes(6);
        isiselements = Arrays.asList(
                                     createIsIsElement(0,nodes.get(0), "nomatch0"),
                                     createIsIsElement(1,nodes.get(1), "1.3"),
                                     createIsIsElement(2,nodes.get(2), "nomatch1"),
                                     createIsIsElement(3,nodes.get(3), "1.2"),
                                     createIsIsElement(4,nodes.get(4), "2.3"),
                                     createIsIsElement(5,nodes.get(5), "2.2")
                                 );
        isisLinks = Arrays.asList(
                                  createIsIsLink(6, "nomatch2", 100, nodes.get(0)),
                                  createIsIsLink(7, "1.2", 11, nodes.get(1)),
                                  createIsIsLink(8, "nomatch3", 101, nodes.get(2)),
                                  createIsIsLink(9, "1.3", 11, nodes.get(3)),
                                  createIsIsLink(10, "2.2", 22, nodes.get(4)),
                                  createIsIsLink(11, "2.3", 22, nodes.get(5))
                          );
        
        cdpelements = Arrays.asList(
                                    createCdpElement(12,nodes.get(0), "Element0"),
                                    createCdpElement(13,nodes.get(1), "match1.4"),
                                    createCdpElement(14,nodes.get(2), "Element2"),
                                    createCdpElement(15,nodes.get(3), "match1.3"),
                                    createCdpElement(16,nodes.get(4), "match2.4"),
                                    createCdpElement(17,nodes.get(5), "match2.3")
                            );
        
        cdpLinks = Arrays.asList(
                                 createCdpLink(18, nodes.get(0), "nomatch1", "nomatch2", "nomatch3"),
                                 createCdpLink(19, nodes.get(1), "match1.3", "match1.1", "match1.2"),
                                 createCdpLink(20, nodes.get(2), "nomatch4", "nomatch5", "nomatch6"),
                                 createCdpLink(21, nodes.get(3), "match1.4", "match1.2", "match1.1"),
                                 createCdpLink(22, nodes.get(4), "match2.3", "match2.1", "match2.2"),
                                 createCdpLink(23, nodes.get(5), "match2.4", "match2.2", "match2.1")
                         );


        List<InetAddress> addresses = createInetAddresses(6);

        ospfLinks = Arrays.asList(
                createOspfLink(24, nodes.get(0), addresses.get(0), addresses.get(5)),
                createOspfLink(25, nodes.get(1), addresses.get(1), addresses.get(3)),
                createOspfLink(26, nodes.get(2), addresses.get(2), addresses.get(3)),
                createOspfLink(27, nodes.get(3), addresses.get(3), addresses.get(1)),
                createOspfLink(28, nodes.get(4), addresses.get(4), addresses.get(5)),
                createOspfLink(29, nodes.get(5), addresses.get(5), addresses.get(4))
        );

        lldpelements = Arrays.asList(
        createLldpElement(30,nodes.get(0), "Element0"),
        createLldpElement(31,nodes.get(1), "match1.1"),
        createLldpElement(32,nodes.get(2), "Element2"),
        createLldpElement(33,nodes.get(3), "match1.2"),
        createLldpElement(34,nodes.get(4), "match2.1"),
        createLldpElement(35,nodes.get(5), "match2.2"));

        lldpLinks = Arrays.asList(
           createLldpLink(0, nodes.get(0), "nomatch1", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT,  "nomatch2", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT, "nomatch3"),
                createLldpLink(1, nodes.get(1), "match1.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME,  "match1.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, "match1.2"),
                createLldpLink(2, nodes.get(2), "nomatch4", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT,  "nomatch5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_PORTCOMPONENT, "nomatch6"),
                createLldpLink(3, nodes.get(3), "match1.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS,  "match1.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, "match1.1"),
                createLldpLink(4, nodes.get(4), "match2.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID,  "match2.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS, "match2.2"),
                createLldpLink(5, nodes.get(5), "match2.3", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS,  "match2.5", LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_AGENTCIRCUITID, "match2.1")
        );

        EasyMock.expect(topologyEntityCache.getCdpLinkTopologyEntities()).andReturn(cdpLinks).anyTimes();
        EasyMock.expect(topologyEntityCache.getCdpElementTopologyEntities()).andReturn(cdpelements).anyTimes();

        EasyMock.expect(topologyEntityCache.getLldpElementTopologyEntities()).andReturn(lldpelements).anyTimes();
        EasyMock.expect(topologyEntityCache.getLldpLinkTopologyEntities()).andReturn(lldpLinks).anyTimes();

        EasyMock.expect(topologyEntityCache.getIsIsElementTopologyEntities()).andReturn(isiselements).anyTimes();
        EasyMock.expect(topologyEntityCache.getIsIsLinkTopologyEntities()).andReturn(isisLinks).anyTimes();

        EasyMock.expect(topologyEntityCache.getOspfLinkTopologyEntities()).andReturn(ospfLinks).anyTimes();
        

        EasyMock.replay(topologyEntityCache);
     }
    
    @After
    public void tearDown() {
        EasyMock.reset(topologyEntityCache);
    }

    @Test
    public void isIsLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match
        List<TopologyConnection<IsIsLinkTopologyEntity, IsIsLinkTopologyEntity>> matchedLinks = isisTopologyService.match();
        assertMatching(isisLinks, matchedLinks);
    }

    @Test
    public void cdpLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match
        List<TopologyConnection<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> matchedLinks = cdpTopologyService.match();
        assertMatching(cdpLinks, matchedLinks);

    }

    @Test
    public void ospfLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match


        List<TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> matchedLinks = ospfTopologyService.match();
        assertMatching(ospfLinks, matchedLinks);
    }

    @Test
    public void lldpLinksShouldMatchCorrectly() {

        // 1 and 3 will match
        // 4 and 5 will match

        List<TopologyConnection<LldpLinkTopologyEntity, LldpLinkTopologyEntity>> matchedLinks = lldpTopologyService.match();
        assertMatching(lldpLinks, matchedLinks);
    }

    private <Link> void assertMatching(List<Link> allLinks, List<TopologyConnection<Link, Link>> matchedLinks){
        // we expect:
        // 1 and 3 will match
        // 4 and 5 will match
        assertEquals(2, matchedLinks.size());
        assertEquals(allLinks.get(1), matchedLinks.get(0).getLeft());
        assertEquals(allLinks.get(3), matchedLinks.get(0).getRight());
        assertEquals(allLinks.get(4), matchedLinks.get(1).getLeft());
        assertEquals(allLinks.get(5), matchedLinks.get(1).getRight());
    }

    private LldpElementTopologyEntity createLldpElement(Integer id, OnmsNode node, String lLdpChassisId) {
        return new LldpElementTopologyEntity(id, lLdpChassisId, node.getId());
    }

    private LldpLinkTopologyEntity createLldpLink(int id, OnmsNode node, String portId, LldpUtils.LldpPortIdSubType portIdSubType
            , String remotePortId, LldpUtils.LldpPortIdSubType remotePortIdSubType, String remoteChassisId) {
        return new LldpLinkTopologyEntity(id, node.getId(), remoteChassisId, remotePortId, remotePortIdSubType, portId, portIdSubType, "dwscr", -1);
    }

    private OspfLinkTopologyEntity createOspfLink(int id, OnmsNode node, InetAddress ipAddress, InetAddress remoteAddress) {
        return new OspfLinkTopologyEntity(id, node.getId(), ipAddress, InetAddressUtils.addr("255.255.255.252"),remoteAddress, -1);
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

    private List<OnmsNode> createNodes(int amount) {
        ArrayList<OnmsNode> nodes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            OnmsNode node = new OnmsNode();
            node.setId(i);
            nodes.add(node);
        }
        return nodes;
    }

    private IsIsElementTopologyEntity createIsIsElement(Integer id,OnmsNode node, String isisSysID) {
        return new IsIsElementTopologyEntity(id, isisSysID, node.getId());
    }

    private IsIsLinkTopologyEntity createIsIsLink(int id, String isisISAdjNeighSysID, int isisISAdjIndex, OnmsNode node) {
        return new IsIsLinkTopologyEntity(id, node.getId(), isisISAdjIndex, isisISAdjIndex, isisISAdjNeighSysID, null);
    }

    private CdpElementTopologyEntity createCdpElement(Integer id,OnmsNode node, String globalDeviceId) {
        return new 
                CdpElementTopologyEntity(id, globalDeviceId, node.getId());
    }

    private CdpLinkTopologyEntity createCdpLink(int id, OnmsNode node, String cdpCacheDeviceId, String cdpInterfaceName,
                                                              String cdpCacheDevicePort) {
        CdpLinkTopologyEntity link = new CdpLinkTopologyEntity(id, node.getId(), 123, cdpInterfaceName,
                "cdpCacheAddress", cdpCacheDeviceId, cdpCacheDevicePort);
        return link;
    }

}