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

package org.opennms.netmgt.snmp.snmp4j;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.snmp4j.PDU;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.VariableBinding;

/**
 * Tests for the SNMP4J strategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class Snmp4JStrategyTest extends MockSnmpAgentTestCase {

	@Override
	protected boolean usingMockStrategy() {
		return false;
	}

	private final Snmp4JStrategy m_strategy = new Snmp4JStrategy();
	

    @Test
    @Ignore
    public void testSendWithNullConfig() throws Exception {
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

    @Test
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
    
    @Test
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
    

    @Test
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
    
    @Test
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
    
    @Test
    public void testGetSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.get(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
    }
    
    @Test
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
    
    @Test
    public void testGetNextSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.getNext(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
    }
    
    @Test
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
    
    @Test
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
    
    @Test
    public void testPreparePduWithValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = new SnmpValue[] {
                snmpValue("foo"),
                snmpValue("bar")
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
    
    @Test
    public void testPreparePduWithTooFewValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = new SnmpValue[] {
                snmpValue("foo"),
        };
        
        PDU pdu = m_strategy.buildPdu(new Snmp4JAgentConfig(getAgentConfig()), PDU.SET, oids, values);
        assertNull("PDU should be null", pdu);
        
        MockLogAppender.resetEvents();
        MockLogAppender.resetLogLevel();
    }
    
    @Test
    public void testPreparePduWithTooManyValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = new SnmpValue[] {
                snmpValue("foo"),
                snmpValue("bar"),
                snmpValue("baz")
        };
        
        PDU pdu = m_strategy.buildPdu(new Snmp4JAgentConfig(getAgentConfig()), PDU.SET, oids, values);
        assertNull("PDU should be null", pdu);
        
        MockLogAppender.resetEvents();
        MockLogAppender.resetLogLevel();
    }
    
    private void assertSnmpValueEquals(String message, int expectedType, int expectedValue, SnmpValue value) {
    	assertNotNull(message + " is null", value);
        assertEquals(message + " getType()", expectedType, value.getType());
        assertEquals(message + " toInt()", expectedValue, value.toInt());
    }
    
    SnmpValue snmpValue(String val) {
    	return m_strategy.getValueFactory().getOctetString(val.getBytes());
    }
}
