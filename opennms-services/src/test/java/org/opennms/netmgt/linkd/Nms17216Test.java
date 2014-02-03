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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.linkd.snmp.CdpCacheTable;
import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.linkd.snmp.CdpInterfaceTable;
import org.opennms.netmgt.linkd.snmp.CdpInterfaceTableEntry;
import org.opennms.netmgt.linkd.snmp.CiscoVlanTable;
import org.opennms.netmgt.linkd.snmp.CiscoVlanTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocTable;
import org.opennms.netmgt.linkd.snmp.LldpLocTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocalGroup;
import org.opennms.netmgt.linkd.snmp.LldpRemTable;
import org.opennms.netmgt.linkd.snmp.LldpRemTableEntry;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

public class Nms17216Test extends Nms17216NetworkBuilder {

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/switch5-walk.txt")
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
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource="classpath:linkd/nms17216/router1-walk.txt")
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
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource="classpath:linkd/nms17216/router2-walk.txt")
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
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource="classpath:linkd/nms17216/router3-walk.txt")
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
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource="classpath:linkd/nms17216/router4-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/switch5-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/switch5-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt")
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
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/switch5-walk.txt")
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
     * Here we add cdp discovery and all test lab devices
     * To the previuos links discovered by lldp
     * should be added the followings discovered with cdp:
     * switch3 Fa0/23 Fa0/24 ---> switch5 Fa0/1 Fa0/9
     * router1 Fa0/0 ----> switch1 Gi0/1
     * router2 Serial0/0/0 ----> router1 Serial0/0/0
     * router3 Serial0/0/1 ----> router2 Serial0/0/1
     * router4 GigabitEthernet0/1 ----> router3   GigabitEthernet0/0
     * switch4 FastEthernet0/1    ----> router3   GigabitEthernet0/1
     * 
     * here are the corresponding ifindex:
     * switch1 Gi0/1 -->  10101
     * 
     * switch3 Fa0/23 -->  10023
     * switch3 Fa0/24 -->  10024
     *
     * switch5 Fa0/1 -->  10001
     * switch5 Fa0/13 -->  10013
     * 
     * router1 Fa0/0 -->  7
     * router1 Serial0/0/0 --> 13
     * router1 Serial0/0/1 --> 14
     * 
     * router2 Serial0/0/0 --> 12
     * router2 Serial0/0/1 --> 13
     * 
     * router3 Serial0/0/1 --> 13
     * router3 GigabitEthernet0/0 --> 8
     * router3 GigabitEthernet0/1 --> 9
     * 
     * router4 GigabitEthernet0/1  --> 3
     * 
     * switch4 FastEthernet0/1 --> 10001
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/switch5-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource="classpath:linkd/nms17216/router1-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource="classpath:linkd/nms17216/router2-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource="classpath:linkd/nms17216/router3-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource="classpath:linkd/nms17216/router4-walk.txt")
    })
    public void testNetwork17216Links() throws Exception {
        
        m_nodeDao.save(getSwitch1());
        m_nodeDao.save(getSwitch2());
        m_nodeDao.save(getSwitch3());
        m_nodeDao.save(getSwitch4());
        m_nodeDao.save(getSwitch5());
        m_nodeDao.save(getRouter1());
        m_nodeDao.save(getRouter2());
        m_nodeDao.save(getRouter3());
        m_nodeDao.save(getRouter4());

        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);

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
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch5.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router4.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        final Collection<LinkableNode> nodes = m_linkd.getLinkableNodesOnPackage("example1");

        assertEquals(9, nodes.size());
        
        for (LinkableNode node: nodes) {
            switch(node.getNodeId()) {
                case 1: assertEquals(5, node.getCdpInterfaces().size());
                assertEquals(SWITCH1_NAME, node.getCdpDeviceId());
                break;
                case 2: assertEquals(6, node.getCdpInterfaces().size());
                assertEquals(SWITCH2_NAME, node.getCdpDeviceId());
                break;
                case 3: assertEquals(4, node.getCdpInterfaces().size());
                assertEquals(SWITCH3_NAME, node.getCdpDeviceId());
                break;
                case 4: assertEquals(1, node.getCdpInterfaces().size());
                assertEquals(SWITCH4_NAME, node.getCdpDeviceId());
                break;
                case 5: assertEquals(2, node.getCdpInterfaces().size());
                assertEquals(SWITCH5_NAME, node.getCdpDeviceId());
                break;
                case 6: assertEquals(2, node.getCdpInterfaces().size());
                assertEquals(ROUTER1_NAME, node.getCdpDeviceId());
                break;
                case 7: assertEquals(2, node.getCdpInterfaces().size());
                assertEquals(ROUTER2_NAME, node.getCdpDeviceId());
                break;
                case 8: assertEquals(3, node.getCdpInterfaces().size());
                assertEquals(ROUTER3_NAME, node.getCdpDeviceId());
                break;
                case 9: assertEquals(1, node.getCdpInterfaces().size());
                assertEquals(ROUTER4_NAME, node.getCdpDeviceId());
                break;
                default: assertEquals(-1, node.getNodeId());
                break;
            }
        }        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(19,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();

        int start=getStartPoint(datalinkinterfaces);

        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {
            Integer linkid = datalinkinterface.getId();
            if ( linkid == start) {
                // switch1 gi0/9 -> switch2 gi0/1 --lldp --cdp
                checkLink(switch2, switch1, 10101, 10109, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+11) {
                checkLink(switch2, switch1, 10101, 10109, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+1 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp --cdp
                checkLink(switch2, switch1, 10102, 10110, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+12 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp --cdp
                checkLink(switch2, switch1, 10102, 10110, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+2) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp --cdp
                checkLink(switch2, switch1, 10103, 10111, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+13) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp --cdp
                checkLink(switch2, switch1, 10103, 10111, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+3) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp --cdp
                checkLink(switch2, switch1, 10104, 10112, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+14) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp --cdp
                checkLink(switch2, switch1, 10104, 10112, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+4) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp --cdp
                checkLink(switch3, switch2, 10019, 10119, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+15) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp --cdp
                checkLink(switch3, switch2, 10019, 10119, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+5) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp --cdp
                checkLink(switch3, switch2, 10020, 10120, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+16) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp --cdp
                checkLink(switch3, switch2, 10020, 10120, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+6) {
                checkLink(router4, router3, 3, 8, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+7) {
                checkLink(router2, router1, 12, 13, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+8) {
                checkLink(router3, router2, 13, 13, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+9) {
                //switch4 FastEthernet0/1    ----> router3   GigabitEthernet0/1
                checkLink(router3, switch4, 9, 10001, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+10) {
                // switch1 gi0/1 -> router1 Fa0/20 --cdp
                checkLink(router1, switch1, 7, 10101, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+17) {
                // switch3 Fa0/1 -> switch5 Fa0/23 --cdp
                checkLink(switch5, switch3, 10001, 10023, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+18) {
                // switch3 gi0/1 -> switch5 Fa0/20 --cdp
                checkLink(switch5, switch3, 10013, 10024, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else {
                // error
                checkLink(switch1,switch1,-1,-1,datalinkinterface);
            }      
        }
    }

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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt")
    })
    public void testNetwork17216LldpLinks() throws Exception {
        m_nodeDao.save(getSwitch1());
        m_nodeDao.save(getSwitch2());
        m_nodeDao.save(getSwitch3());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setUseBridgeDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
               
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(6,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();

        int startid = getStartPoint(links);
        for (final DataLinkInterface link: links) {
//            printLink(datalinkinterface);
            Integer linkid = link.getId();
            if ( linkid == startid) {
                // switch1 gi0/9 -> switch2 gi0/1 --lldp
                checkLink(switch2, switch1, 10101, 10109, link);
            } else if (linkid == startid +1 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp
                checkLink(switch2, switch1, 10102, 10110, link);
            } else if (linkid == startid+2) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp
                checkLink(switch2, switch1, 10103, 10111, link);
            } else if (linkid == startid+3) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp
                checkLink(switch2, switch1, 10104, 10112, link);
            } else if (linkid == startid+4) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp
                checkLink(switch3, switch2, 10019, 10119, link);
            } else if (linkid == startid+5) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp
                checkLink(switch3, switch2, 10020, 10120, link);
            } else {
                // error
                checkLink(switch1,switch1,-1,-1,link);
            }   
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource="classpath:linkd/nms17216/router3-walk.txt")
    })
    public void testNetwork17216Switch4Router4CdpLinks() throws Exception {
        
        m_nodeDao.save(getSwitch4());
        m_nodeDao.save(getRouter3());

        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setUseLldpDiscovery(false);
        example1.setUseBridgeDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseCdpDiscovery(true);
        example1.setEnableVlanDiscovery(false);
        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setUseIsisDiscovery(false);

        
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode router3 = m_nodeDao.findByForeignId("linkd", ROUTER3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch4.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router3.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        
        final Collection<LinkableNode> nodes = m_linkd.getLinkableNodesOnPackage("example1");

        assertEquals(2, nodes.size());
        
        for (LinkableNode node: nodes) {
            assertEquals(1, node.getCdpInterfaces().size());
        }
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(1,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();
                
        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {

                checkLink(router3, switch4, 9, 10001, datalinkinterface);
        }
    }

}
