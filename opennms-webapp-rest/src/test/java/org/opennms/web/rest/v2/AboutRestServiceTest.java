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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.resource.Vault;
import org.opennms.web.rest.v2.AboutRestService.AboutDTO;

public class AboutRestServiceTest {

    private static final String[] VAULT_KEYS = { "version.display", "opennms.product", "opennms.docs.version" };

    @Before
    public void setUp() {
        Vault.getProperties().setProperty("version.display", "37.0.0-SNAPSHOT");
        Vault.getProperties().setProperty("opennms.product", "horizon");
        Vault.getProperties().setProperty("opennms.docs.version", "latest");
    }

    @After
    public void tearDown() {
        for (final String key : VAULT_KEYS) {
            Vault.getProperties().remove(key);
        }
    }

    @Test
    public void reportsVersionRuntimeAndDatabaseDetails() throws SQLException {
        final DatabaseMetaData meta = mock(DatabaseMetaData.class);
        when(meta.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(meta.getDatabaseProductVersion()).thenReturn("16.4");
        final Connection conn = mock(Connection.class);
        when(conn.getMetaData()).thenReturn(meta);
        final DataSource ds = mock(DataSource.class);
        when(ds.getConnection()).thenReturn(conn);

        final ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getServerInfo()).thenReturn("jetty/9.4.58");
        when(servletContext.getMajorVersion()).thenReturn(3);
        when(servletContext.getMinorVersion()).thenReturn(1);

        final AboutRestService service = new AboutRestService();
        service.setDataSource(() -> ds);
        final AboutDTO about = service.getAbout(servletContext);

        assertEquals("37.0.0-SNAPSHOT", about.getDisplayVersion());
        assertEquals("horizon", about.getProduct());
        assertEquals("latest", about.getDocsVersion());
        assertEquals("PostgreSQL", about.getDatabaseType());
        assertEquals("16.4", about.getDatabaseVersion());
        assertEquals("jetty/9.4.58 (Servlet Spec 3.1)", about.getServletContainer());
        assertEquals(System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")", about.getJavaVersion());
        assertNotNull(about.getServerTime());
        assertNotNull(about.getTimeSeriesStrategy());
        assertTrue(about.getIpcStrategy().startsWith("Sink: "));
    }

    @Test
    public void reportsUnknownDatabaseWhenTheConnectionFails() throws SQLException {
        final DataSource ds = mock(DataSource.class);
        when(ds.getConnection()).thenThrow(new SQLException("connection refused"));
        final AboutRestService service = new AboutRestService();
        service.setDataSource(() -> ds);
        final AboutDTO about = service.getAbout(null);

        assertEquals("Unknown", about.getDatabaseType());
        assertEquals("Unknown", about.getDatabaseVersion());
        assertEquals(null, about.getServletContainer());
        assertEquals("37.0.0-SNAPSHOT", about.getDisplayVersion());
    }
}
