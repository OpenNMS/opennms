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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestSuite;

import org.opennms.netmgt.snmp.SnmpCollectorTestCase;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.VersionSettingTestSuite;
import org.opennms.protocols.snmp.SnmpSMI;


public class SnmpIfCollectorTest extends SnmpCollectorTestCase {
    
    public static TestSuite suite() {
        Class testClass = SnmpIfCollectorTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", SnmpSMI.SNMPV1));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", SnmpSMI.SNMPV2));
        return suite;
    }

    private Map m_ifMap;
    private SnmpWalker m_walker;

    protected void setUp() throws Exception {
        super.setUp();
        m_ifMap = new TreeMap();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testZeroVars() throws Exception {
        IfInfo ifInfo = new IfInfo(1, 24, "lo", "P");
        
        ifInfo.setOidList(new ArrayList(m_objList));
        
        addIfInfo(ifInfo);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }

    private void assertInterfaceMibObjectsPresent(List entries) {
        assertNotNull(entries);
        for (Iterator it = entries.iterator(); it.hasNext();) {
            SNMPCollectorEntry entry = (SNMPCollectorEntry) it.next();
            int ifIndex = entry.getIfIndex().intValue();
            IfInfo info = (IfInfo)m_ifMap.get(new Integer(ifIndex));
            if (info == null) continue;
            assertMibObjectsPresent(entry, m_objList);
        }
        
        for (Iterator it = m_ifMap.values().iterator(); it.hasNext();) {
            IfInfo info = (IfInfo) it.next();
            if (m_objList.size() == 0) continue;
            SNMPCollectorEntry entry = findEntryWithIfIndex(info.getIndex(), entries);
            assertNotNull("Could not locate entry for ifIndex "+info.getIndex()+" entries.size() = "+entries.size(), entry);
            assertMibObjectsPresent(entry, m_objList);
        }
    }

    private SNMPCollectorEntry findEntryWithIfIndex(int index, List entries) {
        assertNotNull(entries);
        for (Iterator it = entries.iterator(); it.hasNext();) {
            SNMPCollectorEntry entry = (SNMPCollectorEntry) it.next();
            int ifIndex = entry.getIfIndex().intValue();
            if (ifIndex == index) return entry;
        }
        return null;
    }

    public void testInvalidVar() throws Exception {
        IfInfo ifInfo = new IfInfo(1, 24, "lo", "P");
        
        addMibObject("invalid", "1.3.6.1.2.1.2.2.2.10", "ifIndex", "counter");
        
        ifInfo.setOidList(new ArrayList(m_objList));
        
        addIfInfo(ifInfo);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        // remove the failing element.  Now entries should match
        m_objList.remove(0);
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }
    
    public void testBadApple() throws Exception {

        IfInfo ifInfo = new IfInfo(1, 24, "lo", "P");
        
        addIfSpeed();
        addIfInOctets();
        // the oid below is wrong.  Make sure we collect the others anyway
        addMibObject("invalid", "1.3.66.1.2.1.2.2.299.16", "ifIndex", "counter");
        addIfInErrors();
        addIfOutErrors();
        addIfInDiscards();
        
        ifInfo.setOidList(new ArrayList(m_objList));
        
        addIfInfo(ifInfo);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        // remove the bad apple before compare
        m_objList.remove(2);
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }
    
    public void testManyVars() throws Exception {
        addIfTable();
        
        addIfInfo(createIfInfo(1, 24, "lo", "P"));
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }

    private SnmpIfCollector createSnmpIfCollector() throws UnknownHostException {
        SnmpIfCollector collector = new SnmpIfCollector(InetAddress.getLocalHost(), m_ifMap);
        m_walker = SnmpUtils.createWalker(InetAddress.getLocalHost(), "snmpIfCollector", 50, collector);
        m_walker.start();
        return collector;
    }
    
    private void waitForSignal() throws InterruptedException {
        m_walker.waitFor();
    }

    private IfInfo createIfInfo(int ifIndex, int ifType, String ifName, String ifCollType) {
        IfInfo ifInfo = new IfInfo(ifIndex, ifType, ifName, ifCollType);
        ifInfo.setOidList(new ArrayList(m_objList));
        return ifInfo;
    }

    public void testManyIfs() throws Exception {
        addIfTable();
        
        addIfInfo(createIfInfo(1, 24, "lo0", "P"));
        addIfInfo(createIfInfo(2, 55, "gif0", "S"));
        addIfInfo(createIfInfo(3, 57, "stf0", "C"));
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getEntries());
    }

    // TODO: add test for very large v2 request

    
    private void addIfInfo(IfInfo ifInfo) {
        m_ifMap.put(new Integer(ifInfo.getIndex()), ifInfo);
    }

}
