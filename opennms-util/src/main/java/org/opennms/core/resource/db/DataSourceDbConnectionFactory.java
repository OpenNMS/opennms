/**
 * 
 */
package org.opennms.core.resource.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;


public class DataSourceDbConnectionFactory implements DbConnectionFactory {
    private DataSource m_dataSource;

    public DataSourceDbConnectionFactory(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
    public void destroy() throws SQLException {
    }

    public Connection getConnection() throws SQLException {
        return m_dataSource.getConnection();
    }

    public void init(String dbUrl, String dbDriver, String username, String password) throws ClassNotFoundException, SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    public void releaseConnection(Connection connection) throws SQLException {
        connection.close();
    }
}