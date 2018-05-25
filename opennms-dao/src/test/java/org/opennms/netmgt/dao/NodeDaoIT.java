/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PathElement;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class NodeDaoIT implements InitializingBean {

    @Autowired
    DistPollerDao m_distPollerDao;

    @Autowired
    MonitoringLocationDao m_locationDao;

    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    JdbcTemplate m_jdbcTemplate;

    @Autowired
    DatabasePopulator m_populator;

    @Autowired
    TransactionTemplate m_transTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        org.opennms.core.spring.BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_populator.populateDatabase();
    }

    @After
    public void tearDown() {
        m_populator.resetDatabase();
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

        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "MyFirstNode");
        getNodeDao().save(node);

        getNodeDao().flush();
    }

    @Test
    @Transactional
    public void testSaveWithPathElement() {

        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "MyFirstNode");
        PathElement p = new PathElement("192.168.7.7", "ICMP");
        node.setPathElement(p);
        getNodeDao().save(node);

        getNodeDao().flush();
    }

    @Test
    @Transactional
    public void testSaveWithNullPathElement() {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "MyFirstNode");
        PathElement p = new PathElement("192.168.7.7", "ICMP");
        node.setPathElement(p);
        getNodeDao().save(node);

        OnmsNode myNode = getNodeDao().get(node.getId());
        assertNotNull(myNode);
        myNode.setPathElement(null);
        getNodeDao().save(myNode);

        getNodeDao().flush();
    }
    
    @Test
    @Transactional
    public void testLldpSaveAndUpdate() throws InterruptedException {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "MyFirstLldpNode");
        getNodeDao().save(node);
        getNodeDao().flush();
        
        Collection<OnmsNode> nodes = getNodeDao().findAll();
        assertEquals(7, nodes.size());
        Integer nodeid = null;
        for (OnmsNode retrieved : nodes) {
            if (retrieved.getLabel().equals("MyFirstLldpNode")) {
            	nodeid = retrieved.getId();
                System.out.println("nodeid: " +nodeid);
            }
        }
        
        OnmsNode dbnode1 = getNodeDao().get(nodeid);
        assertNotNull(dbnode1);
        
        if (dbnode1.getLldpElement() == null ) {
	        LldpElement lldpElement = new LldpElement();
	        lldpElement.setLldpChassisId("abc123456");
	        lldpElement.setLldpChassisIdSubType(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS);
	        lldpElement.setLldpSysname("prova");
	        lldpElement.setNode(node);
	        lldpElement.setLldpNodeLastPollTime(lldpElement.getLldpNodeCreateTime());
	        dbnode1.setLldpElement(lldpElement);
    	}
        getNodeDao().save(dbnode1);
        getNodeDao().flush();

        OnmsNode dbnode2 = getNodeDao().get(nodeid);
        assertNotNull(dbnode2);
        assertNotNull(dbnode2.getLldpElement());

        System.out.println("lldp element id: " + dbnode2.getLldpElement().getId());
        System.out.println("lldp element create time: " + dbnode2.getLldpElement().getLldpNodeCreateTime());
        System.out.println("lldp element last poll time: " + dbnode2.getLldpElement().getLldpNodeLastPollTime());
        assertEquals("abc123456", dbnode2.getLldpElement().getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, dbnode2.getLldpElement().getLldpChassisIdSubType());
        assertEquals("prova", dbnode2.getLldpElement().getLldpSysname());
        assertNotNull(dbnode2.getLldpElement().getLldpNodeCreateTime());
        assertNotNull(dbnode2.getLldpElement().getLldpNodeLastPollTime());
        
        System.out.println("---------");
        Thread.sleep(1000);
        System.out.println("---------");

        LldpElement lldpUpdateElement = new LldpElement();
        lldpUpdateElement.setLldpChassisId("abc012345");
        lldpUpdateElement.setLldpChassisIdSubType(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS);
        lldpUpdateElement.setLldpSysname("prova");

        LldpElement dbLldpElement = dbnode2.getLldpElement();
        dbLldpElement.merge(lldpUpdateElement);
        dbnode2.setLldpElement(dbLldpElement);

        getNodeDao().save(dbnode2);
        getNodeDao().flush();

        OnmsNode dbnode3 = getNodeDao().get(nodeid);
        assertNotNull(dbnode3);
        assertNotNull(dbnode3.getLldpElement());

        System.out.println("lldp element id: " + dbnode3.getLldpElement().getId());
        System.out.println("lldp element create time: " + dbnode3.getLldpElement().getLldpNodeCreateTime());
        System.out.println("lldp element last poll time: " + dbnode3.getLldpElement().getLldpNodeLastPollTime());
        assertEquals("abc012345", dbnode3.getLldpElement().getLldpChassisId());
        assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, dbnode3.getLldpElement().getLldpChassisIdSubType());
        assertEquals("prova", dbnode3.getLldpElement().getLldpSysname());
        assertNotNull(dbnode3.getLldpElement().getLldpNodeCreateTime());
        assertNotNull(dbnode3.getLldpElement().getLldpNodeLastPollTime());

        
    }    

    @Test
    @Transactional
    public void testCreate() throws InterruptedException {

        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "MyFirstNode");
        node.getAssetRecord().setDisplayCategory("MyCategory");
        PathElement p = new PathElement("192.168.7.7", "ICMP");
        node.setPathElement(p);

        getNodeDao().save(node);


        System.out.println("BEFORE GET");
        Collection<OnmsNode> nodes = getNodeDao().findAll();
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

        OnmsNode n = getNodeDao().get(getNode1().getId());
        validateNode(n);

    }

    @Test
    @Transactional
    public void testDeleteOnOrphanIpInterface() {

        int preCount = getJdbcTemplate().queryForObject("select count(*) from ipinterface where ipinterface.nodeId = " + getNode1().getId(), Integer.class);

        OnmsNode n = getNodeDao().get(getNode1().getId());
        Iterator<OnmsIpInterface> it = n.getIpInterfaces().iterator();
        it.next();
        it.remove();
        getNodeDao().saveOrUpdate(n);
        getNodeDao().flush();

        int postCount = getJdbcTemplate().queryForObject("select count(*) from ipinterface where ipinterface.nodeId = " + getNode1().getId(), Integer.class);

        assertEquals(preCount-1, postCount);


    }

    @Test
    @Transactional
    public void testDeleteNode() {
        int preCount = getJdbcTemplate().queryForObject("select count(*) from node", Integer.class);

        OnmsNode n = getNodeDao().get(getNode1().getId());
        getNodeDao().delete(n);
        getNodeDao().flush();

        int postCount = getJdbcTemplate().queryForObject("select count(*) from node", Integer.class);

        assertEquals(preCount-1, postCount);
    }

    @Test
    @Transactional
    public void testQueryWithHierarchy() throws Exception {

        OnmsNode n = getNodeDao().getHierarchy(getNode1().getId());
        validateNode(n);
    }

    public OnmsNode getNodeHierarchy(final int nodeId) {
        return m_transTemplate.execute(new TransactionCallback<OnmsNode>() {

            @Override
            public OnmsNode doInTransaction(TransactionStatus status) {
                return getNodeDao().getHierarchy(nodeId);
            }

        });
    }

    /** Test for bug 1594 */
    @Test
    @Transactional
    public void testQueryWithHierarchyCloseTransaction() throws Exception {

        OnmsNode n = getNodeHierarchy(getNode1().getId());

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
    public void testGetForeignIdsPerForeignSourceMap() {
        Map<String, Set<String>> arMap = getNodeDao().getForeignIdsPerForeignSourceMap();
        assertTrue("Expected to find foreign source 'imported:'", arMap.containsKey("imported:"));
        assertEquals(4, arMap.get("imported:").size());
        assertEquals("1", arMap.get("imported:").iterator().next());
    }

    @Test
    @Transactional
    public void testGetForeignIdsPerForeignSource() {
        Set<String> set = getNodeDao().getForeignIdsPerForeignSource("imported:");
        assertEquals("1", set.iterator().next());
    }

    @Test
    @Transactional
    public void testUpdateNodeScanStamp() {

        Date timestamp = new Date(27);

        getNodeDao().updateNodeScanStamp(getNode1().getId(), timestamp);

        OnmsNode n = getNodeDao().get(getNode1().getId());

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
    @Transactional
    public void testGetNodeLabelForId() {
        OnmsNode node = getNode1();
        String label = getNodeDao().getLabelForId(node.getId());
        assertEquals(label, node.getLabel());
    }


    @Test
    @JUnitTemporaryDatabase // This test manages its own transactions so use a fresh database
    public void testDeleteObsoleteInterfaces() {
        try {

            final Date timestamp = new Date(1234);

            m_transTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    simulateScan(timestamp);
                }

            });

            m_transTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    deleteObsoleteInterfaces(timestamp);
                }

            });

            m_transTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    validateScan();
                }

            });
        } finally {
            m_populator.resetDatabase();
        }
    }

    private void validateScan() {
        OnmsNode after = getNodeDao().get(getNode1().getId());

        assertEquals(1, after.getIpInterfaces().size());
        assertEquals(1, after.getSnmpInterfaces().size());
    }

    private void simulateScan(Date timestamp) {
        OnmsNode n = getNodeDao().get(getNode1().getId());

        assertEquals(4, n.getIpInterfaces().size());
        assertEquals(4, n.getSnmpInterfaces().size());

        OnmsIpInterface iface = n.getIpInterfaceByIpAddress("192.168.1.1");
        assertNotNull(iface);
        iface.setIpLastCapsdPoll(timestamp);

        OnmsSnmpInterface snmpIface = n.getSnmpInterfaceWithIfIndex(1);
        assertNotNull(snmpIface);
        snmpIface.setLastCapsdPoll(timestamp);

        getNodeDao().saveOrUpdate(n);

        getNodeDao().flush();
    }

    private void deleteObsoleteInterfaces(Date timestamp) {
        getNodeDao().deleteObsoleteInterfaces(getNode1().getId(), timestamp);
    }

    private void validateNode(OnmsNode n) throws Exception {
        assertNotNull("Expected node to be non-null", n);
        assertNotNull("Expected location to be non-null", n.getLocation());
        assertNotNull("Expected node "+n.getId()+" to have interfaces", n.getIpInterfaces());
        assertEquals("Unexpected number of interfaces for node "+n.getId(), 4, n.getIpInterfaces().size());
        for (Object o : n.getIpInterfaces()) {
            OnmsIpInterface iface = (OnmsIpInterface)o;
            assertNotNull(iface);
            assertNotNull(InetAddressUtils.str(iface.getIpAddress()));
        }

        assertNodeEquals(getNode1(), n);
    }

    private static class PropertyComparator implements Comparator<Object> {

        String m_propertyName;

        public PropertyComparator(String propertyName) {
            m_propertyName = propertyName;
        }

        @Override
        public int compare(Object o1, Object o2) {

            String expectedValue;
            try {
                expectedValue = ""+BeanUtils.getProperty(o1, m_propertyName);
                String actualValue = ""+BeanUtils.getProperty(o2, m_propertyName);
                return expectedValue.compareTo(actualValue);
            } catch (Throwable e) {
                throw new IllegalArgumentException("Unable to compare property "+m_propertyName, e);
            }
        }

    }

    private static void assertNodeEquals(OnmsNode expected, OnmsNode actual) throws Exception {
        assertEquals("Unexpected nodeId", expected.getId(), actual.getId());
        String[] properties = { "id", "label", "labelSource", "assetRecord.assetNumber", "location", "sysContact", "sysName", "sysObjectId" };
        assertPropertiesEqual(properties, expected, actual);

        assertInterfaceSetsEqual(expected.getIpInterfaces(), actual.getIpInterfaces());

    }

    private static interface AssertEquals<T> {
        public void assertEqual(T expected, T actual) throws Exception;
    }

    private static <T> void assertSetsEqual(Set<T> expectedSet, Set<T> actualSet, String orderProperty, AssertEquals<T> comparer) throws Exception {
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

    private static void assertInterfaceSetsEqual(Set<OnmsIpInterface> expectedSet, Set<OnmsIpInterface> actualSet) throws Exception {
        assertSetsEqual(expectedSet, actualSet, "ipAddress" , new AssertEquals<OnmsIpInterface>() {

            @Override
            public void assertEqual(OnmsIpInterface expected, OnmsIpInterface actual) throws Exception {
                assertInterfaceEquals(expected, actual);
            }

        });
    }

    private static void assertInterfaceEquals(OnmsIpInterface expected, OnmsIpInterface actual) throws Exception {
        String[] properties = { "ipAddress", "ifIndex",  "ipHostName", "isManaged", "node.id" };
        assertPropertiesEqual(properties, expected, actual);
        assertServicesEquals(expected.getMonitoredServices(), actual.getMonitoredServices());
    }

    private static void assertServicesEquals(Set<OnmsMonitoredService> expectedSet, Set<OnmsMonitoredService> actualSet) throws Exception {
        assertSetsEqual(expectedSet, actualSet, "serviceId" , new AssertEquals<OnmsMonitoredService>() {

            @Override
            public void assertEqual(OnmsMonitoredService expected, OnmsMonitoredService actual) throws Exception {
                assertServiceEquals(expected, actual);
            }

        });
    }

    protected static void assertServiceEquals(OnmsMonitoredService expected, OnmsMonitoredService actual) {
        assertEquals(expected.getServiceName(), actual.getServiceName());
    }

    private static void assertPropertiesEqual(String[] properties, Object expected, Object actual) throws Exception {
        for (String property : properties) {
            assertPropertyEquals(property, expected, actual);
        }

    }

    private static void assertPropertyEquals(String name, Object expected, Object actual) throws Exception {
        Object expectedValue = BeanUtils.getProperty(expected, name);
        Object actualValue = BeanUtils.getProperty(actual, name);
        assertEquals("Unexpected value for property "+name+" on object "+expected, expectedValue, actualValue);
    }
    
    @Test
    @Transactional
    public void testCB() {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("assetRecord", "asset").match("any").ilike("label", "%ode%").ilike("sysDescription", "%abc%").ilike("asset.comment", "%xyz%");
        List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        System.err.println("Nodes found: "+nodes.size());
        assertEquals(6, nodes.size());
        
        cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("assetRecord", "asset").match("any").ilike("label", "%alt%").ilike("sysDescription", "%abc%").ilike("asset.comment", "%xyz%");
        nodes = m_nodeDao.findMatching(cb.toCriteria());
        System.err.println("Nodes found: "+nodes.size());
        assertEquals(2, nodes.size());
    }

    /**
     * This test exposes a bug in Hibernate: it is not applying join conditions
     * correctly to the many-to-many node-to-category relationship.
     * 
     * This issue is documented in NMS-9470. If we upgrade Hibernate, we should
     * recheck this issue to see if it is fixed.
     * 
     * @see https://issues.opennms.org/browse/NMS-9470
     */
    @Test
    @Transactional
    @Ignore("Ignore until Hibernate can be upgraded and this can be rechecked")
    public void testCriteriaBuilderWithCategoryAlias() {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("categories", "category", JoinType.LEFT_JOIN, Restrictions.eq("category.name", "Routers"));
        m_nodeDao.findMatching(cb.toCriteria());
    }

    @Test
    @Transactional
    public void testCriteriaBuilderOrderBy() {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("ipInterfaces", "ipInterface").distinct();

        // TODO: Make this work but we need to put the fields into
        // an aggregator function since node->ipInterfaces is a 1->M
        // relationship.
        //
        //cb.orderBy("ipInterfaces.ipAddress").distinct();

        Criteria criteria = cb.toCriteria();
        System.out.println("Criteria: " + criteria.toString());
        List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);
        nodes.stream().forEach(System.out::println);
        assertEquals(6, nodes.size());
    }

    @Test
    @Transactional
    public void testQuery2() {
        assertNotNull(m_populator);
        assertNotNull(m_populator.getNode6());
        OnmsNode n = getNodeDao().get(m_populator.getNode6().getId());
        assertNotNull(n);
        assertEquals(3, n.getIpInterfaces().size());
        assertNotNull(n.getAssetRecord());
        assertEquals("category1", n.getAssetRecord().getDisplayCategory());
    }

    /**
     * Node 1 and 2 should have consecutive node IDs.
     */
    @Test
    @Transactional
    public void testGetNextNodeId() {
        assertEquals(m_populator.getNode2().getId(), m_nodeDao.getNextNodeId(m_populator.getNode1().getId()));
    }

    /**
     * Node 1 and 2 should have consecutive node IDs.
     */
    @Test
    @Transactional
    public void testGetPreviousNodeId() {
        assertEquals(m_populator.getNode1().getId(), m_nodeDao.getPreviousNodeId(m_populator.getNode2().getId()));
    }
    
    @Test
    @Transactional
    public void testGetNodeLabelForLocation() {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "openNMS@Apex");
        m_nodeDao.saveOrUpdate(node);
        List<OnmsNode> nodes = m_nodeDao.findByLabelForLocation("openNMS@Apex", m_locationDao.getDefaultLocation().getLocationName());
        assertEquals(nodes.get(0), node);
        
    }
    
    @Test
    @Transactional
    public void testGetForeignIdForLocation() {
        //Verify foreignId without specifying location assuming it's only one present
        OnmsNode node1 = getNode1();
        node1.setForeignSource("Apex");
        node1.setForeignId("TheOpenNMSGroup");
        m_nodeDao.saveOrUpdate(node1);
        List<OnmsNode> nodes1 = m_nodeDao.findByForeignId("TheOpenNMSGroup");
        assertEquals(nodes1.get(0), node1);
        
        //Verify same foreignId with different locations
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("Apex");
        location.setMonitoringArea("Apex");
        m_locationDao.saveOrUpdate(location);
        OnmsNode node2 = new OnmsNode(location, "openNMS@Apex");
        node2.setForeignSource("Triangle");
        node2.setForeignId("TheOpenNMSGroup");
        m_nodeDao.saveOrUpdate(node2);
        List<OnmsNode> nodes2 = m_nodeDao.findByForeignIdForLocation("TheOpenNMSGroup", location.getLocationName());
        assertEquals(nodes2.get(0), node2);
        
    }
}
