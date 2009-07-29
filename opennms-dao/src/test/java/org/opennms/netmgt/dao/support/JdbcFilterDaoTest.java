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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.ThrowableAnticipator;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class JdbcFilterDaoTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private NodeDao m_nodeDao;
    private JdbcFilterDao m_dao;
    private DatabasePopulator m_populator;
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        };
    }

    @Override
    public void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();

        m_populator.populateDatabase();
        setComplete();
        endTransaction();
        startNewTransaction();

        m_dao = new JdbcFilterDao();
        // Don't set the NodeDao because it isn't required for most methods
        m_dao.setDataSource(getDataSource());
        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        m_dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(is));
        is.close();
        m_dao.afterPropertiesSet();
    }

    public void testInstantiate() {
        new JdbcFilterDao();
    }

    public void testAfterPropertiesSetValid() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(getDataSource());
        dao.setNodeDao(m_nodeDao);
        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(is));
        is.close();
        dao.afterPropertiesSet();
    }

    public void testAfterPropertiesSetNoNodeDao() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(getDataSource());
        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(is));
        is.close();

        // The nodeDao isn't required because this ends up getting used outside of a Spring context quite a bit
        dao.afterPropertiesSet();
    }

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

    public void testWithManyCatIncAndServiceIdentifiersInRules() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(getDataSource());
        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(is));
        is.close();

        dao.afterPropertiesSet();

        // node1 has all the categories and an 192.168.1.1

        String rule = "(catincIMP_mid) & (catincDEV_AC) & (catincOPS_Online) & (nodeId == 1) & (ipAddr == '192.168.1.1') & (serviceName == 'ICMP')" ;

        assertTrue(dao.isRuleMatching(rule));

        // node2 doesn't have all the categories but does have 192.168.2.1

        String rule2 = "(catincIMP_mid) & (catincDEV_AC) & (catincOPS_Online) & (nodeId == 2) & (ipAddr == '192.168.2.1') & (serviceName == 'ICMP')" ;

        assertFalse(dao.isRuleMatching(rule2));
    }

    public void testAfterPropertiesSetNoSchemaFactory() {
        ThrowableAnticipator ta = new ThrowableAnticipator();

        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(getDataSource());

        ta.anticipate(new IllegalStateException("property databaseSchemaConfigFactory cannot be null"));
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testGetNodeMap() throws Exception {
        Map<Integer, String> map = m_dao.getNodeMap("ipaddr == '1.1.1.1'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 0, map.size());
    }

    public void testGetIPServiceMap() throws Exception {
        Map<String, Set<String>> map = m_dao.getIPServiceMap("ipaddr == '1.1.1.1'");
        assertNotNull("returned map should not be null", map);
        assertEquals("map size", 0, map.size());
    }

    public void testGetIPList() throws Exception {
        List<String> list = m_dao.getIPList("ipaddr == '1.1.1.1'");
        assertNotNull("returned list should not be null", list);
        assertEquals("list size", 0, list.size());
    }

    public void testIsValid() throws Exception {
        assertFalse("There is nothing in the database, so isValid shouldn't match non-empty rules", m_dao.isValid("1.1.1.1", "ipaddr == '1.1.1.1'"));
    }

    public void testIsValidEmptyRule() throws Exception {
        assertTrue("isValid should return true for non-empty rules", m_dao.isValid("1.1.1.1", ""));
    }

    public void testGetInterfaceWithServiceStatement() throws Exception {
        assertEquals("SQL from getInterfaceWithServiceStatement", "SELECT DISTINCT ipInterface.ipAddr, service.serviceName, node.nodeID FROM ipInterface JOIN ifServices ON (ipInterface.id = ifServices.ipInterfaceId) JOIN service ON (ifServices.serviceID = service.serviceID) JOIN node ON (ipInterface.nodeID = node.nodeID) WHERE IPLIKE(ipInterface.ipaddr, '*.*.*.*')", m_dao.getInterfaceWithServiceStatement("ipaddr IPLIKE *.*.*.*"));
    }

    public void testWalkNodes() throws Exception {
        m_dao.setNodeDao(getNodeDao());

        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        EntityVisitor visitor = new AbstractEntityVisitor() {
            public void visitNode(OnmsNode node) {
                nodes.add(node);
            }
        };
        m_dao.walkMatchingNodes("ipaddr == '10.1.1.1'", visitor);

        assertEquals("node list size", 1, nodes.size());
    }

    public void testVariousWaysToMatchServiceNames() {
        assertEquals("service statement", m_dao.getInterfaceWithServiceStatement("isFooService"), m_dao.getInterfaceWithServiceStatement("serviceName == 'FooService'"));
        assertEquals("ip service mapping statement", m_dao.getIPServiceMappingStatement("isFooService"), m_dao.getIPServiceMappingStatement("serviceName == 'FooService'"));
        assertEquals("ip service mapping statement", m_dao.getNodeMappingStatement("isFooService"), m_dao.getNodeMappingStatement("serviceName == 'FooService'"));

        // Just make sure this one doesn't hurl
        m_dao.getInterfaceWithServiceStatement("serviceName == 'DiskUsage-/foo/bar'");
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public DatabasePopulator getPopulator() {
        return m_populator;
    }

    public void setPopulator(DatabasePopulator populator) {
        m_populator = populator;
    }

}
