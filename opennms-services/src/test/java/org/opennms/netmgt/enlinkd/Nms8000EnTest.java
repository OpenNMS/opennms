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

package org.opennms.netmgt.enlinkd;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR1_SNMP_RESOURCE;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR2_SNMP_RESOURCE;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR3_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR3_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR3_SNMP_RESOURCE;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW1_SNMP_RESOURCE;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW2_SNMP_RESOURCE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.nb.Nms8000NetworkBuilder;

public class Nms8000EnTest extends EnLinkdTestBuilder {
        
	Nms8000NetworkBuilder builder = new Nms8000NetworkBuilder();        
    /* 
     * nmmr1 GigabitEthernet0/0                 ---> nmmr3   GigabitEthernet0/1
     * nmmr1 GigabitEthernet0/1                 ---> nmmsw1  FastEthernet0/1
     * nmmr1 GigabitEthernet0/2                 ---> nmmsw2  FastEthernet0/2
     * 
     * nmmr2 GigabitEthernet0/0                 ---> nmmr3   GigabitEthernet0/2
     * nmmr2 GigabitEthernet0/1                 ---> nmmsw2  FastEthernet0/1
     * nmmr2 GigabitEthernet0/2                 ---> nmmsw1  FastEthernet0/2
     * 
     * nmmr3 GigabitEthernet0/0                 ---> netlabSW03   GigabitEthernet2/0/18
     * nmmr3 GigabitEthernet0/1                 ---> nmmr1  GigabitEthernet0/0
     * nmmr3 GigabitEthernet0/2                 ---> nmmr2  GigabitEthernet0/0
     * 
     * nmmsw1  FastEthernet0/1                  ---> nmmr1 GigabitEthernet0/1
     * nmmsw1  FastEthernet0/2                  ---> nmmr2 GigabitEthernet0/2
     * 
     * nmmsw2  FastEthernet0/1                  ---> nmmr2 GigabitEthernet0/1
     * nmmsw2  FastEthernet0/2                  ---> nmmr1 GigabitEthernet0/2
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=NMMR1_IP, port=161, resource=NMMR1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=NMMR2_IP, port=161, resource=NMMR2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=NMMR3_IP, port=161, resource=NMMR3_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=NMMSW1_IP, port=161, resource=NMMSW1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=NMMSW2_IP, port=161, resource=NMMSW2_SNMP_RESOURCE)
    })
    public void testCdpLinks() throws Exception {
        m_nodeDao.save(builder.getNMMR1());
        m_nodeDao.save(builder.getNMMR2());
        m_nodeDao.save(builder.getNMMR3());
        m_nodeDao.save(builder.getNMMSW1());
        m_nodeDao.save(builder.getNMMSW2());

        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(true);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        final OnmsNode nmmr1 = m_nodeDao.findByForeignId("linkd", NMMR1_NAME);
        final OnmsNode nmmr2 = m_nodeDao.findByForeignId("linkd", NMMR2_NAME);
        final OnmsNode nmmr3 = m_nodeDao.findByForeignId("linkd", NMMR3_NAME);
        final OnmsNode nmmsw1 = m_nodeDao.findByForeignId("linkd",NMMSW1_NAME);
        final OnmsNode nmmsw2 = m_nodeDao.findByForeignId("linkd",NMMSW2_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(nmmr1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(nmmr2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(nmmr3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(nmmsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(nmmsw2.getId()));
        

        assertTrue(m_linkd.runSingleSnmpCollection(nmmr1.getId()));
        assertEquals(3, m_cdpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(nmmr2.getId()));
        assertEquals(6, m_cdpLinkDao.countAll());
       
        assertTrue(m_linkd.runSingleSnmpCollection(nmmr3.getId()));
        assertEquals(9, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(nmmsw1.getId()));
        assertEquals(11, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(nmmsw2.getId()));
        assertEquals(13, m_cdpLinkDao.countAll());

        for (final OnmsNode node: m_nodeDao.findAll()) {
            assertNotNull(node.getCdpElement());
            printCdpElement(node.getCdpElement());
        }
        
        for (CdpLink link: m_cdpLinkDao.findAll()) {
            printCdpLink(link);
            assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
        }
        
        assertEquals(6, m_cdpLinkDao.findLinksForTopology().size());

    }
}
