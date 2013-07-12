package org.opennms.core.test.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.Assert;

public class TemporaryDatabaseHsqldb implements TemporaryDatabase, InitializingBean {
    private static DataSource m_dataSource = null;
    private String m_testDatabase;
    private boolean m_populateSchema = false;
    private SimpleJdbcTemplate m_jdbcTemplate;
    private Set<String> m_initializedUsers = new HashSet<String>();

    public TemporaryDatabaseHsqldb() {
        this(TEST_DB_NAME_PREFIX + System.currentTimeMillis());
    }
    
    public TemporaryDatabaseHsqldb(final String testDatabase) {
        m_testDatabase = testDatabase;
    }

    /*
    public TemporaryDatabaseHsqldb(final String testDatabase, final boolean useExisting) {
        m_testDatabase = testDatabase;

        if (!useExisting || m_dataSource == null) {
            final BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
            dataSource.setUrl("jdbc:hsqldb:mem:" + m_testDatabase + ";sql.syntax_pgs=true");
            dataSource.setUsername(TemporaryDatabase.DEFAULT_ADMIN_USER);
            dataSource.setPassword(TemporaryDatabase.DEFAULT_ADMIN_PASSWORD);
            dataSource.setInitialSize(5);
            dataSource.setMaxActive(10);
            dataSource.setPoolPreparedStatements(true);
            dataSource.setMaxOpenPreparedStatements(10);
            m_dataSource = dataSource;

            m_jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        }
    }
    */
    public DataSource getDataSource() {
        return m_dataSource;
    }
    
    public void setDataSource(final DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_dataSource);
        Assert.notNull(m_jdbcTemplate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return m_dataSource.getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        if (!m_initializedUsers.contains(username)) {
            final Connection conn = m_dataSource.getConnection(TemporaryDatabase.DEFAULT_ADMIN_USER, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD);
            conn.createStatement().execute("CREATE USER '" + username + "' PASSWORD '" + password + "' ADMIN");
            m_initializedUsers.add(username);
        }
        return m_dataSource.getConnection(username, password);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Not implemented.");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return m_dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        m_dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        m_dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return m_dataSource.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return m_dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return m_dataSource.isWrapperFor(iface);
    }

    @Override
    public String getTestDatabase() {
        return m_testDatabase;
    }

    public boolean getPopulateSchema() {
        return m_populateSchema;
    }

    @Override
    public void setPopulateSchema(final boolean populate) {
        m_populateSchema = populate;
    }

    @Override
    public void create() throws TemporaryDatabaseException {
    }

    @Override
    public void drop() throws TemporaryDatabaseException {
    }

    @Override
    public int countRows(final String sql, final Object... values) {
        final RowCountCallbackHandler counter = new RowCountCallbackHandler();
        getJdbcTemplate().getJdbcOperations().query(sql, values, counter);
        return counter.getRowCount();
    }

    @Override
    public SimpleJdbcTemplate getJdbcTemplate() {
        return m_jdbcTemplate;
    }
    
    public void setJdbcTemplate(final SimpleJdbcTemplate template) {
        m_jdbcTemplate = template;
    }

}
