/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created July 22, 2007
 * 
 * 2008 Jul 02: Get rid of DataSource stuff since it is now
 *              in our superclass. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.db.TemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseAware;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcFilterDaoTest implements InitializingBean, TemporaryDatabaseAware<TemporaryDatabase> {
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

    private TemporaryDatabase m_database;

    public void setTemporaryDatabase(TemporaryDatabase database) {
        m_database = database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_nodeDao);
        assertNotNull(m_interfaceDao);
        assertNotNull(m_populator);
        assertNotNull(m_transTemplate);
    }

    @Before
    @Transactional
    public void setUp() throws Exception {
        OnmsServiceType t = new OnmsServiceType("ICMP");
        m_serviceTypeDao.save(t);

        m_populator.populateDatabase();

        // Initialize Filter DAO
        // Give the filter DAO access to the same TemporaryDatabase data source
        // as the autowired DAOs

        System.setProperty("opennms.home", "src/test/resources");
        DatabaseSchemaConfigFactory.init();
        m_dao = new JdbcFilterDao();
        m_dao.setDataSource(m_database);
        m_dao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        m_dao.afterPropertiesSet();
        FilterDaoFactory.setInstance(m_dao);
    }

    @Test
    public void testInstantiate() {
        new JdbcFilterDao();
    }

    @Test
    public void testAfterPropertiesSetValid() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(m_database);
        dao.setNodeDao(m_nodeDao);
        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(is));
        is.close();
        dao.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoNodeDao() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(m_database);
        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(is));
        is.close();

        // The nodeDao isn't required because this ends up getting used outside of a Spring context quite a bit
        dao.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetNoDataSource() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();

        JdbcFilterDao dao = new JdbcFilterDao();
        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(is));
        is.close();

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

        String rule = "(catincIMP_mid) & (catincDEV_AC) & (catincOPS_Online) & (nodeId == 1) & (ipAddr == '192.168.1.1') & (serviceName == 'ICMP')" ;

        assertTrue(m_dao.isRuleMatching(rule));

        // node2 doesn't have all the categories but does have 192.168.2.1

        String rule2 = "(catincIMP_mid) & (catincDEV_AC) & (catincOPS_Online) & (nodeId == 2) & (ipAddr == '192.168.2.1') & (serviceName == 'ICMP')" ;

        assertFalse(m_dao.isRuleMatching(rule2));
    }

    @Test
    public void testAfterPropertiesSetNoSchemaFactory() {
        ThrowableAnticipator ta = new ThrowableAnticipator();

        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(m_database);

        ta.anticipate(new IllegalStateException("property databaseSchemaConfigFactory cannot be null"));
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Transactional
    public void testGetNodeMap() throws Exception {
        Map<Integer, String> map = m_dao.getNodeMap("ipaddr == '1.1.1.1'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 0, map.size());
    }

    @Test
    @Transactional
    public void testGetIPAddressServiceMap() throws Exception {
        Map<InetAddress, Set<String>> map = m_dao.getIPAddressServiceMap("ipaddr == '1.1.1.1'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 0, map.size());
    }

    @Test
    @Transactional
    public void testGetIPAddressList() throws Exception {
        List<InetAddress> list = m_dao.getIPAddressList("ipaddr == '1.1.1.1'");
        assertNotNull("returned list should not be null", list);
        assertEquals("list size", 0, list.size());
    }

    @Test
    @Transactional
    public void testGetActiveIPListWithDeletedNode() throws Exception {
        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                List<InetAddress> list = m_dao.getIPAddressList("ipaddr == '192.168.1.1'");
                final List<OnmsIpInterface> ifaces = m_interfaceDao.findByIpAddress("192.168.1.1");
                
                assertEquals("should be 1 interface", 1, ifaces.size());

                OnmsIpInterface iface = ifaces.get(0);
                iface.setIsManaged("D");
                m_interfaceDao.save(iface);
                m_interfaceDao.flush();
                return null;
            }
        });

        /*
         * We need to flush and finish the transaction because JdbcFilterDao
         * gets its own connection from the DataSource and won't see our data
         * otherwise.
         */

        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                List<InetAddress> list = m_dao.getActiveIPAddressList("ipaddr == '192.168.1.1'");
                assertNotNull("returned list should not be null", list);
                assertEquals("no nodes should be returned, since the only one has been deleted", 0, list.size());
                return null;
            }
        });
    }

    @Test
    @Transactional
    public void testIsValid() throws Exception {
        assertFalse("There is nothing in the database, so isValid shouldn't match non-empty rules", m_dao.isValid("1.1.1.1", "ipaddr == '1.1.1.1'"));
    }

    @Test
    @Transactional
    public void testIsValidEmptyRule() throws Exception {
        assertTrue("isValid should return true for non-empty rules", m_dao.isValid("1.1.1.1", ""));
    }

    @Test
    @Transactional
    public void testGetInterfaceWithServiceStatement() throws Exception {
        assertEquals("SQL from getInterfaceWithServiceStatement", "SELECT DISTINCT ipInterface.ipAddr, service.serviceName, node.nodeID FROM ipInterface JOIN ifServices ON (ipInterface.id = ifServices.ipInterfaceId) JOIN service ON (ifServices.serviceID = service.serviceID) JOIN node ON (ipInterface.nodeID = node.nodeID) WHERE IPLIKE(ipInterface.ipaddr, '*.*.*.*')", m_dao.getInterfaceWithServiceStatement("ipaddr IPLIKE *.*.*.*"));
    }

    @Test
    public void testWalkNodes() throws Exception {
        m_dao.setNodeDao(m_nodeDao);

        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        EntityVisitor visitor = new AbstractEntityVisitor() {
            public void visitNode(OnmsNode node) {
                nodes.add(node);
            }
        };
        m_dao.walkMatchingNodes("ipaddr == '10.1.1.1'", visitor);

        assertEquals("node list size", 1, nodes.size());
    }

    @Test
    @Transactional
    public void testVariousWaysToMatchServiceNames() {
        assertEquals("service statement", m_dao.getInterfaceWithServiceStatement("isFooService"), m_dao.getInterfaceWithServiceStatement("serviceName == 'FooService'"));
        assertEquals("ip service mapping statement", m_dao.getIPServiceMappingStatement("isFooService"), m_dao.getIPServiceMappingStatement("serviceName == 'FooService'"));
        assertEquals("ip service mapping statement", m_dao.getNodeMappingStatement("isFooService"), m_dao.getNodeMappingStatement("serviceName == 'FooService'"));

        // Just make sure this one doesn't hurl
        m_dao.getInterfaceWithServiceStatement("serviceName == 'DiskUsage-/foo/bar'");
    }
}
