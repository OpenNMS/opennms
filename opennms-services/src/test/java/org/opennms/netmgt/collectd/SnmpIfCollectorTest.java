/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestSuite;

import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.core.test.snmp.SnmpTestSuiteUtils;
import org.opennms.netmgt.model.OnmsEntity;
import org.opennms.netmgt.model.OnmsSnmpInterface;


public class SnmpIfCollectorTest extends SnmpCollectorTestCase {
    
    private final class IfInfoVisitor extends ResourceVisitor {
    	
    	public int ifInfoCount = 0;
    	
            @Override
		public void visitResource(CollectionResource resource) {
		    if (!(resource instanceof IfInfo)) return;
		    
		    ifInfoCount++;
		    IfInfo ifInfo = (IfInfo) resource;
		    assertMibObjectsPresent(ifInfo, getAttributeList());
		        
		}
	}

	public static TestSuite suite() {
        return SnmpTestSuiteUtils.createSnmpVersionTestSuite(SnmpIfCollectorTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testZeroVars() throws Exception {
            createSnmpInterface(1, 24, "lo", true);

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

        createSnmpInterface(1, 24, "lo", true);
        
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
        
        createSnmpInterface(1, 24, "lo", true);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        // remove the bad apple before compare
        getAttributeList().remove(2);
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 1);
    }
    
    public void testManyVars() throws Exception {
        addIfTable();
        
        assertFalse(getAttributeList().isEmpty());

        createSnmpInterface(1, 24, "lo", true);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 1);
    }

    private SnmpIfCollector createSnmpIfCollector() throws UnknownHostException, CollectionInitializationException {
        initializeAgent();
        
        SnmpIfCollector collector = new SnmpIfCollector(InetAddress.getLocalHost(), getCollectionSet().getCombinedIndexedAttributes(), getCollectionSet());
        
        createWalker(collector);
        return collector;
    }

    private OnmsEntity createSnmpInterface(final int ifIndex, final int ifType, final String ifName, final boolean collectionEnabled) {
        final OnmsSnmpInterface m_snmpIface = new OnmsSnmpInterface();
    	m_snmpIface.setIfIndex(ifIndex);
    	m_snmpIface.setIfType(ifType);
    	m_snmpIface.setIfName(ifName);
    	m_snmpIface.setCollectionEnabled(collectionEnabled);
    	m_node.addSnmpInterface(m_snmpIface);
        
    	return m_snmpIface;

    }

    public void testManyIfs() throws Exception {
        addIfTable();
        
        assertFalse(getAttributeList().isEmpty());

        createSnmpInterface(1, 24, "lo0", true);
        createSnmpInterface(2, 55, "gif0", true);
        createSnmpInterface(3, 57, "stf0", true);
        
        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();
        
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 3);
    }

    // TODO: add test for very large v2 request

    
}
