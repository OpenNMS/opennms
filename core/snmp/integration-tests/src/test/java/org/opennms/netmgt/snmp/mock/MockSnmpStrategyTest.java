/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class MockSnmpStrategyTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(MockSnmpStrategyTest.class);
	
    private static MockSnmpStrategy m_strategy;
    private InetAddress m_agentAddress = InetAddressUtils.addr("127.0.0.1");
    private int m_agentPort = 1691;
    private String m_oldProperty;

    @Before
    public void setUp() throws Exception {
        m_strategy = new MockSnmpStrategy();
        MockSnmpStrategy.setDataForAddress(new SnmpAgentAddress(m_agentAddress, m_agentPort), new ClassPathResource("loadSnmpDataTest.properties"));
        m_oldProperty = System.getProperty("org.opennms.snmp.strategyClass");
        System.setProperty("org.opennms.snmp.strategyClass", m_strategy.getClass().getName());
    }
    
    @After
    public void tearDown() {
    	if (m_oldProperty == null) {
    		System.getProperties().remove("org.opennms.snmp.strategyClass");
    	} else {
    		System.setProperty("org.opennms.snmp.strategyClass", m_oldProperty);
    	}
    	
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
        final SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        final SnmpValue[] values = m_strategy.get(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_GAUGE32, 42, values[1]);
    }
    
    @Test
    public void testGetBulkMultipleValues() throws Exception {
        final SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        final SnmpValue[] values = m_strategy.get(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_INT32, 42, values[0]);
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_GAUGE32, 42, values[1]);
    }
    
    @Test
    public void testGetNextSingleValue() throws Exception {
        final SnmpObjId[] oids = new SnmpObjId[] { SnmpObjId.get(".1.3.5.1.1.3.0") };
        
        final SnmpValue[] values = m_strategy.getNext(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 1, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
    }
    
    @Test
    public void testGetNextMultipleValues() throws Exception {
        final SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
        };
        
        final SnmpValue[] values = m_strategy.getNext(getAgentConfig(), oids);
        
        assertNotNull("values should not be null", values);
        assertEquals("values list size", 2, values.length);
        // Expect the *next* value, so for .1.3.5.1.1.4.0
        assertSnmpValueEquals("values[0]", SnmpValue.SNMP_GAUGE32, 42, values[0]);
        // Expect the *next* value, so for .1.3.5.1.1.5.0
        assertSnmpValueEquals("values[1]", SnmpValue.SNMP_COUNTER32, 42, values[1]);
    }

    @Test
    public void testSetSingleValue() throws Exception {
    	m_strategy.set(getAgentConfig(), SnmpObjId.get(".1.3.5.1.1.3.0"), m_strategy.getValueFactory().getInt32(4));
    	
    	final SnmpValue result = m_strategy.get(getAgentConfig(), SnmpObjId.get(".1.3.5.1.1.3.0"));
    	assertNotNull(result);
    	assertEquals(4, result.toInt());
    }

    @Test
    public void testSetBadAgent() throws Exception {
    	final SnmpAgentConfig sac = getAgentConfig();
    	sac.setAddress(InetAddressUtils.addr("1.2.3.4"));

    	m_strategy.set(sac, SnmpObjId.get(".1.3.5.1.1.3.0"), m_strategy.getValueFactory().getInt32(4));
    	
    	final SnmpValue result = m_strategy.get(sac, SnmpObjId.get(".1.3.5.1.1.3.0"));
    	
    	assertNull(result);
    }

    @Test
    public void testSetMultipleValues() throws Exception {
        final SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0")
        };
        final SnmpValue[] values = new SnmpValue[] {
        		m_strategy.getValueFactory().getInt32(4),
        		m_strategy.getValueFactory().getGauge32(5)
        };

        m_strategy.set(getAgentConfig(), oids, values);
    	
    	final SnmpValue[] results = m_strategy.get(getAgentConfig(), oids);
    	assertNotNull(results);
    	assertEquals(2, results.length);
    	assertEquals(4, results[0].toInt());
    	assertEquals(5, results[1].toInt());
    }

    @Test
    public void testSetMultipleBadAgent() throws Exception {
    	final SnmpAgentConfig sac = getAgentConfig();
    	sac.setAddress(InetAddressUtils.addr("1.2.3.4"));

        final SnmpObjId[] oids = new SnmpObjId[] {
                SnmpObjId.get(".1.3.5.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0")
        };
        final SnmpValue[] values = new SnmpValue[] {
        		m_strategy.getValueFactory().getInt32(4),
        		m_strategy.getValueFactory().getGauge32(5)
        };

        m_strategy.set(sac, oids, values);
    	
    	final SnmpValue[] results = m_strategy.get(sac, oids);
    	assertNotNull(results);
    	assertEquals(2, results.length);
    	assertNull(results[0]);
    	assertNull(results[1]);
    }

    @Test
    public void testTracker() throws Exception {
        final CountingColumnTracker ct = new CountingColumnTracker(SnmpObjId.get(".1.3.5.1.1"));

        walk(ct, 10, 3);
        assertEquals("number of columns returned must match test data", Long.valueOf(9).longValue(), ct.getCount());
    }

    @Test
    public void testTrackerTimeout() throws Exception {
        final CountingColumnTracker ct = new CountingColumnTracker(SnmpObjId.get(".1.3.5.1.1"));
        final SnmpAgentConfig sac = getAgentConfig();
        sac.setPort(12345);
        final SnmpWalker walker = SnmpUtils.createWalker(sac, "test", ct);
        assertNotNull(walker);
        walker.start();
        walker.waitFor();
        assertEquals("it should match no columns (timeout)", Long.valueOf(0).longValue(), ct.getCount());
    }
    
    private void assertSnmpValueEquals(final String message, final int expectedType, final int expectedValue, final SnmpValue value) {
        assertEquals(message + " getType()", expectedType, value.getType());
        assertEquals(message + " toInt()", expectedValue, value.toInt());
    }

    private SnmpAgentConfig getAgentConfig() {
        final SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(m_agentAddress);
        config.setPort(m_agentPort);
        config.setVersion(SnmpAgentConfig.VERSION1);
        config.setMaxVarsPerPdu(20);
        config.setMaxRepetitions(20);
        config.setRetries(3);
        return config;
    }

    private void walk(final CollectionTracker c, final int maxVarsPerPdu, final int maxRepetitions) throws Exception {
        final SnmpAgentConfig config = getAgentConfig();
        final SnmpWalker walker = SnmpUtils.createWalker(config, "test", c);
        assertNotNull(walker);
        walker.start();
        walker.waitFor();
    }

    static private class CountingColumnTracker extends ColumnTracker {
        private long m_count = 0;
        public CountingColumnTracker(final SnmpObjId base) {
            super(base);
        }
        public CountingColumnTracker(final SnmpObjId base, final int maxRepetitions, final int maxRetries) {
            super(base, maxRepetitions, maxRetries);
        }
        public long getCount() {
            return m_count;
        }
        @Override
        protected void storeResult(final SnmpResult res) {
        	LOG.debug("storing result {}", res);
            m_count++;
        }

    }
}
