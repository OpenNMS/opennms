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
package org.opennms.netmgt.collectd;


import java.net.InetAddress;

import junit.framework.TestSuite;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.VersionSettingTestSuite;

public class SnmpNodeCollectorTest extends SnmpCollectorTestCase {

    public static TestSuite suite() {
        Class testClass = SnmpNodeCollectorTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", SnmpAgentConfig.VERSION1));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", SnmpAgentConfig.VERSION2C));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv3 Tests", SnmpAgentConfig.VERSION3));
        return suite;
    }

    public void testZeroVars() throws Exception {
        SnmpNodeCollector collector = createNodeCollector();
        assertMibObjectsPresent(collector.getCollectionSet().getNodeInfo(), getAttributeList());
    }

    public void testInvalidVar() throws Exception {
        addAttribute("invalid", ".1.3.6.1.2.1.2", "0", "string");
        SnmpNodeCollector collector = createNodeCollector();
        assertTrue(collector.getEntry().isEmpty());
    }

    public void testInvalidInst() throws Exception {
        addAttribute("invalid", ".1.3.6.1.2.1.1.3", "1", "timeTicks");
        SnmpNodeCollector collector = createNodeCollector();
        assertTrue(collector.getEntry().isEmpty());
    }

    public void testOneVar() throws Exception {
        addSysName();
        SnmpNodeCollector collector = createNodeCollector();
        assertMibObjectsPresent(collector.getCollectionSet().getNodeInfo(), getAttributeList());
    }

    private SnmpNodeCollector createNodeCollector() throws Exception, InterruptedException {
        initializeAgent();

        SnmpNodeCollector collector = new SnmpNodeCollector(InetAddress.getLocalHost(), getCollectionSet().getAttributeList(), getCollectionSet());

        createWalker(collector);
        waitForSignal();
        assertNotNull("No entry data", collector.getEntry());
        assertFalse("Timeout collecting data", collector.timedOut());
        assertFalse("Collector failed to collect data", collector.failed());
        return collector;
    }

}
