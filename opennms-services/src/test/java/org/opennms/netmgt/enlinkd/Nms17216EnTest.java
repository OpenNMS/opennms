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

import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER3_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER3_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER3_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER4_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER4_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER4_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH3_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH3_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH3_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH4_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH4_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH4_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH5_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH5_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH5_SNMP_RESOURCE;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH2_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH3_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH4_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH5_LLDP_CHASSISID;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_IF_IFNAME_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH2_IF_IFNAME_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH3_IF_IFNAME_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH2_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH3_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH4_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH5_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER1_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER2_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER3_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER4_IF_IFDESCR_MAP;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_IP_IF_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER1_IP_IF_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER2_IP_IF_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER3_IP_IF_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ROUTER4_IP_IF_MAP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;

public class Nms17216EnTest extends EnLinkdTestBuilder {
        
	Nms17216NetworkBuilder builder = new Nms17216NetworkBuilder();    
    /*
     * These are the links among the following nodes discovered using 
     * only the lldp protocol
     * switch1 Gi0/9 Gi0/10 Gi0/11 Gi0/12 ----> switch2 Gi0/1 Gi0/2 Gi0/3 Gi0/4
     * switch2 Gi0/19 Gi0/20              ----> switch3 Fa0/19 Fa0/20
     * 
     * here are the corresponding ifindex:
     * switch1 Gi0/9 --> 10109
     * switch1 Gi0/10 --> 10110
     * switch1 Gi0/11 --> 10111
     * switch1 Gi0/12 --> 10112
     * 
     * switch2 Gi0/1 --> 10101
     * switch2 Gi0/2 --> 10102
     * switch2 Gi0/3 --> 10103
     * switch2 Gi0/4 --> 10104
     * switch2 Gi0/19 --> 10119
     * switch2 Gi0/20 --> 10120
     * 
     * switch3 Fa0/19 -->  10019
     * switch3 Fa0/20 -->  10020
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE)
    })
    public void testNetwork17216LldpLinks() throws Exception {
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());
        m_nodeDao.save(builder.getSwitch4());
        m_nodeDao.save(builder.getSwitch5());
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

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode switch5 = m_nodeDao.findByForeignId("linkd", SWITCH5_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch4.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch5.getId()));
 
        assertEquals(0,m_lldpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertEquals(4, m_lldpLinkDao.countAll());
        
        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertEquals(10,m_lldpLinkDao.countAll());
       
        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertEquals(12,m_lldpLinkDao.countAll());

        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertEquals(12,m_lldpLinkDao.countAll());

        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch5.getId()));
        assertEquals(12,m_lldpLinkDao.countAll());

        for (final OnmsNode node: m_nodeDao.findAll()) {
            assertNotNull(node.getLldpElement());
            printLldpElement(node.getLldpElement());
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, node.getLldpElement().getLldpChassisIdSubType());
            if        (node.getId().intValue() == switch1.getId().intValue()) {
                assertEquals(SWITCH1_LLDP_CHASSISID, node.getLldpElement().getLldpChassisId());
                assertEquals(SWITCH1_NAME, node.getLldpElement().getLldpSysname());
            } else if (node.getId().intValue() == switch2.getId().intValue()) {
                assertEquals(SWITCH2_LLDP_CHASSISID, node.getLldpElement().getLldpChassisId());
                assertEquals(SWITCH2_NAME, node.getLldpElement().getLldpSysname());                
            } else if (node.getId().intValue() == switch3.getId().intValue()) {
                assertEquals(SWITCH3_LLDP_CHASSISID, node.getLldpElement().getLldpChassisId());
                assertEquals(SWITCH3_NAME, node.getLldpElement().getLldpSysname());
            } else if (node.getId().intValue() == switch4.getId().intValue()) {
                assertEquals(SWITCH4_LLDP_CHASSISID, node.getLldpElement().getLldpChassisId());
                assertEquals(SWITCH4_NAME, node.getLldpElement().getLldpSysname());
            } else if (node.getId().intValue() == switch5.getId().intValue()) {
                assertEquals(SWITCH5_LLDP_CHASSISID, node.getLldpElement().getLldpChassisId());
                assertEquals(SWITCH5_NAME, node.getLldpElement().getLldpSysname());
            } else {
                assertTrue(false);
            }
        }
        
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            printLldpLink(link);
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
            assertNull(link.getLldpPortIfindex());
            if         (link.getNode().getId().intValue() == switch1.getId().intValue()) {
                assertEquals(SWITCH2_LLDP_CHASSISID, link.getLldpRemChassisId());
                assertEquals(SWITCH2_NAME,link.getLldpRemSysname());
                switch (link.getLldpLocalPortNum().intValue()) {
                case 9: 
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10109), link.getLldpPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getLldpPortDescr());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10101), link.getLldpRemPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getLldpRemPortDescr());
                    break;
                case 10: 
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10110), link.getLldpPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getLldpPortDescr());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10102), link.getLldpRemPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getLldpRemPortDescr());
                    break;
                case 11: 
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10111), link.getLldpPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getLldpPortDescr());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10103), link.getLldpRemPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getLldpRemPortDescr());
                    break;
                case 12: 
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10112), link.getLldpPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getLldpPortDescr());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10104), link.getLldpRemPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getLldpRemPortDescr());
                    break;
                 default: assertTrue(false);
                     break;
                }
            } else if  (link.getNode().getId().intValue() == switch2.getId().intValue()) {
                switch (link.getLldpLocalPortNum().intValue()) {
                case 1: 
                    assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10101), link.getLldpPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getLldpPortDescr());
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10109), link.getLldpRemPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getLldpRemPortDescr());
                    break;
                case 2: 
                    assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10102), link.getLldpPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getLldpPortDescr());
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10110), link.getLldpRemPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getLldpRemPortDescr());
                    break;
                case 3: 
                    assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10103), link.getLldpPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getLldpPortDescr());
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10111), link.getLldpRemPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getLldpRemPortDescr());
                    break;
                case 4: 
                    assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10104), link.getLldpPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getLldpPortDescr());
                    assertEquals(SWITCH1_IF_IFNAME_MAP.get(10112), link.getLldpRemPortId());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getLldpRemPortDescr());
                    break;
                case 19: 
                    assertEquals(SWITCH3_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH3_NAME,link.getLldpRemSysname());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10119), link.getLldpPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getLldpPortDescr());
                    assertEquals(SWITCH3_IF_IFNAME_MAP.get(10019), link.getLldpRemPortId());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getLldpRemPortDescr());
                    break;
                case 20: 
                    assertEquals(SWITCH3_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH3_NAME,link.getLldpRemSysname());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10120), link.getLldpPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getLldpPortDescr());
                    assertEquals(SWITCH3_IF_IFNAME_MAP.get(10020), link.getLldpRemPortId());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getLldpRemPortDescr());
                    break;
                default: assertTrue(false);
                break;
                }
            } else if  (link.getNode().getId().intValue() == switch3.getId().intValue()) {
                assertEquals(SWITCH2_LLDP_CHASSISID, link.getLldpRemChassisId());
                assertEquals(SWITCH2_NAME,link.getLldpRemSysname());
                switch (link.getLldpLocalPortNum().intValue()) {
                case 19: 
                    assertEquals(SWITCH3_IF_IFNAME_MAP.get(10019), link.getLldpPortId());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getLldpPortDescr());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10119), link.getLldpRemPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getLldpRemPortDescr());
                    break;
                case 20: 
                    assertEquals(SWITCH3_IF_IFNAME_MAP.get(10020), link.getLldpPortId());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getLldpPortDescr());
                    assertEquals(SWITCH2_IF_IFNAME_MAP.get(10120), link.getLldpRemPortId());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getLldpRemPortDescr());
                    break;
                default: assertTrue(false);
                break;
                }
            } else {
                assertTrue(false);
            }
        }


    }
    
    /* 
     * switch1 GigabitEthernet 0/9 0/10 0/11 0/12 ---> switch2 GigabitEthernet 0/1 0/2 0/3 0/4
     * switch1 GigabitEthernet0/1                 ---> router1 FastEthernet0/0
     * 
     * switch2 GigabitEthernet 0/1 0/2 0/3 0/4    ---> switch1 GigabitEthernet 0/9 0/10 0/11 0/12 
     * switch2 GigabitEthernet 0/19 Gi0/20        ---> switch3 FastEthernet 0/19 0/20
     *  
     * switch3 FastEthernet 0/19 0/20             ---> switch2 GigabitEthernet 0/19 0/20
     * switch3 FastEthernet 0/23 0/24             ---> switch5 FastEthernet 0/1 0/13
     *
     * switch4 FastEthernet0/1                    ---> router3 GigabitEthernet0/1
     * 
     * switch5 FastEthernet 0/1 0/13              ---> switch3 FastEthernet 0/23 0/24
     * 
     * router1 FastEthernet0/0                    ---> switch1 GigabitEthernet0/1
     * router1 Serial0/0/0                        ---> router2 Serial0/0/0
     *  
     * router2 Serial0/0/0                        ---> router1 Serial0/0/0
     * router2 Serial0/0/1                        ---> router3 Serial0/0/1
     * 
     * router3 GigabitEthernet0/0                 ---> router4 GigabitEthernet0/1
     * router3 GigabitEthernet0/1                 ---> switch4 FastEthernet0/1 
     * router3 Serial0/0/1                        ---> router2 Serial0/0/1
     * 
     * router4 GigabitEthernet0/1                 ---> router3   GigabitEthernet0/0
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource=ROUTER1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource=ROUTER2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource=ROUTER3_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource=ROUTER4_SNMP_RESOURCE)

    })
    public void testNetwork17216CdpLinks() throws Exception {
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());
        m_nodeDao.save(builder.getSwitch4());
        m_nodeDao.save(builder.getSwitch5());
        m_nodeDao.save(builder.getRouter1());
        m_nodeDao.save(builder.getRouter2());
        m_nodeDao.save(builder.getRouter3());
        m_nodeDao.save(builder.getRouter4());

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

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode switch5 = m_nodeDao.findByForeignId("linkd", SWITCH5_NAME);
        final OnmsNode router1 = m_nodeDao.findByForeignId("linkd", ROUTER1_NAME);
        final OnmsNode router2 = m_nodeDao.findByForeignId("linkd", ROUTER2_NAME);
        final OnmsNode router3 = m_nodeDao.findByForeignId("linkd", ROUTER3_NAME);
        final OnmsNode router4 = m_nodeDao.findByForeignId("linkd", ROUTER4_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch4.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch5.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router4.getId()));
        

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertEquals(5, m_cdpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertEquals(11, m_cdpLinkDao.countAll());
       
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertEquals(15, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertEquals(16, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(switch5.getId()));
        assertEquals(18, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router1.getId()));
        assertEquals(20, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router2.getId()));
        assertEquals(22, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
        assertEquals(25, m_cdpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router4.getId()));
        assertEquals(26, m_cdpLinkDao.countAll());

        for (final OnmsNode node: m_nodeDao.findAll()) {
            assertNotNull(node.getCdpElement());
            printCdpElement(node.getCdpElement());
            assertEquals(TruthValue.TRUE, node.getCdpElement().getCdpGlobalRun());
            if        (node.getId().intValue() == switch1.getId().intValue()) {
                assertEquals(SWITCH1_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == switch2.getId().intValue()) {
                assertEquals(SWITCH2_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == switch3.getId().intValue()) {
                assertEquals(SWITCH3_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == switch4.getId().intValue()) {
                assertEquals(SWITCH4_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == switch5.getId().intValue()) {
                assertEquals(SWITCH5_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == router1.getId().intValue()) {
                assertEquals(ROUTER1_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == router2.getId().intValue()) {
                assertEquals(ROUTER2_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == router3.getId().intValue()) {
                assertEquals(ROUTER3_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else if (node.getId().intValue() == router4.getId().intValue()) {
                assertEquals(ROUTER4_NAME,node.getCdpElement().getCdpGlobalDeviceId());
            } else {
                assertTrue(false);
            }
        }
        
        for (CdpLink link: m_cdpLinkDao.findAll()) {
            printCdpLink(link);
            assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
            if        (link.getNode().getId().intValue() == switch1.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 10101 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10101), link.getCdpInterfaceName());
                    assertEquals(ROUTER1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, 2800 Software (C2800NM-ADVENTERPRISEK9-M), Version 12.4(24)T1, RELEASE SOFTWARE (fc3) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2009 by Cisco Systems, Inc. Compiled Fri 19-Jun-09 15:13 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco 2811",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER1_IF_IFDESCR_MAP.get(7), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10109 && link.getCdpCacheDeviceIndex().intValue() == 5 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10110 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10111 && link.getCdpCacheDeviceIndex().intValue() == 3 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10112 && link.getCdpCacheDeviceIndex().intValue() == 4 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getCdpCacheDevicePort());
                } else {
                    assertTrue(false);
                }
            } else if (link.getNode().getId().intValue() == switch2.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 10101 && link.getCdpCacheDeviceIndex().intValue() == 3 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10102 && link.getCdpCacheDeviceIndex().intValue() == 4 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10103 && link.getCdpCacheDeviceIndex().intValue() == 5 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10104 && link.getCdpCacheDeviceIndex().intValue() == 6 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10119 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10120 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getCdpCacheDevicePort());
                } else {
                    assertTrue(false);
                }
           } else if (link.getNode().getId().intValue() == switch3.getId().intValue()) {
               if (link.getCdpCacheIfIndex().intValue() == 10019 && link.getCdpCacheDeviceIndex().intValue() == 3 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getCdpInterfaceName());
                   assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex().intValue() == 10020 && link.getCdpCacheDeviceIndex().intValue() == 4 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getCdpInterfaceName());
                   assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex().intValue() == 10023 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10023), link.getCdpInterfaceName());
                   assertEquals(SWITCH5_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH5_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10001), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex().intValue() == 10024 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10024), link.getCdpInterfaceName());
                   assertEquals(SWITCH5_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH5_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10013), link.getCdpCacheDevicePort());
               } else {
                   assertTrue(false);
               }
            } else if (link.getNode().getId().intValue() == switch4.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 10001 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                    assertEquals(SWITCH4_IF_IFDESCR_MAP.get(10001), link.getCdpInterfaceName());
                    assertEquals(ROUTER3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(9), link.getCdpCacheDevicePort());
                } else {
                    assertTrue(false);
                }
            } else if (link.getNode().getId().intValue() == switch5.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 10001 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                    assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10001), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10023), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 10013 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                    assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10013), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10024), link.getCdpCacheDevicePort());
                } else {
                    assertTrue(false);
                }
            } else if (link.getNode().getId().intValue() == router1.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 7 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                    assertEquals(ROUTER1_IF_IFDESCR_MAP.get(7), link.getCdpInterfaceName());
                    assertEquals(10101,SWITCH1_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10101), link.getCdpCacheDevicePort());
                 } else if (link.getCdpCacheIfIndex().intValue() == 13 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                     assertEquals(ROUTER1_IF_IFDESCR_MAP.get(13), link.getCdpInterfaceName());
                     assertEquals(12,ROUTER2_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                     assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                     assertEquals(ROUTER2_NAME, link.getCdpCacheDeviceId());
                     assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                     assertEquals(ROUTER2_IF_IFDESCR_MAP.get(12), link.getCdpCacheDevicePort());
                     
                 } else {
                     assertTrue(false);
                 }
            } else if (link.getNode().getId().intValue() == router2.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 12 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                     assertEquals(ROUTER2_IF_IFDESCR_MAP.get(12), link.getCdpInterfaceName());
                     assertEquals(13,ROUTER1_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                     assertEquals("Cisco IOS Software, 2800 Software (C2800NM-ADVENTERPRISEK9-M), Version 12.4(24)T1, RELEASE SOFTWARE (fc3) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2009 by Cisco Systems, Inc. Compiled Fri 19-Jun-09 15:13 by prod_rel_team",link.getCdpCacheVersion());
                     assertEquals(ROUTER1_NAME, link.getCdpCacheDeviceId());
                     assertEquals("Cisco 2811",link.getCdpCacheDevicePlatform());
                     assertEquals(ROUTER1_IF_IFDESCR_MAP.get(13), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex().intValue() == 13 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                    assertEquals(ROUTER2_IF_IFDESCR_MAP.get(13), link.getCdpInterfaceName());
                    assertEquals(13,ROUTER3_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(13), link.getCdpCacheDevicePort());
                 } else {
                     assertTrue(false);
                 }
            } else if (link.getNode().getId().intValue() == router3.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 8 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(8), link.getCdpInterfaceName());
                    assertEquals(3,ROUTER4_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER4_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER4_IF_IFDESCR_MAP.get(3), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex().intValue() == 9 && link.getCdpCacheDeviceIndex().intValue() == 3 ) {
                   assertEquals(ROUTER3_IF_IFDESCR_MAP.get(9), link.getCdpInterfaceName());
                   assertEquals(SWITCH4_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH4_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH4_IF_IFDESCR_MAP.get(10001), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex().intValue() == 13 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                   assertEquals(ROUTER3_IF_IFDESCR_MAP.get(13), link.getCdpInterfaceName());
                   assertEquals(13,ROUTER2_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                   assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(ROUTER2_NAME, link.getCdpCacheDeviceId());
                   assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                   assertEquals(ROUTER2_IF_IFDESCR_MAP.get(13), link.getCdpCacheDevicePort());
                } else {
                    assertTrue(false);
                }
            } else if (link.getNode().getId().intValue() == router4.getId().intValue()) {
                if (link.getCdpCacheIfIndex().intValue() == 3 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                    assertEquals(ROUTER4_IF_IFDESCR_MAP.get(3), link.getCdpInterfaceName());
                    assertEquals(8,ROUTER3_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(8), link.getCdpCacheDevicePort());
                 } else {
                     assertTrue(false);
                 }
            } else {
                assertTrue(false);
            }
        }
    }
}
