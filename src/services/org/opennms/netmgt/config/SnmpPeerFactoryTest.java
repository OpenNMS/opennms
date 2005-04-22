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

package org.opennms.netmgt.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.snmp4j.CommunityTarget;

public class SnmpPeerFactoryTest extends OpenNMSTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        m_runSupers = false;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCountChar() {
        assertEquals(2, SnmpPeerFactory.countChar('-', "test-this-please"));
        assertEquals(3, SnmpPeerFactory.countChar('-', "test-this-please-"));
        assertEquals(4, SnmpPeerFactory.countChar('-', "-test-this-please-"));
    }
    
    public void testMatchRange() {
        assertTrue(SnmpPeerFactory.matchRange("192", "191-193"));
        assertTrue(SnmpPeerFactory.matchRange("192", "192"));
        assertTrue(SnmpPeerFactory.matchRange("192", "192-200"));
        assertTrue(SnmpPeerFactory.matchRange("192", "1-255"));
        assertTrue(SnmpPeerFactory.matchRange("192", "*"));
        assertFalse(SnmpPeerFactory.matchRange("192", "1-9"));
    }
    
    public void testMatchOctet() {
        assertTrue(SnmpPeerFactory.matchOctet("192", "191,192,193"));
        assertFalse(SnmpPeerFactory.matchOctet("192", "190,191,194"));
        assertTrue(SnmpPeerFactory.matchOctet("192", "10,172,190-193"));
        assertFalse(SnmpPeerFactory.matchOctet("192", "10,172,193-199"));
    }
    
    public void testVerifyIpMatch() {
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "*.*.*.*"));
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "192.*.*.*"));
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "*.168.*.*"));
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "*.*.0.*"));
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "*.*.*.1"));
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "*.*.*.0-7"));
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "192.168.0.0-7"));
        assertTrue(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "192.166,167,168.*.0,1,5-10"));
        assertFalse(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "10.0.0.1"));
        assertFalse(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "*.168.*.2"));
        assertFalse(SnmpPeerFactory.verifyIpMatch("192.168.0.1", "10.168.0.1"));
    }

    public void testGetTargetFromPatterns() throws UnknownHostException {
        //pattern in config is "77.5-12,15.1-255.255"
        CommunityTarget target = (CommunityTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("77.5.5.255"));
        assertEquals("ipmatch", target.getCommunity().toString());
        
        target = (CommunityTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("77.15.80.255"));
        assertEquals("ipmatch", target.getCommunity().toString());
        
        //should be default community "public" because fo 4
        target = (CommunityTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("77.4.5.255"));
        assertEquals("public", target.getCommunity().toString());
        
        //should be default community because of 0
        target = (CommunityTarget)SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("77.6.0.255"));
        assertEquals("public", target.getCommunity().toString());
    }
}
