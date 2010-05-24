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
// Modifications:
//
// 2007 Apr 05: Remove a comment from when we switched to AbstractTransactionalDaoTestCase. - dj@opennms.org
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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PathElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitTemporaryDatabase()
public class NodeDaoTest  {
    
    @Autowired
    DistPollerDao m_distPollerDao;
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    JdbcTemplate m_jdbcTemplate;
    
    @Autowired
    DatabasePopulator m_populator;
    
    @Autowired
    TransactionTemplate m_transTemplate;
    
    @Before
    public void setUp() {
        m_populator.populateDatabase();
    }
    
    public OnmsNode getNode1() {
        return m_populator.getNode1();
    }
    
    public JdbcTemplate getJdbcTemplate() {
        return m_jdbcTemplate;
    }
    
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    
    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }
    
    @Test
    @Transactional
    public void testSave() {
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        OnmsNode node = new OnmsNode(distPoller);
        node.setLabel("MyFirstNode");
        getNodeDao().save(node);
        
        getNodeDao().flush();
    }

    @Test
    @Transactional
    public void testSaveWithPathElement() {
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        OnmsNode node = new OnmsNode(distPoller);
        node.setLabel("MyFirstNode");
        PathElement p = new PathElement("192.168.7.7", "ICMP");
        node.setPathElement(p);
        getNodeDao().save(node);
        
        getNodeDao().flush();
    }

    @Test
    @Transactional
    public void testSaveWithNullPathElement() {
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        OnmsNode node = new OnmsNode(distPoller);
        node.setLabel("MyFirstNode");
        PathElement p = new PathElement("192.168.7.7", "ICMP");
        node.setPathElement(p);
        getNodeDao().save(node);
        
        OnmsNode myNode = getNodeDao().get(node.getId());
        myNode.setPathElement(null);
        getNodeDao().save(myNode);
        
        getNodeDao().flush();
    }

    @Test
    @Transactional
    public void testCreate() throws InterruptedException {
        OnmsDistPoller distPoller = getDistPoller();
        
        OnmsNode node = new OnmsNode(distPoller);
        node.setLabel("MyFirstNode");
        node.getAssetRecord().setDisplayCategory("MyCategory");
        PathElement p = new PathElement("192.168.7.7", "ICMP");
        node.setPathElement(p);
        
        getNodeDao().save(node);
        
        
        System.out.println("BEFORE GET");
        OnmsDistPoller dp = getDistPoller();
        assertSame(distPoller, dp);
        System.out.println("AFTER GET");
        Collection<OnmsNode> nodes = getNodeDao().findNodes(dp);
        assertEquals(7, nodes.size());
        System.out.println("AFTER GETNODES");
        for (OnmsNode retrieved : nodes) {
            System.out.println("category for "+retrieved.getId()+" = "+retrieved.getAssetRecord().getDisplayCategory());
            if (node.getId().intValue() == 5) {
                assertEquals("MyFirstNode", retrieved.getLabel());
                assertEquals("MyCategory", retrieved.getAssetRecord().getDisplayCategory());
                assertEquals("192.168.7.7", retrieved.getPathElement().getIpAddress());
                
            }
        }
        System.out.println("AFTER Loop");
        
    }
    
    @Test
    @Transactional
    public void testQuery() throws Exception {
        
        OnmsNode n = getNodeDao().get(1);
        validateNode(n);
        
    }
    
    @Test
    @Transactional
    public void testDeleteOnOrphanIpInterface() {
        
        int preCount = getJdbcTemplate().queryForInt("select count(*) from ipinterface where ipinterface.nodeId = 1");
        
        OnmsNode n = getNodeDao().get(1);
        Iterator<OnmsIpInterface> it = n.getIpInterfaces().iterator();
        it.next();
        it.remove();
        getNodeDao().saveOrUpdate(n);
        getNodeDao().flush();
        
        int postCount = getJdbcTemplate().queryForInt("select count(*) from ipinterface where ipinterface.nodeId = 1");
        
        assertEquals(preCount-1, postCount);
        

    }
    
    @Test
    @Transactional
    public void testDeleteNode() {
        int preCount = getJdbcTemplate().queryForInt("select count(*) from node");

        OnmsNode n = getNodeDao().get(1);
        getNodeDao().delete(n);
        getNodeDao().flush();
        
        int postCount = getJdbcTemplate().queryForInt("select count(*) from node");
        
        assertEquals(preCount-1, postCount);
    }
    
    @Test
    @Transactional
    public void testQueryWithHierarchy() throws Exception {
        
        OnmsNode n = getNodeDao().getHierarchy(1);
        validateNode(n);
    }
    
    
    @Transactional
    public OnmsNode getNodeHierarchy(final int nodeId) {
        return m_transTemplate.execute(new TransactionCallback<OnmsNode>() {

            public OnmsNode doInTransaction(TransactionStatus status) {
                return getNodeDao().getHierarchy(nodeId);
            }
            
        });
    }
    
    /** Test for bug 1594 */
    @Test
    public void testQueryWithHierarchyCloseTransaction() throws Exception {

        OnmsNode n = getNodeHierarchy(1);
        
        validateNode(n);

        for (OnmsIpInterface ip : n.getIpInterfaces()) {
            ip.getIpAddress();
            for (OnmsMonitoredService service : ip.getMonitoredServices()) {
                service.getServiceName();
            }
        }

        // Test for bug 1594
        for (OnmsSnmpInterface snmp : n.getSnmpInterfaces()) {
            for (OnmsIpInterface ip : snmp.getIpInterfaces()) {
                ip.getIpAddress();
            }
        }
    }
    
    @Test
    @Transactional
    public void testGetForeignIdToNodeIdMap() {
        Map<String, Integer> arMap = getNodeDao().getForeignIdToNodeIdMap("imported:");
        assertTrue("Expected to find foriegnId 1", arMap.containsKey("1"));
        OnmsNode node1 = getNodeDao().get(arMap.get("1"));
        assertNotNull("Exepected foreignId to be mapped to a node", node1);
        assertEquals("Expected foreignId to be mapped to 'node1'", "node1", node1.getLabel());

    }
    
    @Test
    @Transactional
    public void testUpdateNodeScanStamp() {
        
        Date timestamp = new Date(27);
        
        getNodeDao().updateNodeScanStamp(1, timestamp);
        
        OnmsNode n = getNodeDao().get(1);
        
        assertEquals(timestamp, n.getLastCapsdPoll());
        
        
    }
    
    @Test
    @Transactional
    public void testFindByForeignSourceAndIpAddress() {
        assertEquals(0, getNodeDao().findByForeignSourceAndIpAddress("NoSuchForeignSource", "192.168.1.1").size());
        assertEquals(0, getNodeDao().findByForeignSourceAndIpAddress("imported:", "192.167.7.7").size());
        assertEquals(1, getNodeDao().findByForeignSourceAndIpAddress("imported:", "192.168.1.1").size());
        assertEquals(1, getNodeDao().findByForeignSourceAndIpAddress(null, "10.1.1.1").size());
        assertEquals(0, getNodeDao().findByForeignSourceAndIpAddress(null, "192.167.7.7").size());

    }
    
    
    
    @Test
    public void testDeleteObsoleteInterfaces() {
        
        final Date timestampe = new Date(1234);

        m_transTemplate.execute(new TransactionCallback<Object>() {

            public Object doInTransaction(TransactionStatus status) {
                simulateScan(timestampe);
                return null;
            }
            
        });

        m_transTemplate.execute(new TransactionCallback<Object>() {

            public Object doInTransaction(TransactionStatus status) {
                deleteObsoleteInterfaces(timestampe);
                return null;
            }
            
        });

        m_transTemplate.execute(new TransactionCallback<Object>() {

            public Object doInTransaction(TransactionStatus status) {
                validateScan();
                return null;
            }
            
        });
        
        
    }

    private void validateScan() {
        OnmsNode after = getNodeDao().get(1);
        
        assertEquals(1, after.getIpInterfaces().size());
        assertEquals(1, after.getSnmpInterfaces().size());
    }

    private void simulateScan(Date timestampe) {
        OnmsNode n = getNodeDao().get(1);
        
        assertEquals(3, n.getIpInterfaces().size());
        assertEquals(3, n.getSnmpInterfaces().size());

        OnmsIpInterface iface = n.getIpInterfaceByIpAddress("192.168.1.1");
        assertNotNull(iface);
        iface.setIpLastCapsdPoll(timestampe);
        
        OnmsSnmpInterface snmpIface = n.getSnmpInterfaceWithIfIndex(1);
        assertNotNull(snmpIface);
        snmpIface.setLastCapsdPoll(timestampe);
        
        getNodeDao().saveOrUpdate(n);
        
        getNodeDao().flush();

        

    }

    private void deleteObsoleteInterfaces(Date timestampe) {
        getNodeDao().deleteObsoleteInterfaces(1, timestampe);
    }
    
    private void validateNode(OnmsNode n) throws Exception {
        assertNotNull("Expected node to be non-null", n);
        assertNotNull("Expected node "+n.getId()+" to have interfaces", n.getIpInterfaces());
        assertEquals("Unexpected number of interfaces for node "+n.getId(), 3, n.getIpInterfaces().size());
        for (Object o : n.getIpInterfaces()) {
			OnmsIpInterface iface = (OnmsIpInterface)o;
			assertNotNull(iface);
			assertNotNull(iface.getIpAddress());
		}
        
        assertNodeEquals(getNode1(), n);
    }
    
    private class PropertyComparator implements Comparator<Object> {
    	
    	String m_propertyName;
    	
    	public PropertyComparator(String propertyName) {
    		m_propertyName = propertyName;
    	}

		public int compare(Object o1, Object o2) {
			
			String expectedValue;
			try {
				expectedValue = ""+BeanUtils.getProperty(o1, m_propertyName);
				String actualValue = ""+BeanUtils.getProperty(o2, m_propertyName);
				return expectedValue.compareTo(actualValue);
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to compare property "+m_propertyName, e);
			}
		}
    	
    }
    
    private void assertNodeEquals(OnmsNode expected, OnmsNode actual) throws Exception {
    	assertEquals("Unexpected nodeId", expected.getId(), actual.getId());
    	String[] properties = { "id", "label", "labelSource", "assetRecord.assetNumber", "distPoller.name", "sysContact", "sysName", "sysObjectId" };
    	assertPropertiesEqual(properties, expected, actual);
    	
    	assertInterfaceSetsEqual(expected.getIpInterfaces(), actual.getIpInterfaces());
    	
    }
    
    private interface AssertEquals<T> {
    	public void assertEqual(T expected, T actual) throws Exception;
    }
    
    private <T> void assertSetsEqual(Set<T> expectedSet, Set<T> actualSet, String orderProperty, AssertEquals<T> comparer) throws Exception {
    	SortedSet<T> expectedSorted = new TreeSet<T>(new PropertyComparator(orderProperty));
    	expectedSorted.addAll(expectedSet);
    	SortedSet<T> actualSorted = new TreeSet<T>(new PropertyComparator(orderProperty));
    	actualSorted.addAll(actualSet);

    	Iterator<T> expected = expectedSorted.iterator();
    	Iterator<T> actual = actualSorted.iterator();

    	while(expected.hasNext() && actual.hasNext()) {
    		comparer.assertEqual(expected.next(), actual.next());
    	}

    	if (expected.hasNext())
    		fail("Missing item "+expected.next()+" in the actual list");

    	if (actual.hasNext())
    		fail("Unexpected item "+actual.next()+" in the actual list");
    }

    private void assertInterfaceSetsEqual(Set<OnmsIpInterface> expectedSet, Set<OnmsIpInterface> actualSet) throws Exception {
    	assertSetsEqual(expectedSet, actualSet, "ipAddress" , new AssertEquals<OnmsIpInterface>() {

			public void assertEqual(OnmsIpInterface expected, OnmsIpInterface actual) throws Exception {
	    		assertInterfaceEquals(expected, actual);
			}
    		
    	});
	}

	private void assertInterfaceEquals(OnmsIpInterface expected, OnmsIpInterface actual) throws Exception {
		String[] properties = { "ipAddress", "ifIndex",  "ipHostName", "isManaged", "node.id" };
    	assertPropertiesEqual(properties, expected, actual);
    	assertServicesEquals(expected.getMonitoredServices(), actual.getMonitoredServices());
	}
	
	private void assertServicesEquals(Set<OnmsMonitoredService> expectedSet, Set<OnmsMonitoredService> actualSet) throws Exception {
    	assertSetsEqual(expectedSet, actualSet, "serviceId" , new AssertEquals<OnmsMonitoredService>() {

			public void assertEqual(OnmsMonitoredService expected, OnmsMonitoredService actual) throws Exception {
	    		assertServiceEquals(expected, actual);
			}
    		
    	});
	}

	protected void assertServiceEquals(OnmsMonitoredService expected, OnmsMonitoredService actual) {
		assertEquals(expected.getServiceName(), actual.getServiceName());
	}

	private void assertPropertiesEqual(String[] properties, Object expected, Object actual) throws Exception {
    	for (String property : properties) {
			assertPropertyEquals(property, expected, actual);
		}
		
	}

	private void assertPropertyEquals(String name, Object expected, Object actual) throws Exception {
		Object expectedValue = BeanUtils.getProperty(expected, name);
		Object actualValue = BeanUtils.getProperty(actual, name);
		assertEquals("Unexpected value for property "+name+" on object "+expected, expectedValue, actualValue);
	}

    @Test
    @Transactional
	public void testQuery2() {
        OnmsNode n = getNodeDao().get(6);
        assertNotNull(n);
        assertEquals(3, n.getIpInterfaces().size());
        assertNotNull(n.getAssetRecord());
        assertEquals("category1", n.getAssetRecord().getDisplayCategory());
    }

    private OnmsDistPoller getDistPoller() {
        OnmsDistPoller distPoller = getDistPollerDao().load("localhost");
        assertNotNull(distPoller);
        return distPoller;
    }
}
