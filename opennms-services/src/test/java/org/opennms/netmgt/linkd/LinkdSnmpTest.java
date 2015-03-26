/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;

import org.opennms.netmgt.linkd.snmp.CdpCacheTable;
import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.linkd.snmp.CdpInterfaceTable;
import org.opennms.netmgt.linkd.snmp.CdpInterfaceTableEntry;
import org.opennms.netmgt.linkd.snmp.CiscoVlanTable;
import org.opennms.netmgt.linkd.snmp.CiscoVlanTableEntry;
import org.opennms.netmgt.linkd.snmp.Dot1dBaseGroup;
import org.opennms.netmgt.linkd.snmp.IsIsSystemObjectGroup;
import org.opennms.netmgt.linkd.snmp.IsisCircTable;
import org.opennms.netmgt.linkd.snmp.IsisCircTableEntry;
import org.opennms.netmgt.linkd.snmp.IsisISAdjTable;
import org.opennms.netmgt.linkd.snmp.IsisISAdjTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocTable;
import org.opennms.netmgt.linkd.snmp.LldpLocTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocalGroup;
import org.opennms.netmgt.linkd.snmp.LldpRemTable;
import org.opennms.netmgt.linkd.snmp.LldpRemTableEntry;
import org.opennms.netmgt.linkd.snmp.MtxrWlRtabTable;
import org.opennms.netmgt.linkd.snmp.MtxrWlRtabTableEntry;
import org.opennms.netmgt.linkd.snmp.OspfGeneralGroup;
import org.opennms.netmgt.linkd.snmp.OspfIfTable;
import org.opennms.netmgt.linkd.snmp.OspfIfTableEntry;
import org.opennms.netmgt.linkd.snmp.OspfNbrTable;
import org.opennms.netmgt.linkd.snmp.OspfNbrTableEntry;
import org.opennms.netmgt.linkd.snmp.QBridgeDot1dTpFdbTable;
import org.opennms.netmgt.linkd.snmp.QBridgeDot1dTpFdbTableEntry;
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.model.topology.OspfNbrInterface;
import org.opennms.netmgt.nb.Nms10205bNetworkBuilder;
import org.opennms.netmgt.nb.NmsNetworkBuilder;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
public class LinkdSnmpTest extends NmsNetworkBuilder implements InitializingBean {
        
	@Override
    public void afterPropertiesSet() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.mock.snmp", "WARN");
        p.setProperty("log4j.logger.org.opennms.core.test.snmp", "WARN");
        p.setProperty("log4j.logger.org.opennms.netmgt", "WARN");
        p.setProperty("log4j.logger.org.springframework","WARN");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "WARN");
        MockLogAppender.setupLogging(p);
    }

    @Test
    public void testDiscoveryOspfGetSubNetAddress() throws Exception {
        DiscoveryLink discovery = new DiscoveryLink();
        OspfNbrInterface ospfinterface = new OspfNbrInterface(InetAddressUtils.addr("192.168.9.1"));
        ospfinterface.setOspfNbrIpAddr(InetAddressUtils.addr("192.168.15.45"));

        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.255.0"));        
        assertEquals(InetAddressUtils.addr("192.168.15.0"), discovery.getSubnetAddress(ospfinterface));
        
        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.0.0"));
        assertEquals(InetAddressUtils.addr("192.168.0.0"), discovery.getSubnetAddress(ospfinterface));

        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.255.252"));
        assertEquals(InetAddressUtils.addr("192.168.15.44"), discovery.getSubnetAddress(ospfinterface));

        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.255.240"));
        assertEquals(InetAddressUtils.addr("192.168.15.32"), discovery.getSubnetAddress(ospfinterface));

    }

    @Test
    public void testBridgePortFromDesignatedBridgePort() {
        assertEquals(5826, 8191 & Integer.parseInt("96c2",16));
        assertEquals(5781, 8191 & Integer.parseInt("9695",16));
        assertEquals(4230, 8191 & Integer.parseInt("9086",16));
        assertEquals(110, 8191 & Integer.parseInt("806e",16));
     }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = FROH_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = OEDIPUS_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsisSysObjGroupCollection() throws Exception {

        String name = "isisSystemObjectGroup";

        // froh
        IsIsSystemObjectGroup m_isisSystemObjectGroup = new IsIsSystemObjectGroup(InetAddressUtils.addr(FROH_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_isisSystemObjectGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(FROH_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(IsisAdminState.on, m_isisSystemObjectGroup.getIsisSysAdminState());
        assertEquals(FROH_ISIS_SYS_ID, m_isisSystemObjectGroup.getIsisSysId());
        
        // oedipus
        m_isisSystemObjectGroup = new IsIsSystemObjectGroup(InetAddressUtils.addr(OEDIPUS_IP));
        tracker = new CollectionTracker[]{m_isisSystemObjectGroup};
        snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(OEDIPUS_IP));
        walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(IsisAdminState.on, m_isisSystemObjectGroup.getIsisSysAdminState());
        assertEquals(OEDIPUS_ISIS_SYS_ID, m_isisSystemObjectGroup.getIsisSysId());

        // siegfrie
        m_isisSystemObjectGroup = new IsIsSystemObjectGroup(InetAddressUtils.addr(SIEGFRIE_IP));
        tracker = new CollectionTracker[]{m_isisSystemObjectGroup};
        snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SIEGFRIE_IP));
        walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(IsisAdminState.on, m_isisSystemObjectGroup.getIsisSysAdminState());
        assertEquals(SIEGFRIE_ISIS_SYS_ID, m_isisSystemObjectGroup.getIsisSysId());

    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt")
    })
    public void testIsisISAdjTableCollection() throws Exception {

        String name = "isisISAdjTable";
        IsisISAdjTable m_isisISAdjTable = new IsisISAdjTable(InetAddressUtils.addr(FROH_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_isisISAdjTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(FROH_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        Collection<IsisISAdjTableEntry> isisISAdjTableEntryCollection = m_isisISAdjTable.getEntries();
        assertEquals(2, isisISAdjTableEntryCollection.size());
        Iterator<IsisISAdjTableEntry> iter = isisISAdjTableEntryCollection.iterator();
        IsisISAdjTableEntry entry1 = iter.next();
        assertEquals(599, entry1.getIsisCircIndex().intValue());
        assertEquals(1, entry1.getIsisISAdjIndex().intValue());
        assertEquals(IsisISAdjState.up, entry1.getIsIsAdjStatus());
        assertEquals("001f12accbf1", entry1.getIsIsAdjNeighSnpaAddress());
        assertEquals(IsisISAdjNeighSysType.l1_IntermediateSystem, entry1.getIsisISAdjNeighSysType());
        assertEquals(OEDIPUS_ISIS_SYS_ID, entry1.getIsIsAdjNeighSysId());
        assertEquals(0, entry1.getIsisAdjNbrExtendedCircID().intValue());
        
        IsisISAdjTableEntry entry2 = iter.next();
        assertEquals(600, entry2.getIsisCircIndex().intValue());
        assertEquals(1, entry2.getIsisISAdjIndex().intValue());
        assertEquals(IsisISAdjState.up, entry2.getIsIsAdjStatus());
        assertEquals("001f12acc3f2", entry2.getIsIsAdjNeighSnpaAddress());
        assertEquals(IsisISAdjNeighSysType.l1_IntermediateSystem, entry2.getIsisISAdjNeighSysType());
        assertEquals(SIEGFRIE_ISIS_SYS_ID, entry2.getIsIsAdjNeighSysId());
        assertEquals(0, entry2.getIsisAdjNbrExtendedCircID().intValue());

        
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt")
    }, forceMockStrategy=true)
    public void testIsisCircTableCollection() throws Exception {

        String name = "isisCircTable";
        IsisCircTable m_isisCircTable = new IsisCircTable(InetAddressUtils.addr(FROH_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_isisCircTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(FROH_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        Collection<IsisCircTableEntry> isisCircTableEntryCollection = m_isisCircTable.getEntries();
        assertEquals(3, isisCircTableEntryCollection.size());
        Iterator<IsisCircTableEntry> iter = isisCircTableEntryCollection.iterator();
        IsisCircTableEntry entry1 = iter.next();
        assertEquals(16, entry1.getIsisCircIndex().intValue());
        assertEquals(16, entry1.getIsisCircIfIndex().intValue());
        
        IsisCircTableEntry entry2 = iter.next();
        assertEquals(599, entry2.getIsisCircIndex().intValue());
        assertEquals(599, entry2.getIsisCircIfIndex().intValue());

        IsisCircTableEntry entry3 = iter.next();
        assertEquals(600, entry3.getIsisCircIndex().intValue());
        assertEquals(600, entry3.getIsisCircIfIndex().intValue());
        
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = MUMBAI_IP, port = 161, resource = MUMBAI_SNMP_RESOURCE_B)
    })
    public void testMumbayOspfGeneralGroupCollection() throws Exception {

        String name = "ospfGeneralGroup";
        OspfGeneralGroup m_ospfGeneralGroup = new OspfGeneralGroup(InetAddressUtils.addr(MUMBAI_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfGeneralGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(MUMBAI_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(MUMBAI_OSPF_ID, m_ospfGeneralGroup.getOspfRouterId());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = SRX_100_IP, port = 161, resource = SRX_100_SNMP_RESOURCE_B)
    })
    public void testSrx100OspfGeneralGroupCollection() throws Exception {

        String name = "ospfGeneralGroup";
        OspfGeneralGroup m_ospfGeneralGroup = new OspfGeneralGroup(InetAddressUtils.addr(SRX_100_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfGeneralGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SRX_100_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(SRX_100_OSPF_ID, m_ospfGeneralGroup.getOspfRouterId());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = MUMBAI_IP, port = 161, resource = MUMBAI_SNMP_RESOURCE_B)
    })
    public void testMumbayOspfIfTableCollection() throws Exception {
    	Nms10205bNetworkBuilder builder = new Nms10205bNetworkBuilder();
    	builder.getMumbai();
        String name = "ospfIfTable";
        OspfIfTable m_ospfIfTable = new OspfIfTable(InetAddressUtils.addr(MUMBAI_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfIfTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(MUMBAI_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfIfTableEntry> ospfifTableCollection = m_ospfIfTable.getEntries();
        assertEquals(6, ospfifTableCollection.size());
        for (final OspfIfTableEntry entry : ospfifTableCollection) {
            assertEquals(0, entry.getOspfAddressLessIf().intValue());
            InetAddress ospfIpAddress = entry.getOspfIpAddress();
            assertEquals(true, MUMBAI_IP_IF_MAP.containsKey(ospfIpAddress));
        }
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = SRX_100_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "SRX-100_" + SRX_100_IP + ".txt")
    })
    public void testSrx100OspfIfTableCollection() throws Exception {

        String name = "ospfIfTable";
        OspfIfTable m_ospfIfTable = new OspfIfTable(InetAddressUtils.addr(SRX_100_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfIfTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SRX_100_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfIfTableEntry> ospfifTableCollection = m_ospfIfTable.getEntries();
        assertEquals(0, ospfifTableCollection.size());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = MUMBAI_IP, port = 161, resource = "classpath:linkd/nms10205b/" + MUMBAI_NAME + "_" + MUMBAI_IP + ".txt")
    })
    public void testMumbayOspfNbrTableCollection() throws Exception {

    	Nms10205bNetworkBuilder builder = new Nms10205bNetworkBuilder();
    	builder.getMumbai();
        String name = "ospfNbrTable";
        OspfNbrTable m_ospfNbrTable = new OspfNbrTable(InetAddressUtils.addr(MUMBAI_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfNbrTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(MUMBAI_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfNbrTableEntry> ospfNbrTableCollection = m_ospfNbrTable.getEntries();
        assertEquals(4, ospfNbrTableCollection.size());
        for (final OspfNbrTableEntry entry : ospfNbrTableCollection) {
            assertEquals(0, entry.getOspfNbrAddressLessIndex().intValue());
            assertEquals(OspfNbrTableEntry.OSPF_NBR_STATE_FULL, entry.getOspfNbrState());
            checkrow(entry);
        }
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = SRX_100_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "SRX-100_" + SRX_100_IP + ".txt")
    })
    public void testSrx100OspfNbrTableCollection() throws Exception {

        String name = "ospfNbrTable";
        OspfNbrTable m_ospfNbrTable = new OspfNbrTable(InetAddressUtils.addr(SRX_100_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfNbrTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SRX_100_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfNbrTableEntry> ospfNbrTableCollection = m_ospfNbrTable.getEntries();
        assertEquals(0, ospfNbrTableCollection.size());
    }

    private void checkrow(OspfNbrTableEntry entry) {
        InetAddress ip = entry.getOspfNbrIpAddress();
        if (ip.getHostAddress().equals("192.168.5.10")) {
            assertEquals(DELHI_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, DELHI_IP_IF_MAP.containsKey(ip));
        } else if (ip.getHostAddress().equals("192.168.5.14")) {
            assertEquals(BANGALORE_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, BANGALORE_IP_IF_MAP.containsKey(ip));
        } else if (ip.getHostAddress().equals("192.168.5.18")) {
            assertEquals(BAGMANE_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, BAGMANE_IP_IF_MAP.containsKey(ip));
        } else if (ip.getHostAddress().equals("192.168.5.22")) {
            assertEquals(MYSORE_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, MYSORE_IP_IF_MAP.containsKey(ip));
        } else {
            assertEquals(true, false);
        }
    }

    
    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = J6350_42_IP, port = 161, resource = J6350_42_SNMP_RESOURCE_B)
    })
    public void testLldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(J6350_42_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(J6350_42_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(J6350_42_LLDP_CHASSISID, m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(J6350_42_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }


    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = J6350_42_IP, port = 161, resource = J6350_42_SNMP_RESOURCE_B)
    })
    public void testLldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(J6350_42_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(J6350_42_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(0, lldpTableEntryCollection.size());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = J6350_42_IP, port = 161, resource = J6350_42_SNMP_RESOURCE_B)
    })
    public void testLldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpLocTable = new LldpLocTable(InetAddressUtils.addr(J6350_42_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_lldpLocTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(J6350_42_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpLocTable.getEntries();
        assertEquals(4, lldpTableEntryCollection.size());
        for (final LldpLocTableEntry entry : lldpTableEntryCollection) {
            assertEquals(7, entry.getLldpLocPortIdSubtype().intValue());
        }

    }

	@Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=MIKROTIK_IP, port=161, resource=MIKROTIK_SNMP_RESOURCE)
    })
    public void testMtxrWlRtabTableCollection() throws Exception {
        
        String name = "mtxrWlRtabTable";

        // froh
        MtxrWlRtabTable m_mtxrWlRtabTable = new MtxrWlRtabTable(InetAddressUtils.addr(MIKROTIK_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_mtxrWlRtabTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(MIKROTIK_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        Collection<MtxrWlRtabTableEntry> m_m_mtxrWlRtabTableEntryCollection = m_mtxrWlRtabTable.getEntries();
        assertEquals(4, m_m_mtxrWlRtabTableEntryCollection.size());
        
        int i=0;
        for (MtxrWlRtabTableEntry entry: m_m_mtxrWlRtabTableEntryCollection) {
            assertEquals(2, entry.getMtxrWlRtabIface().intValue());
            switch (i) {
                case 0: assertEquals("0015999f07ef", entry.getMtxrWlRtabAddr());
                        break;
                case 1: assertEquals("001b63cda9fd", entry.getMtxrWlRtabAddr());
                        break;
                case 2: assertEquals("60334b0817a8", entry.getMtxrWlRtabAddr());
                        break;
                case 3: assertEquals("f0728c99994d", entry.getMtxrWlRtabAddr());
                        break;
                default: assertEquals(true, false);
                        break;
            }
            i++;
        }
    }

	@Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource=PENROSE_SNMP_RESOURCE)
    })
    public void testPenroseLldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(PENROSE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(PENROSE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(PENROSE_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(PENROSE_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource=DELAWARE_SNMP_RESOURCE)
    })
    public void testDelawareLldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(DELAWARE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(DELAWARE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(DELAWARE_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(DELAWARE_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PHOENIX_IP, port=161, resource=PHOENIX_SNMP_RESOURCE)
    })
    public void testPhoenixLldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(PHOENIX_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(PHOENIX_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(PHOENIX_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(PHOENIX_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=AUSTIN_IP, port=161, resource=AUSTIN_SNMP_RESOURCE)
    })
    public void testAustinLldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(AUSTIN_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(AUSTIN_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(AUSTIN_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(AUSTIN_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SANJOSE_IP, port=161, resource=SANJOSE_SNMP_RESOURCE)
    })
    public void testSanjoseLlldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(SANJOSE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SANJOSE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(SANJOSE_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(SANJOSE_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=RIOVISTA_IP, port=161, resource=RIOVISTA_SNMP_RESOURCE)
    })
    public void testRiovistaLldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(RIOVISTA_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(RIOVISTA_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(RIOVISTA_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(RIOVISTA_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }


    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource=PENROSE_SNMP_RESOURCE)
    })
    public void testPenroseDot1dBaseCollection() throws Exception {

        String name = "dot1dbase";
        Dot1dBaseGroup m_lLldpLocalGroup = new Dot1dBaseGroup(InetAddressUtils.addr(PENROSE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(PENROSE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        assertEquals("80711f8fafd0",m_lLldpLocalGroup.getBridgeAddress());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource=DELAWARE_SNMP_RESOURCE)
    })
    public void testDelawareLldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpRemTable = new LldpLocTable(InetAddressUtils.addr(DELAWARE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(DELAWARE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(5, lldpTableEntryCollection.size());
        
        for (final LldpLocTableEntry lldpLocTableEntry: lldpTableEntryCollection) {
            checkRowNms1055(lldpLocTableEntry);
        }
    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource=DELAWARE_SNMP_RESOURCE)
    })
    public void testDelawareLldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(DELAWARE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(DELAWARE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(4, lldpTableEntryCollection.size());
        
        for (final LldpRemTableEntry lldpRemTableEntry: lldpTableEntryCollection) {
            checkRowNms1055(lldpRemTableEntry);
        }
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE)
    })
    public void testSwitch1CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(5, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE)
    })
    public void testSwitch2CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH2_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(6, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE)
    })
    public void testSwitch3CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH3_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE)
    })
    public void testSwitch4CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH4_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(1, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE)
    })
    public void testSwitch5CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH5_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH5_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(2, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource=ROUTER1_SNMP_RESOURCE)
    })
    public void testRouter1CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(2, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource=ROUTER2_SNMP_RESOURCE)
    })
    public void testRouter2CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER2_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(2, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource=ROUTER3_SNMP_RESOURCE)
    })
    public void testRouter3CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER3_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(3, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource=ROUTER4_SNMP_RESOURCE)
    })
    public void testRouter4CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER4_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(1, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE)
    })
    public void testSwicth1CiscoVlanTableCollection() throws Exception {
        
        String name = "vlanTable";
        CiscoVlanTable m_vlan = new CiscoVlanTable(InetAddressUtils.addr(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_vlan};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }

        for (SnmpStore store: m_vlan) {
                CiscoVlanTableEntry ent = (CiscoVlanTableEntry) store;
            System.out.println("VLAN-----Start");
                System.out.println("vlan index: " + ent.getVlanIndex());
            System.out.println("vlan name: " + ent.getVlanName());
            System.out.println("vlan type: " + ent.getVlanType());
            System.out.println("vlan status: " + ent.getVlanStatus()); 
            System.out.println("VLAN-----End");
            
        }

        
        assertEquals(10, m_vlan.size());
        assertEquals(6, m_vlan.getVlansForSnmpCollection().size());
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE)
    })
    public void testSwitch1CdpInterfaceTableCollection() throws Exception {
        
        String name = "cdpInterfaceTable";
        CdpInterfaceTable m_cdpinterface = new CdpInterfaceTable(InetAddressUtils.addr(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpinterface};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }

        for (SnmpStore store: m_cdpinterface) {
                CdpInterfaceTableEntry ent = (CdpInterfaceTableEntry) store;
            System.out.println("-----Cdp Interface----");
                System.out.println("cdpInterfaceIfIndex: " + ent.getCdpInterfaceIfIndex());
            System.out.println("cdpInterfaceIfName: " + ent.getCdpInterfaceName());
            System.out.println("");
        }

        
        assertEquals(28, m_cdpinterface.size());
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE)
    })
    public void testSwitch1LldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(SWITCH1_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(SWITCH1_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE)
    })
    public void testSwitch2LldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(SWITCH2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH2_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(SWITCH2_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(SWITCH2_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE)
    })
    public void testSwitch3LldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(SWITCH3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH3_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(SWITCH3_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(SWITCH3_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE)
    })
    public void testSwitch4LldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(SWITCH4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH4_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(SWITCH4_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(SWITCH4_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE)
    })
    public void testSwitch5LldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(SWITCH5_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH5_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(SWITCH5_LLDP_CHASSISID,m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(SWITCH5_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE)
    })
    public void testSwitch1LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(4, lldpTableEntryCollection.size());
        
        for (final LldpRemTableEntry lldpRemTableEntry: lldpTableEntryCollection) {
            checkSwitch1Row(lldpRemTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE)
    })
    public void testSwitch2LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(SWITCH2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH2_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(6, lldpTableEntryCollection.size());
        
        for (final LldpRemTableEntry lldpRemTableEntry: lldpTableEntryCollection) {
            checkSwitch2Row(lldpRemTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE)
    })
    public void testSwitch3LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(SWITCH3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH3_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(2, lldpTableEntryCollection.size());
        
        for (final LldpRemTableEntry lldpRemTableEntry: lldpTableEntryCollection) {
            checkSwitch3Row(lldpRemTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE)
    })
    public void testSwitch4LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(SWITCH4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH4_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(0, lldpTableEntryCollection.size());        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE)
    })
    public void testSwitch5LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(SWITCH5_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH5_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(0, lldpTableEntryCollection.size());        
    }

    private void checkSwitch1Row(LldpRemTableEntry lldpRemTableEntry) {
        final Integer lldpRemLocalPortNum = lldpRemTableEntry.getLldpRemLocalPortNum();
        final String lldpRemSysname = lldpRemTableEntry.getLldpRemSysname();
        final String lldpRemChassiid = lldpRemTableEntry.getLldpRemChassiid();
        final Integer lldpRemChassisidSubtype = lldpRemTableEntry.getLldpRemChassisidSubtype();
        String lldpRemPortid = lldpRemTableEntry.getLldpRemPortid();
        Integer lldpRemPortidSubtype = lldpRemTableEntry.getLldpRemPortidSubtype();
        printLldpRemRow(lldpRemLocalPortNum, lldpRemSysname, lldpRemChassiid, lldpRemChassisidSubtype, lldpRemPortid, lldpRemPortidSubtype);
        assertEquals(4,lldpRemChassisidSubtype.intValue());
        assertEquals(5,lldpRemPortidSubtype.intValue());
        
        if (lldpRemLocalPortNum.intValue() == 9 ) {
            assertEquals(SWITCH2_NAME,lldpRemSysname);
            assertEquals(SWITCH2_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/1", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 10) {
            assertEquals(SWITCH2_NAME,lldpRemSysname);
            assertEquals(SWITCH2_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/2", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 11) {
            assertEquals(SWITCH2_NAME,lldpRemSysname);
            assertEquals(SWITCH2_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/3", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 12) {
            assertEquals(SWITCH2_NAME,lldpRemSysname);
            assertEquals(SWITCH2_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/4", lldpRemPortid);
        } else {
            assertEquals(false, true);
        }
    }

    private void checkSwitch2Row(LldpRemTableEntry lldpRemTableEntry) {
        final Integer lldpRemLocalPortNum = lldpRemTableEntry.getLldpRemLocalPortNum();
        final String lldpRemSysname = lldpRemTableEntry.getLldpRemSysname();
        final String lldpRemChassiid = lldpRemTableEntry.getLldpRemChassiid();
        final Integer lldpRemChassisidSubtype = lldpRemTableEntry.getLldpRemChassisidSubtype();
        String lldpRemPortid = lldpRemTableEntry.getLldpRemPortid();
        Integer lldpRemPortidSubtype = lldpRemTableEntry.getLldpRemPortidSubtype();
        printLldpRemRow(lldpRemLocalPortNum, lldpRemSysname, lldpRemChassiid, lldpRemChassisidSubtype, lldpRemPortid, lldpRemPortidSubtype);
        assertEquals(4,lldpRemChassisidSubtype.intValue());
        assertEquals(5,lldpRemPortidSubtype.intValue());
        
        if (lldpRemLocalPortNum.intValue() == 1 ) {
            assertEquals(SWITCH1_NAME,lldpRemSysname);
            assertEquals(SWITCH1_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/9", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 2) {
            assertEquals(SWITCH1_NAME,lldpRemSysname);
            assertEquals(SWITCH1_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/10", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 3) {
            assertEquals(SWITCH1_NAME,lldpRemSysname);
            assertEquals(SWITCH1_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/11", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 4) {
            assertEquals(SWITCH1_NAME,lldpRemSysname);
            assertEquals(SWITCH1_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/12", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 19) {
            assertEquals(SWITCH3_NAME,lldpRemSysname);
            assertEquals(SWITCH3_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Fa0/19", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 20) {
            assertEquals(SWITCH3_NAME,lldpRemSysname);
            assertEquals(SWITCH3_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Fa0/20", lldpRemPortid);
        } else {
            assertEquals(false, true);
        }

    }

    private void checkSwitch3Row(LldpRemTableEntry lldpRemTableEntry) {
        final Integer lldpRemLocalPortNum = lldpRemTableEntry.getLldpRemLocalPortNum();
        final String lldpRemSysname = lldpRemTableEntry.getLldpRemSysname();
        final String lldpRemChassiid = lldpRemTableEntry.getLldpRemChassiid();
        final Integer lldpRemChassisidSubtype = lldpRemTableEntry.getLldpRemChassisidSubtype();
        String lldpRemPortid = lldpRemTableEntry.getLldpRemPortid();
        Integer lldpRemPortidSubtype = lldpRemTableEntry.getLldpRemPortidSubtype();
        printLldpRemRow(lldpRemLocalPortNum, lldpRemSysname, lldpRemChassiid, lldpRemChassisidSubtype, lldpRemPortid, lldpRemPortidSubtype);
        assertEquals(4,lldpRemChassisidSubtype.intValue());
        assertEquals(5,lldpRemPortidSubtype.intValue());
        
        if (lldpRemLocalPortNum.intValue() == 19) {
            assertEquals(SWITCH2_NAME,lldpRemSysname);
            assertEquals(SWITCH2_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/19", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 20) {
            assertEquals(SWITCH2_NAME,lldpRemSysname);
            assertEquals(SWITCH2_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("Gi0/20", lldpRemPortid);
        } else {
            assertEquals(false, true);
        }

    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE)
    })
    public void testSwitch1LldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpLocTable = new LldpLocTable(InetAddressUtils.addr(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpLocTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpLocTable.getEntries();
        assertEquals(30, lldpTableEntryCollection.size());
        
        for (final LldpLocTableEntry lldpLocTableEntry: lldpTableEntryCollection) {
            checkSwitch1Row(lldpLocTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE)
    })
    public void testSwitch2LldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpLocTable = new LldpLocTable(InetAddressUtils.addr(SWITCH2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpLocTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH2_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpLocTable.getEntries();
        assertEquals(27, lldpTableEntryCollection.size());
        
        for (final LldpLocTableEntry lldpLocTableEntry: lldpTableEntryCollection) {
            checkSwitch2Row(lldpLocTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE)
    })
    public void testSwitch3LldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpLocTable = new LldpLocTable(InetAddressUtils.addr(SWITCH3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpLocTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH3_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpLocTable.getEntries();
        assertEquals(28, lldpTableEntryCollection.size());
        
        for (final LldpLocTableEntry lldpLocTableEntry: lldpTableEntryCollection) {
            checkSwitch3Row(lldpLocTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE)
    })
    public void testSwitch4LldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpLocTable = new LldpLocTable(InetAddressUtils.addr(SWITCH4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpLocTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH4_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpLocTable.getEntries();
        assertEquals(27, lldpTableEntryCollection.size());
        
        for (final LldpLocTableEntry lldpLocTableEntry: lldpTableEntryCollection) {
            checkSwitch4Row(lldpLocTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE)
    })
    public void testSwitch5LldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpLocTable = new LldpLocTable(InetAddressUtils.addr(SWITCH5_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpLocTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH5_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpLocTable.getEntries();
        assertEquals(27, lldpTableEntryCollection.size());
        
        for (final LldpLocTableEntry lldpLocTableEntry: lldpTableEntryCollection) {
            checkSwitch5Row(lldpLocTableEntry);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host="10.1.1.2", port=161, resource="classpath:linkd/nms4930/dlink_DES-3026.properties")
    })
    public void testDot1qTpFdbTableWalk() throws Exception {

    	String trackerName = "dot1qTpFdbTable";
    	QBridgeDot1dTpFdbTable dot1qTpFdbTable = new QBridgeDot1dTpFdbTable(InetAddressUtils.addr("10.1.1.2"));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {dot1qTpFdbTable};
    	SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName("10.1.1.2"));

        SnmpWalker walker =  SnmpUtils.createWalker(config, trackerName, tracker);

        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                assertEquals(false, true);
            }  else if (walker.failed()) {
                assertEquals(false, true);
           }
        } catch (final InterruptedException e) {
            assertEquals(false, true);
            return;
        }

        final Collection<QBridgeDot1dTpFdbTableEntry> entries = dot1qTpFdbTable.getEntries();
        assertEquals(61, entries.size());

        for (QBridgeDot1dTpFdbTableEntry link: entries) {
        	System.out.println(link.getQBridgeDot1dTpFdbAddress());
        	System.out.println(link.getQBridgeDot1dTpFdbPort());
        	System.out.println(link.getQBridgeDot1dTpFdbStatus());
        }
    }


    private void checkSwitch1Row(LldpLocTableEntry lldpLocTableEntry) {
        final Integer lldpLocPortNum = lldpLocTableEntry.getLldpLocPortNum();
        String lldpLocPortid = lldpLocTableEntry.getLldpLocPortid();
        Integer lldpLocPortidSubtype = lldpLocTableEntry.getLldpLocPortIdSubtype();
        printLldpLocRow(lldpLocPortNum, lldpLocPortidSubtype, lldpLocPortid);
        assertEquals(5,lldpLocPortidSubtype.intValue());
        
        if (lldpLocPortNum.intValue() >= 1 && lldpLocPortNum.intValue() <= 28) {
            assertEquals("Gi0/"+lldpLocPortNum,lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 64) {
            assertEquals("Po1",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 448) {
            assertEquals("St1",lldpLocPortid);
        } else {
            assertEquals(true,false);
        }
    }

    private void checkSwitch2Row(LldpLocTableEntry lldpLocTableEntry) {
        final Integer lldpLocPortNum = lldpLocTableEntry.getLldpLocPortNum();
        String lldpLocPortid = lldpLocTableEntry.getLldpLocPortid();
        Integer lldpLocPortidSubtype = lldpLocTableEntry.getLldpLocPortIdSubtype();
        printLldpLocRow(lldpLocPortNum, lldpLocPortidSubtype, lldpLocPortid);
        assertEquals(5,lldpLocPortidSubtype.intValue());
        
        if (lldpLocPortNum.intValue() >= 1 && lldpLocPortNum.intValue() <= 24) {
            assertEquals("Gi0/"+lldpLocPortNum,lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 64) {
            assertEquals("Po1",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 72) {
            assertEquals("Po2",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 112) {
            assertEquals("St1",lldpLocPortid);
        } else {
            assertEquals(true,false);
        }
    }

    private void checkSwitch3Row(LldpLocTableEntry lldpLocTableEntry) {
        final Integer lldpLocPortNum = lldpLocTableEntry.getLldpLocPortNum();
        String lldpLocPortid = lldpLocTableEntry.getLldpLocPortid();
        Integer lldpLocPortidSubtype = lldpLocTableEntry.getLldpLocPortIdSubtype();
        printLldpLocRow(lldpLocPortNum, lldpLocPortidSubtype, lldpLocPortid);
        assertEquals(5,lldpLocPortidSubtype.intValue());
        
        if (lldpLocPortNum.intValue() >= 1 && lldpLocPortNum.intValue() <= 24) {
            assertEquals("Fa0/"+lldpLocPortNum,lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 25) {
            assertEquals("Gi0/1",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 26) {
            assertEquals("Gi0/2",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 56) {
            assertEquals("Po1",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 104) {
            assertEquals("St1",lldpLocPortid);
        } else {
            assertEquals(true,false);
        }
    }

    private void checkSwitch4Row(LldpLocTableEntry lldpLocTableEntry) {
        final Integer lldpLocPortNum = lldpLocTableEntry.getLldpLocPortNum();
        String lldpLocPortid = lldpLocTableEntry.getLldpLocPortid();
        Integer lldpLocPortidSubtype = lldpLocTableEntry.getLldpLocPortIdSubtype();
        printLldpLocRow(lldpLocPortNum, lldpLocPortidSubtype, lldpLocPortid);
        assertEquals(5,lldpLocPortidSubtype.intValue());
        
        if (lldpLocPortNum.intValue() >= 1 && lldpLocPortNum.intValue() <= 24) {
            assertEquals("Fa0/"+lldpLocPortNum,lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 25) {
            assertEquals("Gi0/1",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 26) {
            assertEquals("Gi0/2",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 104) {
            assertEquals("St1",lldpLocPortid);
        } else {
            assertEquals(true,false);
        }
    }
    
    private void checkSwitch5Row(LldpLocTableEntry lldpLocTableEntry) {
        checkSwitch4Row(lldpLocTableEntry);
    }


    private void checkRowNms1055(LldpRemTableEntry lldpRemTableEntry) {
        final Integer lldpRemLocalPortNum = lldpRemTableEntry.getLldpRemLocalPortNum();
        final String lldpRemSysname = lldpRemTableEntry.getLldpRemSysname();
        final String lldpRemChassiid = lldpRemTableEntry.getLldpRemChassiid();
        final Integer lldpRemChassisidSubtype = lldpRemTableEntry.getLldpRemChassisidSubtype();
        String lldpRemPortid = lldpRemTableEntry.getLldpRemPortid();
        Integer lldpRemPortidSubtype = lldpRemTableEntry.getLldpRemPortidSubtype();
        printLldpRemRow(lldpRemLocalPortNum, lldpRemSysname, lldpRemChassiid, lldpRemChassisidSubtype, lldpRemPortid, lldpRemPortidSubtype);
        assertEquals(4,lldpRemChassisidSubtype.intValue());
        assertEquals(7,lldpRemPortidSubtype.intValue());
        if (lldpRemLocalPortNum.intValue() == 574) {
            assertEquals(PENROSE_NAME,lldpRemSysname);
            assertEquals(PENROSE_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("510", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 522) {
            assertEquals(PENROSE_NAME,lldpRemSysname);
            assertEquals(PENROSE_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("525", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 575) {
            assertEquals(AUSTIN_NAME,lldpRemSysname);
            assertEquals(AUSTIN_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("509", lldpRemPortid);
        } else if (lldpRemLocalPortNum.intValue() == 540) {
            assertEquals(RIOVISTA_NAME,lldpRemSysname);
            assertEquals(RIOVISTA_LLDP_CHASSISID, lldpRemChassiid);
            assertEquals("503", lldpRemPortid);
        } else {
            assertEquals(false, true);
        }
    }
    
    private void checkRowNms1055(LldpLocTableEntry lldpLocTableEntry) {
        final Integer lldpLocPortNum = lldpLocTableEntry.getLldpLocPortNum();
        String lldpLocPortid = lldpLocTableEntry.getLldpLocPortid();
        Integer lldpLocPortidSubtype = lldpLocTableEntry.getLldpLocPortIdSubtype();
        printLldpLocRow(lldpLocPortNum, lldpLocPortidSubtype, lldpLocPortid);
        assertEquals(7,lldpLocPortidSubtype.intValue());

        if (lldpLocPortNum.intValue() == 521) {
            assertEquals("521",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 522) {
            assertEquals("522",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 574) {
            assertEquals("574",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 575) {
            assertEquals("575",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 540) {
            assertEquals("540",lldpLocPortid);
        } else {
            assertEquals(true,false);
        }
    }
    
    protected void printCdpRow(CdpCacheTableEntry cdpCacheTableEntry) {
        System.err.println("-----------------------------------------------------------");    
        System.err.println("getCdpCacheIfIndex: "+cdpCacheTableEntry.getCdpCacheIfIndex());
        System.err.println("getCdpCacheDeviceIndex: "+cdpCacheTableEntry.getCdpCacheDeviceIndex());
        System.err.println("getCdpCacheAddressType: "+cdpCacheTableEntry.getCdpCacheAddressType());
        System.err.println("getCdpCacheAddress: "+cdpCacheTableEntry.getCdpCacheAddress());
        if (cdpCacheTableEntry.getCdpCacheIpv4Address() != null )
            System.err.println("getCdpCacheIpv4Address: "+cdpCacheTableEntry.getCdpCacheIpv4Address().getHostName());
        System.err.println("getCdpCacheVersion: "+cdpCacheTableEntry.getCdpCacheVersion());
        System.err.println("getCdpCacheDeviceId: "+cdpCacheTableEntry.getCdpCacheDeviceId());
        System.err.println("getCdpCacheDevicePort: "+cdpCacheTableEntry.getCdpCacheDevicePort());
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        
        
    }

    protected void printLldpRemRow(Integer lldpRemLocalPortNum, String lldpRemSysname, 
            String lldpRemChassiid,Integer lldpRemChassisidSubtype,String lldpRemPortid, Integer lldpRemPortidSubtype) {
        System.err.println("-----------------------------------------------------------");    
        System.err.println("getLldpRemLocalPortNum: "+lldpRemLocalPortNum);
        System.err.println("getLldpRemSysname: "+lldpRemSysname);
        System.err.println("getLldpRemChassiid: "+lldpRemChassiid);
        System.err.println("getLldpRemChassisidSubtype: "+lldpRemChassisidSubtype);
        System.err.println("getLldpRemPortid: "+lldpRemPortid);
        System.err.println("getLldpRemPortidSubtype: "+lldpRemPortidSubtype);
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        
    }
    
    protected void printLldpLocRow(Integer lldpLocPortNum,
            Integer lldpLocPortidSubtype, String lldpLocPortid) {
        System.err.println("-----------------------------------------------------------");    
        System.err.println("getLldpLocPortNum: "+lldpLocPortNum);
        System.err.println("getLldpLocPortid: "+lldpLocPortid);
        System.err.println("getLldpRemPortidSubtype: "+lldpLocPortidSubtype);
        System.err.println("-----------------------------------------------------------");
        System.err.println("");
      
    }


}
