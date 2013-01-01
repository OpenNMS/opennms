/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.test.snmp;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy;
import org.opennms.netmgt.snmp.mock.MockSnmpStrategy;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.opennms.test.PropertySettingTestSuite;
import org.opennms.test.VersionSettingTestSuite;

/**
 * Test utilities for creating an SNMP TestSuites for JUnit.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public abstract class SnmpTestSuiteUtils {
    private static final String STRATEGY_CLASS_PROPERTY_NAME = "org.opennms.snmp.strategyClass";

    private SnmpTestSuiteUtils() {
        throw new UnsupportedOperationException("you aren't the boss of me!");
    }
    
    public static TestSuite createSnmpStrategyTestSuite(Class<? extends TestCase> testClass) {
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", STRATEGY_CLASS_PROPERTY_NAME, JoeSnmpStrategy.class.getName()));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", STRATEGY_CLASS_PROPERTY_NAME, Snmp4JStrategy.class.getName()));
        suite.addTest(new PropertySettingTestSuite(testClass, "MockStrategy Tests", STRATEGY_CLASS_PROPERTY_NAME, MockSnmpStrategy.class.getName()));
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
