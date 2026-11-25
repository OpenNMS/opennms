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
package org.opennms.core.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class DataSourceFactoryTest {

    private static final String DS_NAME = "nms20620";

    @Before
    public void setUp() {
        FakePool.created = null;
    }

    @After
    public void tearDown() {
        DataSourceFactory.setDataSourceConfigurationFactory(null);
    }

    @Test
    public void shouldApplyTheSettings() {
        useConfig("2", "4");

        DataSourceFactory.init(DS_NAME);

        assertEquals(FakePool.created, DataSourceFactory.getInstance(DS_NAME));
        assertEquals(2, FakePool.created.minPool);
        assertEquals(4, FakePool.created.maxPool);
        assertFalse(FakePool.created.closed);
    }

    @Test
    public void shouldNotCreateAPoolForAnInvalidValue() {
        useConfig("2", "abc");

        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> DataSourceFactory.init(DS_NAME));
        assertTrue(e.getMessage(), e.getMessage().contains("maxPool"));
        assertNull("no pool may be opened before the settings are known", FakePool.created);
    }

    @Test
    public void shouldCloseThePoolWhenItRejectsASetting() {
        useConfig("2", "0");

        assertThrows(IllegalArgumentException.class, () -> DataSourceFactory.init(DS_NAME));
        assertTrue("the pool must not be left open", FakePool.created.closed);
    }

    private static void useConfig(final String minPool, final String maxPool) {
        final String xml = "<datasource-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/opennms-datasources\">\n"
                + "  <connection-pool factory=\"" + FakePool.class.getName() + "\" idleTimeout=\"600\" loginTimeout=\"3\""
                + " minPool=\"" + minPool + "\" maxPool=\"" + maxPool + "\" maxSize=\"50\" />\n"
                + "  <jdbc-data-source name=\"" + DS_NAME + "\" class-name=\"org.postgresql.Driver\""
                + " url=\"jdbc:postgresql://localhost:5432/opennms\" user-name=\"opennms\" password=\"opennms\" />\n"
                + "</datasource-configuration>\n";
        DataSourceFactory.setDataSourceConfigurationFactory(
                new DataSourceConfigurationFactory(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
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
