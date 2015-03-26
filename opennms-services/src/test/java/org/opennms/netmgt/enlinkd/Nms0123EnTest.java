/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.nb.NmsNetworkBuilder.ITPN0111_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ITPN0111_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ITPN0111_SNMP_RESOURCE;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.ITPN0112_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ITPN0112_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ITPN0112_SNMP_RESOURCE;

import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms0123NetworkBuilder;

public class Nms0123EnTest extends EnLinkdTestBuilder {

    Nms0123NetworkBuilder builder = new Nms0123NetworkBuilder();
    /*
     *   
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = ITPN0111_IP, port = 161, resource = ITPN0111_SNMP_RESOURCE)
    })
    public void testItpn0111Lldp() throws Exception {
        
        m_nodeDao.save(builder.getItpn0111());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        final OnmsNode itpn0111 = m_nodeDao.findByForeignId("linkd", ITPN0111_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(itpn0111.getId()));

        assertEquals(0,m_lldpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(itpn0111.getId()));
        
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
        }
        final List<LldpLink> topologyA = m_lldpLinkDao.findAll();
        assertEquals(4,topologyA.size());
        printLldpTopology(topologyA);
       
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = ITPN0112_IP, port = 161, resource = ITPN0112_SNMP_RESOURCE)
    })
    public void testItpn0112Lldp() throws Exception {
        
        m_nodeDao.save(builder.getItpn0112());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        final OnmsNode itpn0112 = m_nodeDao.findByForeignId("linkd", ITPN0112_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(itpn0112.getId()));

        assertEquals(0,m_lldpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(itpn0112.getId()));
        
        for (final OnmsNode node: m_nodeDao.findAll()) {
                if (node.getLldpElement() != null)
                        printLldpElement(node.getLldpElement());
        }
        final List<LldpLink> topologyA = m_lldpLinkDao.findAll();
        assertEquals(2,topologyA.size());
        printLldpTopology(topologyA);
       
    }
     

}
