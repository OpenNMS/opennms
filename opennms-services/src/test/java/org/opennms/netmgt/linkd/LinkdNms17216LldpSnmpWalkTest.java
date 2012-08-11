/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.snmp.LldpLocalGroup;
import org.opennms.netmgt.linkd.snmp.LldpRemTable;
import org.opennms.netmgt.linkd.snmp.LldpRemTableEntry;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-linkd-test.xml"
})
@JUnitConfigurationEnvironment
public class LinkdNms17216LldpSnmpWalkTest extends LinkdNms17216NetworkBuilder implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH1_NAME+"-walk.txt")
    })
    public void testNetwork17216Switch1LLDPLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddress.getByName(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
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
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH2_NAME+"-walk.txt")
    })
    public void testNetwork17216SWITCH2LLDPLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddress.getByName(SWITCH2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH2_IP));
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
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH3_NAME+"-walk.txt")
    })
    public void testNetwork17216SWITCH3LLDPLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddress.getByName(SWITCH3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH3_IP));
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
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH4_NAME+"-walk.txt")
    })
    public void testNetwork17216SWITCH4LLDPLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddress.getByName(SWITCH4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH4_IP));
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
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH5_NAME+"-walk.txt")
    })
    public void testNetwork17216SWITCH5LLDPLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddress.getByName(SWITCH5_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH5_IP));
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
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH1_NAME+"-walk.txt")
    })
    public void testNetwork17216Switch1LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddress.getByName(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
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
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH2_NAME+"-walk.txt")
    })
    public void testNetwork17216Switch2LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddress.getByName(SWITCH2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH2_IP));
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
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH3_NAME+"-walk.txt")
    })
    public void testNetwork17216Switch3LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddress.getByName(SWITCH3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH3_IP));
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

    private void printRow(Integer lldpRemLocalPortNum, String lldpRemSysname, 
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
    
    private void checkSwitch1Row(LldpRemTableEntry lldpRemTableEntry) {
        final Integer lldpRemLocalPortNum = lldpRemTableEntry.getLldpRemLocalPortNum();
        final String lldpRemSysname = lldpRemTableEntry.getLldpRemSysname();
        final String lldpRemChassiid = lldpRemTableEntry.getLldpRemChassiid();
        final Integer lldpRemChassisidSubtype = lldpRemTableEntry.getLldpRemChassisidSubtype();
        String lldpRemPortid = lldpRemTableEntry.getLldpRemPortid();
        Integer lldpRemPortidSubtype = lldpRemTableEntry.getLldpRemPortidSubtype();
        printRow(lldpRemLocalPortNum, lldpRemSysname, lldpRemChassiid, lldpRemChassisidSubtype, lldpRemPortid, lldpRemPortidSubtype);
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
        printRow(lldpRemLocalPortNum, lldpRemSysname, lldpRemChassiid, lldpRemChassisidSubtype, lldpRemPortid, lldpRemPortidSubtype);
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
        printRow(lldpRemLocalPortNum, lldpRemSysname, lldpRemChassiid, lldpRemChassisidSubtype, lldpRemPortid, lldpRemPortidSubtype);
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

}
