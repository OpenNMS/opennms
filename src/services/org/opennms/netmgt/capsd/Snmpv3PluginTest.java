//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.capsd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.capsd.plugins.SnmpPlugin;
import org.opennms.netmgt.capsd.plugins.SnmpV3Plugin;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.poller.monitors.IPv4NetworkInterface;
import org.opennms.netmgt.poller.monitors.NetworkInterface;
import org.opennms.netmgt.poller.monitors.ServiceMonitor;
import org.opennms.netmgt.poller.monitors.SnmpV3Monitor;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.snmp4j.CommunityTarget;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.TransportIpAddress;

public class Snmpv3PluginTest extends OpenNMSTestCase {

    /*
     * Set this flag to false before checking in code.  Use this flag to
     * test against a v3 compatible agent running on the localhost
     * until the MockAgent code is finished.
     */
    private boolean m_runAssertions = false;

    /**
     * Required method for TestCase
     */
    protected void setUp() throws Exception {
        super.setUp();
        m_runSupers = false;
    }

    /**
     * Required method for TestCase
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * This tests getting a JoeSNMP peer
     * @throws UnknownHostException
     */
    public void testGetPeer() throws UnknownHostException {
        assertNotNull(SnmpPeerFactory.getInstance().getPeer(InetAddress.getLocalHost()));
    }
    
    /**
     * This tests ranges configured for a v1 node and community string
     * @throws UnknownHostException
     */
    public void testGetv1RangePeer() throws UnknownHostException {
        SnmpPeer peer = SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName("10.0.0.192"));
        assertNotNull(peer);
        SnmpParameters parms = peer.getParameters();
        assertEquals(SnmpSMI.SNMPV1, parms.getVersion());
        assertEquals("rangev1", parms.getReadCommunity());
    }
    
    /**
     * This tests for ranges configured for a v2 node and community string
     * @throws UnknownHostException
     */
    public void testGetv2cRangePeer() throws UnknownHostException {
        SnmpPeer peer = SnmpPeerFactory.getInstance().getPeer(InetAddress.getByName("10.7.23.100"));
        assertNotNull(peer);
        SnmpParameters parms = peer.getParameters();
        assertEquals(SnmpSMI.SNMPV2, parms.getVersion());
        assertEquals("rangev2c", parms.getReadCommunity());
    }
    
    /**
     * This tests for ranges configured for v3 node and security name
     * @throws UnknownHostException 
     */
    public void testGetv3RangeTarget() throws UnknownHostException {
        UserTarget target = (UserTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("1.1.1.50"));
        assertNotNull(target);
        assertEquals(SnmpConstants.version3, target.getVersion());
        assertEquals("opennmsRangeUser", target.getSecurityName().toString());
    }
    
    /**
     * This tests creating a v1 target
     * @throws UnknownHostException
     */
    public void testGetV1Target() throws UnknownHostException {
        CommunityTarget target = (CommunityTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.0.100"));
        assertNotNull(target);
        assertTrue(target.getVersion() == SnmpConstants.version1);
        assertEquals("public", target.getCommunity().toString());
    }
    
    /**
     * This tests for a specifically defined v2c target
     * @throws UnknownHostException
     */
    public void testGetV2cTarget() throws UnknownHostException {
        CommunityTarget target = (CommunityTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.0.50"));
        assertNotNull(target);
        assertEquals(target.getVersion(), SnmpConstants.version2c);
        assertEquals("specificv2c", target.getCommunity().toString());
    }

    /**
     * This tests for a specifically defined v3 target
     * @throws UnknownHostException
     */
    public void testGetV3Target() throws UnknownHostException {
        UserTarget target = (UserTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.3.3"));
        assertNotNull(target);
        assertEquals(target.getVersion(), SnmpConstants.version3);
        assertEquals("opennmsUser", target.getSecurityName().toString());
    }
    
    /**
     * This tests for a target using an IP that matches no specific or range definition
     * @throws UnknownHostException
     */
    public void testGetDefaultTarget() throws UnknownHostException {
        CommunityTarget target = (CommunityTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("10.1.1.1"));
        assertNotNull(target);
        assertEquals(target.getVersion(), SnmpConstants.version1);
        assertEquals(target.getRetries(), 3);
        assertEquals(target.getTimeout(), 800L);
        assertEquals("public", target.getCommunity().toString());
        TransportIpAddress ta = (TransportIpAddress)target.getAddress();
        assertEquals(ta.getPort(), 161);
        assertEquals(target.getMaxSizeRequestPDU(), 484);
    }
    
    public void testIsSNMPProtocolSupported () throws UnknownHostException, IOException {
        InetAddress address = InetAddress.getByName(myLocalHost());
        Map map = new HashMap();
        map.put("forced version", "snmpv1");
        
        AbstractPlugin plugin = new SnmpPlugin();
        if(m_runAssertions)
            assertTrue(plugin.isProtocolSupported(address, map));        
    }

    /**
     * This test works against a live v1/2c compatible agent until
     * the MockAgent code is completed.
     * @throws UnknownHostException 
     */
    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(myLocalHost());
        Map map = new HashMap();
        map.put("forced version", "snmpv1");
        
        AbstractPlugin plugin = new SnmpV3Plugin();
        if(m_runAssertions)
            assertTrue(plugin.isProtocolSupported(address, map));
    }
    
    /**
     * This tests works against a live v3 compatible agent.
     */
    public void testIsV3ProtocolSupported() {

        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            MockUtil.println("Testing for v3 on: "+address.getHostAddress());
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        
        Map map = new HashMap();
        
        SnmpV3Plugin plugin = new SnmpV3Plugin();
        
        if (m_runAssertions)
            assertTrue(plugin.isProtocolSupported(address, map));
        
    }
   
    public void testIsV3ProtocolSupported2() {

        InetAddress address = null;
        try {
            address = InetAddress.getByName("127.0.0.1");
            MockUtil.println("Testing for v3 on: "+address.getHostAddress());
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        
        Map map = new HashMap();
        
        SnmpV3Plugin plugin = new SnmpV3Plugin();
        if (m_runAssertions)
            assertTrue(plugin.isProtocolSupported(address, map));
        
    }
    
    /**
     * This test uses the v3 monitor class to do a poll and check
     * the status of the v3 agent
     *
     */
    public void testIsV3ProtocolAvailable() {
        
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        SnmpV3Monitor monitor = new SnmpV3Monitor();
        NetworkInterface iface = new IPv4NetworkInterface(address);
        monitor.initialize(iface);

        if (m_runAssertions) {
            int result = monitor.poll(iface, new HashMap(), new Package());
            assertEquals(ServiceMonitor.SERVICE_AVAILABLE, result);
        }
    }
    
    /**
     * This test uses the v3 monitor class to do a poll and check the
     * status of an agent forcing the version to v1
     */
    public void testIsForcedV1ProtocolAvailable() {
        
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        SnmpV3Monitor monitor = new SnmpV3Monitor();
        NetworkInterface iface = new IPv4NetworkInterface(address);
        monitor.initialize(iface, SnmpConstants.version1);

        if (m_runAssertions) {
            int result = monitor.poll(iface, new HashMap(), new Package());
            assertEquals(ServiceMonitor.SERVICE_AVAILABLE, result);
        }
    }

}
