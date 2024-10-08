/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.test.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XAConnection;

import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.util.Assert;

public class TemporaryDatabaseHsqldb implements TemporaryDatabase, InitializingBean {
    public static final String TEST_DB_NAME_PREFIX = "opennms_test_";

    private static DataSource m_dataSource = null;
    private String m_testDatabase;
    private boolean m_populateSchema = false;
    private JdbcTemplate m_jdbcTemplate;
    private Set<String> m_initializedUsers = new HashSet<>();

    @SuppressWarnings("unused")
    private JUnitTemporaryDatabase m_jtd;

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
        getJdbcTemplate().query(sql, values, counter);
        return counter.getRowCount();
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return m_jdbcTemplate;
    }
    
    public void setJdbcTemplate(final JdbcTemplate template) {
        m_jdbcTemplate = template;
    }

    /**
     * TODO Implement these functions to use the XADataSource implementation 
     * from HSQLDB
     */
    @Override
    public XAConnection getXAConnection() throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * TODO Implement these functions to use the XADataSource implementation 
     * from HSQLDB
     */
    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClassName(String string) {
        // Do nothing; doesn't support blame yet
    }

    @Override
    public void setMethodName(String string) {
        // Do nothing; doesn't support blame yet
    }

    @Override
    public void setTestDetails(String string) {
        // Do nothing; doesn't support blame yet
    }

    @Override
    public void setPlpgsqlIplike(final boolean iplike) {
        // Do nothing, hsqldb doesn't have iplike?
    }
}
