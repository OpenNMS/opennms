/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_IP;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_NAME;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_CDP_GLOBAL_DEVICE_ID;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_LLDP_CHASSID_ID;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_LLDP_SYSNAME;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.CISCO01_IF_IFNAME_MAP;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.SWITCH02_IP;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.SWITCH02_NAME;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.SWITCH02_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.SWITCH02_LLDP_CHASSID_ID;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.SWITCH02_LLDP_SYSNAME;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.SWITCH02_CDP_GLOBAL_DEVICE_ID;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.HOMESERVER_IP;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.HOMESERVER_NAME;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.HOMESERVER_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.HOMESERVER_LLDP_CHASSID_ID;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.HOMESERVER_LLDP_SYSNAME;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.HOMESERVER_IF_MAC_MAP;
import static org.opennms.netmgt.nb.Nms7563NetworkBuilder.HOMESERVER_CDP_GLOBAL_DEVICE_ID;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.enlinkd.model.OspfElement.TruthValue;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms7563NetworkBuilder;

public class Nms7563EnIT extends EnLinkdBuilderITCase {

	Nms7563NetworkBuilder builder = new Nms7563NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO01_IP, port=161, resource=CISCO01_SNMP_RESOURCE)
    })
    public void testCisco01Links() {
        
        m_nodeDao.save(builder.getCisco01());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertFalse(m_linkdConfig.useIsisDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode cisco01 = m_nodeDao.findByForeignId("linkd", CISCO01_NAME);
        
        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(cisco01.getId()));

        for (final LldpElement node: m_lldpElementDao.findAll()) {
            assertNotNull(node);
            printLldpElement(node);
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, node.getLldpChassisIdSubType());
            assertEquals(CISCO01_LLDP_CHASSID_ID,node.getLldpChassisId());
            assertEquals(CISCO01_LLDP_SYSNAME, node.getLldpSysname());
        }
        for (final CdpElement node: m_cdpElementDao.findAll()) {
            assertNotNull(node);
            printCdpElement(node);
            assertEquals(TruthValue.TRUE, node.getCdpGlobalRun());
            assertEquals(CISCO01_CDP_GLOBAL_DEVICE_ID,node.getCdpGlobalDeviceId());
        }

        assertEquals(1, m_lldpLinkDao.countAll());
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            assertNotNull(link);
            printLldpLink(link);
            assertEquals(8, link.getLldpRemLocalPortNum().intValue());
            assertEquals(1, link.getLldpRemIndex().intValue());
            assertEquals(10008,link.getLldpPortIfindex().intValue());
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
    public void testHomeServerLinks() {
        
        m_nodeDao.save(builder.getHomeServer());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);

        assertFalse(m_linkdConfig.useIsisDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode homeserver = m_nodeDao.findByForeignId("linkd", HOMESERVER_NAME);
        
        m_linkd.reload();
        assertTrue(m_linkd.runSingleSnmpCollection(homeserver.getId()));

        for (final LldpElement node: m_lldpElementDao.findAll()) {
            assertNotNull(node);
            printLldpElement(node);
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, node.getLldpChassisIdSubType());
            assertEquals(HOMESERVER_LLDP_CHASSID_ID,node.getLldpChassisId());
            assertEquals(HOMESERVER_LLDP_SYSNAME, node.getLldpSysname());
        }

        assertEquals(1, m_lldpLinkDao.countAll());
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            assertNotNull(link);
            printLldpLink(link);
            assertEquals(1, link.getLldpRemIndex().intValue());
            assertEquals(2, link.getLldpRemLocalPortNum().intValue());
            assertEquals(2,link.getLldpPortIfindex().intValue());
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
    public void testSwitch02Links() {
        
        m_nodeDao.save(builder.getSwitch02());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);

        assertFalse(m_linkdConfig.useIsisDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode switch02 = m_nodeDao.findByForeignId("linkd", SWITCH02_NAME);
        
        m_linkd.reload();
        assertTrue(m_linkd.runSingleSnmpCollection(switch02.getId()));

        for (final CdpElement node: m_cdpElementDao.findAll()) {
            assertNotNull(node);
            printCdpElement(node);
            assertEquals(TruthValue.TRUE, node.getCdpGlobalRun());
            assertEquals(SWITCH02_CDP_GLOBAL_DEVICE_ID,node.getCdpGlobalDeviceId());
        }

        assertEquals(3, m_cdpLinkDao.countAll());
        for (CdpLink link: m_cdpLinkDao.findAll()) {
            printCdpLink(link);
            if (link.getCdpCacheIfIndex() == 7 && link.getCdpCacheDeviceIndex() == 1 ) {
                assertNull(link.getCdpInterfaceName());
                assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
                // here the ip address is associated to wlan0 but the mac is associated to eth0
                // clearly the link is with eth0.
                assertEquals("192.168.87.16", link.getCdpCacheAddress());
                assertEquals(HOMESERVER_CDP_GLOBAL_DEVICE_ID, link.getCdpCacheDeviceId());
                assertEquals("Debian GNU/Linux 7 (wheezy) Linux 3.2.0-4-amd64 #1 SMP Debian 3.2.65-1+deb7u2 x86_64",link.getCdpCacheVersion());
                assertEquals("Debian GNU/Linux 7 (wheezy) Linux 3.2.0-4-amd64 #1 SMP Debian 3.2.65-1+deb7u2 x86_64",link.getCdpCacheDevicePlatform());
                assertEquals(HOMESERVER_IF_MAC_MAP.get(2), link.getCdpCacheDevicePort());
            } else if (link.getCdpCacheIfIndex() == 24 && link.getCdpCacheDeviceIndex() == 1 ) {
                // This is a link to cisco01 port fastethernet0/8
                assertNull(link.getCdpInterfaceName());
                assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
                assertEquals("192.168.88.240", link.getCdpCacheAddress());
                assertEquals(CISCO01_CDP_GLOBAL_DEVICE_ID, link.getCdpCacheDeviceId());
                assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 15.0(2)SE4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyrighcisco WS-C2960-8TC-L",link.getCdpCacheVersion());
                assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 15.0(2)SE4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyrighcisco WS-C2960-8TC-L",link.getCdpCacheDevicePlatform());
                assertEquals(CISCO01_IF_IFDESCR_MAP.get(10008), link.getCdpCacheDevicePort());
            } else if (link.getCdpCacheIfIndex() == 24 && link.getCdpCacheDeviceIndex() == 2 ) {
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
                fail();
            }
        }
    }

}
