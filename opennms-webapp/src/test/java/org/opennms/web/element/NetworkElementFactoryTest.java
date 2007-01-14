package org.opennms.web.element;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.opennms.core.resource.Vault;
import org.opennms.core.resource.db.DbConnectionFactory;
import org.opennms.netmgt.dao.db.PopulatedTemporaryDatabaseTestCase;

public class NetworkElementFactoryTest extends PopulatedTemporaryDatabaseTestCase {
    @Override
    protected void setUp() throws Exception {
        setSetupIpLike(true);
        
        super.setUp();
        
        Vault.setDbConnectionFactory(new DataSourceDbConnectionFactory(getDataSource()));
    }
    
    public void testGetNodesWithIpLikeOneInterface() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (1, now(), 'A')");
        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.1', 'M')");
        
        assertEquals("node count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM node"));
        assertEquals("ipInterface count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM ipInterface"));
        
        Node[] nodes = NetworkElementFactory.getNodesWithIpLike("*.*.*.*");
        assertEquals("node count", 1, nodes.length);
    }
    
    // bug introduced in revision 2932
    public void testGetNodesWithIpLikeTwoInterfaces() throws Exception {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (1, now(), 'A')");
        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.1', 'M')");
        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.2', 'M')");
        
        assertEquals("node count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM node"));
        assertEquals("ipInterface count in DB", 2, jdbcTemplate.queryForInt("SELECT count(*) FROM ipInterface"));

        Node[] nodes = NetworkElementFactory.getNodesWithIpLike("*.*.*.*");
        assertEquals("node count", 1, nodes.length);
    }
    
    public class DataSourceDbConnectionFactory implements DbConnectionFactory {
        private DataSource m_dataSource;

        public DataSourceDbConnectionFactory(DataSource dataSource) {
            m_dataSource = dataSource;
        }
        
        public void destroy() throws SQLException {
            throw new UnsupportedOperationException("not implemented");
        }

        public Connection getConnection() throws SQLException {
            return m_dataSource.getConnection();
        }

        public void init(String dbUrl, String dbDriver) throws ClassNotFoundException, SQLException {
            throw new UnsupportedOperationException("not implemented");
        }

        public void init(String dbUrl, String dbDriver, String username, String password) throws ClassNotFoundException, SQLException {
            throw new UnsupportedOperationException("not implemented");
        }

        public void init(String dbUrl, String dbDriver, Properties properties) throws ClassNotFoundException, SQLException {
            throw new UnsupportedOperationException("not implemented");
        }

        public void releaseConnection(Connection connection) throws SQLException {
            connection.close();
        }
        
    }
}
