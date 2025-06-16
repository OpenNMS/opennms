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
package org.opennms.web.element;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class NetworkElementFactoryIT implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    ApplicationContext m_appContext;
    
    @Autowired
    DataSource m_dataSource;

    @Autowired
    JdbcTemplate m_jdbcTemplate;

    @Autowired
    NodeDao m_nodeDao;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp() {
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testGetNodeLabel() throws SQLException {
        String nodeLabel = NetworkElementFactory.getInstance(m_appContext).getNodeLabel(1);
        
        assertEquals(nodeLabel, "node1");
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testGetIpPrimaryAddress() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeType, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', 12, now(), 'A', 'nodeLabel')");
        m_jdbcTemplate.update("INSERT INTO ipinterface (nodeid, ipaddr, iplastcapsdpoll, issnmpprimary) VALUES (12, '172.168.1.1', now(), 'P')");
        
        String ipAddr = NetworkElementFactory.getInstance(m_appContext).getIpPrimaryAddress(12);
        
        assertEquals(ipAddr, "172.168.1.1");
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testGetNodesWithIpLikeOneInterface() throws Exception {
        // setUp() creates nodes by default, start with a clean slate
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        m_nodeDao.flush();

        m_jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeType, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', 12, now(), 'A', 'test')");
        m_jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (12, '1.1.1.1', 'M')");
        
        final List<OnmsNode> nodes = NetworkElementFactory.getInstance(m_appContext).getNodesWithIpLike("*.*.*.*");
        assertEquals("node count", 1, nodes.size());
    }
    
    // bug introduced in revision 2932
    @Test
    @JUnitTemporaryDatabase
    public void testGetNodesWithIpLikeTwoInterfaces() throws Exception {
        // setUp() creates nodes by default, start with a clean slate
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        m_nodeDao.flush();

        m_jdbcTemplate.update("INSERT INTO node (location, nodeId, nodeCreateTime, nodeType, nodeLabel) VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', 12, now(), 'A', 'test')");
        m_jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (12, '1.1.1.1', 'M')");
        m_jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (12, '1.1.1.2', 'M')");
        
        final List<OnmsNode> nodes = NetworkElementFactory.getInstance(m_appContext).getNodesWithIpLike("*.*.*.*");
        assertEquals("node count", 1, nodes.size());
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testGetInterfacesWithIpAddress() throws Exception {
        Interface[] interfaces = NetworkElementFactory.getInstance(m_appContext).getInterfacesWithIpAddress("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5");
        assertEquals("interface count", 1, interfaces.length);
        assertEquals("node ID", m_dbPopulator.getNode1().getId().intValue(), interfaces[0].getNodeId());
        assertEquals("ifIndex", 4, interfaces[0].getIfIndex());

        interfaces = NetworkElementFactory.getInstance(m_appContext).getInterfacesWithIpAddress("fe80:0000:0000:0000:aaaa:bbbb:cccc:0001%5");
        assertEquals("interface count", 0, interfaces.length);

        interfaces = NetworkElementFactory.getInstance(m_appContext).getInterfacesWithIpAddress("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4");
        assertEquals("interface count", 0, interfaces.length);
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testGetActiveInterfacesOnNode() {
    	Interface[] intfs = NetworkElementFactory.getInstance(m_appContext).getActiveInterfacesOnNode(m_dbPopulator.getNode1().getId());
    	assertEquals("active interfaces", 4, intfs.length);
    	
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetServicesOnInterface() {
        m_jdbcTemplate.update("UPDATE ifservices SET status='A' WHERE id=2;");
        Service[] svc = NetworkElementFactory.getInstance(m_appContext).getServicesOnInterface(1, "192.168.1.1");
        assertEquals(2, svc.length);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetNodeByPhysAddr() {
        List<OnmsNode> nodes = NetworkElementFactory.getInstance(m_appContext).getNodesFromPhysaddr("34:E4:56:04:BB:69");
        assertEquals(1, nodes.size());
        // try without :
        nodes = NetworkElementFactory.getInstance(m_appContext).getNodesFromPhysaddr("34E45604BB69");
        assertEquals(1, nodes.size());
        // try with -
        nodes = NetworkElementFactory.getInstance(m_appContext).getNodesFromPhysaddr("34-E4-56-04-BB-69");
        assertEquals(1, nodes.size());
        // try with lower-case
        nodes = NetworkElementFactory.getInstance(m_appContext).getNodesFromPhysaddr("34:e4:56:04:bb:69");
        assertEquals(1, nodes.size());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetNodesWithPhysAddr() {
        List<OnmsNode> nodes = NetworkElementFactory.getInstance(m_appContext).getNodesWithPhysAddr("34:E4:56:04:BB:69");
        assertEquals(1, nodes.size());
    }
}
