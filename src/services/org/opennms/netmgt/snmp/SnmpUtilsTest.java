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
    
    public void testCreateWalker() throws UnknownHostException {
        SnmpWalker walker = SnmpUtils.createWalker(InetAddress.getLocalHost(), "Test", 5, new ColumnTracker(SnmpObjId.get(".1.2.3.4")));
        assertNotNull(walker);
    }
    
    public void testCreateConfig() throws UnknownHostException {
        SnmpConfig config = SnmpUtils.createConfig(InetAddress.getLocalHost());
        assertNotNull(config);
        config.setRetries(4);
        assertEquals(4, config.getRetries());
    }
    
    public void testGetStrategy() {
        SnmpStrategy strategy = SnmpUtils.getStrategy();
        assertNotNull(strategy);
        assertEquals(System.getProperty("org.opennms.snmp.strategyClass"), strategy.getClass().getName());
    }
    
    

}
