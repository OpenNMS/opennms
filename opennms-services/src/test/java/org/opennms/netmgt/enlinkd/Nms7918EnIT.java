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
import static org.junit.Assert.assertTrue;
//import static org.opennms.netmgt.nb.NmsNetworkBuilder.PE01_IP;
//import static org.opennms.netmgt.nb.NmsNetworkBuilder.PE01_NAME;
//import static org.opennms.netmgt.nb.NmsNetworkBuilder.PE01_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ASW01_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ASW01_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ASW01_SNMP_RESOURCE;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.BridgeMacLink;
//import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
//import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.OnmsNode;
//import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;
import org.opennms.netmgt.nb.Nms7918NetworkBuilder;

public class Nms7918EnIT extends EnLinkdBuilderITCase {

	Nms7918NetworkBuilder builder = new Nms7918NetworkBuilder();
    @Before
    public void setUpNetwork4930() throws Exception {
    	builder.setNodeDao(m_nodeDao);
        builder.setIpNetToMediaDao(m_ipNetToMediaDao);
        builder.buildNetwork7918();
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
            printBridgeMacLink(link);
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
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(40,m_bridgeMacLinkDao.countAll());
        
        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            printStoredBridgeMacLink(link);
        }
        
        
        
        assertTrue(m_linkd.runSingleSnmpCollection(asw01.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(40,m_bridgeMacLinkDao.countAll());


        Thread.sleep(5000);
        
        assertTrue(m_linkd.runTopologyDiscovery(asw01.getId()));
        
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(40,m_bridgeMacLinkDao.countAll());

        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            printStoredBridgeMacLink(link);
        }

        
    }

}
