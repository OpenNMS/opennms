/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Created: October 25, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.snmp;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.opennms.test.PropertySettingTestSuite;
import org.opennms.test.VersionSettingTestSuite;

/**
 * Test utilities for creating an SNMP TestSuites for JUnit.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SnmpTestSuiteUtils {
    private static final String STRATEGY_CLASS_PROPERTY_NAME = "org.opennms.snmp.strategyClass";

    private SnmpTestSuiteUtils() {
        throw new UnsupportedOperationException("you aren't the boss of me!");
    }
    
    public static TestSuite createSnmpStrategyTestSuite(Class<? extends TestCase> testClass) {
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", STRATEGY_CLASS_PROPERTY_NAME, JoeSnmpStrategy.class.getName()));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", STRATEGY_CLASS_PROPERTY_NAME, Snmp4JStrategy.class.getName()));
        return suite;
    }
    
    public static TestSuite createSnmpVersionTestSuite(Class<? extends TestCase> testClass) {
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", SnmpAgentConfig.VERSION1));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", SnmpAgentConfig.VERSION2C));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv3 Tests", SnmpAgentConfig.VERSION3));
        return suite;
    }
}
