//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.utils.IPSorter;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;

/**
 * Tests SnmpPeerFactory (created initially to test the configureSNMP event handling)
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class SnmpPeerFactoryTest extends TestCase {
    
    String m_SnmpSpec = "<?xml version=\"1.0\"?>\n" + 
            "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
            "   read-community=\"public\" write-community=\"private\">\n" + 
            "   <definition version=\"v2c\">\n" + 
            "       <specific>192.168.0.5</specific>\n" + 
            "   </definition>\n" + 
            "\n" + 
            "   <definition read-community=\"opennmsrules\">\n" + 
            "       <range begin=\"192.168.100.1\" end=\"192.168.100.254\"/>\n" + 
            "       <range begin=\"192.168.101.1\" end=\"192.168.101.254\"/>\n" + 
            "       <range begin=\"192.168.102.1\" end=\"192.168.102.254\"/>\n" + 
            "       <range begin=\"192.168.103.1\" end=\"192.168.103.254\"/>\n" + 
            "       <range begin=\"192.168.104.1\" end=\"192.168.104.254\"/>\n" + 
            "       <range begin=\"192.168.105.1\" end=\"192.168.105.254\"/>\n" + 
            "       <range begin=\"192.168.106.1\" end=\"192.168.106.254\"/>\n" + 
            "       <range begin=\"192.168.107.1\" end=\"192.168.107.254\"/>\n" +
            "       <range begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
            "   </definition>\n" + 
            "   <definition version=\"v2c\" read-community=\"splice-test\">\n" + 
            "       <specific>10.1.1.1</specific>\n" + 
            "       <specific>10.1.1.2</specific>\n" + 
            "       <specific>10.1.1.3</specific>\n" + 
            "       <specific>10.1.1.5</specific>\n" + 
            "       <specific>10.1.1.6</specific>\n" + 
            "       <specific>10.1.1.10</specific>\n" + 
            "       <range begin=\"10.1.2.1\" end=\"10.1.2.100\"/>\n" + 
            "       <range begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
            "       <range begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
            "   </definition>\n" + 
            "   <definition read-community=\"splice2-test\">\n" + 
            "       <specific>10.1.1.10</specific>\n" + 
            "       <range begin=\"10.1.1.11\" end=\"10.1.1.100\"/>\n" + 
            "       <range begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
            "       <range begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
            "   </definition>\n" + 
            "   <definition read-community=\"splice3-test\">\n" + 
            "       <specific>10.1.1.10</specific>\n" + 
            "       <specific>10.1.1.12</specific>\n" + 
            "       <range begin=\"10.1.1.11\" end=\"10.1.1.100\"/>\n" + 
            "       <range begin=\"11.1.2.1\" end=\"11.1.2.1\"/>\n" + 
            "       <range begin=\"12.1.2.1\" end=\"12.1.2.1\"/>\n" + 
            "   </definition>\n" + 
            "\n" + 
            "</snmp-config>\n" + 
            "";
    final private int m_startingDefCount = 5;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        Reader rdr = new StringReader(m_SnmpSpec);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
    }

    /**
     * Test method for {@link org.opennms.netmgt.config.SnmpPeerFactory#toLong(java.net.InetAddress)}.
     * Tests creating a string representation of an IP address that is converted to an InetAddress and then
     * a long and back to an IP address.
     * 
     * @throws UnknownHostException 
     */
    public void testToLongToAddr() throws UnknownHostException {
        String addr = "192.168.1.1";
        assertEquals(addr, SnmpPeerFactory.toIpAddr(SnmpPeerFactory.toLong(InetAddress.getByName(addr))));
    }


    /**
     * Test method for {@link org.opennms.netmgt.config.SnmpPeerFactory#createSnmpEventInfo(org.opennms.netmgt.xml.event.Event)}.
     * Tests creating an SNMP config definition from a configureSNMP event.
     * 
     * @throws UnknownHostException 
     */
    public void testCreateSnmpEventInfo() throws UnknownHostException {
        Event event = createConfigureSnmpEvent("192.168.1.1", null);
        addCommunityStringToEvent(event, "seemore");
        
        SnmpEventInfo info = SnmpPeerFactory.getInstance().createSnmpEventInfo(event);
        
        assertNotNull(info);
        assertEquals("192.168.1.1", info.getFirstIPAddress());
        assertEquals(SnmpPeerFactory.toLong(InetAddress.getByName("192.168.1.1")), info.getFirst());
        assertNull(info.getLastIPAddress());
        assertTrue(info.isSpecific());
    }
    
    /**
     * Tests getting the correct SNMP Peer after a configureSNMP event and merge to the running config.
     * @throws UnknownHostException
     */
    public void testSnmpEventInfoClassWithSpecific() throws UnknownHostException {
        final String addr = "192.168.0.5";
        Event event = createConfigureSnmpEvent(addr, null);
        addCommunityStringToEvent(event, "abc");
        SnmpEventInfo info = SnmpPeerFactory.getInstance().createSnmpEventInfo(event);
        
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getConfig());
        mgr.mergeIntoConfig(info.createDef());

        SnmpPeer peer = SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(addr));
        assertEquals(peer.getPeer().getHostAddress(), addr);
        assertEquals("abc", peer.getParameters().getReadCommunity());
    }
    
    /**
     * This test should remove the specific 192.168.0.5 from the first definition and
     * replace it with a range 192.168.0.5 - 192.168.0.7.
     * 
     * @throws UnknownHostException
     */
    public void testSnmpEventInfoClassWithRangeReplacingSpecific() throws UnknownHostException {
        final String addr1 = "192.168.0.5";
        final String addr2 = "192.168.0.7";
        
        SnmpPeer peer = SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(addr1));
        assertEquals(1, peer.getParameters().getVersion());
        
        Event event = createConfigureSnmpEvent(addr1, addr2);
        SnmpEventInfo info = SnmpPeerFactory.getInstance().createSnmpEventInfo(event);
        info.setVersion("v2c");
        
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getConfig());
        mgr.mergeIntoConfig(info.createDef());
        
        peer = SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(addr1));
        assertEquals(peer.getPeer().getHostAddress(), addr1);
        assertEquals(SnmpSMI.SNMPV2, peer.getParameters().getVersion());
        assertEquals(m_startingDefCount, SnmpPeerFactory.getConfig().getDefinitionCount());
    }

    /**
     * Tests getting the correct SNMP Peer after merging a new range that super sets a current range.
     * 
     * @throws UnknownHostException
     */
    public void testSnmpEventInfoClassWithRangeSuperSettingDefRanges() throws UnknownHostException {
        final String addr1 = "192.168.99.1";
        final String addr2 = "192.168.108.254";
        
        SnmpPeer peer = SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(addr1));
        assertEquals(0, peer.getParameters().getVersion());
        
        Event event = createConfigureSnmpEvent(addr1, addr2);
        SnmpEventInfo info = SnmpPeerFactory.getInstance().createSnmpEventInfo(event);
        info.setCommunityString("opennmsrules");
        
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getConfig());
        mgr.mergeIntoConfig(info.createDef());
        
        peer = SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName(addr1));
        assertEquals(peer.getPeer().getHostAddress(), addr1);
        assertEquals(0, peer.getParameters().getVersion());
        assertEquals(m_startingDefCount, SnmpPeerFactory.getConfig().getDefinitionCount());
    }

    /**
     * Tests getting the correct SNMP Peer after receiving a configureSNMP event that moves a
     * specific from one definition into another.
     * 
     * @throws UnknownHostException
     */
    public void testSplicingSpecificsIntoRanges() throws UnknownHostException {
        assertEquals(3, SnmpPeerFactory.getConfig().getDefinition(2).getRangeCount());
        assertEquals(6, SnmpPeerFactory.getConfig().getDefinition(2).getSpecificCount());
        
        final String specificAddr = "10.1.1.7";
        final Event event = createConfigureSnmpEvent(specificAddr, null);
        final SnmpEventInfo info = SnmpPeerFactory.getInstance().createSnmpEventInfo(event);
        info.setCommunityString("splice-test");
        info.setVersion("v2c");
        
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getConfig());
        mgr.mergeIntoConfig(info.createDef());
        
        assertEquals(5, SnmpPeerFactory.getConfig().getDefinition(2).getRangeCount());
        
        assertEquals("10.1.1.10", SnmpPeerFactory.getConfig().getDefinition(2).getSpecific(0));
        assertEquals(1, SnmpPeerFactory.getConfig().getDefinition(2).getSpecificCount());
        assertEquals(m_startingDefCount, SnmpPeerFactory.getConfig().getDefinitionCount());
    }
    
    /**
     * This test should show that a specific is added to the definition and the current
     * single definition should become the beginning address in the adjacent range.
     * 
     * @throws UnknownHostException
     */
    public void testSplice2() throws UnknownHostException {
        assertEquals(3, SnmpPeerFactory.getConfig().getDefinition(3).getRangeCount());
        assertEquals(1, SnmpPeerFactory.getConfig().getDefinition(3).getSpecificCount());
        assertEquals("10.1.1.10", SnmpPeerFactory.getConfig().getDefinition(3).getSpecific(0));
        assertEquals("10.1.1.11", SnmpPeerFactory.getConfig().getDefinition(3).getRange(0).getBegin());
        
        final String specificAddr = "10.1.1.7";
        final Event event = createConfigureSnmpEvent(specificAddr, null);
        final SnmpEventInfo info = SnmpPeerFactory.getInstance().createSnmpEventInfo(event);
        info.setCommunityString("splice2-test");

        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getConfig());
        mgr.mergeIntoConfig(info.createDef());
        
        assertEquals(3, SnmpPeerFactory.getConfig().getDefinition(3).getRangeCount());
        assertEquals(1, SnmpPeerFactory.getConfig().getDefinition(3).getSpecificCount());
        assertEquals("10.1.1.7", SnmpPeerFactory.getConfig().getDefinition(3).getSpecific(0));
        assertEquals("10.1.1.10", SnmpPeerFactory.getConfig().getDefinition(3).getRange(0).getBegin());

        String marshalledConfig = SnmpPeerFactory.marshallConfig();
        assertNotNull(marshalledConfig);
        
    }

    /**
     * This tests the redundant static method found in the IPSorter class.
     * 
     * @throws UnknownHostException
     */
    public void testToLongToIpAddr() throws UnknownHostException {
        String testAddr = "192.168.0.255";
        
        long factoryValue = SnmpPeerFactory.toLong(InetAddress.getByName(testAddr));
        long sorterValue = IPSorter.convertToLong(testAddr);
        
        assertEquals(factoryValue, sorterValue);
    }

    
    private Event createConfigureSnmpEvent(final String firstIp, final String lastIp) {
        Event event = new Event();
        event.setUei(EventConstants.CONFIGURE_SNMP_EVENT_UEI);
        
        Parm vParm = new Parm();
        vParm.setParmName(EventConstants.PARM_FIRST_IP_ADDRESS);
        Value value = new Value();
        value.setContent(firstIp);
        value.setType("String");
        vParm.setValue(value);
        
        Parms parms = new Parms();
        parms.addParm(vParm);
        
        vParm = new Parm();
        vParm.setParmName(EventConstants.PARM_LAST_IP_ADDRESS);
        value = new Value();
        value.setContent(lastIp);
        value.setType("String");
        vParm.setValue(value);
        parms.addParm(vParm);

        event.setParms(parms);
        return event;
    }
    
    private void addCommunityStringToEvent(final Event event, final String commStr) {
        Parms parms = event.getParms();
        Parm vParm = new Parm();
        vParm.setParmName(EventConstants.PARM_COMMUNITY_STRING);
        Value value = new Value();
        value.setContent(commStr);
        value.setType("String");
        vParm.setValue(value);
        parms.addParm(vParm);
    }

}
