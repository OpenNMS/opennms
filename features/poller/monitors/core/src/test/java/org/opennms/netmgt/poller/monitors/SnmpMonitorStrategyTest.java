/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.test.ThrowableAnticipator;

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
        SnmpValue result = nullValue();
        testSyntaxEquals(result, "", "1");
    }
    

    @Test
    public void testMeetsCriteriaWithString() {
        SnmpValue result = octetString("A Test String");
        testSyntaxEquals(result, "A Test String", "a test string");
        testSyntaxMatches(result, "[tT][eE][sS][tT]", "test");
        testSyntaxMatches(result, "^A Test String$", "^A Test$");
    }

    @Test
    public void testMeetsCriteriaWithObjectID() {
        SnmpValue result = oid(".1.2.3.4.5.6.7.8.9");
        testSyntaxEquals(result, ".1.2.3.4.5.6.7.8.9", "..1.2.3.4.5.6.7.8.9");
        testSyntaxMatches(result, "\\.7\\.", "\\.11\\.");
    }

    @Test
    public void testMeetsCriteriaWithIPAddr() throws Exception {
        SnmpValue result = ipAddr("10.1.1.1");
        testSyntaxEquals(result, "10.1.1.1", "10.1.1.2");
        testSyntaxMatches(result, "10\\.1\\.1\\.[1-5]", "10\\.1\\.1\\.[02-9]");
    }

	@Test
    public void testNumericString() {
        SnmpValue result = octetString("12345");
        testOrderOperations(result, 12345);
    }

    @Test
    public void testMeetsCriteriaWithInteger() {
        SnmpValue result = int32Value(1234);
        testSyntaxEquals(result, "1234", "2234");
        testOrderOperations(result, 1234);
        testSyntaxMatches(result, "23", "14");
    }

    private SnmpValue int32Value(int i) {
		return SnmpUtils.getValueFactory().getInt32(i);
	}

	@Test
    public void testMeetsCriteriaWithCounter32() {
        SnmpValue result = counter32Value(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    private SnmpValue counter32Value(int i) {
		return SnmpUtils.getValueFactory().getCounter32(i);
	}

	@Test
    public void testMeetsCriteriaWithGauge32() {
        SnmpValue result = gauge32Value(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    private SnmpValue gauge32Value(int i) {
		return SnmpUtils.getValueFactory().getGauge32(i);
	}

	@Test
    public void testMeetsCriteriaWithTimeTicks() {
        SnmpValue result = timeticks("1");
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    private SnmpValue timeticks(String val) {
		return SnmpUtils.getValueFactory().getTimeTicks(Long.parseLong(val));
	}

	@Test
    public void testMeetsCriteriaWithCounter64() {
        SnmpValue result = counter64Value(1);
        testSyntaxEquals(result, "1", "2");
        testOrderOperations(result, 1);
    }

    private SnmpValue counter64Value(int i) {
		return SnmpUtils.getValueFactory().getCounter64(BigInteger.valueOf(i));
	}

	@Test
    public void testErrorConditions() {
    	SnmpValue result = int32Value(1);
        
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
        SnmpValue result = int32Value(1);

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
    
    SnmpValue octetString(String val) {
    	return SnmpUtils.getValueFactory().getOctetString(val.getBytes());
    }
    
    SnmpValue nullValue() {
    	return SnmpUtils.getValueFactory().getNull();
    }
    
    SnmpValue oid(String objectId) {
    	return SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(objectId));
    }
    
    private SnmpValue ipAddr(String addr) throws UnknownHostException {
        return SnmpUtils.getValueFactory().getIpAddress(InetAddressUtils.addr(addr));
    }



}
