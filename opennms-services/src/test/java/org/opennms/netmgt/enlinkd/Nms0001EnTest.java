/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.netmgt.nb.TestNetworkBuilder.FROH_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.FROH_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.FROH_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.OEDIPUS_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.OEDIPUS_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.OEDIPUS_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SIEGFRIE_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SIEGFRIE_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SIEGFRIE_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms0001NetworkBuilder;

public class Nms0001EnTest extends EnLinkdTestBuilder {

	Nms0001NetworkBuilder builder = new Nms0001NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = FROH_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = OEDIPUS_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsIsLinks() throws Exception {
        
        m_nodeDao.save(builder.getFroh());
        m_nodeDao.save(builder.getOedipus());
        m_nodeDao.save(builder.getSiegFrie());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        
        assertTrue(m_linkdConfig.useIsisDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode froh = m_nodeDao.findByForeignId("linkd", FROH_NAME);
        final OnmsNode oedipus = m_nodeDao.findByForeignId("linkd", OEDIPUS_NAME);
        final OnmsNode siegfrie = m_nodeDao.findByForeignId("linkd", SIEGFRIE_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(froh.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(oedipus.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(siegfrie.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(froh.getId()));
        assertEquals(2, m_isisLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(oedipus.getId()));
        assertEquals(4, m_isisLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(siegfrie.getId()));
        assertEquals(6, m_isisLinkDao.countAll());

        for (OnmsNode node: m_nodeDao.findAll()) {
        	assertNotNull(node.getIsisElement());
        	System.err.println(node.getIsisElement());
        }
        
        for (IsIsLink link: m_isisLinkDao.findAll())
        	System.err.println(link);
        
        /*
         * 
         * These are the links among the following nodes discovered using 
         * only the isis protocol
         * froh:ae1.0(599):10.1.3.6/30       <-->    oedipus:ae1.0(578):10.1.3.5/30
         * froh:ae2.0(600):10.1.3.2/30       <-->    siegfrie:ae2.0(552):10.1.3.1/30
         * oedipus:ae0.0(575):10.1.0.10/30   <-->    siegfrie:ae0.0(533):10.1.0.9/30
         * 
         */
    }
}
