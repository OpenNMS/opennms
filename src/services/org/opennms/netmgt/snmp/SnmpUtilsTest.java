//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestSuite;

import org.opennms.netmgt.mock.OpenNMSTestCase;

public class SnmpUtilsTest extends OpenNMSTestCase {
    
    public static TestSuite suite() {
        Class testClass = SnmpUtilsTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy"));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy"));
        return suite;
    }


    protected void setUp() throws Exception {
        super.setStartEventd(false);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCreateSnmpAgentConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig();
        assertNull(agentConfig.getAddress());
        assertSnmpAgentConfigDefaults(agentConfig);
        
        agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        assertNotNull(agentConfig.getAddress());
        assertEquals(InetAddress.getLocalHost().getHostAddress(), agentConfig.getAddress().getHostAddress());
        assertSnmpAgentConfigDefaults(agentConfig);
    }
    
    public void testGet() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpValue val = SnmpUtils.get(agentConfig, new SnmpObjId(".1.3.6.1.2.1.1.2.0"));
        assertNotNull(val);
    }
    
    public void testBadGet() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpValue val = SnmpUtils.get(agentConfig, new SnmpObjId(".1.3.6.1.2.1.1.2"));
        assertEquals(null, val);
    }
    
    public void getMultipleVarbinds() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpObjId[] oids = { new SnmpObjId(".1.3.6.1.2.1.1.2.0"), new SnmpObjId(".1.3.6.1.2.1.1.3.0") };
        SnmpValue[] vals = SnmpUtils.get(agentConfig, oids);
        assertNotNull(vals);
        assertEquals(2, vals.length);
    }
    
    public void testGetNext() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpValue val = SnmpUtils.getNext(agentConfig, new SnmpObjId(".1.3.6.1.2.1.1"));
        assertNotNull(val);
    }
    
    public void testGetNextMultipleVarbinds() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpObjId[] oids = { new SnmpObjId(".1.3.6.1.2.1.1.2.0"), new SnmpObjId(".1.3.6.1.2.1.1.3.0") };
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
    public void testCreateWalkerWithAgentConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddress.getLocalHost());
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "Test", new ColumnTracker(SnmpObjId.get("1.2.3.4")));
        assertNotNull(walker);
    }
    
    public void testGetStrategy() {
        SnmpStrategy strategy = SnmpUtils.getStrategy();
        assertNotNull(strategy);
        assertEquals(System.getProperty("org.opennms.snmp.strategyClass"), strategy.getClass().getName());
    }
    
    public void testGetValueFactory() throws UnknownHostException {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
        assertNotNull(valueFactory);
        
        // SnmpValue.SNMP_OCTET_STRING;
        SnmpValue octetString = valueFactory.getOctetString("mystring".getBytes());
        assertEquals("Expect an octectString", SnmpValue.SNMP_OCTET_STRING, octetString.getType());
        assertEquals("mystring", octetString.toDisplayString());
        assertEquals("mystring", new String(octetString.getBytes()));
        
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
    
}