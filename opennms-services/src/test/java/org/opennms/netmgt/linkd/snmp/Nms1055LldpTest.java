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
import org.opennms.netmgt.linkd.nb.Nms1055NetworkBuilder;
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
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class Nms1055LldpTest extends Nms1055NetworkBuilder implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt")
    })
    public void testNetwork1055PenroseLLDPLocalBaseCollection() throws Exception {

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
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
    })
    public void testNetwork1055DelawareLLDPLocalBaseCollection() throws Exception {

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
            @JUnitSnmpAgent(host=PHOENIX_IP, port=161, resource="classpath:linkd/nms1055/"+PHOENIX_NAME+"_"+PHOENIX_IP+".txt")
    })
    public void testNetwork1055PhoenixLLDPLocalBaseCollection() throws Exception {

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
            @JUnitSnmpAgent(host=AUSTIN_IP, port=161, resource="classpath:linkd/nms1055/"+AUSTIN_NAME+"_"+AUSTIN_IP+".txt")
    })
    public void testNetwork1055AustinLLDPLocalBaseCollection() throws Exception {

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
            @JUnitSnmpAgent(host=SANJOSE_IP, port=161, resource="classpath:linkd/nms1055/"+SANJOSE_NAME+"_"+SANJOSE_IP+".txt")
    })
    public void testNetwork1055SanjoseLLDPLocalBaseCollection() throws Exception {

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
            @JUnitSnmpAgent(host=RIOVISTA_IP, port=161, resource="classpath:linkd/nms1055/"+RIOVISTA_NAME+"_"+RIOVISTA_IP+".txt")
    })
    public void testNetwork1055RiovistaLLDPLocalBaseCollection() throws Exception {

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
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt")
    })
    public void testNetwork1055Dot1dBaseCollection() throws Exception {

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
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
    })
    public void testNetwork1055LldpLocTableCollection() throws Exception {

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
            checkRow(lldpLocTableEntry);
        }
    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
    })
    public void testNetwork1055LldpRemTableCollection() throws Exception {

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
            checkRow(lldpRemTableEntry);
        }
    }
    
    private void checkRow(LldpRemTableEntry lldpRemTableEntry) {
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
    
    private void checkRow(LldpLocTableEntry lldpLocTableEntry) {
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
}
