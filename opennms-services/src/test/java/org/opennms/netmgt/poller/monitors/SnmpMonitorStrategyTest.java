/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.Counter32SnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.Counter64SnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.Gauge32SnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.Integer32SnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.IpAddressSnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.OidSnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.StringSnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.TimeticksSnmpValue;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SnmpMonitorStrategyTest {

    private SnmpMonitorStrategy monitor = new SnmpMonitorStrategy() {
        @Override
        public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
            throw new UnsupportedOperationException("method not implemented; go away, punk!");
        }
    };

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testMeetsCriteriaWithNullResult() {
        SnmpValue result = null;
        assertFalse(monitor.meetsCriteria(result, null, null));
    }

    @Test
    public void testMeetsCriteriaWithSnmpNull() {
        SnmpValue result = TestSnmpValue.NULL_VALUE;
        testSyntaxEquals(result, "", "1");
    }

    @Test
    public void testMeetsCriteriaWithString() {
        StringSnmpValue result = new StringSnmpValue("A Test String");
        testSyntaxEquals(result, "A Test String", "a test string");
        testSyntaxMatches(result, "[tT][eE][sS][tT]", "test");
        testSyntaxMatches(result, "^A Test String$", "^A Test$");
    }

    @Test
    public void testMeetsCriteriaWithObjectID() {
        OidSnmpValue result = new OidSnmpValue(".1.2.3.4.5.6.7.8.9");
        testSyntaxEquals(result, ".1.2.3.4.5.6.7.8.9", "..1.2.3.4.5.6.7.8.9");
        testSyntaxMatches(result, "\\.7\\.", "\\.11\\.");
    }

    @Test
    public void testMeetsCriteriaWithIPAddr() throws Exception {
        IpAddressSnmpValue result = new IpAddressSnmpValue("10.1.1.1");
        testSyntaxEquals(result, "10.1.1.1", "10.1.1.2");
        testSyntaxMatches(result, "10\\.1\\.1\\.[1-5]", "10\\.1\\.1\\.[02-9]");
    }

    @Test
    public void testNumericString() {
        StringSnmpValue result = new StringSnmpValue("12345");
        testOrderOperations(result, 12345);
    }

    @Test
    public void testMeetsCriteriaWithInteger() {
        Integer32SnmpValue result = new Integer32SnmpValue(1234);
        testSyntaxEquals(result, "1234", "2234");
        testOrderOperations(result, 1234);
        testSyntaxMatches(result, "23", "14");
    }

    @Test
    public void testMeetsCriteriaWithCounter32() {
        Counter32SnmpValue result = new Counter32SnmpValue(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    @Test
    public void testMeetsCriteriaWithGauge32() {
        Gauge32SnmpValue result = new Gauge32SnmpValue(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    @Test
    public void testMeetsCriteriaWithTimeTicks() {
        TimeticksSnmpValue result = new TimeticksSnmpValue("1");
        testSyntaxEquals(result, "0d 0h 0m 0s 10ms", "1d 1h 1m 1s 10ms");
        testSyntaxMatches(result, "0h", "1h");
        testOrderOperations(result, 1);
    }

    @Test
    public void testMeetsCriteriaWithCounter64() {
        Counter64SnmpValue result = new Counter64SnmpValue(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    @Test
    public void testErrorConditions() {
        Integer32SnmpValue result = new Integer32SnmpValue(1);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("operator X is unknown"));
        try {
            monitor.meetsCriteria(result, "X", "123");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testErrorConditions2() {
        Integer32SnmpValue result = new Integer32SnmpValue(1);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new NumberFormatException("For input string: \"abc\""));
        try {
            monitor.meetsCriteria(result, "<", "abc");
            fail("expected an exception");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    private void testSyntaxEquals(SnmpValue result, String eqString, String neString) {
        assertTrue(monitor.meetsCriteria(result, null, null));

        assertTrue("result '" + result + "' should pass equal test with '" + eqString + "'", monitor.meetsCriteria(result, SnmpMonitor.EQUALS, eqString));
        assertFalse("result '" + result + "' should fail equal test with '" + neString + "'", monitor.meetsCriteria(result, SnmpMonitor.EQUALS, neString));

        assertFalse("result '" + result + "' should fail not equal test with '" + eqString + "'", monitor.meetsCriteria(result, SnmpMonitor.NOT_EQUAL, eqString));
        assertTrue("result '" + result + "' should pass not equal test with '" + neString + "'", monitor.meetsCriteria(result, SnmpMonitor.NOT_EQUAL, neString));

    }

    private void testSyntaxMatches(SnmpValue result, String matchString, String noMatchString) {
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.MATCHES, matchString));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.MATCHES, noMatchString));
    }
    
    private void testOrderOperations(SnmpValue result, int value) {
        // less-than
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.LESS_THAN, Integer.toString(value + 1)));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.LESS_THAN, Integer.toString(value)));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.LESS_THAN, Integer.toString(value - 1)));

        // less-equals
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.LESS_THAN_EQUALS, Integer.toString(value + 1)));
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.LESS_THAN_EQUALS, Integer.toString(value)));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.LESS_THAN_EQUALS, Integer.toString(value - 1)));

        // greater-than
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.GREATER_THAN, Integer.toString(value + 1)));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.GREATER_THAN, Integer.toString(value)));
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.GREATER_THAN, Integer.toString(value - 1)));

        // greater-equals
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.GREATER_THAN_EQUALS, Integer.toString(value + 1)));
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.GREATER_THAN_EQUALS, Integer.toString(value)));
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.GREATER_THAN_EQUALS, Integer.toString(value - 1)));
    }

}
