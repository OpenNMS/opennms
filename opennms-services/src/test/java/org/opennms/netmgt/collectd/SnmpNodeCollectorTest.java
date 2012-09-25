/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;


import java.net.InetAddress;

import junit.framework.TestSuite;

import org.opennms.core.test.snmp.SnmpTestSuiteUtils;

public class SnmpNodeCollectorTest extends SnmpCollectorTestCase {

    public static TestSuite suite() {
        return SnmpTestSuiteUtils.createSnmpVersionTestSuite(SnmpNodeCollectorTest.class);
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
