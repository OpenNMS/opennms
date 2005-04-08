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
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

public class Snmpv3PluginTest extends TestCase {

    private static final String DEFAULT_PORT = "161";
    private static final String DEFAULT_TIMEOUT = "3000";
    private static final String DEFAULT_RETRY = "2";

    private static final String DEFAULT_SECURITY_NAME = "opennms";
    private static final OID DEFAULT_AUTH_PROTOCOL = AuthMD5.ID;
    private static final OctetString DEFAULT_AUTH_PASSPHRASE = new OctetString("opennms");
    private static final OID DEFAULT_PRIV_PROTOCOL = PrivDES.ID;
    private static final OctetString DEFAULT_PRIV_PASSPHRASE = new OctetString("opennms");
    private static final String DEFAULT_VERSION = "snmpv3";

    /**
     * The system object identifier to retreive from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";
    
    private static final String SNMP_CONFIG ="<?xml version=\"1.0\"?>\n" + 
            "<snmp-config "+ 
            " retry=\"3\" timeout=\"800\"\n" + 
            " read-community=\"public\"" +
            " write-community=\"private\"\n" + 
            " port=\"161\"\n" +
            " version=\"v1\"\n" +
            " max-request-size=\"484\">\n" +
            "   <definition version=\"v2c\">\n" + 
            "       <specific>192.168.0.50</specific>\n" +
            "   </definition>\n" + 
            "\n" + 
            "   <definition version=\"v3\" " +
            "       security-name=\"opennmsUser\" >\n" + 
            "       <specific>192.168.0.102</specific>\n" +
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
    
    public void testGetPeer() throws UnknownHostException {
        assertNotNull(SnmpPeerFactory.getInstance().getPeer(InetAddress.getLocalHost()));
    }
    
    public void testGetV1Target() throws UnknownHostException {
        Target target = SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.1.1"));
        assertNotNull(target);
        assertTrue(target.getVersion() == SnmpConstants.version1);
    }
    
    public void testGetV2cTarget() throws UnknownHostException {
        Target target = SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.0.50"));
        assertNotNull(target);
        assertTrue(target.getVersion() == SnmpConstants.version2c);
    }

    public void testGetV3Target() throws UnknownHostException {
        Target target = SnmpPeerFactory.getInstance().getTarget(InetAddress.getByName("192.168.0.102"));
        assertNotNull(target);
        assertTrue(target.getVersion() == SnmpConstants.version3);
    }

    //This tests works against a live v3 compatible agent.  Need to
    //work on the mockAgent.  Don't not check-in to cvs uncommented.

    public void testIsProtocolSupported() {
        
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            address = InetAddress.getByName("192.168.0.102");
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        
        Map map = new HashMap();
        
        SnmpV3Plugin plugin = new SnmpV3Plugin();
//        assertTrue(plugin.isProtocolSupported(address, map));
        
    }
   
}
