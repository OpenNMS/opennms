/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class NetworkElementFactoryTest implements InitializingBean {
    
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
        m_jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType, nodeLabel) VALUES (12, now(), 'A', 'nodeLabel')");
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

        m_jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (12, now(), 'A')");
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

        m_jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (12, now(), 'A')");
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
    @Transactional
    @JUnitTemporaryDatabase
    public void testGetDataLinksOnInterface() {
        List<LinkInterface> dlis = NetworkElementFactory.getInstance(m_appContext).getDataLinksOnInterface(m_dbPopulator.getNode1().getId(), 1);
        assertEquals(4, dlis.size());
        
        List<LinkInterface> dlis2 = NetworkElementFactory.getInstance(m_appContext).getDataLinksOnInterface(m_dbPopulator.getNode1().getId(), 9);
        assertEquals(0, dlis2.size());
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testGetAtInterfaces() throws Exception {
        AtInterface atif = NetworkElementFactory.getInstance(m_appContext).getAtInterface(m_dbPopulator.getNode2().getId(), "192.168.2.1");
        assertEquals("AA:BB:CC:DD:EE:FF", atif.get_physaddr());
        
        List<OnmsNode> nodes = NetworkElementFactory.getInstance(m_appContext).getNodesFromPhysaddr("AA:BB:CC:DD:EE:FF");
        assertEquals(1, nodes.size());
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testGetDataLinksOnNode() throws SQLException {
    	List<LinkInterface> dlis = NetworkElementFactory.getInstance(m_appContext).getDataLinksOnNode(m_dbPopulator.getNode1().getId());
        assertEquals(5, dlis.size());
        
        List<LinkInterface> dlis2 = NetworkElementFactory.getInstance(m_appContext).getDataLinksOnNode(100);
        assertEquals(0, dlis2.size());
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testGetServicesOnInterface() {
        m_jdbcTemplate.update("UPDATE ifservices SET status='A' WHERE id=2;");
        Service[] svc = NetworkElementFactory.getInstance(m_appContext).getServicesOnInterface(1, "192.168.1.1");
        assertEquals(1, svc.length);
    }
}
