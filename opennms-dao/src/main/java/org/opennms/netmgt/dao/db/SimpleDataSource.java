package org.opennms.netmgt.dao.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

public class SimpleDataSource implements DataSource {
    private String m_driver;
    private String m_url;
    private String m_user;
    private String m_password;

    public SimpleDataSource(String driver, String url,
                               String user, String password) throws ClassNotFoundException {
        m_driver = driver;
        m_url = url;
        m_user = user;
        m_password = password;
        
        Class.forName(m_driver);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(m_url, m_user, m_password);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("getConnection(String, String) not implemented");
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("getLogWriter() not implemented");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("getLoginTimeout() not implemented");
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter(PrintWriter) not implemented");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout(int) not implemented");
    }
}
