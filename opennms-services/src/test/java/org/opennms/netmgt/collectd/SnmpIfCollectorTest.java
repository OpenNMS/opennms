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
import org.opennms.test.VersionSettingTestSuite;


public class SnmpIfCollectorTest extends SnmpCollectorTestCase {
    
    private final class IfInfoVisitor extends ResourceVisitor {
    	
    	public int ifInfoCount = 0;
    	
		public void visitResource(CollectionResource resource) {
		    if (!(resource instanceof IfInfo)) return;
		    
		    ifInfoCount++;
		    IfInfo ifInfo = (IfInfo) resource;
		    assertMibObjectsPresent(ifInfo, getAttributeList());
		        
		}
	}

	public static TestSuite suite() {
        Class testClass = SnmpIfCollectorTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv1 Tests", SnmpAgentConfig.VERSION1));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv2 Tests", SnmpAgentConfig.VERSION2C));
        suite.addTest(new VersionSettingTestSuite(testClass, "SNMPv3 Tests", SnmpAgentConfig.VERSION3));
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


            assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 1);
    }

    private void assertInterfaceMibObjectsPresent(CollectionSet collectionSet, int expectedIfCount) {
        assertNotNull(collectionSet);
      
        if (getAttributeList().isEmpty()) return;
        

        IfInfoVisitor ifInfoVisitor = new IfInfoVisitor();
		collectionSet.visit(ifInfoVisitor);
		
		assertEquals("Unexpected number of interfaces", expectedIfCount, ifInfoVisitor.ifInfoCount);
		
    }

    public void testInvalidVar() throws Exception {
        addAttribute("invalid", "1.3.6.1.2.1.2.2.2.10", "ifIndex", "counter");
        
        assertFalse(getAttributeList().isEmpty());

        createSnmpInterface(1, 24, "lo", CollectionType.PRIMARY);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        // remove the failing element.  Now entries should match
        getAttributeList().remove(0);
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 1);
    }
    
    public void testBadApple() throws Exception {

        addIfSpeed();
        addIfInOctets();
        // the oid below is wrong.  Make sure we collect the others anyway
        addAttribute("invalid", "1.3.66.1.2.1.2.2.299.16", "ifIndex", "counter");
        addIfInErrors();
        addIfOutErrors();
        addIfInDiscards();
        
        assertFalse(getAttributeList().isEmpty());
        
        createSnmpInterface(1, 24, "lo", CollectionType.PRIMARY);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        // remove the bad apple before compare
        getAttributeList().remove(2);
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 1);
    }
    
    public void testManyVars() throws Exception {
        addIfTable();
        
        assertFalse(getAttributeList().isEmpty());

        createSnmpInterface(1, 24, "lo", CollectionType.PRIMARY);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 1);
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
    	m_node.addSnmpInterface(m_snmpIface);
        
    	return m_snmpIface;

    }

    public void testManyIfs() throws Exception {
        addIfTable();
        
        assertFalse(getAttributeList().isEmpty());

        createSnmpInterface(1, 24, "lo0", CollectionType.PRIMARY);
        createSnmpInterface(2, 55, "gif0", CollectionType.SECONDARY);
        createSnmpInterface(3, 57, "stf0", CollectionType.COLLECT);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 3);
    }

    // TODO: add test for very large v2 request

    
}
