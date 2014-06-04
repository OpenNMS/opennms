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

package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy;
import org.opennms.netmgt.snmp.mock.MockSnmpStrategy;
import org.opennms.netmgt.snmp.snmp4j.MockSnmpAgentTestCase;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.springframework.core.io.ClassPathResource;

@RunWith(Parameterized.class)
public class SnmpUtilsTest extends MockSnmpAgentTestCase implements TrapProcessorFactory {
	
	@Parameters
	public static List<Object[]> data() {
		return Arrays.asList(new Object[][] {
				/* Strategy class,        SnmpVersion,              trapsSupported */
				{ JoeSnmpStrategy.class.getName(),  SnmpAgentConfig.VERSION1,  true },	
				{ Snmp4JStrategy.class.getName(),   SnmpAgentConfig.VERSION1,  true },	
				{ MockSnmpStrategy.class.getName(), SnmpAgentConfig.VERSION1,  false },	
		});
	}
    
    private TestTrapListener m_trapListener;

    static private final class TestTrapProcessor implements TrapProcessor {
        @Override
        public void setCommunity(String community) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setTimeStamp(long timeStamp) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setVersion(String version) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setAgentAddress(InetAddress agentAddress) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void processVarBind(SnmpObjId name, SnmpValue value) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setTrapAddress(InetAddress trapAddress) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setTrapIdentity(TrapIdentity trapIdentity) {
            // TODO Auto-generated method stub
            
        }
    }

    private static final class TestTrapListener implements TrapNotificationListener {
        private boolean m_error = false;
        private int m_receivedTrapCount = 0;

        @Override
        public void trapReceived(TrapNotification trapNotification) {
            m_receivedTrapCount++;
        }

        @Override
        public void trapError(int error, String msg) {
            m_error = true;
        }
        
        @SuppressWarnings("unused")
		public boolean hasError() {
            return m_error;
        }
        public int getReceivedTrapCount() {
            return m_receivedTrapCount;
        }
    }
    
    String m_strategyClass;
    int m_snmpVersion;
    boolean m_trapsSupported;
    String m_oldProperty;
    
    public SnmpUtilsTest(String strategyClass, int snmpVersion, boolean trapsSupported) {
    	m_strategyClass = strategyClass;
    	m_snmpVersion = snmpVersion;
    	m_trapsSupported = trapsSupported;
    	
    	m_oldProperty = System.getProperty("org.opennms.snmp.strategyClass");
    	System.setProperty("org.opennms.snmp.strategyClass", m_strategyClass);
    	
        setPropertiesResource(new ClassPathResource("snmpTestData1.properties"));
    }


	@After
    public void cleanupTrapListener() throws Exception {
    	if (m_trapListener != null) {
    		SnmpUtils.unregisterForTraps(m_trapListener, null, 9162);
    	}
    	
    	if (m_oldProperty == null) {
    		System.getProperties().remove("org.opennms.snmp.strategyClass");
    	} else {
    		System.setProperty("org.opennms.snmp.strategyClass", m_oldProperty);
    	}
    }
    
    @Test
    public void testCreateSnmpAgentConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig();
        assertNull(agentConfig.getAddress());
        assertSnmpAgentConfigDefaults(agentConfig);
        
        agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        assertNotNull(agentConfig.getAddress());
        assertEquals(InetAddress.getLocalHost().getHostAddress(), agentConfig.getAddress().getHostAddress());
        assertSnmpAgentConfigDefaults(agentConfig);
    }
    
    @Test
    public void testGet() throws UnknownHostException {
        SnmpAgentConfig agentConfig = getAgentConfig();
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(".1.3.6.1.2.1.1.2.0"));
        assertNotNull(val);
    }

    @Test
    public void testBadGet() throws UnknownHostException {
        SnmpAgentConfig agentConfig = getAgentConfig();
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(".1.3.6.1.2.1.1.2"));
        assertEquals(null, val);
    }
    
    @Test
    public void getMultipleVarbinds() throws UnknownHostException {
        SnmpAgentConfig agentConfig = getAgentConfig();
        SnmpObjId[] oids = { SnmpObjId.get(".1.3.6.1.2.1.1.2.0"), SnmpObjId.get(".1.3.6.1.2.1.1.3.0") };
        SnmpValue[] vals = SnmpUtils.get(agentConfig, oids);
        assertNotNull(vals);
        assertEquals(2, vals.length);
    }
    
    @Test
    public void testGetNext() throws UnknownHostException {
        SnmpAgentConfig agentConfig = getAgentConfig();
        SnmpValue val = SnmpUtils.getNext(agentConfig, SnmpObjId.get(".1.3.6.1.2.1.1"));
        assertNotNull(val);
    }
    
    @Test
    public void testGetNextMultipleVarbinds() throws UnknownHostException {
        SnmpAgentConfig agentConfig = getAgentConfig();
        SnmpObjId[] oids = { SnmpObjId.get(".1.3.6.1.2.1.1.2.0"), SnmpObjId.get(".1.3.6.1.2.1.1.3.0") };
        SnmpValue[] vals = SnmpUtils.getNext(agentConfig, oids);
        assertNotNull(vals);
        assertEquals(2, vals.length);
        assertNotNull(vals);
    }
    
    private void assertSnmpAgentConfigDefaults(SnmpAgentConfig agentConfig) {
        assertEquals(SnmpAgentConfig.DEFAULT_PORT, agentConfig.getPort());
        assertEquals(SnmpAgentConfig.DEFAULT_TIMEOUT, agentConfig.getTimeout());
        assertEquals(SnmpAgentConfig.DEFAULT_VERSION, agentConfig.getVersion());
    }
    
/*    public void testCreateWalker() throws UnknownHostException {
        SnmpWalker walker = SnmpUtils.createWalker(InetAddress.getLocalHost(), "Test", 5, new ColumnTracker(SnmpObjId.get(".1.2.3.4")));
        assertNotNull(walker);
    }
*/    
    @Test
    public void testCreateWalkerWithAgentConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = getAgentConfig();
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "Test", new ColumnTracker(SnmpObjId.get("1.2.3.4")));
        assertNotNull(walker);
    }
    
    @Test
    public void testGetStrategy() {
        SnmpStrategy strategy = SnmpUtils.getStrategy();
        assertNotNull(strategy);
        assertEquals(System.getProperty("org.opennms.snmp.strategyClass"), strategy.getClass().getName());
    }
    
    @Test
    public void testSendV1Trap() throws Exception {
    	assumeTrue(m_trapsSupported);
        m_trapListener = new TestTrapListener();
        SnmpUtils.registerForTraps(m_trapListener, this, null, 9162);

        SnmpV1TrapBuilder trap = SnmpUtils.getV1TrapBuilder();
        trap.setAgentAddress(InetAddress.getLocalHost());
        trap.setEnterprise(SnmpObjId.get(".0.0"));
        trap.setGeneric(6);
        trap.setSpecific(1);
        trap.setTimeStamp(8640000);
        trap.send(InetAddress.getLocalHost().getHostAddress(), 9162, "public");
        Thread.sleep(1000);
        assertEquals("Unexpected number of traps Received", 1, m_trapListener.getReceivedTrapCount());
    }
    
    @Test
    public void testSendV2Trap() throws Exception {
    	assumeTrue(m_trapsSupported);
        m_trapListener = new TestTrapListener();
        SnmpUtils.registerForTraps(m_trapListener, this, null, 9162);

        SnmpObjId enterpriseId = SnmpObjId.get(".0.0");
        SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        
        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(trapOID));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(enterpriseId));

        pdu.send(InetAddress.getLocalHost().getHostAddress(), 9162, "public");
        Thread.sleep(1000);
        assertEquals("Unexpected number of traps Received", 1, m_trapListener.getReceivedTrapCount());
    }
    
        @Override
    public TrapProcessor createTrapProcessor() {
        return new TestTrapProcessor();
    }
    
    @Test
    public void testSendV1TestTrap() throws Exception {
    	assumeTrue(m_trapsSupported);
        m_trapListener = new TestTrapListener();
        SnmpUtils.registerForTraps(m_trapListener, this, null, 9162);

        SnmpV1TrapBuilder trap = SnmpUtils.getV1TrapBuilder();
        trap.setAgentAddress(InetAddress.getLocalHost());
        trap.setEnterprise(SnmpObjId.get(".0.0"));
        trap.setGeneric(6);
        trap.setSpecific(1);
        trap.setTimeStamp(8640000);
        trap.sendTest(InetAddress.getLocalHost().getHostAddress(), 9162, "public");
        assertEquals("Unexpected number of traps Received", 1, m_trapListener.getReceivedTrapCount());
    }
    
    @Test
    public void testSendV2TestTrap() throws Exception {
    	assumeTrue(m_trapsSupported);
        m_trapListener = new TestTrapListener();
        SnmpUtils.registerForTraps(m_trapListener, this, null, 9162);

        SnmpObjId enterpriseId = SnmpObjId.get(".0.0");
        SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        
        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(trapOID));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(enterpriseId));

        pdu.sendTest(InetAddress.getLocalHost().getHostAddress(), 9162, "public");
        assertEquals("Unexpected number of traps Received", 1, m_trapListener.getReceivedTrapCount());
    }
    
    @Test
    public void testGetValueFactory() throws UnknownHostException {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
        assertNotNull(valueFactory);
        
        // SnmpValue.SNMP_OCTET_STRING;
        SnmpValue octetString = valueFactory.getOctetString("mystring".getBytes());
        assertEquals("Expect an octectString", SnmpValue.SNMP_OCTET_STRING, octetString.getType());
        assertEquals("mystring", octetString.toDisplayString());
        assertEquals("mystring", new String(octetString.getBytes()));
        // test for non-printables in string
        SnmpValue nonPrintable = valueFactory.getOctetString("non-printable\0".getBytes());
        assertEquals("non-printable.", nonPrintable.toDisplayString());
        SnmpValue hexString = valueFactory.getOctetString("\1\2\3\4".getBytes());
        assertEquals("01020304", hexString.toHexString());
        
        // SnmpValue.SNMP_COUNTER32;
        SnmpValue counter32 = valueFactory.getCounter32(0xF7654321L);
        assertEquals("Expected a counter32", SnmpValue.SNMP_COUNTER32, counter32.getType());
        assertEquals(0xF7654321L, counter32.toLong());
        assertEquals(0xF7654321L, valueFactory.getValue(SnmpValue.SNMP_COUNTER32, BigInteger.valueOf(0xF7654321L).toByteArray()).toLong());
        assertEquals(counter32.toBigInteger(), new BigInteger(counter32.getBytes()));
                
        // SnmpValue.SNMP_COUNTER64;
        BigInteger maxLongPlusSome = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(16));
        SnmpValue counter64 = valueFactory.getCounter64(maxLongPlusSome);
        assertEquals("Expected a counter64", SnmpValue.SNMP_COUNTER64, counter64.getType());
        assertEquals(maxLongPlusSome, counter64.toBigInteger());
        assertEquals(maxLongPlusSome, valueFactory.getValue(SnmpValue.SNMP_COUNTER64, maxLongPlusSome.toByteArray()).toBigInteger());
        assertEquals(counter64.toBigInteger(), new BigInteger(counter64.getBytes()));

        // SnmpValue.SNMP_GAUGE32;
        SnmpValue gauge32 = valueFactory.getGauge32(0xF7654321L);
        assertEquals("Expected a gauge32", SnmpValue.SNMP_GAUGE32, gauge32.getType());
        assertEquals(0xF7654321L, gauge32.toLong());
        assertEquals(0xF7654321L, valueFactory.getValue(SnmpValue.SNMP_GAUGE32, BigInteger.valueOf(0xF7654321L).toByteArray()).toLong());
        assertEquals(gauge32.toBigInteger(), new BigInteger(gauge32.getBytes()));
                
        // SnmpValue.SNMP_INT32;
        SnmpValue int32 = valueFactory.getInt32(0x77654321);
        assertEquals("Expected a int32", SnmpValue.SNMP_INT32, int32.getType());
        assertEquals(0x77654321, int32.toInt());
        assertEquals(0x77654321L, valueFactory.getValue(SnmpValue.SNMP_INT32, BigInteger.valueOf(0x77654321L).toByteArray()).toLong());
        assertEquals(int32.toBigInteger(), new BigInteger(int32.getBytes()));

        // SnmpValue.SNMP_IPADDRESS;
        InetAddress addr = InetAddress.getLocalHost();
        SnmpValue ipAddr = valueFactory.getIpAddress(addr);
        assertEquals("Expected an ipAddress", SnmpValue.SNMP_IPADDRESS, ipAddr.getType());
        assertEquals(addr, ipAddr.toInetAddress());
        assertEquals(addr, valueFactory.getValue(SnmpValue.SNMP_IPADDRESS, addr.getAddress()).toInetAddress());
        assertEquals(addr, InetAddress.getByAddress(ipAddr.getBytes()));
        
        // SnmpValue.SNMP_OBJECT_IDENTIFIER;
        SnmpObjId objId = SnmpObjId.get(".1.3.6.1.2.1.1.3.0");
        SnmpValue objVal = valueFactory.getObjectId(objId);
        assertEquals("Expected an object identifier", SnmpValue.SNMP_OBJECT_IDENTIFIER, objVal.getType());
        assertEquals(objId, objVal.toSnmpObjId());
        assertEquals(objId, valueFactory.getValue(SnmpValue.SNMP_OBJECT_IDENTIFIER, objId.toString().getBytes()).toSnmpObjId());
        assertEquals(objId, SnmpObjId.get(new String(objVal.getBytes())));

        // SnmpValue.SNMP_TIMETICKS;
        long ticks = 4700;
        SnmpValue timeTicks = valueFactory.getTimeTicks(ticks);
        assertEquals("Expected an timeticks object", SnmpValue.SNMP_TIMETICKS, timeTicks.getType());
        assertEquals(ticks, timeTicks.toLong());
        assertEquals(ticks, valueFactory.getValue(SnmpValue.SNMP_TIMETICKS, BigInteger.valueOf(ticks).toByteArray()).toLong());
        assertEquals(timeTicks.toBigInteger(), new BigInteger(timeTicks.getBytes()));
        
        
    }
    
    @Test
    public void testGetProtoCounter64Value() {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
        assertNotNull(valueFactory);

        byte[] ourBytes = new byte[]{ 0x00, 0x00, (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef, (byte)0xca, (byte)0xfe };
        SnmpValue octStr = valueFactory.getOctetString(ourBytes);
        assertEquals("Expecting 0x0000deadbeefcafe", new Long(0x0000deadbeefcafeL), SnmpUtils.getProtoCounter64Value(octStr));
    }
    
}
