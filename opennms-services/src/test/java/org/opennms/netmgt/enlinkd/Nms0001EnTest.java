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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FROH_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FROH_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FROH_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.OEDIPUS_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.OEDIPUS_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.OEDIPUS_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SIEGFRIE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SIEGFRIE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SIEGFRIE_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms0001NetworkBuilder;

import java.util.List;

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

        List<Object[]> links = m_isisLinkDao.getLinksForTopology();
        assertEquals(3, links.size());

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
         *     froh:ae1.0(599):10.1.3.6/30         
         *     froh:ae2.0(600):10.1.3.2/30           
         *  oedipus:ae0.0(575):10.1.0.10/30       
         *  oedipus:ae1.0(578):10.1.3.5/30
         * siegfrie:ae2.0(552):10.1.3.1/30
         * siegfrie:ae0.0(533):10.1.0.9/30
         * 
         * siegfrie:0001 10.25.50.54:533    ---->  0001 10.25.50.62:00 1F 12 AC CB F0:0
         * siegfrie:0001 10.25.50.54:552    ---->  0001 10.08.85.00:00 21 59 0E 47 C2:0
         * 
         *     froh:0001 10.08.85.00:599    ---->  0001 10.25.50.62:00 1F 12 AC CB F1:0 
         *     froh:0001 10.08.85.00:600    ---->  0001 10.25.50.54:00 1F 12 AC C3 F2:0
         * 
         *  oedipus:0001 10.25.50.62:575     ----> 0001 10.25.50.54:00 1F 12 AC C3 F0:0   
         *  oedipus:0001 10.25.50.62:578     ----> 0001 10.08.85.00:00 21 59 0E 47 C1:0
         * 
         * The problem is that the association with Address is into another mib
         * 
         * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.6.1.1.4."599".1 = Hex-STRING: 00 1F 12 AC CB F1 
         * 
         * routing table for ip address                                      "ip route"  "mask" "level"      "next hop Snpa"
         * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13."1.1.4"."10.1.0.4"   ."30"    .1    = Hex-STRING: 00 1F 12 AC CB F1 
         * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13."1.1.4"."10.1.0.8"   ."30"    .1    = Hex-STRING: 00 1F 12 AC CB F1 
         * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13."1.1.4"."10.255.0.62"."32     .1    = Hex-STRING: 00 1F 12 AC CB F1 
         * 
         * 
         * oedipus-192.168.239.62-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.576 = Hex-STRING: 00 1F 12 AC CB F1 
         * oedipus-192.168.239.62-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.578 = Hex-STRING: 00 1F 12 AC CB F1
         * 
         * 
         *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.6.1.1.4."575".1 = Hex-STRING: 00 1F 12 AC C3 F0 
         *  
         *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13.1.1.4.10.1.0.0.30.1 = Hex-STRING: 00 1F 12 AC C3 F0 
         *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13.1.1.4.10.1.3.0.30.1 = Hex-STRING: 00 1F 12 AC C3 F0 
         *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13.1.1.4.10.255.0.54.32.1 = Hex-STRING: 00 1F 12 AC C3 F0 
         *  
         *  siegfrie-192.168.239.54-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.532 = Hex-STRING: 00 1F 12 AC C3 F0 
         *  siegfrie-192.168.239.54-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.533 = Hex-STRING: 00 1F 12 AC C3 F0
         */
    }
}
