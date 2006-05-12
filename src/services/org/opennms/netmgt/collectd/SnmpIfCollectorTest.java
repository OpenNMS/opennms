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
package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestSuite;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpCollectorTestCase;
import org.opennms.netmgt.snmp.VersionSettingTestSuite;


public class SnmpIfCollectorTest extends SnmpCollectorTestCase {
    
    public static TestSuite suite() {
        Class testClass = SnmpIfCollectorTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", SnmpAgentConfig.VERSION1));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", SnmpAgentConfig.VERSION2C));
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testZeroVars() throws Exception {
            createSnmpInterface(1, 24, "lo", CollectionType.PRIMARY);

            SnmpIfCollector collector = createSnmpIfCollector();
            waitForSignal();


            assertInterfaceMibObjectsPresent(collector.getEntries());
    }

    private void assertInterfaceMibObjectsPresent(List entries) {
        assertNotNull(entries);

        for (Iterator it = getSnmpInterfaces().iterator(); it.hasNext();) {
            OnmsSnmpInterface info = (OnmsSnmpInterface) it.next();
            if (getAttributeList().size() == 0) continue;
            SNMPCollectorEntry entry = findEntryWithIfIndex(info.getIfIndex(), entries);
            assertNotNull("Could not locate entry for ifIndex "+info.getIfIndex()+" entries.size() = "+entries.size(), entry);
            assertMibObjectsPresent(entry, getAttributeList());
        }
    }

    private Set getSnmpInterfaces() {
        return m_agent.getNode().getSnmpInterfaces();
    }

    private SNMPCollectorEntry findEntryWithIfIndex(Integer ifIndex, List entries) {
        assertNotNull(ifIndex);
        assertNotNull(entries);
        for (Iterator it = entries.iterator(); it.hasNext();) {
            SNMPCollectorEntry entry = (SNMPCollectorEntry) it.next();
            if (ifIndex.equals(entry.getIfIndex())) return entry;
        }
        return null;
    }

    public void testInvalidVar() throws Exception {
        addAttribute("invalid", "1.3.6.1.2.1.2.2.2.10", "ifIndex", "counter");
        
        createSnmpInterface(1, 24, "lo", CollectionType.PRIMARY);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        // remove the failing element.  Now entries should match
        getAttributeList().remove(0);
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }
    
    public void testBadApple() throws Exception {

        addIfSpeed();
        addIfInOctets();
        // the oid below is wrong.  Make sure we collect the others anyway
        addAttribute("invalid", "1.3.66.1.2.1.2.2.299.16", "ifIndex", "counter");
        addIfInErrors();
        addIfOutErrors();
        addIfInDiscards();
        
        createSnmpInterface(1, 24, "lo", CollectionType.PRIMARY);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        // remove the bad apple before compare
        getAttributeList().remove(2);
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }
    
    public void testManyVars() throws Exception {
        addIfTable();
        
        createSnmpInterface(1, 24, "lo", CollectionType.PRIMARY);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }

    private SnmpIfCollector createSnmpIfCollector() throws UnknownHostException {
        initializeAgent();
        
        SnmpIfCollector collector = new SnmpIfCollector(InetAddress.getLocalHost(), getCollectionSet().getCombinedInterfaceAttributes(), getCollectionSet());
        
        createWalker(collector);
        return collector;
    }

    private OnmsSnmpInterface createSnmpInterface(int ifIndex, int ifType, String ifName, CollectionType ifCollType) {
        OnmsSnmpInterface m_snmpIface = new OnmsSnmpInterface();
    	m_snmpIface.setIfIndex(new Integer(ifIndex));
    	m_snmpIface.setIfType(new Integer(ifType));
    	m_snmpIface.setIfName(ifName);
    	m_agent.getNode().addSnmpInterface(m_snmpIface);
        
    	return m_snmpIface;

    }

    public void testManyIfs() throws Exception {
        addIfTable();
        
        createSnmpInterface(1, 24, "lo0", CollectionType.PRIMARY);
        createSnmpInterface(2, 55, "gif0", CollectionType.SECONDARY);
        createSnmpInterface(3, 57, "stf0", CollectionType.COLLECT);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }

    // TODO: add test for very large v2 request

    
}
