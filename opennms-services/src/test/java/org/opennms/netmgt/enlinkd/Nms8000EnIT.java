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
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR1_SNMP_RESOURCE_2;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR2_SNMP_RESOURCE_2;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR3_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR3_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR3_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMR3_SNMP_RESOURCE_2;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW1_SNMP_RESOURCE_2;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NMMSW2_SNMP_RESOURCE_2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.nb.Nms8000NetworkBuilder;

public class Nms8000EnIT extends EnLinkdBuilderITCase {
        
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
        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=NMMR1_IP, port=161, resource=NMMR1_SNMP_RESOURCE_2),
            @JUnitSnmpAgent(host=NMMR2_IP, port=161, resource=NMMR2_SNMP_RESOURCE_2),
            @JUnitSnmpAgent(host=NMMR3_IP, port=161, resource=NMMR3_SNMP_RESOURCE_2),
            @JUnitSnmpAgent(host=NMMSW1_IP, port=161, resource=NMMSW1_SNMP_RESOURCE_2),
            @JUnitSnmpAgent(host=NMMSW2_IP, port=161, resource=NMMSW2_SNMP_RESOURCE_2)
    })
    public void testLldpLinks() throws Exception {
        m_nodeDao.save(builder.getNMMR1());
        m_nodeDao.save(builder.getNMMR2());
        m_nodeDao.save(builder.getNMMR3());
        m_nodeDao.save(builder.getNMMSW1());
        m_nodeDao.save(builder.getNMMSW2());

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
        assertEquals(3, m_lldpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(nmmr2.getId()));
        assertEquals(6, m_lldpLinkDao.countAll());
       
        assertTrue(m_linkd.runSingleSnmpCollection(nmmr3.getId()));
        assertEquals(8, m_lldpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(nmmsw1.getId()));
        assertEquals(10, m_lldpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(nmmsw2.getId()));
        assertEquals(12, m_lldpLinkDao.countAll());

        for (final OnmsNode node: m_nodeDao.findAll()) {
            assertNotNull(node.getLldpElement());
            printLldpElement(node.getLldpElement());
        }
        
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            printLldpLink(link);
        }
        
        List<LldpLink> allLinks = m_lldpLinkDao.findAll();
        int combinedlinkfound=0;
        Set<Integer> parsed = new HashSet<>();
        for (LldpLink sourceLink : allLinks) {
            System.err.println("loadtopology: parsing lldp link with id "+ sourceLink.getId());
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            parsed.add(sourceLink.getId());
            LldpElement sourceElement = m_nodeDao.get(sourceLink.getNode().getId()).getLldpElement();
            LldpLink targetLink = null;
            for (LldpLink link : allLinks) {
                if (parsed.contains(link.getId())) {
                    continue;
                }
                LldpElement element = m_nodeDao.get(link.getNode().getId()).getLldpElement();
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getLldpRemChassisId().equals(element.getLldpChassisId()) || !link.getLldpRemChassisId().equals(sourceElement.getLldpChassisId())) 
                    continue;
                boolean bool1 = sourceLink.getLldpRemPortId().equals(link.getLldpPortId()) && link.getLldpRemPortId().equals(sourceLink.getLldpPortId());
                boolean bool2 = sourceLink.getLldpRemPortDescr().equals(link.getLldpPortDescr()) && link.getLldpRemPortDescr().equals(sourceLink.getLldpPortDescr());
                boolean bool3 = sourceLink.getLldpRemPortIdSubType() == link.getLldpPortIdSubType() && link.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType();

                if (bool1 && bool2 && bool3) {
                    targetLink=link;
                    parsed.add(targetLink.getId());
                    System.err.println("loadtopology: lldp link with id "+ link.getId() + " is target.");
                    break;
                }
            }
            
            if (targetLink == null) {
                final org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsNode.class).addRestriction(new EqRestriction("sysName", sourceLink.getLldpRemSysname()));
                List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);
                if (nodes.size() == 1) {
                    targetLink = reverseLldpLink(nodes.get(0), sourceLink.getNode().getLldpElement(), sourceLink); 
                    System.err.println("loadtopology: found using sysname: lldp link with id "+ targetLink + " is target.");
                }
            }
            
            if (targetLink == null) {
                continue;
            } 
            combinedlinkfound++;
                
        }

        assertEquals(6, combinedlinkfound);
        
        
    }
    
    private LldpLink reverseLldpLink(OnmsNode sourcenode, LldpElement element, LldpLink link) {
        LldpLink reverseLink = new LldpLink();
        reverseLink.setId(-link.getId());
        reverseLink.setNode(sourcenode);
        
        reverseLink.setLldpLocalPortNum(0);
        reverseLink.setLldpPortId(link.getLldpRemPortId());
        reverseLink.setLldpPortIdSubType(link.getLldpRemPortIdSubType());
        reverseLink.setLldpPortDescr(link.getLldpRemPortDescr());
        if (link.getLldpRemPortIdSubType() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
            try {
                reverseLink.setLldpPortIfindex(Integer.getInteger(link.getLldpRemPortId()));
            } catch (Exception e) {
            }
        }

        reverseLink.setLldpRemChassisId(element.getLldpChassisId());
        reverseLink.setLldpRemChassisIdSubType(element.getLldpChassisIdSubType());
        reverseLink.setLldpRemSysname(element.getLldpSysname());
        
        reverseLink.setLldpRemPortId(link.getLldpPortId());
        reverseLink.setLldpRemPortIdSubType(link.getLldpPortIdSubType());
        reverseLink.setLldpRemPortDescr(link.getLldpPortDescr());
        
        reverseLink.setLldpLinkCreateTime(link.getLldpLinkCreateTime());
        reverseLink.setLldpLinkLastPollTime(link.getLldpLinkLastPollTime());
        
        return reverseLink;
    }


}
