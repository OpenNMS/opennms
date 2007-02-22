package org.opennms.netmgt.dao.support;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;


public class JdbcFilterDaoTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private DataSource m_dataSource;
    private JdbcFilterDao m_dao;
    
    @Override
    public void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();
        
        m_dao = new JdbcFilterDao();
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
        dao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));
        dao.afterPropertiesSet();
    }
    
    public void testAfterPropertiesSetNoDataSource() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        
        JdbcFilterDao dao = new JdbcFilterDao();
        
        ta.anticipate(new IllegalStateException("property dataSource cannot be null"));
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
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

    @Override
    protected String[] getConfigLocations() {
        return new String[0];
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

}
