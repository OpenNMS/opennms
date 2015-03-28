/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.nb.NmsNetworkBuilder.MAC1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MAC1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MAC1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MAC2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MAC2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MAC2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MIKROTIK_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MIKROTIK_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MIKROTIK_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SAMSUNG_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SAMSUNG_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SAMSUNG_SNMP_RESOURCE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Filter;
import org.opennms.netmgt.config.linkd.IncludeRange;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms102NetworkBuilder;

public class Nms102Test extends LinkdTestBuilder {
	    
	Nms102NetworkBuilder builder = new Nms102NetworkBuilder();

    /*
     *  Discover the following topology
     * 
     *                     mikrotik
     *                         |
     *  ----------------------wifi-----------------
     *    |      |        |            |
     *  mac1    mac2    samsung     mobile
     *  
     */
    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=MIKROTIK_IP, port=161, resource=MIKROTIK_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=SAMSUNG_IP, port=161, resource=SAMSUNG_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=MAC1_IP, port=161, resource=MAC1_SNMP_RESOURCE),
        @JUnitSnmpAgent(host=MAC2_IP, port=161, resource=MAC2_SNMP_RESOURCE)
    })
    public void testWifiLinksWithExclusiveConf() throws Exception {

    	m_nodeDao.save(builder.getMac1());
        m_nodeDao.save(builder.getMac2());
        m_nodeDao.save(builder.getMikrotik());
        m_nodeDao.save(builder.getSamsung());
        m_nodeDao.save(builder.getNodeWithoutSnmp("mobile", "192.168.0.13"));
    	m_nodeDao.flush();
    	
        final OnmsNode mac1 = m_nodeDao.findByForeignId("linkd", MAC1_NAME);
        final OnmsNode mac2 = m_nodeDao.findByForeignId("linkd", MAC2_NAME);
        final OnmsNode samsung = m_nodeDao.findByForeignId("linkd", SAMSUNG_NAME);
        final OnmsNode mikrotik = m_nodeDao.findByForeignId("linkd", MIKROTIK_NAME);
        final OnmsNode mobile = m_nodeDao.findByForeignId("linkd", "mobile");

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseIsisDiscovery(false);
        example1.setUseCdpDiscovery(false);

        assertTrue(m_linkd.scheduleNodeCollection(mac1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mac2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samsung.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mikrotik.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(mobile.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(mac1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mac2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(samsung.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mikrotik.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 4 data links", 4, ifaces.size());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MIKROTIK_IP, port=161, resource=MIKROTIK_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SAMSUNG_IP, port=161, resource=SAMSUNG_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MAC1_IP, port=161, resource=MAC1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MAC2_IP, port=161, resource=MAC2_SNMP_RESOURCE)
    })
    public void testWifiLinksWithDefaultConf() throws Exception {

        m_nodeDao.save(builder.getMac1());
        m_nodeDao.save(builder.getMac2());
        m_nodeDao.save(builder.getMikrotik());
        m_nodeDao.save(builder.getSamsung());
        m_nodeDao.save(builder.getNodeWithoutSnmp("mobile", "192.168.0.13"));
        m_nodeDao.flush();
        
        final OnmsNode mac1 = m_nodeDao.findByForeignId("linkd", MAC1_NAME);
        final OnmsNode mac2 = m_nodeDao.findByForeignId("linkd", MAC2_NAME);
        final OnmsNode samsung = m_nodeDao.findByForeignId("linkd", SAMSUNG_NAME);
        final OnmsNode mikrotik = m_nodeDao.findByForeignId("linkd", MIKROTIK_NAME);
        final OnmsNode mobile = m_nodeDao.findByForeignId("linkd", "mobile");

        assertTrue(m_linkd.scheduleNodeCollection(mac1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mac2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samsung.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mikrotik.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(mobile.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(mac1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mac2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(samsung.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mikrotik.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 4 data links", 4, ifaces.size());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MIKROTIK_IP, port=161, resource=MIKROTIK_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SAMSUNG_IP, port=161, resource=SAMSUNG_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MAC1_IP, port=161, resource=MAC1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MAC2_IP, port=161, resource=MAC2_SNMP_RESOURCE)
    })
    public void testLinksWithIpRoute() throws Exception {

        m_nodeDao.save(builder.getMac1());
        m_nodeDao.save(builder.getMac2());
        m_nodeDao.save(builder.getMikrotik());
        m_nodeDao.save(builder.getSamsung());
        m_nodeDao.save(builder.getNodeWithoutSnmp("mobile", "192.168.0.13"));
        m_nodeDao.flush();
        
        final OnmsNode mac1 = m_nodeDao.findByForeignId("linkd", MAC1_NAME);
        final OnmsNode mac2 = m_nodeDao.findByForeignId("linkd", MAC2_NAME);
        final OnmsNode samsung = m_nodeDao.findByForeignId("linkd", SAMSUNG_NAME);
        final OnmsNode mikrotik = m_nodeDao.findByForeignId("linkd", MIKROTIK_NAME);
        final OnmsNode mobile = m_nodeDao.findByForeignId("linkd", "mobile");

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIpRouteDiscovery(true);
        example1.setForceIpRouteDiscoveryOnEthernet(true);

        assertTrue(m_linkd.scheduleNodeCollection(mac1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mac2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samsung.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mikrotik.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(mobile.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(mac1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mac2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(samsung.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mikrotik.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 7 data links", 7, ifaces.size());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MIKROTIK_IP, port=161, resource=MIKROTIK_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SAMSUNG_IP, port=161, resource=SAMSUNG_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MAC1_IP, port=161, resource=MAC1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=MAC2_IP, port=161, resource=MAC2_SNMP_RESOURCE)
    })
    public void testLinksTwoPackage() throws Exception {

        m_nodeDao.save(builder.getMac1());
        m_nodeDao.save(builder.getMac2());
        m_nodeDao.save(builder.getMikrotik());
        m_nodeDao.save(builder.getSamsung());
        m_nodeDao.save(builder.getNodeWithoutSnmp("mobile", "192.168.0.13"));
        m_nodeDao.flush();
        
        final OnmsNode mac1 = m_nodeDao.findByForeignId("linkd", MAC1_NAME);
        final OnmsNode mac2 = m_nodeDao.findByForeignId("linkd", MAC2_NAME);
        final OnmsNode samsung = m_nodeDao.findByForeignId("linkd", SAMSUNG_NAME);
        final OnmsNode mikrotik = m_nodeDao.findByForeignId("linkd", MIKROTIK_NAME);
        final OnmsNode mobile = m_nodeDao.findByForeignId("linkd", "mobile");

        
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseIsisDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(true);
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        example1.setUseWifiDiscovery(false);
        
        Package example2 = new Package();
        example2.setName("example2");
        Filter filter = new Filter();
        filter.setContent("IPADDR != '0.0.0.0'");
        example2.setFilter(filter);
        IncludeRange range = new IncludeRange();
        range.setBegin("1.1.1.1");
        range.setEnd("255.255.255.255");
        example2.addIncludeRange(range);
        example2.setUseBridgeDiscovery(false);
        example2.setSaveStpInterfaceTable(false);
        example2.setSaveStpNodeTable(false);
        example2.setUseIpRouteDiscovery(false);
        example2.setSaveRouteTable(false);
        example2.setEnableVlanDiscovery(false);
        example2.setUseOspfDiscovery(false);
        example2.setUseLldpDiscovery(false);
        example2.setUseIsisDiscovery(false);
        example2.setUseCdpDiscovery(false);
        example2.setUseIpRouteDiscovery(false);
        example2.setUseWifiDiscovery(true);
        m_linkdConfig.getConfiguration().addPackage(example2);
        
        assertTrue(m_linkd.scheduleNodeCollection(mac1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mac2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samsung.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mikrotik.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(mobile.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(mac1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mac2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(samsung.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mikrotik.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 3 data links", 3, ifaces.size());

        assertTrue(m_linkd.runSingleLinkDiscovery("example2"));
        
        final List<DataLinkInterface> ifacesAll = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifacesAll) {
            printLink(link);
        }

        assertEquals("we should have found 7 data links", 7, ifacesAll.size());

    }

}
