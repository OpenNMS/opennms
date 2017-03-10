/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
//import static org.opennms.netmgt.nb.NmsNetworkBuilder.PE01_IP;
//import static org.opennms.netmgt.nb.NmsNetworkBuilder.PE01_NAME;
//import static org.opennms.netmgt.nb.NmsNetworkBuilder.PE01_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ASW01_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ASW01_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ASW01_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SAMASW01_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SAMASW01_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SAMASW01_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.STCASW01_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.STCASW01_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.STCASW01_SNMP_RESOURCE;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms7918NetworkBuilder;

public class Nms7918EnIT extends EnLinkdBuilderITCase {

	//mac address found on asw01 port 1
    private static String[] asw01port1bft = {
    	"00131971d480", "001319bdb440", "000c295cde87", "000c29f49b80", "000a5e540ee6"
        };
    
    // mac address found on asw01 port 3
    private static String[] asw01port3bft = {
    	"001763010d4f"
        };
    
    // mac address found on asw01 port 4
    private static String[] asw01port4bft = {
    	"4c5e0c891d93", "000c42f213af", "000c427bfee3", "00176301050f"
        };
    
    // mac address found on sam port 23
    private static String[] samport23bft = {
    	"0025454ac907"
    };
  
    // mac address found on stc port 19
    private static String[] stcport19bft = {
    	"4c00822458d2"
    };
    
    // mac address found on stc port 24
    private static String[] stcport24bft = {
    	"000e83f6120a"
    };

    // mac address found on asw01 port 2 but not on  stc port 11 and sam port 3
    private static String[] asw01port2bft = {
    	"0012cf68f800", "0012cf3f4ee0"
    };

    // mac addresses found only on sam port 3
    private static String[] samport3bft = {
    };
    
    // mac addresses found only on stc port 11
    private static String[] stcport11bft = {
    	"0003ea017579"  
    };
    
    // mac address found on asw01 port 2 and stc port 11 but not on sam port 3
    private static String[] stcasw01shared = {
    	"001763010792"
    };
    
    // mac address found on sam port 3 and stc port 11 but not on asw01 port 2
    private static String[] stcsamshared = {
    	"00e0b1bd2f5f", "00e0b1bd2f5c"
    };


    // mac address found on asw01 port 2 and sam port 3 but not on stc port 11
    private static String[] samasw01shared = {
    	"00e0b1bd265e", "00e0b1bd2652", "001d71d5e4e7"
    };
    
    
    // mac address found on asw01 port 2 and sam port 3 and stc port 11
    private static String[] allshared = {
    	"000c42f5d30a", "001d454777dc", "d4ca6ded84ce", "0022557fd894", 
    	"0021a4357254", "d4ca6dedd059", "c4641393f352", "d4ca6d954b3b", 
    	"d4ca6d88234f", "0012cf68f80f", "d4ca6ded84d6", "000c42ef1df6", 
    	"d4ca6d69c484", "d4ca6d954aed", "d4ca6df7f801", "000c429e3f3d", 
    	"4c5e0c246b08", "4c5e0c841245", "d4ca6da2d626", "d4ca6ded84c8",
    	"000c42db4e11" 
    };

    Nms7918NetworkBuilder builder = new Nms7918NetworkBuilder();
    
    @Before
    public void setUpNetwork4930() throws Exception {
    	builder.setNodeDao(m_nodeDao);
        builder.setIpNetToMediaDao(m_ipNetToMediaDao);
        builder.buildNetwork7918();
    }
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=STCASW01_IP, port=161, resource=STCASW01_SNMP_RESOURCE)
    })
    public void testNms7918STCASW01BftCollection() throws Exception {
        final OnmsNode stcasw01 = m_nodeDao.findByForeignId("linkd", STCASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(stcasw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(stcasw01.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        List<BridgeMacLink> links  = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(stcasw01.getId());
        
        assertEquals(34, links.size());
        for (BridgeMacLink link: links) {
            System.err.println(link.printTopology());
        }

        assertTrue(m_linkd.runTopologyDiscovery(stcasw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=STCASW01_IP, port=161, resource=STCASW01_SNMP_RESOURCE)
    })
    public void testNms7918STCASW01Bft() throws Exception {
        final OnmsNode stcasw01 = m_nodeDao.findByForeignId("linkd", STCASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(stcasw01.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(stcasw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(stcasw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runTopologyDiscovery(stcasw01.getId()));

        assertNull(m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(stcasw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(34,m_bridgeMacLinkDao.countAll());
        
        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            System.err.println(link.printTopology());
        }
        
        
        
        assertTrue(m_linkd.runSingleSnmpCollection(stcasw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(34,m_bridgeMacLinkDao.countAll());


        Thread.sleep(5000);
        
        assertTrue(m_linkd.runTopologyDiscovery(stcasw01.getId()));
        
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(34,m_bridgeMacLinkDao.countAll());

        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            System.err.println(link.printTopology());
        }

        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SAMASW01_IP, port=161, resource=SAMASW01_SNMP_RESOURCE)
    })
    public void testNms7918SAMASW01BftCollection() throws Exception {
        final OnmsNode samasw01 = m_nodeDao.findByForeignId("linkd", SAMASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(samasw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(samasw01.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        List<BridgeMacLink> links  = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(samasw01.getId());
        
        assertEquals(31, links.size());
        for (BridgeMacLink link: links) {
            System.err.println(link.printTopology());
        }

        assertTrue(m_linkd.runTopologyDiscovery(samasw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SAMASW01_IP, port=161, resource=SAMASW01_SNMP_RESOURCE)
    })
    public void testNms7918SAMASW01Bft() throws Exception {
        final OnmsNode samasw01 = m_nodeDao.findByForeignId("linkd", SAMASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(samasw01.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(samasw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(samasw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runTopologyDiscovery(samasw01.getId()));

        assertNull(m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(samasw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(31,m_bridgeMacLinkDao.countAll());
        
        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            System.err.println(link.printTopology());
        }
        
        
        
        assertTrue(m_linkd.runSingleSnmpCollection(samasw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(31,m_bridgeMacLinkDao.countAll());


        Thread.sleep(5000);
        
        assertTrue(m_linkd.runTopologyDiscovery(samasw01.getId()));
        
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(31,m_bridgeMacLinkDao.countAll());

        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            System.err.println(link.printTopology());
        }

        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ASW01_IP, port=161, resource=ASW01_SNMP_RESOURCE)
    })
    public void testNms7918ASW01BftCollection() throws Exception {
        final OnmsNode asw01 = m_nodeDao.findByForeignId("linkd", ASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(asw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(asw01.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        List<BridgeMacLink> links  = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(asw01.getId());
        
        assertEquals(40, links.size());;
        for (BridgeMacLink link: links) {
            System.err.println(link.printTopology());
        }

        assertTrue(m_linkd.runTopologyDiscovery(asw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
    }
    
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ASW01_IP, port=161, resource=ASW01_SNMP_RESOURCE)
    })
    public void testNms7918ASW01Bft() throws Exception {
        final OnmsNode asw01 = m_nodeDao.findByForeignId("linkd", ASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(asw01.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(asw01.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(asw01.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runTopologyDiscovery(asw01.getId()));

        assertNull(m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(asw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(40,m_bridgeMacLinkDao.countAll());
        
        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            System.err.println(link.printTopology());
        }
        
        
        
        assertTrue(m_linkd.runSingleSnmpCollection(asw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(40,m_bridgeMacLinkDao.countAll());


        Thread.sleep(5000);
        
        assertTrue(m_linkd.runTopologyDiscovery(asw01.getId()));
        
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(40,m_bridgeMacLinkDao.countAll());

        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            System.err.println(link.printTopology());
        }

        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ASW01_IP, port=161, resource=ASW01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SAMASW01_IP, port=161, resource=SAMASW01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=STCASW01_IP, port=161, resource=STCASW01_SNMP_RESOURCE)
    })
    public void testNms7918() throws Exception {
        final OnmsNode stcasw01 = m_nodeDao.findByForeignId("linkd", STCASW01_NAME);
        final OnmsNode samasw01 = m_nodeDao.findByForeignId("linkd", SAMASW01_NAME);
        final OnmsNode asw01 = m_nodeDao.findByForeignId("linkd", ASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setMax_bft(3);
        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(asw01.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samasw01.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(stcasw01.getId()));

        assertEquals(0,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(asw01.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(samasw01.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(stcasw01.getId()));
        assertEquals(3,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runTopologyDiscovery(asw01.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(samasw01.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(stcasw01.getId()));
        
        checkTopology(asw01,stcasw01,samasw01);
        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ASW01_IP, port=161, resource=ASW01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SAMASW01_IP, port=161, resource=SAMASW01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=STCASW01_IP, port=161, resource=STCASW01_SNMP_RESOURCE)
    })
    public void testNms7918TwoSteps() throws Exception {
        final OnmsNode stcasw01 = m_nodeDao.findByForeignId("linkd", STCASW01_NAME);
        final OnmsNode samasw01 = m_nodeDao.findByForeignId("linkd", SAMASW01_NAME);
        final OnmsNode asw01 = m_nodeDao.findByForeignId("linkd", ASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setMax_bft(2);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(asw01.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samasw01.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(stcasw01.getId()));

        assertEquals(0,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(asw01.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(samasw01.getId()));
        assertEquals(2,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        assertTrue(m_linkd.runTopologyDiscovery(asw01.getId()));
        checkAsw01SamAsw01Topology(asw01, samasw01);

        assertTrue(m_linkd.runTopologyDiscovery(samasw01.getId()));
        checkAsw01SamAsw01Topology(asw01, samasw01);

        assertTrue(m_linkd.runSingleSnmpCollection(stcasw01.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(stcasw01.getId()));
        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ASW01_IP, port=161, resource=ASW01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SAMASW01_IP, port=161, resource=SAMASW01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=STCASW01_IP, port=161, resource=STCASW01_SNMP_RESOURCE)
    })
    public void testNms7918ThreeSteps() throws Exception {
        final OnmsNode stcasw01 = m_nodeDao.findByForeignId("linkd", STCASW01_NAME);
        final OnmsNode samasw01 = m_nodeDao.findByForeignId("linkd", SAMASW01_NAME);
        final OnmsNode asw01 = m_nodeDao.findByForeignId("linkd", ASW01_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(asw01.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samasw01.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(stcasw01.getId()));

        assertEquals(0,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(asw01.getId()));
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runTopologyDiscovery(asw01.getId()));
        checkAsw01Topology(asw01);

        assertTrue(m_linkd.runSingleSnmpCollection(samasw01.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(samasw01.getId()));
        checkAsw01SamAsw01Topology(asw01, samasw01);

        assertTrue(m_linkd.runSingleSnmpCollection(stcasw01.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(stcasw01.getId()));
        checkTopology(asw01, stcasw01, samasw01);

    }

    private void checkAsw01Topology(OnmsNode  asw01) {
        assertEquals(1,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        //the final size of bridgemaclink is 
        // 40 =
        //+ 5 = macs learned on port 1 of asw01
        //+ 1 = mac learned on port 3 of asw01
        //+ 4 = mac learned on port 4 of asw01
        //+ 30 = mac learned on port 2 of asw01
        assertEquals(40,m_bridgeMacLinkDao.countAll());

        for (String mac: asw01port1bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(1, link.getBridgePort().intValue());
            assertEquals(1001, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        // 1 
        for (String mac: asw01port2bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(2, link.getBridgePort().intValue());
            assertEquals(1002, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }

        for (String mac: asw01port3bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(3, link.getBridgePort().intValue());
            assertEquals(1003, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: asw01port4bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(4, link.getBridgePort().intValue());
            assertEquals(1004, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: samport3bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        // 1
        for (String mac: samport23bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(2, link.getBridgePort().intValue());
            assertEquals(1002, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        for (String mac: stcport11bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }
        // 1
        for (String mac: stcport19bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(2, link.getBridgePort().intValue());
            assertEquals(1002, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        //1
        for (String mac: stcport24bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(2, link.getBridgePort().intValue());
            assertEquals(1002, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        for (String mac: stcsamshared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }
        //3
        for (String mac: samasw01shared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(2, link.getBridgePort().intValue());
            assertEquals(1002, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        //1
        for (String mac: stcasw01shared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(2, link.getBridgePort().intValue());
            assertEquals(1002, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        //21
        for (String mac: allshared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(2, link.getBridgePort().intValue());
            assertEquals(1002, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
    }
    
    private void checkAsw01SamAsw01Topology(OnmsNode  asw01,OnmsNode samasw01) {
        assertEquals(2,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(1,m_bridgeBridgeLinkDao.countAll());
        //the final size of bridgemaclink is 
        // 61 =
        // 50 = 21 + 3 + 1  * 2 (21 mac are learned on the common shared, 3 are shared on the port and 1 is the mac address learned on port 19 of stc)  
        //+ 5 = macs learned on port 1 of asw01
        //+ 1 = mac learned on port 3 of asw01
        //+ 4 = mac learned on port 4 of asw01
        //+ 1 = mac learned on port 23 of sam
        assertEquals(61,m_bridgeMacLinkDao.countAll());
        
        for (BridgeBridgeLink bblink : m_bridgeBridgeLinkDao.findAll()) {
            assertNotNull(bblink);
            assertEquals(asw01.getId(), bblink.getDesignatedNode().getId());
            assertEquals(2, bblink.getDesignatedPort().intValue());
            assertEquals(1002, bblink.getDesignatedPortIfIndex().intValue());
		if (samasw01.getId().intValue() ==  bblink.getNode().getId().intValue()) {
                assertEquals(3, bblink.getBridgePort().intValue());
                assertEquals(3, bblink.getBridgePortIfIndex().intValue());
            } else {
                assertTrue(false);
            }
        }

        for (String mac: asw01port1bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(1, link.getBridgePort().intValue());
            assertEquals(1001, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: asw01port2bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }
        
        for (String mac: asw01port3bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(3, link.getBridgePort().intValue());
            assertEquals(1003, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: asw01port4bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(4, link.getBridgePort().intValue());
            assertEquals(1004, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: samport3bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }


        for (String mac: samport23bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(samasw01.getId(), link.getNode().getId());
            assertEquals(23, link.getBridgePort().intValue());
            assertEquals(23, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }

        for (String mac: stcport11bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: stcport19bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(2, links.size());
            for (BridgeMacLink link:  links) {
                assertEquals(mac, link.getMacAddress());
                if (samasw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(3, link.getBridgePort().intValue());
                    assertEquals(3, link.getBridgePortIfIndex().intValue());
                } else if (asw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(2, link.getBridgePort().intValue());
                    assertEquals(1002, link.getBridgePortIfIndex().intValue());
                } else {
                    assertTrue(false);
                }
            }
        }
        
        for (String mac: stcport24bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: samasw01shared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(2, links.size());
            for (BridgeMacLink link:  links) {
                assertEquals(mac, link.getMacAddress());
                if (samasw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(3, link.getBridgePort().intValue());
                    assertEquals(3, link.getBridgePortIfIndex().intValue());
                } else if (asw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(2, link.getBridgePort().intValue());
                    assertEquals(1002, link.getBridgePortIfIndex().intValue());
                } else {
                    assertTrue(false);
                }
            }
            
        }

        for (String mac: stcasw01shared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: stcsamshared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: allshared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(2, links.size());
            for (BridgeMacLink link:  links) {
                assertEquals(mac, link.getMacAddress());
                if (samasw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(3, link.getBridgePort().intValue());
                    assertEquals(3, link.getBridgePortIfIndex().intValue());
                } else if (asw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(2, link.getBridgePort().intValue());
                    assertEquals(1002, link.getBridgePortIfIndex().intValue());
                } else {
                    assertTrue(false);
                }
            }
        }

    	
    }
    
    private void checkTopology(OnmsNode  asw01, OnmsNode stcasw01, OnmsNode samasw01)    {
        assertEquals(3,m_bridgeElementDao.countAll());
        assertEquals(0,m_bridgeStpLinkDao.countAll());
        assertEquals(2,m_bridgeBridgeLinkDao.countAll());
        //the final size of bridgemaclink is 
        // 76 =
        // 63 = 21 * 3 (21 mac are learned on the common shared and entry for each port, the ports are 3 
        //+ 5 = macs learned on port 1 of asw01
        //+ 1 = mac learned on port 3 of asw01
        //+ 4 = mac learned on port 4 of asw01
        //+ 1 = mac learned on port 23 of sam
        //+ 1 = mac learned on port 19 of stc
        //+ 1 = mac learned on port 24 of stc
        assertEquals(76,m_bridgeMacLinkDao.countAll());


        for (BridgeBridgeLink bblink : m_bridgeBridgeLinkDao.findAll()) {
            assertNotNull(bblink);
            assertEquals(asw01.getId(), bblink.getDesignatedNode().getId());
            assertEquals(2, bblink.getDesignatedPort().intValue());
            assertEquals(1002, bblink.getDesignatedPortIfIndex().intValue());
            if (stcasw01.getId().intValue() ==  bblink.getNode().getId().intValue()) {
                assertEquals(11, bblink.getBridgePort().intValue());
                assertEquals(1011, bblink.getBridgePortIfIndex().intValue());
            } else if (samasw01.getId().intValue() ==  bblink.getNode().getId().intValue()) {
                assertEquals(3, bblink.getBridgePort().intValue());
                assertEquals(3, bblink.getBridgePortIfIndex().intValue());
            } else {
                assertTrue(false);
            }
        }

        for (String mac: asw01port1bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(1, link.getBridgePort().intValue());
            assertEquals(1001, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: asw01port2bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }
        
        for (String mac: asw01port3bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(3, link.getBridgePort().intValue());
            assertEquals(1003, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: asw01port4bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(asw01.getId(), link.getNode().getId());
            assertEquals(4, link.getBridgePort().intValue());
            assertEquals(1004, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: samport3bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }


        for (String mac: samport23bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(samasw01.getId(), link.getNode().getId());
            assertEquals(23, link.getBridgePort().intValue());
            assertEquals(23, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }

        for (String mac: stcport11bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: stcport19bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(stcasw01.getId(), link.getNode().getId());
            assertEquals(19, link.getBridgePort().intValue());
            assertEquals(1019, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }
        
        for (String mac: stcport24bft) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1, links.size());
            BridgeMacLink link = links.iterator().next();
            assertEquals(stcasw01.getId(), link.getNode().getId());
            assertEquals(24, link.getBridgePort().intValue());
            assertEquals(1024, link.getBridgePortIfIndex().intValue());
            assertEquals(mac, link.getMacAddress());
        }


        for (String mac: samasw01shared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: stcasw01shared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: stcsamshared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(0, links.size());
        }

        for (String mac: allshared) {
            List<BridgeMacLink>links = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(3, links.size());
            for (BridgeMacLink link:  links) {
                assertEquals(mac, link.getMacAddress());
                if (stcasw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(11, link.getBridgePort().intValue());
                    assertEquals(1011, link.getBridgePortIfIndex().intValue());
                } else if (samasw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(3, link.getBridgePort().intValue());
                    assertEquals(3, link.getBridgePortIfIndex().intValue());
                } else if (asw01.getId().intValue() == link.getNode().getId()) {
                    assertEquals(2, link.getBridgePort().intValue());
                    assertEquals(1002, link.getBridgePortIfIndex().intValue());
                } else {
                    assertTrue(false);
                }
            }
        }
        
    }



}
