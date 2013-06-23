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

package org.opennms.netmgt.linkd.snmp;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.nb.Nms17216NetworkBuilder;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
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
public class Nms17216LldpTest extends Nms17216NetworkBuilder implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testNetwork17216Switch1LLDPLocalBaseCollection() throws Exception {

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
    public void testNetwork17216Switch2LLDPLocalBaseCollection() throws Exception {

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
    public void testNetwork17216Switch3LLDPLocalBaseCollection() throws Exception {

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
    public void testNetwork17216Switch4LLDPLocalBaseCollection() throws Exception {

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
    public void testNetwork17216Switch5LLDPLocalBaseCollection() throws Exception {

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
    public void testNetwork17216Switch1LldpRemTableCollection() throws Exception {

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
    public void testNetwork17216Switch2LldpRemTableCollection() throws Exception {

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
    public void testNetwork17216Switch3LldpRemTableCollection() throws Exception {

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
    public void testNetwork17216Switch4LldpRemTableCollection() throws Exception {

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
    public void testNetwork17216Switch5LldpRemTableCollection() throws Exception {

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
    public void testNetwork17216Switch1LldpLocTableCollection() throws Exception {

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
    public void testNetwork17216Switch2LldpLocTableCollection() throws Exception {

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
    public void testNetwork17216Switch3LldpLocTableCollection() throws Exception {

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
    public void testNetwork17216Switch4LldpLocTableCollection() throws Exception {

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
    public void testNetwork17216Switch5LldpLocTableCollection() throws Exception {

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

}
