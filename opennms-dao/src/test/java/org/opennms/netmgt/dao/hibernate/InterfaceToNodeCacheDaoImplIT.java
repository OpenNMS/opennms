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
package org.opennms.netmgt.dao.hibernate;

import static org.awaitility.Awaitility.await;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

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
@JUnitTemporaryDatabase(reuseDatabase=false)
public class InterfaceToNodeCacheDaoImplIT implements InitializingBean {

    @Autowired
    InterfaceToNodeCache m_cache;

    @Autowired
    DatabasePopulator m_databasePopulator;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    TransactionOperations transactionOperations;

    @Autowired
    MonitoringLocationDao m_monitoringLocationDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_databasePopulator.populateDatabase();
        m_cache.clear();
    }

    // Verify reloading works. See HZN-1311
    @Test
    public void testReloading() {
        // We manually have to create a reloading dao, as by default it does not
        m_cache = new InterfaceToNodeCacheDaoImpl(1000L);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(m_cache);
        applicationContext.getAutowireCapableBeanFactory().initializeBean(m_cache, "reloading-interface-to-node-cache");

        // Verify it is initialized
        Optional<Integer> nodeId = m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, m_databasePopulator.getNode1().getPrimaryInterface().getIpAddress());
        Assert.assertEquals(true, nodeId.isPresent());
        Assert.assertEquals(m_databasePopulator.getNode1().getId(), nodeId.get());

        // Verify bean is not there yet
        nodeId = m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, InetAddressUtils.addr("8.8.8.8"));
        Assert.assertEquals(false, nodeId.isPresent());

        // Execute adding of missing node in transaction scope
        transactionOperations.execute((status) -> {
            // Add missing node
            final OnmsNode node = new OnmsNode();
            node.setLocation(m_databasePopulator.getNode1().getLocation());
            node.setLabel("Dummy-Node");
            node.setType(OnmsNode.NodeType.ACTIVE);

            // Add interface to node
            new OnmsIpInterface(InetAddressUtils.addr("8.8.8.8"), node);
            m_databasePopulator.getNodeDao().save(node);
            m_databasePopulator.getNodeDao().flush();
            return null;
        });

        // Try for number of seconds until it succeeds
        await().atMost(10, TimeUnit.SECONDS)
               .pollInterval(1000, TimeUnit.MILLISECONDS)
               .until(() -> m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, InetAddressUtils.addr("8.8.8.8")).isPresent());
    }

    @Test
    @Transactional
    public void testGetId() throws Exception {
        m_cache.dataSourceSync();

        final InetAddress ipAddr = m_databasePopulator.getNode2().getPrimaryInterface().getIpAddress();
        final int expectedNodeId = m_databasePopulator.getNode2().getId();

        final int nodeId = m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, ipAddr).get();
        Assert.assertEquals(expectedNodeId, nodeId);
    }

    @Test
    @Transactional
    public void testSetId() throws Exception {
        final int nodeId = m_databasePopulator.getNode1().getId();

        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.3"), nodeId));
        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.2"), nodeId));
        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.1"), nodeId));
        Assert.assertEquals(false, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, addr("192.168.1.3"), nodeId));
    }

    @Test
    @Transactional
    public void testSetIdWithDifferentNodes() throws Exception {
        m_cache.dataSourceSync();

        final OnmsNode node = new OnmsNode(m_databasePopulator.getMonitoringLocationDao().getDefaultLocation(), "my-new-node");
        node.setForeignSource("junit");
        node.setForeignId("10001");
        final OnmsIpInterface iface = new OnmsIpInterface(InetAddress.getByName("192.168.1.2"), node);
        iface.setIsManaged("M");
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        final OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(node, 1001);
        iface.setSnmpInterface(snmpIf);
        snmpIf.getIpInterfaces().add(iface);
        node.addIpInterface(iface);
        m_databasePopulator.getNodeDao().save(node);

        // Different node ID must return true again
        Assert.assertEquals(true, m_cache.setNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, iface.getIpAddress(), node.getId()));

        // Primary must be first
        Assert.assertEquals(m_databasePopulator.getNode1().getId(), m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, iface.getIpAddress()).get());
    }

    @Test
    @Transactional
    public void testNullLocation() throws Exception {
        m_cache.dataSourceSync();

        // Retrieve a known entry stored using the default location id
        InetAddress ipAddr = m_databasePopulator.getNode2().getPrimaryInterface().getIpAddress();
        long expectedNodeId = Long.parseLong(m_databasePopulator.getNode2().getNodeId());
        long nodeId = m_cache.getFirstNodeId(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, ipAddr).get();
        Assert.assertEquals(expectedNodeId, nodeId);

        // Now try retrieving that same node using null for the location
        nodeId = m_cache.getFirstNodeId(null, ipAddr).get();
        Assert.assertEquals(expectedNodeId, nodeId);
    }

    @Test
    @Transactional
    public void testDuplicate() throws Exception {
        final OnmsMonitoringLocation defaultLocation = m_monitoringLocationDao.getDefaultLocation();

        final InetAddress theAddress = InetAddress.getByName("1.2.3.4");

        Assert.assertNotNull(m_cache);

        final OnmsNode node1 = new OnmsNode(defaultLocation,"node1");
        final OnmsIpInterface iface1 = new OnmsIpInterface();
        iface1.setIpAddress(theAddress);
        iface1.setIsSnmpPrimary(PrimaryType.PRIMARY);
        node1.addIpInterface(iface1);
        final int nodeId1 = m_databasePopulator.getNodeDao().save(node1);

        m_cache.setNodeId(defaultLocation.getLocationName(), iface1.getIpAddress(), node1.getId());

        Assert.assertEquals(nodeId1, (int) m_cache.getFirstNodeId(defaultLocation.getLocationName(), theAddress).get());

        final OnmsNode node2 = new OnmsNode(defaultLocation,"node2");
        final OnmsIpInterface iface2 = new OnmsIpInterface();
        iface2.setIpAddress(theAddress);
        iface2.setIsSnmpPrimary(PrimaryType.PRIMARY);
        node2.addIpInterface(iface2);
        final int nodeId2 = m_databasePopulator.getNodeDao().save(node2);

        m_cache.setNodeId(defaultLocation.getLocationName(), iface2.getIpAddress(), node2.getId());

        Assert.assertEquals(nodeId1, (int) m_cache.getFirstNodeId(defaultLocation.getLocationName(), theAddress).get());

        m_cache.removeNodeId(defaultLocation.getLocationName(), theAddress, nodeId1);

        Assert.assertEquals(nodeId2, (int) m_cache.getFirstNodeId(defaultLocation.getLocationName(), theAddress).get());
    }

    @Test
    @Transactional
    public void testNodeDeletion() throws Exception {

        final OnmsMonitoringLocation defaultLocation = m_monitoringLocationDao.getDefaultLocation();
        Assert.assertNotNull(m_cache);

        final OnmsNode node = new OnmsNode(defaultLocation,"node1");
        InetAddress ipAddr1  = InetAddress.getByName("192.168.0.2");
        String nodeLocation = defaultLocation.getLocationName();
        addInterface(node, ipAddr1, nodeLocation);
        InetAddress ipAddr2  = InetAddress.getByName("192.168.0.7");
        addInterface(node, ipAddr2, nodeLocation);
        InetAddress ipAddr3  = InetAddress.getByName("192.168.0.8");
        addInterface(node, ipAddr3, nodeLocation);
        final int nodeId = m_databasePopulator.getNodeDao().save(node);
        m_cache.setNodeId(nodeLocation, ipAddr1, nodeId);
        m_cache.setNodeId(nodeLocation, ipAddr2, nodeId);
        m_cache.setNodeId(nodeLocation, ipAddr3, nodeId);

        Assert.assertEquals(nodeId, (int) m_cache.getFirstNodeId(defaultLocation.getLocationName(), ipAddr2).get());
        Assert.assertThat(m_cache.size(), Matchers.greaterThan(0));
        m_cache.removeInterfacesForNode(nodeId);
        Assert.assertEquals(0, m_cache.size());
    }

    private void addInterface(OnmsNode node, InetAddress inetAddress, String location) {
        final OnmsIpInterface iface = new OnmsIpInterface();
        iface.setIpAddress(inetAddress);
        node.addIpInterface(iface);
    }
}
