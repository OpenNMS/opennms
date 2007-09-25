package org.opennms.netmgt.dao.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

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


public class JdbcFilterDaoTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private DataSource m_dataSource;
    private NodeDao m_nodeDao;
    private JdbcFilterDao m_dao;
    private DatabasePopulator m_populator;
    
    public JdbcFilterDaoTest() {
        super();
        
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
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
        m_dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));
        m_dao.afterPropertiesSet();
    }
    
    public void testInstantiate() {
        new JdbcFilterDao();
    }
    
    public void testAfterPropertiesSetValid() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(getDataSource());
        dao.setNodeDao(m_nodeDao);
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));
        dao.afterPropertiesSet();
    }

    public void testAfterPropertiesSetNoNodeDao() throws Exception {
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDataSource(getDataSource());
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));
        
        // The nodeDao isn't required because this ends up getting used outside of a Spring context quite a bit
        dao.afterPropertiesSet();
    }
    
    public void testAfterPropertiesSetNoDataSource() throws Exception {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        
        JdbcFilterDao dao = new JdbcFilterDao();
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));

        
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
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));
        
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
        assertEquals("SQL from getInterfaceWithServiceStatement", "SELECT DISTINCT ipInterface.ipAddr, service.serviceName, node.nodeID FROM ipInterface, ifServices, service, node WHERE (iplike(ipInterface.ipaddr, '*.*.*.*')) AND ifServices.ipInterfaceId = ipInterface.id AND service.serviceID = ifServices.serviceID AND ifServices.ipInterfaceId = ipInterface.id AND node.nodeID = ipInterface.nodeID", m_dao.getInterfaceWithServiceStatement("ipaddr IPLIKE *.*.*.*"));
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

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
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
