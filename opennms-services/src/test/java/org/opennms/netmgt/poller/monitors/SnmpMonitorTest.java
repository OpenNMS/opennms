/*
 * Created on Nov 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.poller.monitors;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opennms.netmgt.snmp.PropertySettingTestSuite;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SnmpMonitorTest extends TestCase {

    SnmpMonitorStrategy monitor;

    public static TestSuite suite() {
        Class testClass = SnmpMonitorTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy"));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy"));
        return suite;
    }

    public void setUp() {
        MockLogAppender.setupLogging();
    }

    public void tearDown() {
        monitor = null;
    }
    
    public void testMeetsCriteriaWithNullResult() {
        monitor = new SnmpMonitor();
        assertNotNull(monitor);
        SnmpValue result = null;
        assertFalse(monitor.meetsCriteria(result, null, null));
    }

/*    public void testMeetsCriteriaWithSnmpNull() {
        monitor = new SnmpMonitor();
        SnmpNull result = null;
        testSyntaxEquals(result, "", "1");
    }
*/
/*    public void testMeetsCriteriaWithString() {
        monitor = new SnmpMonitor();
        SnmpOctetString result = new SnmpOctetString("A Test String".getBytes());
        testSyntaxEquals(result, "A Test String", "a test string");
        testSyntaxMatches(result, "[tT][eE][sS][tT]", "test");
        testSyntaxMatches(result, "^A Test String$", "^A Test$");
        
        monitor = new SnmpV3Monitor();
        Variable resultV3 = new OctetString("A Test String".getBytes());
        testSyntaxEquals(resultV3, "A Test String", "a test string");
        testSyntaxMatches(resultV3, "[tT][eE][sS][tT]", "test");
        testSyntaxMatches(resultV3, "^A Test String$", "^A Test$");
    }
*/
/*    public void testMeetsCriteriaWithObjectID() {
        monitor = new SnmpMonitor();
        SnmpObjectId result = new SnmpObjectId(".1.2.3.4.5.6.7.8.9");
        testSyntaxEquals(result, ".1.2.3.4.5.6.7.8.9", "..1.2.3.4.5.6.7.8.9");
        testSyntaxMatches(result, "\\.7\\.", "\\.11\\.");
        
        monitor = new SnmpV3Monitor();
        Variable resultV3 = new OID(".1.2.3.4.5.6.7.8.9");
        testSyntaxEquals(resultV3, ".1.2.3.4.5.6.7.8.9", "..1.2.3.4.5.6.7.8.9");
        testSyntaxMatches(resultV3, "\\.7\\.", "\\.11\\.");
    }

    public void testMeetsCriteriaWithIPAddr() throws Exception {
        monitor = new SnmpMonitor();
        SnmpIPAddress result = new SnmpIPAddress("10.1.1.1");
        testSyntaxEquals(result, "10.1.1.1", "10.1.1.2");
        testSyntaxMatches(result, "10\\.1\\.1\\.[1-5]", "10\\.1\\.1\\.[02-9]");
        
        monitor = new SnmpV3Monitor();
        Variable resultV3 = new IpAddress("10.1.1.1");
        testSyntaxEquals(resultV3, "10.1.1.1", "10.1.1.2");
        testSyntaxMatches(resultV3, "10\\.1\\.1\\.[1-5]", "10\\.1\\.1\\.[02-9]");
    }

    public void testNumericString() {
        monitor = new SnmpMonitor();
        SnmpOctetString result = new SnmpOctetString("12345".getBytes());
        testOrderOperations(result, 12345);
        
        monitor = new SnmpV3Monitor();
        Variable resultV3 = new OctetString("12345".getBytes());
        testOrderOperations(resultV3, 12345);
    }
*/
/*    public void testMeetsCriteriaWithInteger() {
        monitor = new SnmpMonitor();
        SnmpInt32 result = new SnmpInt32(1234);
        testSyntaxEquals(result, "1234", "2234");
        testOrderOperations(result, 1234);
        testSyntaxMatches(result, "23", "14");
        monitor = new SnmpV3Monitor();
        
        Variable resultV3 = new Integer32(1234);
        testSyntaxEquals(resultV3, "1234", "2234");
        testOrderOperations(resultV3, 1234);
        testSyntaxMatches(resultV3, "23", "14");
    }

    public void testMeetsCriteriaWithCounter32() {
        monitor = new SnmpMonitor();
        SnmpCounter32 result = new SnmpCounter32(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);

        monitor = new SnmpV3Monitor();
        Variable resultV3 = new Counter32(1);
        testSyntaxEquals(resultV3, "1", "2");
        testOrderOperations(resultV3, 1);
    }

    public void testMeetsCriteriaWithGuage32() {
        monitor = new SnmpMonitor();
        SnmpGauge32 result = new SnmpGauge32(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);

        monitor = new SnmpV3Monitor();
        Variable resultV3 = new Gauge32(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    public void testMeetsCriteriaWithTimeTicks() {
        monitor = new SnmpMonitor();
        SnmpTimeTicks result = new SnmpTimeTicks(1);
        testSyntaxEquals(result, "0d 0h 0m 0s 10ms", "1d 1h 1m 1s 10ms");
        testSyntaxMatches(result, "0h", "1h");
        testOrderOperations(result, 1);

        monitor = new SnmpV3Monitor();
        Variable resultV3 = new TimeTicks(1);
        testSyntaxEquals(result, "0d 0h 0m 0s 10ms", "1d 1h 1m 1s 10ms");
        testSyntaxMatches(result, "0h", "1h");
        testOrderOperations(result, 1);
    }

    public void testMeetsCriteriaWithCounter64() {
        monitor = new SnmpMonitor();
        SnmpCounter64 result = new SnmpCounter64(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);

        monitor = new SnmpMonitor();
        Variable resultV3 = new Counter64(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }
*/
/*    public void testErrorConditions() {
        monitor = new SnmpMonitor();
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

        monitor = new SnmpV3Monitor();
        Variable resultV3 = new Integer32(1);
        try {
            monitor.meetsCriteria(resultV3, "X", "123");
            fail("Expected an exception to be thrown");
        } catch (IllegalArgumentException e) {
        }
        
        try {
            monitor.meetsCriteria(resultV3, "<", "abc");
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
    private void testSyntaxEquals(Variable result, String eqString, String neString) {
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
    private void testSyntaxMatches(Variable result, String matchString, String noMatchString) {
        assertTrue(monitor.meetsCriteria(result, SnmpMonitor.MATCHES, matchString));
        assertFalse(monitor.meetsCriteria(result, SnmpMonitor.MATCHES, noMatchString));
    }

    *//**
     * @param result
     *//*
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
    private void testOrderOperations(Variable result, int value) {
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
*/
}
