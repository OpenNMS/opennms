package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0DbConnectionFactory implements DbConnectionFactory {

    private ComboPooledDataSource m_pool;

    public C3P0DbConnectionFactory(DbConfiguration dbConfig) throws ClassNotFoundException {

        try {
            m_pool = new ComboPooledDataSource();
            m_pool.setPassword(dbConfig.getDriverPass());
            m_pool.setUser(dbConfig.getDriverUser());
            m_pool.setJdbcUrl(dbConfig.getDriverUrl());
            m_pool.setDriverClass(dbConfig.getDriverClassName());
        } catch (PropertyVetoException e) {
            // wrap this to keep the same signature as the LegacyDatabase
            throw new ClassNotFoundException("Unable to setDriverClass", e);
        }
        
    }

    public Connection getConnection() throws SQLException {
        return m_pool.getConnection();
    }

}
