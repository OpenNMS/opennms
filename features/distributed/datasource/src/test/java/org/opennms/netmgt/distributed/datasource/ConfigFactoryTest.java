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
package org.opennms.netmgt.distributed.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.ClosableDataSource;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class ConfigFactoryTest {

    private final ConfigFactory configFactory = new ConfigFactory();
    private final JdbcDataSource jdbcDataSource = new JdbcDataSource();
    private ConnectionPool connectionPool;

    @Before
    public void setUp() {
        FakePool.created = null;
        connectionPool = new ConnectionPool();
        connectionPool.setFactory(FakePool.class.getName());
        connectionPool.setIdleTimeout("600");
        connectionPool.setLoginTimeout("3");
        connectionPool.setMinPool("2");
        connectionPool.setMaxPool("4");
        connectionPool.setMaxSize("50");
    }

    @Test
    public void shouldApplyTheSettings() throws Exception {
        final DataSource dataSource = configFactory.createDataSource(connectionPool, jdbcDataSource);

        assertSame(FakePool.created, dataSource);
        assertEquals(2, FakePool.created.minPool);
        assertEquals(4, FakePool.created.maxPool);
        assertFalse(FakePool.created.closed);
    }

    @Test
    public void shouldNotCreateAPoolForAnInvalidValue() {
        connectionPool.setMaxPool("abc");

        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> configFactory.createDataSource(connectionPool, jdbcDataSource));
        assertTrue(e.getMessage(), e.getMessage().contains("maxPool"));
        assertEquals("no pool may be opened before the settings are known", null, FakePool.created);
    }

    @Test
    public void shouldCloseThePoolWhenItRejectsASetting() {
        connectionPool.setMaxPool("0");

        assertThrows(IllegalArgumentException.class,
                () -> configFactory.createDataSource(connectionPool, jdbcDataSource));
        assertTrue("the pool must not be left open", FakePool.created.closed);
    }

    /** Stands in for HikariCPConnectionFactory, which opens connections in its constructor. */
    public static class FakePool implements ClosableDataSource {
        static FakePool created;

        int minPool;
        int maxPool;
        boolean closed;

        public FakePool(final JdbcDataSource ignored) {
            created = this;
        }

        @Override
        public void setMaxPool(final int maxPool) {
            // Same check as HikariCP's setMaximumPoolSize
            if (maxPool < 1) {
                throw new IllegalArgumentException("maxPoolSize cannot be less than 1");
            }
            this.maxPool = maxPool;
        }

        @Override
        public void setMinPool(final int minPool) {
            this.minPool = minPool;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override public void setIdleTimeout(final int idleTimeout) { }
        @Override public void setLoginTimeout(final int loginTimeout) { }
        @Override public void setMaxSize(final int maxSize) { }
        @Override public int getLoginTimeout() { return 0; }
        @Override public Connection getConnection() { throw new UnsupportedOperationException(); }
        @Override public Connection getConnection(final String username, final String password) { throw new UnsupportedOperationException(); }
        @Override public PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(final PrintWriter out) { }
        @Override public Logger getParentLogger() { return null; }
        @Override public <T> T unwrap(final Class<T> iface) { throw new UnsupportedOperationException(); }
        @Override public boolean isWrapperFor(final Class<?> iface) { return false; }
    }
}
