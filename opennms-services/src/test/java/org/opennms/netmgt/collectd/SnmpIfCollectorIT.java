/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.model.OnmsEntity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

@RunWith(Parameterized.class)
public class SnmpIfCollectorIT extends SnmpCollectorITCase {

	public SnmpIfCollectorIT(int config) {
		setVersion(config);
		m_allowWarnings = true; // warnings are expected when we are unable to compute the resource index for SNMP interfaces
	}

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

	@Parameters
	public static Collection<Object[]> params() {
		Object[][] retval = new Object[][] {
			{ SnmpAgentConfig.VERSION1 },
			{ SnmpAgentConfig.VERSION2C },
			{ SnmpAgentConfig.VERSION3 }
		};
		return Arrays.asList(retval);
	}

    @Test
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

    @Test
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

    @Test
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
    
    @Test
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

    @Test
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

    /**
     * See NMS-11764. Verify that the interface is ignored and not included in the collection set
     * if the interface name is null.
     */
    @Test
    public void testNullIfName() throws Exception {
        addIfTable();

        assertFalse(getAttributeList().isEmpty());

        createSnmpInterface(1, 24, null, true);

        SnmpIfCollector collector = createSnmpIfCollector();
        waitForSignal();

        // The interface should be skipped, and the results are not expected to be added to the CollectionSet
        assertInterfaceMibObjectsPresent(collector.getCollectionSet(), 0);
    }

    // TODO: add test for very large v2 request

    
}
