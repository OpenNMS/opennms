/*
 * Created on Nov 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.poller.monitors;

import junit.framework.TestCase;

import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpGauge32;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SnmpMonitorTest extends TestCase {

    SnmpMonitor monitor;

    public void setUp() {
        monitor = new SnmpMonitor();
    }

    public void tearDown() {
        monitor = null;
    }

    public void testMeetsCriteriaWithNullResult() {
        assertNotNull(monitor);
        assertFalse(monitor.meetsCriteria(null, null, null));
    }

    public void testMeetsCriteriaWithSnmpNull() {
        SnmpNull result = new SnmpNull();
        testSyntaxEquals(result, "", "1");
    }

    public void testMeetsCriteriaWithString() {
        SnmpOctetString result = new SnmpOctetString("A Test String".getBytes());
        testSyntaxEquals(result, "A Test String", "a test string");
        testSyntaxMatches(result, "[tT][eE][sS][tT]", "test");
        testSyntaxMatches(result, "^A Test String$", "^A Test$");
    }

    public void testMeetsCriteriaWithObjectID() {
        SnmpObjectId result = new SnmpObjectId(".1.2.3.4.5.6.7.8.9");
        testSyntaxEquals(result, ".1.2.3.4.5.6.7.8.9", "1.2.3.4.5.6.7.8.9");
        testSyntaxMatches(result, "\\.7\\.", "\\.11\\.");
    }

    public void testMeetsCriteriaWithIPAddr() throws Exception {
        SnmpIPAddress result = new SnmpIPAddress("10.1.1.1");
        testSyntaxEquals(result, "10.1.1.1", "10.1.1.2");
        testSyntaxMatches(result, "10\\.1\\.1\\.[1-5]", "10\\.1\\.1\\.[02-9]");
    }

    public void testNumericString() {
        SnmpOctetString result = new SnmpOctetString("12345".getBytes());
        testOrderOperations(result, 12345);
    }

    public void testMeetsCriteriaWithInteger() {
        SnmpInt32 result = new SnmpInt32(1234);
        testSyntaxEquals(result, "1234", "2234");
        testOrderOperations(result, 1234);
        testSyntaxMatches(result, "23", "14");
    }

    public void testMeetsCriteriaWithCounter32() {
        SnmpCounter32 result = new SnmpCounter32(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    public void testMeetsCriteriaWithGuage32() {
        SnmpGauge32 result = new SnmpGauge32(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    public void testMeetsCriteriaWithTimeTicks() {
        SnmpTimeTicks result = new SnmpTimeTicks(1);
        testSyntaxEquals(result, "0d 0h 0m 0s 10ms", "1d 1h 1m 1s 10ms");
        testSyntaxMatches(result, "0h", "1h");
        testOrderOperations(result, 1);
    }

    public void testMeetsCriteriaWithCounter64() {
        SnmpCounter64 result = new SnmpCounter64(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    public void testErrorConditions() {
        SnmpInt32 result = new SnmpInt32(1);
        try {
            monitor.meetsCriteria(result, "X", "123");
            fail("Expected an exception to be thrown");
        } catch (IllegalArgumentException e) {
        }

        try {
            monitor.meetsCriteria(result, "<", "abc");
            fail("expected an exception");
        } catch (NumberFormatException e) {
        }
    }

    private void testSyntaxEquals(SnmpSyntax result, String eqString, String neString) {
        assertTrue(monitor.meetsCriteria(result, null, null));

        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.EQUALS, eqString));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.EQUALS, neString));

        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.NOT_EQUAL, eqString));
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.NOT_EQUAL, neString));

    }

    private void testSyntaxMatches(SnmpSyntax result, String matchString, String noMatchString) {
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.MATCHES, matchString));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.MATCHES, noMatchString));
    }

    /**
     * @param result
     */
    private void testOrderOperations(SnmpSyntax result, int value) {
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
