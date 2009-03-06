/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2007 Jun 23: Add tests for building PDUs. - dj@opennms.org
 * 
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.snmp.snmp4j;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue.StringSnmpValue;
import org.opennms.test.mock.MockLogAppender;
import org.snmp4j.PDU;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests for the SNMP4J strategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class Snmp4JStrategyTest extends MockSnmpAgentTestCase {
    private Snmp4JStrategy m_strategy = new Snmp4JStrategy();

    public void testNothing() throws Exception {
        // Just test our setUp()/tearDown()
    }
    
    public void XXXtestSendWithNullConfig() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        SnmpValue[] retvalues = null;
        
        PDU pdu = m_strategy.buildPdu(null, PDU.GET, oids, null);
        if (pdu != null) {
            retvalues = m_strategy.send(null, pdu, true);
        }
        
        SnmpValue[] values = retvalues;
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
    }

    public void testSendWithGetPduSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(getAgentConfig());
        SnmpValue[] retvalues = null;
        
        PDU pdu = m_strategy.buildPdu(agentConfig, PDU.GET, oids, null);
        if (pdu != null) {
            retvalues = m_strategy.send(agentConfig, pdu, true);
        }
        
        SnmpValue[] values = retvalues;
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
    }
    
    public void testSendWithGetPduMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(getAgentConfig());
        SnmpValue[] retvalues = null;
        
        PDU pdu = m_strategy.buildPdu(agentConfig, PDU.GET, oids, null);
        if (pdu != null) {
            retvalues = m_strategy.send(agentConfig, pdu, true);
        }
        
        SnmpValue[] values = retvalues;
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[1]);
    }
    

    public void testSendWithGetNextPduSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(getAgentConfig());
        SnmpValue[] retvalues = null;
        
        PDU pdu = m_strategy.buildPdu(agentConfig, PDU.GETNEXT, oids, null);
        if (pdu != null) {
            retvalues = m_strategy.send(agentConfig, pdu, true);
        }
        
        SnmpValue[] values = retvalues;
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
    }
    
    public void testSendWithGetNextPduMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(getAgentConfig());
        SnmpValue[] retvalues = null;
        
        /*
         * Build the PDU first, since there isn't any value in doing other
         * work like setting up the session if we don't have anything to send.
         */
        PDU pdu = m_strategy.buildPdu(agentConfig, PDU.GETNEXT, oids, null);
        if (pdu != null) {
            retvalues = m_strategy.send(agentConfig, pdu, true);
        }
        
        SnmpValue[] values = retvalues;
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
        // Expect the *next* value, so for .1.3.5.1.1.5.0
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_COUNTER32, 42, values[1]);
    }
    
    public void testGetSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.get(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
    }
    
    public void testGetMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = m_strategy.get(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_GAUGE32, 42, values[1]);
    }
    
    public void testGetNextSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.getNext(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
    }
    
    public void testGetNextMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = m_strategy.getNext(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
        // Expect the *next* value, so for .1.3.5.1.1.5.0
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_COUNTER32, 42, values[1]);
    }
    
    public void testPreparePduWithNoValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = null;
        
        PDU pdu = m_strategy.buildPdu(new Snmp4JAgentConfig(getAgentConfig()), PDU.SET, oids, values);
        assertNotNull("PDU should not be null", pdu);
        
        assertEquals("PDU variable bindings size", oids.length, pdu.getVariableBindings().size());
        
        for (int i = 0; i < oids.length; i++) {
            VariableBinding vb = pdu.get(i);
            assertEquals("PDU variable binding " + i + " OID", oids[i].toString(), "." + vb.getOid().toString());
            assertEquals("PDU variable binding " + i + " syntax", vb.getSyntax(), SMIConstants.SYNTAX_NULL);
            assertEquals("PDU variable binding " + i + " variable syntax", vb.getVariable().getSyntax(), SMIConstants.SYNTAX_NULL);
        }
    }
    
    public void testPreparePduWithValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = new SnmpValue[] {
                new StringSnmpValue("foo"),
                new StringSnmpValue("bar")
        };
        
        PDU pdu = m_strategy.buildPdu(new Snmp4JAgentConfig(getAgentConfig()), PDU.SET, oids, values);
        assertNotNull("PDU should not be null", pdu);
        
        assertEquals("PDU variable bindings size", oids.length, pdu.getVariableBindings().size());
        
        for (int i = 0; i < oids.length; i++) {
            VariableBinding vb = pdu.get(i);
            assertEquals("PDU variable binding " + i + " OID", oids[i].toString(), "." + vb.getOid().toString());
            assertEquals("PDU variable binding " + i + " syntax", vb.getSyntax(), SMIConstants.SYNTAX_OCTET_STRING);
            assertEquals("PDU variable binding " + i + " variable syntax", vb.getVariable().getSyntax(), SMIConstants.SYNTAX_OCTET_STRING);
            assertEquals("PDU variable binding " + i + " variable value", vb.getVariable().toString(), values[i].toString());
        }
    }
    
    public void testPreparePduWithTooFewValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = new SnmpValue[] {
                new StringSnmpValue("foo"),
        };
        
        PDU pdu = m_strategy.buildPdu(new Snmp4JAgentConfig(getAgentConfig()), PDU.SET, oids, values);
        assertNull("PDU should be null", pdu);
        
        LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
        assertEquals("number of logged events", 1, events.length);
        assertEquals("first logged event severity (should be ERROR)", Level.ERROR, events[0].getLevel());
        
        MockLogAppender.resetEvents();
        MockLogAppender.resetLogLevel();
    }
    
    public void testPreparePduWithTooManyValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = new SnmpValue[] {
                new StringSnmpValue("foo"),
                new StringSnmpValue("bar"),
                new StringSnmpValue("baz")
        };
        
        PDU pdu = m_strategy.buildPdu(new Snmp4JAgentConfig(getAgentConfig()), PDU.SET, oids, values);
        assertNull("PDU should be null", pdu);
        
        LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
        assertEquals("number of logged events", 1, events.length);
        assertEquals("first logged event severity (should be ERROR)", Level.ERROR, events[0].getLevel());
        
        MockLogAppender.resetEvents();
        MockLogAppender.resetLogLevel();
    }
    
    private void assertSnmpValueEquals(String message, int expectedType, int expectedValue, SnmpValue value) {
        assertEquals(message + " getType()", expectedType, value.getType());
        assertEquals(message + " toInt()", expectedValue, value.toInt());
    }
}
