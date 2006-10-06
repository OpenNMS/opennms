package org.opennms.install;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class TemporaryDatabaseDataSource implements DataSource {
    private TemporaryDatabaseTestCase m_tc;

    public void setTemporaryDatabaseTestCase(TemporaryDatabaseTestCase tc) {
        m_tc = tc;
    }

    public Connection getConnection() throws SQLException {
        return m_tc.getConnection();
    }

    public Connection getConnection(String arg0, String arg1) throws SQLException {
        return getConnection();
    }

    public PrintWriter getLogWriter() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public int getLoginTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setLogWriter(PrintWriter arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    public void setLoginTimeout(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }
    
}