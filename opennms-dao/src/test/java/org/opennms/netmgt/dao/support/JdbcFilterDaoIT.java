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
package org.opennms.netmgt.dao.support;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.JdbcFilterDao;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcFilterDaoIT implements InitializingBean {
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    IpInterfaceDao m_interfaceDao;
    
    @Autowired
    ServiceTypeDao m_serviceTypeDao;

    JdbcFilterDao m_dao;
    
    @Autowired
    DatabasePopulator m_populator;

    @Autowired
    TransactionTemplate m_transTemplate;

    @Autowired
    DataSource m_dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {

        m_populator.populateDatabase();

        // Initialize Filter DAO
        // Give the filter DAO access to the same TemporaryDatabase data source
        // as the autowired DAOs

        DatabaseSchemaConfigFactory.init();
        m_dao = new JdbcFilterDao();
        m_dao.setDataSource(m_dataSource);
        m_dao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        m_dao.afterPropertiesSet();
        FilterDaoFactory.setInstance(m_dao);
    }

    @After
    public void tearDown() {
        m_populator.resetDatabase();
    }

    @Test
    public void testInstantiate() {
        new JdbcFilterDao();
    }

    @Test
    public void testAfterPropertiesSetValid() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(m_dataSource);
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory());
        dao.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoNodeDao() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(m_dataSource);
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory());

        // The nodeDao isn't required because this ends up getting used outside of a Spring context quite a bit
        dao.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoDataSource() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();

        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory());

        ta.anticipate(new IllegalStateException("property dataSource cannot be null"));
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testWithManyCatIncAndServiceIdentifiersInRules() throws Exception {

        // node1 has all the categories and an 192.168.1.1

        String rule = String.format("(catincIMP_mid) & (catincDEV_AC) & (catincOPS_Online) & (nodeId == '%s') & (ipAddr == '192.168.1.1') & (serviceName == 'ICMP')", m_populator.getNode1().getId().toString()) ;

        assertTrue("Rule match failed: " + rule, m_dao.isRuleMatching(rule));

        // node2 doesn't have all the categories but does have 192.168.2.1

        String rule2 = String.format("(catincIMP_mid) & (catincDEV_AC) & (catincOPS_Online) & (nodeId == '%s') & (ipAddr == '192.168.2.1') & (serviceName == 'ICMP')", m_populator.getNode2().getId().toString());

        assertFalse("Rule match succeeded unexpectedly: " + rule, m_dao.isRuleMatching(rule2));
    }

    @Test
    public void testAfterPropertiesSetNoSchemaFactory() {
        ThrowableAnticipator ta = new ThrowableAnticipator();

        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(m_dataSource);

        ta.anticipate(new IllegalStateException("property databaseSchemaConfigFactory cannot be null"));
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testGetNodeMapMatch() throws Exception {
        Map<Integer, String> map = m_dao.getNodeMap("ipaddr == '192.168.1.1'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 1, map.size());
    }

    @Test
    public void testGetNodeMapNoMatch() throws Exception {
        Map<Integer, String> map = m_dao.getNodeMap("ipaddr == '1.1.1.1'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 0, map.size());
    }

    @Test
    public void testGetIPAddressServiceMapMatch() throws Exception {
        Map<InetAddress, Set<String>> map = m_dao.getIPAddressServiceMap("ipaddr == '192.168.1.1'");
        assertNotNull("returned map should not be null", map);
        // ICMP, SNMP
        assertEquals("map size", 1, map.size());
        Set<String> services = map.get(InetAddressUtils.addr("192.168.1.1"));
        assertEquals("services size", 2, services.size());
        assertTrue(services.contains("ICMP"));
        assertTrue(services.contains("SNMP"));
    }

    @Test
    public void testGetNodeIPAddressServiceMapMatch() throws Exception {
        Map<Integer, Map<InetAddress, Set<String>>> nodeMap = m_dao.getNodeIPAddressServiceMap("ipaddr == '192.168.1.1'");
        assertThat(nodeMap.entrySet(), hasSize(1));
        Map<InetAddress, Set<String>> map = nodeMap.entrySet().iterator().next().getValue();
        Set<String> services = map.get(InetAddressUtils.addr("192.168.1.1"));
        assertEquals("services size", 2, services.size());
        assertTrue(services.contains("ICMP"));
        assertTrue(services.contains("SNMP"));
    }

    @Test
    public void testGetIPAddressServiceMapNoMatch() throws Exception {
        Map<InetAddress, Set<String>> map = m_dao.getIPAddressServiceMap("ipaddr == '1.1.1.1'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 0, map.size());
    }

    @Test
    public void testLocationFilterMatch() throws Exception {
        Map<Integer, String> map = m_dao.getNodeMap(String.format("location == '%s'",
                MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID));
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 6, map.size());
    }

    @Test
    public void testLocationFilterNoMatch() throws Exception {
        Map<Integer, String> map = m_dao.getNodeMap("location == 'DOESN_T_EXIST'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 0, map.size());
    }

    @Test
    public void testGetIPAddressListMatch() throws Exception {
        List<InetAddress> list = m_dao.getIPAddressList("ipaddr == '192.168.1.1'");
        assertNotNull("returned list should not be null", list);
        assertEquals("list size", 1, list.size());
    }

    @Test
    public void testGetIPAddressListNoMatch() throws Exception {
        List<InetAddress> list = m_dao.getIPAddressList("ipaddr == '1.1.1.1'");
        assertNotNull("returned list should not be null", list);
        assertEquals("list size", 0, list.size());
    }

    @Test
    public void testGetActiveIPListWithDeletedNode() throws Exception {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                final List<OnmsIpInterface> ifaces = m_interfaceDao.findByIpAddress("192.168.1.1");
                
                assertEquals("should be 1 interface", 1, ifaces.size());

                OnmsIpInterface iface = ifaces.get(0);
                iface.setIsManaged("D");
                m_interfaceDao.save(iface);
                m_interfaceDao.flush();
            }
        });

        /*
         * We need to flush and finish the transaction because JdbcFilterDao
         * gets its own connection from the DataSource and won't see our data
         * otherwise.
         */

        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                List<InetAddress> list = m_dao.getActiveIPAddressList("ipaddr == '192.168.1.1'");
                assertNotNull("returned list should not be null", list);
                assertEquals("no nodes should be returned, since the only one has been deleted", 0, list.size());
            }
        });
    }

    @Test
    public void testIsValid() throws Exception {
        assertFalse("There is nothing in the database, so isValid shouldn't match non-empty rules", m_dao.isValid("1.1.1.1", "ipaddr == '1.1.1.1'"));
    }

    @Test
    public void testIsValidEmptyRule() throws Exception {
        assertTrue("isValid should return true for non-empty rules", m_dao.isValid("1.1.1.1", ""));
    }

    @Test
    public void testGetInterfaceWithServiceStatement() throws Exception {
        assertEquals("SQL from getInterfaceWithServiceStatement", "SELECT DISTINCT ipInterface.ipAddr, service.serviceName, node.nodeID FROM ipInterface JOIN ifServices ON (ipInterface.id = ifServices.ipInterfaceId) JOIN service ON (ifServices.serviceID = service.serviceID) JOIN node ON (ipInterface.nodeID = node.nodeID) WHERE IPLIKE(ipInterface.ipaddr, '*.*.*.*')", m_dao.getInterfaceWithServiceStatement("ipaddr IPLIKE *.*.*.*"));
    }

    @Test
    public void testGetIpv6InterfaceWithServiceStatement() throws Exception {
        assertEquals("SQL from getIpv6InterfaceWithServiceStatement", "SELECT DISTINCT ipInterface.ipAddr, service.serviceName, node.nodeID FROM ipInterface JOIN ifServices ON (ipInterface.id = ifServices.ipInterfaceId) JOIN service ON (ifServices.serviceID = service.serviceID) JOIN node ON (ipInterface.nodeID = node.nodeID) WHERE IPLIKE(ipInterface.ipaddr, '*:*:*:*:*:*:*:*')", m_dao.getInterfaceWithServiceStatement("ipaddr IPLIKE *:*:*:*:*:*:*:*"));
    }

    @Test
    public void testWalkNodes() throws Exception {
        final List<OnmsNode> nodes = new ArrayList<>();
        EntityVisitor visitor = new AbstractEntityVisitor() {
            @Override
            public void visitNode(OnmsNode node) {
                nodes.add(node);
            }
        };
        FilterWalker walker = new FilterWalker();
        walker.setFilterDao(m_dao);
        walker.setNodeDao(m_nodeDao);
        walker.setFilter("ipaddr == '10.1.1.1'");
        walker.setVisitor(visitor);
        walker.walk();

        assertEquals("node list size", 1, nodes.size());
    }

    @Test
    public void testVariousWaysToMatchServiceNames() {
        assertEquals("service statement", m_dao.getInterfaceWithServiceStatement("isFooService"), m_dao.getInterfaceWithServiceStatement("serviceName == 'FooService'"));
        assertEquals("ip service mapping statement", m_dao.getNodeIPServiceMappingStatement("isFooService"), m_dao.getNodeIPServiceMappingStatement("serviceName == 'FooService'"));
        assertEquals("ip service mapping statement", m_dao.getNodeMappingStatement("isFooService"), m_dao.getNodeMappingStatement("serviceName == 'FooService'"));

        // Just make sure this one doesn't hurl
        m_dao.getInterfaceWithServiceStatement("serviceName == 'DiskUsage-/foo/bar'");
    }

    // Verifies that if a bunch of interfaces exists, checking if an ip address is valid should be faster
    // than retrieving all interfaces.
    // See HZN-1161 for more details.
    @Test
    public void verifyPerformance() {
        final int[] numberOfInterfaces = new int[1];
        
        // Create interfaces in a transaction and commit them
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                // Create a bunch of interfaces
                final OnmsNode node1 = m_populator.getNode1();
                final IPAddressRange ipAddresses = new IPAddressRange("10.10.0.0", "10.10.255.255");
                final Iterator<IPAddress> iterator = ipAddresses.iterator();
                while (iterator.hasNext()) {
                    IPAddress address = iterator.next();
                    OnmsIpInterface ipInterface = new OnmsIpInterface();
                    ipInterface.setNode(node1);
                    ipInterface.setIpAddress(address.toInetAddress());
                    m_interfaceDao.saveOrUpdate(ipInterface);
                }
                // Flush to ensure all interfaces are persisted
                numberOfInterfaces[0] = m_interfaceDao.countAll();
                assertThat(numberOfInterfaces[0], greaterThan(255 * 255));
            }
        });
        assertThat(m_dao.getActiveIPAddressList("IPADDR != '0.0.0.0'"), Matchers.hasSize(numberOfInterfaces[0]));
        assertThat(m_dao.isValid("10.10.0.1", "IPADDR != '0.0.0.0'"), is(true));

    }
}
