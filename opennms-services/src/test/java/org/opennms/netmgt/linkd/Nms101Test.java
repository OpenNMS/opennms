/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO1700B_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO1700B_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO1700B_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO1700_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO1700_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO1700_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO2691_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO2691_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO2691_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO3600_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO3600_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO3600_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO3700_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO3700_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO3700_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO7200A_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO7200A_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO7200A_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO7200B_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO7200B_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO7200B_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LAPTOP_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LAPTOP_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LAPTOP_SNMP_RESOURCE;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.topology.CdpInterface;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.model.topology.RouterInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms101NetworkBuilder;

public class Nms101Test extends LinkdTestBuilder {
	
	Nms101NetworkBuilder builder = new Nms101NetworkBuilder();
    @Before
    public void setUpForceDisvoeryOnEthernet() {
    for (Package pkg : Collections.list(m_linkdConfig.enumeratePackage())) {
            pkg.setForceIpRouteDiscoveryOnEthernet(true);
        }
    }

    /*
     * cisco1700 --- cisco1700b ??????
     * cisco1700b clearly does not have relation with this net...it has the same address
     * of cisco2691......and the link is between cisco1700 and cisco2691
     * what a fake....very interesting test.....
     * CDP now fails....but iproute discovery found a right route information 
     * no way of fixing this in the actual code implementation
     * 
     */
	@Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=CISCO1700B_IP, port=161, resource=CISCO1700B_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO1700_IP, port=161, resource=CISCO1700_SNMP_RESOURCE)
    })
    public void testSimpleFakeConnection() throws Exception {
	m_nodeDao.save(builder.getCisco1700());
	m_nodeDao.save(builder.getCisco1700b());
	m_nodeDao.save(builder.getExampleCom());
        m_nodeDao.flush();

        final OnmsNode cisco1700 = m_nodeDao.findByForeignId("linkd", CISCO1700_NAME);
        final OnmsNode cisco1700b = m_nodeDao.findByForeignId("linkd", CISCO1700B_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(cisco1700.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco1700b.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(cisco1700.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco1700b.getId()));

        for (LinkableNode node: m_linkd.getLinkableNodesOnPackage("example1")) {
        	int nodeid = node.getNodeId();
        	printNode(m_nodeDao.get(nodeid));
        	for (RouterInterface route: node.getRouteInterfaces()) {
        		printRouteInterface(nodeid, route);
        	}
        	
        	for (CdpInterface cdp: node.getCdpInterfaces()) {
        		printCdpInterface(nodeid, cdp);
        	}
        		
        }
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }
        assertEquals("we should have found 1 data link", 1, ifaces.size());
    }

    /*
     *  Discover the following topology
     *  The CDP protocol must found all the links
     *  Either Ip Route must found links
     * 
     *  laptop
     *     |
     *  cisco7200a (2) --- (4) cisco7200b (1) --- (4) cisco2691 (2) --- (2) cisco1700
     *                     (2)                    (1)    
     *                      |                      |
     *                     (1)                    (2)
     *                  cisco3700  (3) --- (1)  cisco3600      
     */	
    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=CISCO7200A_IP, port=161, resource=CISCO7200A_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO7200B_IP, port=161, resource=CISCO7200B_SNMP_RESOURCE)
    })
    public void testsimpleLinkCisco7200aCisco7200b() throws Exception {

    	m_nodeDao.save(builder.getCisco7200a());
    	m_nodeDao.save(builder.getCisco7200b());
    	m_nodeDao.flush();
    	
        final OnmsNode cisco7200a = m_nodeDao.findByForeignId("linkd", CISCO7200A_NAME);
        final OnmsNode cisco7200b = m_nodeDao.findByForeignId("linkd", CISCO7200B_NAME);
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200a.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200b.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(cisco7200a.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco7200b.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            if (link.getProtocol() == DiscoveryProtocol.iproute)
                checkLink(cisco7200a, cisco7200b, 2, 4, link);
            else if (link.getProtocol() ==  DiscoveryProtocol.cdp)
                checkLink(cisco7200b, cisco7200a, 4, 2, link);
        }

        assertEquals("we should have found 2 data links", 2, ifaces.size());
    }

    /*
     *  Discover the following topology
     *  The CDP protocol must found all the links
     *  Either Ip Route must found links
     * 
     *  laptop
     *     |
     *  cisco7200a (2) --- (4) cisco7200b (1) --- (4) cisco2691 (2) --- (2) cisco1700
     *                     (2)                    (1)    
     *                      |                      |
     *                     (1)                    (2)
     *                  cisco3700  (3) --- (1)  cisco3600      
     */	
    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=CISCO7200A_IP, port=161, resource=CISCO7200A_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=LAPTOP_IP, port=161, resource=LAPTOP_SNMP_RESOURCE)
    })
    public void testsimpleLinkCisco7200alaptop() throws Exception {

    	m_nodeDao.save(builder.getCisco7200a());
    	m_nodeDao.save(builder.getLaptop());
    	m_nodeDao.flush();
    	
        final OnmsNode cisco7200a = m_nodeDao.findByForeignId("linkd", CISCO7200A_NAME);
        final OnmsNode laptop = m_nodeDao.findByForeignId("linkd", LAPTOP_NAME);
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200a.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(laptop.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(cisco7200a.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(laptop.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 1 data links", 1, ifaces.size());
    }

    /*
     *  Discover the following topology
     *  The CDP protocol must found all the links
     *  Either Ip Route must found links
     * 
     *  laptop
     *     |
     *  cisco7200a (2) --- (4) cisco7200b (1) --- (4) cisco2691 (2) --- (2) cisco1700
     *                     (2)                    (1)    
     *                      |                      |
     *                     (1)                    (2)
     *                  cisco3700  (3) --- (1)  cisco3600      
     */	
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO3700_IP, port=161, resource=CISCO3700_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=CISCO3600_IP, port=161, resource=CISCO3600_SNMP_RESOURCE)
    })
    public void testsimpleLinkCisco3600aCisco3700() throws Exception {

    	m_nodeDao.save(builder.getCisco3700());
    	m_nodeDao.save(builder.getCisco3600());
    	m_nodeDao.flush();
    	
        final OnmsNode cisco3600 = m_nodeDao.findByForeignId("linkd", CISCO3600_NAME);
        final OnmsNode cisco3700 = m_nodeDao.findByForeignId("linkd", CISCO3700_NAME);
        assertTrue(m_linkd.scheduleNodeCollection(cisco3700.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3600.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(cisco3700.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco3600.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            if (link.getProtocol() == DiscoveryProtocol.iproute)
                checkLink(cisco3600, cisco3700, 1, 3, link);
            else if (link.getProtocol() ==  DiscoveryProtocol.cdp)
                checkLink(cisco3600, cisco3700, 1, 3, link);
        }

        assertEquals("we should have found 2 data links", 2, ifaces.size());
    }


    /*
     *  Discover the following topology
     *  The CDP protocol must found all the links
     *  Either Ip Route must found links
     * 
     *  laptop
     *     |
     *  cisco7200a (2) --- (4) cisco7200b (1) --- (4) cisco2691 (2) --- (2) cisco1700
     *                     (2)                    (1)    
     *                      |                      |
     *                     (1)                    (2)
     *                  cisco3700  (3) --- (1)  cisco3600      
     */	
    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=CISCO7200A_IP, port=161, resource=CISCO7200A_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=LAPTOP_IP, port=161, resource=LAPTOP_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO7200B_IP, port=161, resource=CISCO7200B_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO3700_IP, port=161, resource=CISCO3700_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO2691_IP, port=161, resource=CISCO2691_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO1700_IP, port=161, resource=CISCO1700_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO3600_IP, port=161, resource=CISCO3600_SNMP_RESOURCE)
    })
    public void testCiscoNetwork() throws Exception {

    	m_nodeDao.save(builder.getExampleCom());
    	m_nodeDao.save(builder.getLaptop());
    	m_nodeDao.save(builder.getCisco7200a());
    	m_nodeDao.save(builder.getCisco7200b());
    	m_nodeDao.save(builder.getCisco3700());
    	m_nodeDao.save(builder.getCisco2691());
    	m_nodeDao.save(builder.getCisco1700());
    	m_nodeDao.save(builder.getCisco3600());
    	m_nodeDao.flush();
    	
        final OnmsNode laptop = m_nodeDao.findByForeignId("linkd", LAPTOP_NAME);
        final OnmsNode cisco7200a = m_nodeDao.findByForeignId("linkd", CISCO7200A_NAME);
        final OnmsNode cisco7200b = m_nodeDao.findByForeignId("linkd", CISCO7200B_NAME);
        final OnmsNode cisco3700  = m_nodeDao.findByForeignId("linkd", CISCO3700_NAME);
        final OnmsNode cisco2691  = m_nodeDao.findByForeignId("linkd", CISCO2691_NAME);
        final OnmsNode cisco1700  = m_nodeDao.findByForeignId("linkd", CISCO1700_NAME);
        final OnmsNode cisco3600  = m_nodeDao.findByForeignId("linkd", CISCO3600_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(laptop.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200a.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200b.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3700.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco2691.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco1700.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3600.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(laptop.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco7200a.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco7200b.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco3700.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco2691.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco1700.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco3600.getId()));

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        int start = getStartPoint(links);
        for (final DataLinkInterface link: links) {
            int id = link.getId().intValue();
            if (id == start) {
                checkLink(laptop, cisco7200a, 10, 3, link);
                assertEquals(DiscoveryProtocol.iproute, link.getProtocol());
            } else if (id == start+1) {
                checkLink(cisco7200a, cisco7200b, 2, 4, link);
                assertEquals(DiscoveryProtocol.iproute, link.getProtocol());
            } else if (id == start+10) {
                checkLink(cisco7200b, cisco7200a, 4, 2, link);
                assertEquals(DiscoveryProtocol.cdp, link.getProtocol());
            } else if (id == start+2) {
                checkLink(cisco7200b, cisco2691, 1, 4, link);
                assertEquals(DiscoveryProtocol.iproute, link.getProtocol());
            } else if (id == start+8) {
                checkLink(cisco2691,cisco7200b , 4, 1, link);
                assertEquals(DiscoveryProtocol.cdp, link.getProtocol());
            } else if (id == start+3) {
                checkLink(cisco7200b, cisco3700, 2, 1, link);
                assertEquals(DiscoveryProtocol.iproute, link.getProtocol());
            } else if (id == start+9) {
                checkLink(cisco3700, cisco7200b, 1, 2, link);
                assertEquals(DiscoveryProtocol.cdp, link.getProtocol());
            } else if (id == start+4) {
                checkLink(cisco1700, cisco2691, 2, 2, link);
                assertEquals(DiscoveryProtocol.iproute, link.getProtocol());
            } else if (id == start+7) {
                checkLink(cisco1700, cisco2691, 2, 2, link);
                assertEquals(DiscoveryProtocol.cdp, link.getProtocol());
            } else if (id == start+6) {
                checkLink(cisco3600, cisco2691, 2, 1, link);
                assertEquals(DiscoveryProtocol.cdp, link.getProtocol());
            } else if (id == start+5) {
                checkLink(cisco3600, cisco3700, 1, 3, link);
                assertEquals(DiscoveryProtocol.iproute, link.getProtocol());
            } else if (id == start+11) {
                checkLink(cisco3600, cisco3700, 1, 3, link);
                assertEquals(DiscoveryProtocol.cdp, link.getProtocol());
            } else {
                assertEquals(false, true);
            }
        }

        assertEquals("we should have found 12 data links", 12, links.size());
    }
    
    
    /*
     *  Discover the following topology
     *  The CDP protocol must found all the links
     *  Either Ip Route must found links
     * 
     *  laptop
     *     |
     *  cisco7200a (2) --- (4) cisco7200b (1) --- (4) cisco2691 (2) --- (2) cisco1700
     *                     (2)                    (1)    
     *                      |                      |
     *                     (1)                    (2)
     *                  cisco3700  (3) --- (1)  cisco3600      
     */	
    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=CISCO7200A_IP, port=161, resource=CISCO7200A_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=CISCO7200B_IP, port=161, resource=CISCO7200B_SNMP_RESOURCE)
    })
    public void testsimpleCdpLinkCisco7200aCisco7200b() throws Exception {

        for (Package pkg : Collections.list(m_linkdConfig.enumeratePackage())) {
            pkg.setUseIpRouteDiscovery(false);
            pkg.setUseOspfDiscovery(false);
            pkg.setUseLldpDiscovery(false);
            pkg.setUseBridgeDiscovery(false);
            pkg.setSaveRouteTable(false);
            pkg.setSaveStpNodeTable(false);
            pkg.setSaveStpInterfaceTable(false);
            pkg.setEnableVlanDiscovery(false);
            pkg.setUseIsisDiscovery(false);
        }

    	m_nodeDao.save(builder.getCisco7200a());
    	m_nodeDao.save(builder.getCisco7200b());
    	m_nodeDao.flush();
    	
        final OnmsNode cisco7200a = m_nodeDao.findByForeignId("linkd", CISCO7200A_NAME);
        final OnmsNode cisco7200b = m_nodeDao.findByForeignId("linkd", CISCO7200B_NAME);
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200a.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200b.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(cisco7200a.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(cisco7200b.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 1 data links", 1, ifaces.size());
    }


}
