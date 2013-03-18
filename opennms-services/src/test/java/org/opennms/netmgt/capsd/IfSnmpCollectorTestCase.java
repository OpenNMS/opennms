/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.capsd.snmp.IfTable;
import org.opennms.netmgt.capsd.snmp.IfXTable;
import org.opennms.netmgt.capsd.snmp.IpAddrTable;
import org.opennms.netmgt.capsd.snmp.SystemGroup;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.springframework.core.io.ClassPathResource;

public class IfSnmpCollectorTestCase extends OpenNMSTestCase {
    private static final String HOST_PROPERTY = "mock.snmpHost";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int PORT = 9161;

    private InetAddress m_addr;
    
    private volatile MockSnmpAgent m_agent;

    private volatile IfSnmpCollector m_ifSnmpc;

    private volatile boolean m_hasRun = false;
    
    public static class JoeSnmpIfSnmpCollectorTestCase extends IfSnmpCollectorTestCase {
        public void setUp() throws Exception {
            System.setProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy");
            super.setUp();
        }
    }

    public static class SNMP4JIfSnmpCollectorTestCase extends IfSnmpCollectorTestCase {
        public void setUp() throws Exception {
            System.setProperty("org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
            super.setUp();
        }
    }

    @Override
    protected void setUp() throws Exception {
        setStartEventd(false);
        super.setUp();

        String hostName = System.getProperty(HOST_PROPERTY, DEFAULT_HOST);
        m_addr = InetAddressUtils.addr(hostName);
        m_ifSnmpc = new IfSnmpCollector(m_addr);

        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties").getURL(), InetAddressUtils.str(m_addr) + "/" + PORT);
        
        runCollection();
    }

	@Override
    protected void tearDown() throws Exception {
        m_agent.shutDownAndWait();

        super.tearDown();
    }

    public final void testIfSnmpCollector() throws UnknownHostException {
        assertFalse("collection should not have failed", m_ifSnmpc.failed());
    }

    public void testFailed() throws Exception {
        // We'll shut down the agent and then give things a whirl
        m_agent.shutDownAndWait();

        // Now things should fail nicely
        IfSnmpCollector ifSnmpc = new IfSnmpCollector(m_addr);
        ifSnmpc.run();
        assertTrue("collection should fail", ifSnmpc.failed());
        assertTrue("system group collection should fail", ifSnmpc.getSystemGroup().failed());
        assertTrue("ifTable collection should fail", ifSnmpc.getIfTable().failed());
        assertTrue("ifXTable collection should fail", ifSnmpc.getIfXTable().failed());
        assertTrue("ipAddrTable collection should fail", ifSnmpc.getIpAddrTable().failed());

    }

    public final void testHasSystemGroup() {
        assertTrue("should have a system group", m_ifSnmpc.hasSystemGroup());
    }

    public final void testGetSystemGroup() throws UnknownHostException {
        SystemGroup sg = m_ifSnmpc.getSystemGroup();
        assertNotNull("system group should not be null", sg);
        assertFalse("system group should not have failed", sg.failed());
        assertEquals("system group name", "brozow.local", sg.getSysName());
    }

    public final void testHasIfTable() {
        assertTrue(m_ifSnmpc.hasIfTable());
    }

    public final void testGetIfTable() {
        IfTable ifTable = m_ifSnmpc.getIfTable();
        assertNotNull("should have an ifTable", ifTable);
        assertFalse("ifTable collection should not have failed", ifTable.failed());
        assertEquals("iftype", 24, ifTable.getIfType(1));
    }

    public final void testHasIpAddrTable() {
        assertTrue("should have an ipAddrTable", m_ifSnmpc.hasIpAddrTable());
    }

    public final void testGetIpAddrTable() throws UnknownHostException {
        IpAddrTable ipAddrTable = m_ifSnmpc.getIpAddrTable();
        
        assertNotNull("ipAddrTable should not be null", ipAddrTable);
        assertFalse("ipAddrTable collection should not hahve failed", ipAddrTable.failed());
        assertEquals("ipAddrTable ifIndex of 127.0.0.1", 1, ipAddrTable.getIfIndex(InetAddressUtils.addr(DEFAULT_HOST)));
        
        List<InetAddress> addresses = ipAddrTable.getIpAddresses();
        assertTrue("ipAddrTable should contain 172.20.1.201", addresses.contains(InetAddressUtils.addr("172.20.1.201")));
        assertTrue("ipAddrTable should contain 127.0.0.1 like any good IP stack should", addresses.contains(InetAddressUtils.addr(DEFAULT_HOST)));
    }

    public final void testHasIfXTable() {
        assertTrue("should have an ifXTable", m_ifSnmpc.hasIfXTable());
    }

    public final void testGetIfXTable() {
        IfXTable ifXTable = m_ifSnmpc.getIfXTable();
        assertNotNull("ifXTable should not be null", ifXTable);
        assertFalse("ifXTable collection should not have failed", ifXTable.failed());
    }

    public final void testGetCollectorTargetAddress() {
        InetAddress target = m_ifSnmpc.getCollectorTargetAddress();
        assertNotNull("target addresss should not be null", target);
        assertEquals("target address", myLocalHost(), target);
    }

    public final void testGetIfAddressAndMask() {
        InetAddress addrMask[] = m_ifSnmpc.getIfAddressAndMask(1);
        assertNotNull("address mask should not be null", addrMask);
        assertEquals("localhost address", DEFAULT_HOST, InetAddressUtils.str(addrMask[0]));
        assertEquals("localhost mask... mmm... class A.... yummy", "255.0.0.0", InetAddressUtils.str(addrMask[1]));
    }

    public final void testGetAdminStatus() {
        int adminStatus = m_ifSnmpc.getAdminStatus(1);
        assertEquals("admin status", 1, adminStatus);
    }

    public final void testGetIfType() {
        int ifType = m_ifSnmpc.getIfType(1);
        assertEquals("ifType", 24, ifType);
    }

    public final void testGetIfIndex() throws UnknownHostException {
        int ifIndex = m_ifSnmpc.getIfIndex(InetAddressUtils.addr("172.20.1.201"));
        assertEquals("ifIndex", 5, ifIndex);
    }

    public final void testGetIfName() {
        String ifName = m_ifSnmpc.getIfName(1);
        assertNotNull("ifName should not be null", ifName);
        assertEquals("ifName", "There's no place like 127.0.0.1", ifName);
    }
    
    public final void testGetIfSpeed() {
        Long ifSpeed = m_ifSnmpc.getInterfaceSpeed(4);
        assertNotNull("ifSpeed should not be null", ifSpeed);
        assertEquals("ifSpeed", Long.valueOf(10000000), ifSpeed);
    }

    public final void testGetIfAlias() {
        String ifAlias = m_ifSnmpc.getIfAlias(1);
        assertNotNull("ifAlias should not be null", ifAlias);
        assertEquals("ifAlias", "We don't need no stinkin' ifAlias!", ifAlias);
    }

    private void runCollection() {
        if (m_hasRun) {
            return;
        }

        m_ifSnmpc.run();
        m_hasRun = true;
    }

}
