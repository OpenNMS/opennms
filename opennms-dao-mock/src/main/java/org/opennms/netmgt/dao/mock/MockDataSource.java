package org.opennms.netmgt.dao.mock;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class MockDataSource implements DataSource {

    public MockDataSource() {
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
