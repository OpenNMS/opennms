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

import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.poller.monitors.IPv4NetworkInterface;
import org.opennms.netmgt.poller.monitors.NetworkInterface;
import org.opennms.netmgt.poller.monitors.ServiceMonitor;
import org.opennms.netmgt.poller.monitors.SnmpV3Monitor;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;

public class Snmpv3PluginTest extends TestCase {

//    private static final OID DEFAULT_AUTH_PROTOCOL = AuthMD5.ID;
//    private static final OID DEFAULT_PRIV_PROTOCOL = PrivDES.ID;
    private static final String SNMP_CONFIG ="<?xml version=\"1.0\"?>\n" + 
            "<snmp-config "+ 
            " retry=\"3\" timeout=\"800\"\n" + 
            " read-community=\"public\"" +
            " write-community=\"private\"\n" + 
            " port=\"161\"\n" +
            " version=\"v1\"\n" +
            " max-request-size=\"484\">\n" +
            "\n" +
            "   <definition version=\"v1\">\n" + 
            "       <specific>192.168.0.100</specific>\n" +
            "   </definition>\n" + 
            "\n" + 
            "   <definition version=\"v2c\">\n" + 
            "       <specific>192.168.0.50</specific>\n" +
            "   </definition>\n" + 
            "\n" + 
            "   <definition version=\"v3\" " +
            "       security-name=\"opennmsUser\" \n" + 
            "       auth-passphrase=\"0p3nNMSv3\" >\n" +
            "       <specific>"+myLocalHost()+"</specific>\n" +
            "   </definition>\n" + 
            "\n" + 
            "\n" + 
            "</snmp-config>";

    protected void setUp() throws Exception {
        super.setUp();
        Reader rdr = new StringReader(SNMP_CONFIG);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private static String myLocalHost()  {
        
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Exception getting localhost");
        }
        
        return null;
    }

    /**
     * This tests getting a JoeSNMP peer
     * @throws UnknownHostException
     */
    public void testGetPeer() throws UnknownHostException {
        assertNotNull(SnmpPeerFactory.getInstance().getPeer(InetAddress.getLocalHost()));
    }
    
    /**
     * This tests creating a v1 target
     * @throws UnknownHostException
     */
    public void testGetV1Target() throws UnknownHostException {
        Target target = SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.0.100"));
        assertNotNull(target);
        assertTrue(target.getVersion() == SnmpConstants.version1);
    }
    
    /**
     * This tests for a specifically defined v2c target
     * @throws UnknownHostException
     */
    public void testGetV2cTarget() throws UnknownHostException {
        Target target = SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.0.50"));
        assertNotNull(target);
        assertEquals(target.getVersion(), SnmpConstants.version2c);
    }

    /**
     * This tests for a specifically defined v3 target
     * @throws UnknownHostException
     */
    public void testGetV3Target() throws UnknownHostException {
        Target target = SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.0.102"));
        assertNotNull(target);
        assertEquals(target.getVersion(), SnmpConstants.version3);
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
        assertEquals(new OctetString("public"), target.getCommunity());
        TransportIpAddress ta = (TransportIpAddress)target.getAddress();
        assertEquals(ta.getPort(), 161);
        assertEquals(target.getMaxSizeRequestPDU(), 484);
    }
    
    //This tests works against a live v3 compatible agent.  Need to
    //work on the mockAgent.  Don't not check-in to cvs uncommented.

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
//        assertTrue(plugin.isProtocolSupported(address, map));
        
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
//        assertTrue(plugin.isProtocolSupported(address, map));
        
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
        int result = monitor.poll(iface, new HashMap(), new Package());
        
//        assertEquals(ServiceMonitor.SERVICE_AVAILABLE, result);
    }
}
