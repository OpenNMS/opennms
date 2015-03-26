/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CHENNAI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CHENNAI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CHENNAI_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_41_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_41_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_41_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SSG550_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SSG550_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SSG550_SNMP_RESOURCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms10205aNetworkBuilder;

public class Nms10205aTest extends LinkdTestBuilder {

	Nms10205aNetworkBuilder builder = new Nms10205aNetworkBuilder();
	/*
     *  The 
     *  MUMBAI:port ge 0/1/3:ip 192.168.5.5   ------> CHENNAI:port ge 4/0/2: ip 192.168.5.6
     *  MUMBAI:port ge 0/1/2:ip 192.168.5.9   ------> DELHI:port ge 1/0/2: ip 192.168.5.10
     *  MUMBAI:port ge 0/0/1:ip 192.168.5.13   ------> BANGALORE:port ge 0/0/0: ip 192.168.5.14
     *  DELHI:port ge 1/0/1:ip 192.168.1.5     ------> BANGALORE:port ge 0/0/1: ip 192.168.1.6
     *  DELHI:port ge 1/1/6:ip 172.16.7.1     ------> Space-EX-SW1: port 0/0/6: ip 172.16.7.1 ???? same ip address
     *  CHENNAI:port ge 4/0/3:ip 192.168.1.1  ------> DELHI: port ge 1/1/0: ip 192.168.1.2
     *  
     *  a lot of duplicated ip this is a clear proof that linkd is not able to 
     *  gather topology of this lab using the useBridgeTopology and ip routes.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource=MUMBAI_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=CHENNAI_IP, port=161, resource=CHENNAI_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource=DELHI_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource=BANGALORE_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource=BAGMANE_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource=MYSORE_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource=SPACE_EX_SW1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource=SPACE_EX_SW2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=J6350_41_IP, port=161, resource=J6350_41_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource=J6350_42_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource=SRX_100_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SSG550_IP, port=161, resource=SSG550_SNMP_RESOURCE)
    }, forceMockStrategy=true)
    public void testNetwork10205Links() throws Exception {
        m_nodeDao.save(builder.getMumbai());
        m_nodeDao.save(builder.getChennai());
        m_nodeDao.save(builder.getDelhi());
        m_nodeDao.save(builder.getBangalore());
        m_nodeDao.save(builder.getBagmane());
        m_nodeDao.save(builder.getMysore());
        m_nodeDao.save(builder.getSpaceExSw1());
        m_nodeDao.save(builder.getSpaceExSw2());
        m_nodeDao.save(builder.getJ635041());
        m_nodeDao.save(builder.getJ635042());
        m_nodeDao.save(builder.getSRX100());
        m_nodeDao.save(builder.getSGG550());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        example1.setUseCdpDiscovery(false);
        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode chennai = m_nodeDao.findByForeignId("linkd", CHENNAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635041 = m_nodeDao.findByForeignId("linkd", J6350_41_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);
        final OnmsNode ssg550 = m_nodeDao.findByForeignId("linkd", SSG550_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(chennai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635041.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ssg550.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(chennai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635041.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ssg550.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        
        assertEquals(20, links.size());
        
        
        // Linkd is able to find partially the topology using the next hop router
        // among the core nodes:
        // mumbai, chennai, delhi, mysore,bangalore and bagmane
        // the link between chennai and delhi is lost 
        // the link between chennai and bagmane is lost
        // the link between bagmane and delhi is lost
        // I checked the walks and no route info
        // is there for discovering the link.
        // I have to guess that linkd is working as expected
        
        // The bridge and RSTP topology information are
        // unusuful, the devices supporting RSTP
        // have themselves as designated bridge.
        
        // Other links are lost...
        // no routing entry and no bridge 
        // forwarding
        
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            
            
            int id = datalinkinterface.getId().intValue();
            // mumbai delhi
            if (start == id ) {
                checkLink(mumbai, delhi, 519, 28503, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+11 == id ) {
                checkLink(delhi, mumbai, 28503, 519, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+1 == id ) {
                checkLink(mumbai, bangalore, 507, 2401, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+12 == id ) {
                checkLink(bangalore, mumbai, 2401, 507, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+2 == id ) {
                checkLink(mumbai, bagmane, 977, 534, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+13 == id ) {
                checkLink(bagmane, mumbai, 534, 977, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+3 == id ) {
                checkLink(mumbai, mysore, 978, 508, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+14 == id ) {
                checkLink(mysore, mumbai, 508, 978, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+4 == id ) {
                checkLink(mumbai, chennai, 520, 528, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+10 == id ) {
                checkLink(chennai, mumbai, 528, 520, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+5 == id ) {
                checkLink(chennai, mysore, 517, 505, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+15 == id ) {
                checkLink(mysore, chennai, 505, 517, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+6 == id ) {
               checkLink(delhi, bangalore, 3674, 2397, datalinkinterface);
               assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+16 == id ) {
                checkLink(bangalore, delhi, 2397, 3674, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+7 == id ) {
                checkLink(bangalore, bagmane, 2396, 1732, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+17 == id ) {
                checkLink(bagmane, bangalore, 1732, 2396, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+8 == id ) {
                checkLink(bagmane, mysore, 654, 520, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+18 == id ) {
                checkLink(mysore, bagmane, 520, 654, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (start+9 == id ) {
                checkLink(spaceexsw2, mumbai, 34, 508, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol());
            } else if (start+19 == id ) {
//                checkLink(spaceexsw1,spaceexsw2, 1361, 501 , datalinkinterface);
            	checkLink(spaceexsw2, spaceexsw1, 523,1361, datalinkinterface);
                assertEquals(DiscoveryProtocol.bridge, datalinkinterface.getProtocol());
            } else {
                assertEquals(-1, 0);
            }

        }
    }
    
    
    /*
     *  
     *  Get only ospf links.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource=MUMBAI_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=CHENNAI_IP, port=161, resource=CHENNAI_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource=DELHI_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource=BANGALORE_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource=BAGMANE_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource=MYSORE_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource=SPACE_EX_SW1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource=SPACE_EX_SW2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=J6350_41_IP, port=161, resource=J6350_41_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource=J6350_42_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource=SRX_100_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SSG550_IP, port=161, resource=SSG550_SNMP_RESOURCE)
    }, forceMockStrategy=true)
    public void testNetwork10205OspfLinks() throws Exception {
        m_nodeDao.save(builder.getMumbai());
        m_nodeDao.save(builder.getChennai());
        m_nodeDao.save(builder.getDelhi());
        m_nodeDao.save(builder.getBangalore());
        m_nodeDao.save(builder.getBagmane());
        m_nodeDao.save(builder.getMysore());
        m_nodeDao.save(builder.getSpaceExSw1());
        m_nodeDao.save(builder.getSpaceExSw2());
        m_nodeDao.save(builder.getJ635041());
        m_nodeDao.save(builder.getJ635042());
        m_nodeDao.save(builder.getSRX100());
        m_nodeDao.save(builder.getSGG550());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseIsisDiscovery(false);
        
        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode chennai = m_nodeDao.findByForeignId("linkd", CHENNAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635041 = m_nodeDao.findByForeignId("linkd", J6350_41_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);
        final OnmsNode ssg550 = m_nodeDao.findByForeignId("linkd", SSG550_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(chennai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635041.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ssg550.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(chennai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635041.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ssg550.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        
        assertEquals(9, links.size());  
        
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int id = datalinkinterface.getId().intValue();
            assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            if (start == id ) {
                checkLink(chennai, mumbai, 528, 520, datalinkinterface);
            } else if (start+1 == id ) {
                checkLink(delhi, mumbai, 28503, 519, datalinkinterface);
            } else if (start+2 == id ) {
                checkLink(bangalore, mumbai, 2401, 507, datalinkinterface);
            } else if (start+3 == id ) {
                checkLink(bagmane, mumbai, 534, 977, datalinkinterface);
            } else if (start+4 == id ) {
                checkLink(mysore, mumbai, 508, 978, datalinkinterface);
            } else if (start+5 == id ) {
                checkLink(mysore, chennai, 505, 517, datalinkinterface);
            } else if (start+6 == id ) {
               checkLink(bangalore, delhi, 2397, 3674, datalinkinterface);
            } else if (start+7 == id ) {
                checkLink(bagmane, bangalore, 1732, 2396, datalinkinterface);
            } else if (start+8 == id ) {
                checkLink(mysore, bagmane, 520, 654, datalinkinterface);
            } else {
                checkLink(mumbai,mumbai,-1,-1,datalinkinterface);
            }
        }

    }

    /*
     *  
     *  Get only bridge links.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource=SPACE_EX_SW1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource=SPACE_EX_SW2_SNMP_RESOURCE)
    }, forceMockStrategy=true)
    public void testNetwork10205BridgeLinks() throws Exception {
        m_nodeDao.save(builder.getSpaceExSw1());
        m_nodeDao.save(builder.getSpaceExSw2());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseIsisDiscovery(false);
        
        example1.setSaveRouteTable(false);
        
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        
        assertEquals(1, links.size());  
        
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int id = datalinkinterface.getId().intValue();
            if (start == id ) {
//            	checkLink(spaceexsw1, spaceexsw2, 1361, 501, datalinkinterface);
            	checkLink(spaceexsw2, spaceexsw1, 523,1361, datalinkinterface);
                assertEquals(DiscoveryProtocol.bridge, datalinkinterface.getProtocol());
             } else {
                checkLink(spaceexsw1,spaceexsw1,-1,-1,datalinkinterface);
            }
        }

    }


}
