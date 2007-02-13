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

import java.net.InetAddress;

import junit.framework.TestCase;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.snmp4j.PDU;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for the SNMP4J strategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class Snmp4JStrategyTest extends TestCase {

    private InetAddress m_agentAddress;
    private int m_agentPort;
    private MockSnmpAgent m_agent;
    private Snmp4JStrategy m_strategy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();
        m_strategy = new Snmp4JStrategy();
        
        m_agentAddress = InetAddress.getByName("127.0.0.1");
        m_agentPort = 1691;

        initializeAgent("/org/opennms/netmgt/snmp/loadSnmpDataTest.properties");
}

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }

        MockUtil.println("------------ End Test " + getName() + " --------------------------");
        super.tearDown();
    }
    
    public void testNothing() throws Exception {
        // Just test our setUp()/tearDown()
    }

    public void testSendWithGetPduSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { new SnmpObjId(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.send(new Snmp4JAgentConfig(getAgentConfig()), PDU.GET, oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
    }
    
    public void testSendWithGetPduMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                new SnmpObjId(".1.3.5.1.1.3.0"),
                new SnmpObjId(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = m_strategy.send(new Snmp4JAgentConfig(getAgentConfig()), PDU.GET, oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[1]);
    }
    

    public void testSendWithGetNextPduSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { new SnmpObjId(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.send(new Snmp4JAgentConfig(getAgentConfig()), PDU.GETNEXT, oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
    }
    
    public void testSendWithGetNextPduMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                new SnmpObjId(".1.3.5.1.1.3.0"),
                new SnmpObjId(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = m_strategy.send(new Snmp4JAgentConfig(getAgentConfig()), PDU.GETNEXT, oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
        // Expect the *next* value, so for .1.3.5.1.1.5.0
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_COUNTER32, 42, values[1]);
    }
    
    public void testGetSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { new SnmpObjId(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.get(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
    }
    
    public void testGetMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                new SnmpObjId(".1.3.5.1.1.3.0"),
                new SnmpObjId(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = m_strategy.get(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_GAUGE32, 42, values[1]);
    }
    
    public void testGetNextSingleValue() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] { new SnmpObjId(".1.3.5.1.1.3.0") };
        
        SnmpValue[] values = m_strategy.getNext(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
    }
    
    public void testGetNextMultipleValues() throws Exception {
        SnmpObjId[] oids = new SnmpObjId[] {
                new SnmpObjId(".1.3.5.1.1.3.0"),
                new SnmpObjId(".1.3.5.1.1.4.0"),
        };
        
        SnmpValue[] values = m_strategy.getNext(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
        // Expect the *next* value, so for .1.3.5.1.1.5.0
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_COUNTER32, 42, values[1]);
    }
    
    private SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(m_agentAddress);
        config.setPort(m_agentPort);
        config.setVersion(SnmpAgentConfig.VERSION1);
        return config;
    }
    
    private void initializeAgent(String testData) throws InterruptedException {
        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource(testData),
                                                  m_agentAddress.getHostAddress() + "/" + m_agentPort);
    }
    
    private void assertSnmpValueEquals(String message, int expectedType, int expectedValue, SnmpValue value) {
        assertEquals(message + " getType()", expectedType, value.getType());
        assertEquals(message + " toInt()", expectedValue, value.toInt());
    }
}
