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
import org.opennms.netmgt.linkd.snmp.Dot1dBaseGroup;
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
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

public class Nms1055Test extends Nms1055NetworkBuilder {

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt")
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
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
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
            @JUnitSnmpAgent(host=PHOENIX_IP, port=161, resource="classpath:linkd/nms1055/"+PHOENIX_NAME+"_"+PHOENIX_IP+".txt")
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
            @JUnitSnmpAgent(host=AUSTIN_IP, port=161, resource="classpath:linkd/nms1055/"+AUSTIN_NAME+"_"+AUSTIN_IP+".txt")
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
            @JUnitSnmpAgent(host=SANJOSE_IP, port=161, resource="classpath:linkd/nms1055/"+SANJOSE_NAME+"_"+SANJOSE_IP+".txt")
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
            @JUnitSnmpAgent(host=RIOVISTA_IP, port=161, resource="classpath:linkd/nms1055/"+RIOVISTA_NAME+"_"+RIOVISTA_IP+".txt")
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
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt")
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
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
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
            checkRow(lldpLocTableEntry);
        }
    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
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
    
    /*
     * Penrose: baseBridgeAddress = 80711f8fafd0
     * Penrose: stpDesignatedRoot = 001f12373dc0
     * Penrose: stpport/designatedbridge/designatedport 62/8000 001f12373dc0/8201 
     *          -----To Riovista - Root Spanning tree: ifindex 515 ---ge-1/2/1
     * Penrose: stpport/designatedbridge/designatedport 483/8000 0022830957d0/81e3
     *          -----this is a backbone port to a higher bridge: ifindex 2693 -- ae0
     *          ---- aggregated port almost sure the link to Delaware
     *          
     * Delaware: baseBridgeAddress = 0022830957d0
     * Delaware: stpDesignatedRoot = 001f12373dc0
     * Delaware: stpport/designatedbridge/designatedport 21/8000 001f12373dc0/822f 
     *          -----To Riovista - Root Spanning tree: ifindex 540 -- ge-0/2/0
     * Delaware: stpport/designatedbridge/designatedport 483/8000 0022830957d0/81e3
     *          -----this is a backbone port to a lower bridge: ifindex 658 ---ae0
     *          -----aggregated port almost sure the link to Penrose
     *
     * Riovista: baseBridgeAddress = 001f12373dc0
     * Riovista: stpDesignatedRoot = 001f12373dc0
     * Riovista: stpport/designatedbridge/designatedport 513/8000 001f12373dc0/8201 
     *          -----To Penrose ifindex 584 ---ge-0/0/0.0
     * Riovista: stpport/designatedbridge/designatedport 559/8000001f12373dc0/822f
     *          -----To Delaware ifindex 503 ---ge-0/0/46.0
     *
     *          
     * Phoenix: baseBridgeAddress = 80711fc414d0
     * Phoenix: Spanning Tree is disabled
     * 
     * Austin: baseBridgeAddress = 80711fc413d0
     * Austin: Spanning Tree is disabled
     * 
     * Sanjose: baseBridgeAddress = 002283d857d0
     * Sanjose: Spanning Tree is disabled
     * 
     * There are two links found between Penrose and Delaware,
     * one on ae0 using stp and another over xe-1/0/0.0 using the ip route next hop strategy
     * 
     * Also the link between Austin and Delaware is not found because
     * no route entry is found so no way to find it.
     * This prove how weak is the way in which is set up linkd.
     * This test passes because i've verified that this is what the linkd can discover using it's values
     * 
     * root@sanjose-mx240# run show lldp neighbors    
     * Local Interface Chassis Id        Port info     System Name
     * ge-1/0/1        80:71:1f:c4:13:c0  ge-1/0/3     Austin       
     * ge-1/0/0        80:71:1f:c4:14:c0  ge-1/0/3     phoenix-mx80 
     * 
     * root@phoenix-mx80# run show lldp neighbors 
     * Local Interface Chassis Id        Port info     System Name
     * ge-1/0/3        00:22:83:d8:57:c0  ge-1/0/0     sanjose-mx240 
     * xe-0/0/1        80:71:1f:8f:af:c0  xe-1/0/1     penrose-mx480 
     * xe-0/0/0        80:71:1f:c4:13:c0  <ToPHX-xe000> Austin
     * root@Austin# run show lldp neighbors 
     * Local Interface Chassis Id        Port info     System Name
     * xe-0/0/1        00:22:83:09:57:c0  xe-1/0/1     delaware     
     * ge-1/0/3        00:22:83:d8:57:c0  ge-1/0/1     sanjose-mx240 
     * xe-0/0/0        80:71:1f:c4:14:c0  <ToAUS-xe000> phoenix-mx80 
     * 
     * root@penrose-mx480# run show lldp neighbors 
     * Local Interface Chassis Id        Port info     System Name
     * ge-1/2/1        00:1f:12:37:3d:c0  ge-0/0/0.0   Riovista-ce  
     * ge-1/3/1        00:22:83:09:57:c0  ge-0/0/6     delaware     
     * xe-1/0/0        00:22:83:09:57:c0  <To_Penrose> delaware     
     * xe-1/0/1        80:71:1f:c4:14:c0  xe-0/0/1     phoenix-mx80 
     * 
     * root@delaware# run show lldp neighbors 
     * Local Interface Chassis Id        Port info     System Name
     * ge-0/2/0        00:1f:12:37:3d:c0  ge-0/0/46.0  Riovista-ce  
     * xe-1/0/0        80:71:1f:8f:af:c0  <To_Delaware> penrose-mx480 
     * ge-0/0/6        80:71:1f:8f:af:c0  ge-1/3/1     penrose-mx480 
     * xe-1/0/1        80:71:1f:c4:13:c0  xe-0/0/1     Austin      
     * 
     * root@Riovista-ce# run show lldp neighbors 
     * Local Interface    Parent Interface    Chassis Id          Port info          System Name
     * ge-0/0/46.0        -                   00:22:83:09:57:c0   ge-0/2/0           delaware            
     * ge-0/0/0.0         -                   80:71:1f:8f:af:c0   ge-1/2/1           penrose-mx480       
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt"),
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt"),
            @JUnitSnmpAgent(host=PHOENIX_IP, port=161, resource="classpath:linkd/nms1055/"+PHOENIX_NAME+"_"+PHOENIX_IP+".txt"),
            @JUnitSnmpAgent(host=AUSTIN_IP, port=161, resource="classpath:linkd/nms1055/"+AUSTIN_NAME+"_"+AUSTIN_IP+".txt"),
            @JUnitSnmpAgent(host=SANJOSE_IP, port=161, resource="classpath:linkd/nms1055/"+SANJOSE_NAME+"_"+SANJOSE_IP+".txt"),
            @JUnitSnmpAgent(host=RIOVISTA_IP, port=161, resource="classpath:linkd/nms1055/"+RIOVISTA_NAME+"_"+RIOVISTA_IP+".txt")
    })
    public void testNetwork1055Links() throws Exception {
        m_nodeDao.save(getPenrose());
        m_nodeDao.save(getDelaware());
        m_nodeDao.save(getPhoenix());
        m_nodeDao.save(getAustin());
        m_nodeDao.save(getSanjose());
        m_nodeDao.save(getRiovista());
        m_nodeDao.flush();

        HibernateEventWriter queryManager = (HibernateEventWriter)m_linkd.getQueryManager();
        /*
         *         DELAWARE_IF_IFNAME_MAP.put(517, "ge-0/0/1");
         *         DELAWARE_IF_IFALIAS_MAP.put(517, "test");
         */
        assertEquals(517, queryManager.getFromSysnameIfAlias(DELAWARE_NAME, "test").getIfIndex().intValue());
        assertEquals(517, queryManager.getFromSysnameIfName(DELAWARE_NAME, "ge-0/0/1").getIfIndex().intValue());

        /*
         * DELAWARE_IF_MAC_MAP.put(585, "0022830951f5");
         */
        assertEquals(585, queryManager.getFromSysnameMacAddress(DELAWARE_NAME, "0022830951f5").getIfIndex().intValue());
        /*
         * DELAWARE_IP_IF_MAP.put(InetAddressUtils.addr("10.155.69.17"), 13);
         */
        assertEquals(13, queryManager.getFromSysnameIpAddress(DELAWARE_NAME, InetAddressUtils.addr("10.155.69.17")).getIfIndex().intValue());
   
        /*
         * DELAWARE_IF_IFALIAS_MAP.put(574, "<To_Penrose>");
         */
        assertEquals(574, queryManager.getFromSysnameIfAlias(DELAWARE_NAME, "<To_Penrose>").getIfIndex().intValue());

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        
        final OnmsNode penrose = m_nodeDao.findByForeignId("linkd", PENROSE_NAME);
        final OnmsNode delaware = m_nodeDao.findByForeignId("linkd", DELAWARE_NAME);
        final OnmsNode phoenix = m_nodeDao.findByForeignId("linkd", PHOENIX_NAME);
        final OnmsNode austin = m_nodeDao.findByForeignId("linkd", AUSTIN_NAME);
        final OnmsNode sanjose = m_nodeDao.findByForeignId("linkd", SANJOSE_NAME);
        final OnmsNode riovista = m_nodeDao.findByForeignId("linkd", RIOVISTA_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(penrose.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delaware.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(phoenix.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(austin.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(sanjose.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(riovista.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(penrose.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delaware.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(phoenix.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(austin.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(sanjose.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(riovista.getId()));
               
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(22,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
                
        final int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            Integer linkid = datalinkinterface.getId();
            
            if ( linkid == start) {
                checkLink(penrose,phoenix , 644, 564, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol()); 
            } else if (linkid == start+6 ) {
                checkLink(phoenix, penrose, 564, 644, datalinkinterface);
               assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol()); 
            } else if (linkid == start+1 ) {
                checkLink(penrose, delaware, 535, 598, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol()); 
            } else if (linkid == start+5 ) {
                checkLink(delaware, penrose, 598, 535, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol()); 
            } else if (linkid == start+2) {
                checkLink(phoenix,austin,565,554,datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol()); 
            } else if (linkid == start+7) {
                checkLink(austin,phoenix,554,565,datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (linkid == start+3) {
                checkLink(sanjose,phoenix,564,566,datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol()); 
            } else if (linkid == start+8) {
                checkLink(sanjose,phoenix,564,566,datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (linkid == start+4) {
                checkLink(sanjose,austin, 8562, 586, datalinkinterface);
                assertEquals(DiscoveryProtocol.iproute, datalinkinterface.getProtocol()); 
            } else if (linkid == start+9) {
                checkLink(sanjose,austin, 8562, 586, datalinkinterface);
                assertEquals(DiscoveryProtocol.ospf, datalinkinterface.getProtocol());
            } else if (linkid == start+10) {
                // penrose xe-1/0/0 -> delaware xe-1/0/0 --lldp
                checkLink(delaware, penrose, 574, 510, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+11) {
                // penrose xe-1/0/1 -> phoenix xe-0/0/1  --lldp
                checkLink(phoenix, penrose, 509, 511, datalinkinterface);   
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+12) {
                // penrose ge-1/3/1 -> delaware ge-0/0/6 --lldp
                checkLink(delaware, penrose, 522, 525, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+13) {
                // penrose ge-1/2/1 -> riovista ge-0/0/0.0  --lldp
                // this link is also discovered using the bridge strategy
                checkLink(riovista, penrose, 584, 515, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if ( linkid == start+14) {
                // delaware xe-1/0/1 -> austin xe-0/0/1  --lldp
                checkLink(austin, delaware, 509, 575, datalinkinterface);                   
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+15 ) {
                // delaware ge-0/2/0 -> riovista ge-0/0/46.0  --lldp
                // this link is also discovered using the bridge strategy
                checkLink(riovista, delaware, 503, 540, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+16) {
                // phoenix ge-1/0/3 -> sanjose ge-1/0/0  --lldp
                checkLink(sanjose, phoenix, 516, 515, datalinkinterface);                   
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+17) {
                // phoenix ge-0/2/0 -> austin ge-0/0/46.0  --lldp
                checkLink(austin, phoenix, 508, 508, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+18) {
                // austin ge-1/0/3 -> sanjose ge-1/0/1  --lldp
                checkLink(sanjose, austin, 517, 515, datalinkinterface);                
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+19) {
                checkLink(penrose,riovista,515,584,datalinkinterface);
                assertEquals(DiscoveryProtocol.bridge, datalinkinterface.getProtocol());
            } else if (linkid == start+20) {
                checkLink(penrose,delaware,2693,658,datalinkinterface);
                assertEquals(DiscoveryProtocol.bridge, datalinkinterface.getProtocol());
            } else if (linkid == start+21) {
                checkLink(delaware,riovista,540,503,datalinkinterface);
                assertEquals(DiscoveryProtocol.bridge, datalinkinterface.getProtocol());
            } else {
                checkLink(penrose,penrose,-1,-1,datalinkinterface);
            }
        }
    }
    
    /*
    * We want to test that the next hop router discovered 
    * links can be discovered using the ospf neb table
    */
   @Test
   @JUnitSnmpAgents(value={
           @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt"),
           @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
   })
   public void testNetwork1055StpLinks() throws Exception {
       m_nodeDao.save(getPenrose());
       m_nodeDao.save(getDelaware());
       m_nodeDao.flush();
       
       Package example1 = m_linkdConfig.getPackage("example1");
       example1.setUseBridgeDiscovery(true);
       example1.setUseLldpDiscovery(false);
       example1.setUseCdpDiscovery(false);
       example1.setUseIpRouteDiscovery(false);
       example1.setUseOspfDiscovery(false);
       example1.setUseIsisDiscovery(false);

       example1.setSaveRouteTable(false);
       example1.setSaveStpInterfaceTable(false);
       example1.setSaveStpNodeTable(false);

       final OnmsNode penrose = m_nodeDao.findByForeignId("linkd", PENROSE_NAME);
       final OnmsNode delaware = m_nodeDao.findByForeignId("linkd", DELAWARE_NAME);

       assertTrue(m_linkd.scheduleNodeCollection(penrose.getId()));
       assertTrue(m_linkd.scheduleNodeCollection(delaware.getId()));

       assertTrue(m_linkd.runSingleSnmpCollection(penrose.getId()));
       assertTrue(m_linkd.runSingleSnmpCollection(delaware.getId()));
 
       assertEquals(0,m_dataLinkInterfaceDao.countAll());

       assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

       assertEquals(1,m_dataLinkInterfaceDao.countAll());

   }
   

    
    /*
     * We want to test that the next hop router discovered 
     * links can be discovered using the ospf neb table
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt"),
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt"),
            @JUnitSnmpAgent(host=PHOENIX_IP, port=161, resource="classpath:linkd/nms1055/"+PHOENIX_NAME+"_"+PHOENIX_IP+".txt"),
            @JUnitSnmpAgent(host=AUSTIN_IP, port=161, resource="classpath:linkd/nms1055/"+AUSTIN_NAME+"_"+AUSTIN_IP+".txt"),
            @JUnitSnmpAgent(host=SANJOSE_IP, port=161, resource="classpath:linkd/nms1055/"+SANJOSE_NAME+"_"+SANJOSE_IP+".txt"),
            @JUnitSnmpAgent(host=RIOVISTA_IP, port=161, resource="classpath:linkd/nms1055/"+RIOVISTA_NAME+"_"+RIOVISTA_IP+".txt")
    })
    public void testNetwork1055OspfLinks() throws Exception {
        m_nodeDao.save(getPenrose());
        m_nodeDao.save(getDelaware());
        m_nodeDao.save(getPhoenix());
        m_nodeDao.save(getAustin());
        m_nodeDao.save(getSanjose());
        m_nodeDao.save(getRiovista());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(true);
        example1.setUseIsisDiscovery(false);

        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        m_linkdConfig.update();

        
        final OnmsNode penrose = m_nodeDao.findByForeignId("linkd", PENROSE_NAME);
        final OnmsNode delaware = m_nodeDao.findByForeignId("linkd", DELAWARE_NAME);
        final OnmsNode phoenix = m_nodeDao.findByForeignId("linkd", PHOENIX_NAME);
        final OnmsNode austin = m_nodeDao.findByForeignId("linkd", AUSTIN_NAME);
        final OnmsNode sanjose = m_nodeDao.findByForeignId("linkd", SANJOSE_NAME);
        final OnmsNode riovista = m_nodeDao.findByForeignId("linkd", RIOVISTA_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(penrose.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delaware.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(phoenix.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(austin.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(sanjose.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(riovista.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(penrose.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delaware.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(phoenix.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(austin.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(sanjose.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(riovista.getId()));
               
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(5,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
                
        final int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            Integer linkid = datalinkinterface.getId();
            if ( linkid == start) {
                // penrose  -> delaware --ip route next hop
                checkLink(delaware, penrose, 598, 535, datalinkinterface);
            } else if (linkid == start+1 ) {
                // penrose   -> phoenix     --ip route next hop
                checkLink(phoenix, penrose, 564, 644, datalinkinterface);
            } else if (linkid == start+2) {
                // phoenix  -> austin --ip route next hop
                checkLink(austin,phoenix,554,565,datalinkinterface);
            } else if (linkid == start+3) {
                // phoenix  -> sanjose --ip route next hop
                checkLink(sanjose,phoenix,564,566,datalinkinterface);
            } else if (linkid == start+4) {
                // austin  -> sanjose --ip route next hop
                checkLink(sanjose,austin ,8562 , 586, datalinkinterface);
            } else {
                // error
                checkLink(penrose,penrose,-1,-1,datalinkinterface);
            }
        }
    }

}
