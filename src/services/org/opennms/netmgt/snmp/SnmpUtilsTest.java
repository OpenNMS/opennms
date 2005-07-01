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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestSuite;

import org.opennms.netmgt.mock.OpenNMSTestCase;

public class SnmpUtilsTest extends OpenNMSTestCase {
    
    public static TestSuite suite() {
        Class testClass = SnmpUtilsTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy"));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy"));
        return suite;
    }


    protected void setUp() throws Exception {
        super.setStartEventd(false);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCreateSnmpAgentConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig();
        assertNull(agentConfig.getAddress());
        assertSnmpAgentConfigDefaults(agentConfig);
        
        agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        assertNotNull(agentConfig.getAddress());
        assertEquals(InetAddress.getLocalHost().getHostAddress(), agentConfig.getAddress().getHostAddress());
        assertSnmpAgentConfigDefaults(agentConfig);
    }
    
    public void testGet() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpValue val = SnmpUtils.get(agentConfig, new SnmpObjId(".1.3.6.1.2.1.1.2.0"));
        assertNotNull(val);
    }
    
    public void testBadGet() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpValue val = SnmpUtils.get(agentConfig, new SnmpObjId(".1.3.6.1.2.1.1.2"));
        assertEquals(null, val);
    }
    
    public void getMultipleVarbinds() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpObjId[] oids = { new SnmpObjId(".1.3.6.1.2.1.1.2.0"), new SnmpObjId(".1.3.6.1.2.1.1.3.0") };
        SnmpValue[] vals = SnmpUtils.get(agentConfig, oids);
        assertNotNull(vals);
        assertEquals(2, vals.length);
    }
    
    public void testGetNext() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpValue val = SnmpUtils.getNext(agentConfig, new SnmpObjId(".1.3.6.1.2.1.1"));
        assertNotNull(val);
    }
    
    public void testGetNextMultipleVarbinds() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpObjId[] oids = { new SnmpObjId(".1.3.6.1.2.1.1.2.0"), new SnmpObjId(".1.3.6.1.2.1.1.3.0") };
        SnmpValue[] vals = SnmpUtils.getNext(agentConfig, oids);
        assertNotNull(vals);
        assertEquals(2, vals.length);
        assertNotNull(vals);
    }
    
    private void assertSnmpAgentConfigDefaults(SnmpAgentConfig agentConfig) {
        assertEquals(SnmpAgentConfig.DEFAULT_PORT, agentConfig.getPort());
        assertEquals(SnmpAgentConfig.DEFAULT_TIMEOUT, agentConfig.getTimeout());
        assertEquals(SnmpAgentConfig.DEFAULT_VERSION, agentConfig.getVersion());
    }
    
/*    public void testCreateWalker() throws UnknownHostException {
        SnmpWalker walker = SnmpUtils.createWalker(InetAddress.getLocalHost(), "Test", 5, new ColumnTracker(SnmpObjId.get(".1.2.3.4")));
        assertNotNull(walker);
    }
*/    
    public void testCreateWalkerWithAgentConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "Test", new ColumnTracker(SnmpObjId.get("1.2.3.4")));
        assertNotNull(walker);
    }
    
    public void testGetStrategy() {
        SnmpStrategy strategy = SnmpUtils.getStrategy();
        assertNotNull(strategy);
        assertEquals(System.getProperty("org.opennms.snmp.strategyClass"), strategy.getClass().getName());
    }
    
}