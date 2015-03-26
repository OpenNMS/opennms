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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms4930NetworkBuilder;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_SNMP_RESOURCE;

public class Nms4930EnTest extends EnLinkdTestBuilder {

	Nms4930NetworkBuilder builder = new Nms4930NetworkBuilder();
    String[] macsonbbport = { 
            "001e58a6aed7", "00265abd0b08", "1caff72905d8", "1caff702cffd", "00e0d8107c0c", "001562cae2cf", "001cf0d18441", "001e58a31b47"
    };
    
    @Before
    public void setUpNetwork4930() throws Exception {
    	builder.setNodeDao(m_nodeDao);
        builder.buildNetwork4930();
    }

    /*
     * The main fact is that this devices have only the Bridge MIb walk
     * dlink_DES has STP disabled
     * dlink_DGS has STP enabled but root is itself
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testNms4930Network() throws Exception {

        
    	final OnmsNode dlink1 = m_nodeDao.findByForeignId("linkd", DLINK1_NAME);
        final OnmsNode dlink2 = m_nodeDao.findByForeignId("linkd", DLINK2_NAME);
        m_nodeDao.flush();

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

        assertTrue(m_linkd.scheduleNodeCollection(dlink1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(dlink2.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(58,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(659,m_bridgeMacLinkDao.countAll());
        // Matt here you find that the macs on backbone port have all the same
        // switch port
        // the array "macsonbbport" is the intersection between 
        // the mac address forwarding table of dlink1 port 24
        // and the mac address forwarding table f dlink2 port 10
        // The following code will print the links as they are discovered
        for (String mac: macsonbbport) {
        	List<BridgeMacLink> maclinks = m_bridgeMacLinkDao.findByMacAddress(mac);
        	assertEquals(2,maclinks.size());
    		printBackboneBridgeMacLink(maclinks.get(0),maclinks.get(1));
        }

        // Test the bridge link second query
        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testNms4930NetworkReverse() throws Exception {

          
    	final OnmsNode dlink1 = m_nodeDao.findByForeignId("linkd", DLINK1_NAME);
        final OnmsNode dlink2 = m_nodeDao.findByForeignId("linkd", DLINK2_NAME);

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

        assertTrue(m_linkd.scheduleNodeCollection(dlink2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(dlink1.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(977,m_bridgeMacLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(659,m_bridgeMacLinkDao.countAll());
        
        for (String mac: macsonbbport) {
        	List<BridgeMacLink> maclinks = m_bridgeMacLinkDao.findByMacAddress(mac);
        	assertEquals(2,maclinks.size());
        	for (BridgeMacLink maclink: maclinks) {
        		printBridgeMacLink(maclink);
        	}
        }
        
        BridgeMacLink mac1 = m_bridgeMacLinkDao.getByNodeIdBridgePortMac(dlink1.getId(), 1, "64168dfa8d49");
        assertNotNull(mac1);
        assertNotNull(mac1.getBridgePortIfIndex());
        assertEquals(1, mac1.getBridgePortIfIndex().intValue());
    }

}
