package org.opennms.netmgt.config.tester;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

class ConfigTesterDataSource implements DataSource {

	private List<SQLException> m_connectionGetAttempts = new ArrayList<SQLException>();

	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public void setLogWriter(PrintWriter arg0) throws SQLException {
	}

	public void setLoginTimeout(int arg0) throws SQLException {
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}

	public Connection getConnection() throws SQLException {
		return createStoreAndThrowException();
	}

	public Connection getConnection(String arg0, String arg1)
			throws SQLException {
		return createStoreAndThrowException();
	}

	private Connection createStoreAndThrowException() throws SQLException {
		SQLException e = createException();
		m_connectionGetAttempts.add(e);
		throw e;
	}

	private SQLException createException() {
		return new SQLException("No database connections should be requested when reading a configuration file, dude.");
	}
	
	public List<SQLException> getConnectionGetAttempts() {
		return m_connectionGetAttempts;
	}
}