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
import static org.junit.Assert.assertNull;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_CDP_GLOBAL_DEVICE_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_LLDP_CHASSID_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_LLDP_SYSNAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO01_IF_IFNAME_MAP;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH02_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH02_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH02_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH02_LLDP_CHASSID_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH02_LLDP_SYSNAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH02_CDP_GLOBAL_DEVICE_ID;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.HOMESERVER_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.HOMESERVER_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.HOMESERVER_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.HOMESERVER_LLDP_CHASSID_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.HOMESERVER_LLDP_SYSNAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.HOMESERVER_IF_MAC_MAP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.HOMESERVER_CDP_GLOBAL_DEVICE_ID;


import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.nb.Nms7563NetworkBuilder;

public class Nms7563EnTest extends EnLinkdTestBuilder {

	Nms7563NetworkBuilder builder = new Nms7563NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO01_IP, port=161, resource=CISCO01_SNMP_RESOURCE)
    })
    public void testCisco01Links() throws Exception {
        
        m_nodeDao.save(builder.getCisco01());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        
        assertTrue(!m_linkdConfig.useIsisDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode cisco01 = m_nodeDao.findByForeignId("linkd", CISCO01_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(cisco01.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(cisco01.getId()));

        for (final OnmsNode node: m_nodeDao.findAll()) {
            assertNotNull(node.getLldpElement());
            printLldpElement(node.getLldpElement());
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, node.getLldpElement().getLldpChassisIdSubType());
            assertEquals(CISCO01_LLDP_CHASSID_ID,node.getLldpElement().getLldpChassisId());
            assertEquals(CISCO01_LLDP_SYSNAME, node.getLldpElement().getLldpSysname());
            
            assertNotNull(node.getCdpElement());
            printCdpElement(node.getCdpElement());
            assertEquals(TruthValue.TRUE, node.getCdpElement().getCdpGlobalRun());
            assertEquals(CISCO01_CDP_GLOBAL_DEVICE_ID,node.getCdpElement().getCdpGlobalDeviceId());
        }

        assertEquals(1, m_lldpLinkDao.countAll());
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            assertNotNull(link);
            printLldpLink(link);
            assertEquals(8, link.getLldpLocalPortNum().intValue());
            assertNull(link.getLldpPortIfindex());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME,link.getLldpPortIdSubType());
            assertEquals("Fa0/8",link.getLldpPortId());
            assertEquals("FastEthernet0/8",link.getLldpPortDescr());
            
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            assertEquals(SWITCH02_LLDP_CHASSID_ID,link.getLldpRemChassisId());
            assertEquals(SWITCH02_LLDP_SYSNAME,link.getLldpRemSysname());
            
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, link.getLldpRemPortIdSubType());
            assertEquals("24",link.getLldpRemPortId());
            assertEquals("24",link.getLldpRemPortDescr());
           
        }


        assertEquals(0, m_cdpLinkDao.countAll());

        
        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=HOMESERVER_IP, port=161, resource=HOMESERVER_SNMP_RESOURCE)
    })
    public void testHomeServerLinks() throws Exception {
        
        m_nodeDao.save(builder.getHomeServer());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        
        assertTrue(!m_linkdConfig.useIsisDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode homeserver = m_nodeDao.findByForeignId("linkd", HOMESERVER_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(homeserver.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(homeserver.getId()));

        for (final OnmsNode node: m_nodeDao.findAll()) {
            assertNotNull(node.getLldpElement());
            printLldpElement(node.getLldpElement());
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, node.getLldpElement().getLldpChassisIdSubType());
            assertEquals(HOMESERVER_LLDP_CHASSID_ID,node.getLldpElement().getLldpChassisId());
            assertEquals(HOMESERVER_LLDP_SYSNAME, node.getLldpElement().getLldpSysname());
        }

        assertEquals(1, m_lldpLinkDao.countAll());
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            assertNotNull(link);
            printLldpLink(link);
            assertEquals(2, link.getLldpLocalPortNum().intValue());
            assertNull(link.getLldpPortIfindex());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS,link.getLldpPortIdSubType());
            assertEquals(HOMESERVER_IF_MAC_MAP.get(2),link.getLldpPortId());
            assertEquals("eth0",link.getLldpPortDescr());
            
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            assertEquals(SWITCH02_LLDP_CHASSID_ID,link.getLldpRemChassisId());
            assertEquals(SWITCH02_LLDP_SYSNAME,link.getLldpRemSysname());
            
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, link.getLldpRemPortIdSubType());
            assertEquals("7",link.getLldpRemPortId());
            assertEquals("7",link.getLldpRemPortDescr());
           
        }        
        
    }

    /*
     * The SNMP walk reported by the HP ProcurceSwitch for CDP
     * seems to be wrong, there is a double reported link
     * that with the Cisco2900.
     * In any case we decide to save the data as it is walked 
     * and put all the necessarly login to manage
     * links in other classes.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH02_IP, port=161, resource=SWITCH02_SNMP_RESOURCE)
    })
    public void testSwitch02Links() throws Exception {
        
        m_nodeDao.save(builder.getSwitch02());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        
        assertTrue(!m_linkdConfig.useIsisDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode switch02 = m_nodeDao.findByForeignId("linkd", SWITCH02_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch02.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switch02.getId()));

        for (final OnmsNode node: m_nodeDao.findAll()) {
            assertNotNull(node.getCdpElement());
            printCdpElement(node.getCdpElement());
            assertEquals(TruthValue.TRUE, node.getCdpElement().getCdpGlobalRun());
            assertEquals(SWITCH02_CDP_GLOBAL_DEVICE_ID,node.getCdpElement().getCdpGlobalDeviceId());
        }

        assertEquals(3, m_cdpLinkDao.countAll());
        for (CdpLink link: m_cdpLinkDao.findAll()) {
            printCdpLink(link);
            if (link.getCdpCacheIfIndex().intValue() == 7 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                assertNull(link.getCdpInterfaceName());
                assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
                // here the ip address is associated to wlan0 but the mac is associated to eth0
                // clearly the link is with eth0.
                assertEquals("192.168.87.16", link.getCdpCacheAddress());
                assertEquals(HOMESERVER_CDP_GLOBAL_DEVICE_ID, link.getCdpCacheDeviceId());
                assertEquals("Debian GNU/Linux 7 (wheezy) Linux 3.2.0-4-amd64 #1 SMP Debian 3.2.65-1+deb7u2 x86_64",link.getCdpCacheVersion());
                assertEquals("Debian GNU/Linux 7 (wheezy) Linux 3.2.0-4-amd64 #1 SMP Debian 3.2.65-1+deb7u2 x86_64",link.getCdpCacheDevicePlatform());
                assertEquals(HOMESERVER_IF_MAC_MAP.get(2), link.getCdpCacheDevicePort());
            } else if (link.getCdpCacheIfIndex().intValue() == 24 && link.getCdpCacheDeviceIndex().intValue() == 1 ) {
                // This is a link to cisco01 port fastethernet0/8
                assertNull(link.getCdpInterfaceName());
                assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
                assertEquals("192.168.88.240", link.getCdpCacheAddress());
                assertEquals(CISCO01_CDP_GLOBAL_DEVICE_ID, link.getCdpCacheDeviceId());
                assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 15.0(2)SE4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyrighcisco WS-C2960-8TC-L",link.getCdpCacheVersion());
                assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 15.0(2)SE4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyrighcisco WS-C2960-8TC-L",link.getCdpCacheDevicePlatform());
                assertEquals(CISCO01_IF_IFDESCR_MAP.get(10008), link.getCdpCacheDevicePort());
            } else if (link.getCdpCacheIfIndex().intValue() == 24 && link.getCdpCacheDeviceIndex().intValue() == 2 ) {
                // This is a link to cisco01 port fastethernet0/8 with different data also
                // the cdpcacheversion is always different from the cdpcacheplatform.
                assertNull(link.getCdpInterfaceName());
                assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
                assertEquals("192.168.88.240", link.getCdpCacheAddress());
                assertEquals(CISCO01_LLDP_CHASSID_ID, link.getCdpCacheDeviceId());
                assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 15.0(2)SE4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2013 by Cisco Systems, Inc.  Compiled Wed 26-Jun-13 02:49 by prod_rel_team",link.getCdpCacheVersion());
                assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 15.0(2)SE4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2013 by Cisco Systems, Inc.  Compiled Wed 26-Jun-13 02:49 by prod_rel_team",link.getCdpCacheDevicePlatform());
                assertEquals(CISCO01_IF_IFNAME_MAP.get(10008), link.getCdpCacheDevicePort());                
            } else {
                assertTrue(false);
            }
        }
    }

}
