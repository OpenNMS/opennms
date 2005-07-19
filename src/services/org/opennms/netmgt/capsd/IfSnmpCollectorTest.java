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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import junit.framework.TestSuite;

import org.opennms.netmgt.capsd.snmp.IfTable;
import org.opennms.netmgt.capsd.snmp.IpAddrTable;
import org.opennms.netmgt.capsd.snmp.SystemGroup;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.snmp.PropertySettingTestSuite;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.VersionSettingTestSuite;

public class IfSnmpCollectorTest extends OpenNMSTestCase {
    
    private IfSnmpCollector m_ifSnmpc;
    private boolean m_run = true;
    private boolean m_hasRun = false;

    public static TestSuite suite() {
        Class testClass = IfSnmpCollectorTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        TestSuite joeSuite = new PropertySettingTestSuite("JoeSnmp Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy");
        joeSuite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", SnmpAgentConfig.VERSION1));
        joeSuite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", SnmpAgentConfig.VERSION2C));
        joeSuite.addTest(new VersionSettingTestSuite(testClass, "SNMPv3 Tests", SnmpAgentConfig.VERSION3));
        suite.addTest(joeSuite);

        TestSuite snmp4jSuite = new PropertySettingTestSuite("SNMP4J Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy");
        snmp4jSuite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", SnmpAgentConfig.VERSION1));
        snmp4jSuite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", SnmpAgentConfig.VERSION2C));
        snmp4jSuite.addTest(new VersionSettingTestSuite(testClass, "SNMPv3 Tests", SnmpAgentConfig.VERSION3));
        suite.addTest(snmp4jSuite);
        
        return suite;
    }

    protected void setUp() throws Exception {
        
        super.setUp();
        m_runSupers = true;
        
        InetAddress addr = InetAddress.getByName(myLocalHost());
        m_ifSnmpc = new IfSnmpCollector(addr);
        if(m_run && !m_hasRun) {
            m_ifSnmpc.run();
            m_hasRun = true;
        }
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public final void dummy() {
        String ar[] = "127.0.0.1".split("\\.", 0);
        MockUtil.println("Size of array with 0:" +Integer.toString(ar.length)+" toString[3]: "+ar[3]);
        ar = "127.0.0.1".split("\\.", -1);
        MockUtil.println("Size of array with -1:" +Integer.toString(ar.length)+" toString[3]: "+ar[3]);
    }

    public final void testIfSnmpCollector() throws UnknownHostException {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            assertFalse(m_ifSnmpc.failed());
        }
    }
    
    public void testFailed() throws Exception {
        InetAddress addr = InetAddress.getByName("1.1.1.1");
        IfSnmpCollector ifSnmpc = new IfSnmpCollector(addr);
        ifSnmpc.run();
        assertTrue(ifSnmpc.failed());
        assertTrue(ifSnmpc.getSystemGroup().failed());
        assertTrue(ifSnmpc.getIfTable().failed());
        assertTrue(ifSnmpc.getIfXTable().failed());
        assertTrue(ifSnmpc.getIpAddrTable().failed());

    }

    public final void testHasSystemGroup() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            assertTrue(m_ifSnmpc.hasSystemGroup());
        }
    }

    public final void testGetSystemGroup() throws UnknownHostException {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            SystemGroup sg = m_ifSnmpc.getSystemGroup();
            assertNotNull(sg);
            assertFalse(sg.failed());
//            assertEquals("brozow.local", sg.getSysName());
        }        
    }

    public final void testHasIfTable() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            assertTrue(m_ifSnmpc.hasIfTable());
        }
    }

    public final void testGetIfTable() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            IfTable ifTable = m_ifSnmpc.getIfTable();
            assertNotNull(ifTable);
            assertFalse(ifTable.failed());
            assertEquals(24, ifTable.getIfType(1));
        }        
    }

    public final void testHasIpAddrTable() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            assertTrue(m_ifSnmpc.hasIpAddrTable());
        }
    }

    public final void testGetIpAddrTable() throws UnknownHostException {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            IpAddrTable ipAddrTable = m_ifSnmpc.getIpAddrTable();
            assertNotNull(ipAddrTable);
            assertFalse(ipAddrTable.failed());
            assertEquals(1, ipAddrTable.getIfIndex(InetAddress.getByName("127.0.0.1")));
            List entries = ipAddrTable.getEntries();
            List addresses = IpAddrTable.getIpAddresses(entries);
            assertTrue(addresses.contains(InetAddress.getByName(myLocalHost())));
            assertTrue(addresses.contains(InetAddress.getByName("127.0.0.1")));
        }        
    }
    
    public final void testHasIfXTable() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            assertTrue(m_ifSnmpc.hasIfXTable());
        }
    }

    public final void testGetIfXTable() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();

            IpAddrTable ipAddrTable = m_ifSnmpc.getIpAddrTable();
            assertNotNull(ipAddrTable);
            assertFalse(ipAddrTable.failed());
        }        
    }

    public final void testGetCollectorTargetAddress() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();
            
            InetAddress target = m_ifSnmpc.getCollectorTargetAddress();
            assertNotNull(target);
            assertEquals(myLocalHost(), target.getHostAddress());
        }
    }

    public final void testGetIfAddressAndMask() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();
            
            InetAddress addrMask[] = m_ifSnmpc.getIfAddressAndMask(1);
            assertNotNull(addrMask);
            assertEquals("127.0.0.1", addrMask[0].getHostAddress());
            assertEquals("255.0.0.0", addrMask[1].getHostAddress());
        }
    }

    public final void testGetAdminStatus() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();
            
            int adminStatus = m_ifSnmpc.getAdminStatus(1);
            assertEquals(1, adminStatus);
        }
    }

    public final void testGetIfType() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();
            
            int ifType = m_ifSnmpc.getIfType(1);
            assertEquals(24, ifType);
        }
    }

    public final void testGetIfIndex() throws UnknownHostException {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();
            
            int ifIndex = m_ifSnmpc.getIfIndex(InetAddress.getLocalHost());
            assertTrue(0 < ifIndex);
        }
    }

    public final void xtestGetIfName() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();
            
            if (m_ifSnmpc.hasIfXTable()) {
                String ifName = m_ifSnmpc.getIfName(1);
                assertNotNull(ifName);
            }
        }
    }

    public final void xtestGetIfAlias() {
        if(m_run) {

            if(!m_hasRun)
                m_ifSnmpc.run();
            
            if (m_ifSnmpc.hasIfXTable()) {
                String ifAlias = m_ifSnmpc.getIfAlias(1);
                assertNotNull(ifAlias);
            }
        }
    }

}
