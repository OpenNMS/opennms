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
package org.opennms.netmgt.flows.postgres;

import javax.sql.DataSource;

import org.opennms.core.db.ClosableDataSource;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.db.HikariCPConnectionFactory;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplies the {@link DataSource} used by the Postgres flow repository and query service, in one of two
 * modes selected by whether {@code url} is configured:
 *
 * <ul>
 *   <li><b>Internal</b> (default — {@code url} blank): reuse the OpenNMS internal datasource via
 *       {@link DataSourceFactory}. Zero configuration; flows are written to the internal OpenNMS database.
 *       Ideal for small-scale / proof-of-concept Horizon deployments (which would not run Sentinel anyway).</li>
 *   <li><b>Dedicated</b> ({@code url} set): build a self-managed HikariCP pool against an external/dedicated
 *       flow database. Required on Sentinel (no {@code opennms-datasources.xml} there) and recommended for
 *       scale. This provider owns the pool and closes it on shutdown.</li>
 * </ul>
 *
 * The internal datasource is owned by OpenNMS and is never closed here; only a pool this provider created
 * is closed.
 */
public class FlowDataSourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDataSourceProvider.class);

    private String internalDataSourceName = "opennms";
    private String url;
    private String databaseName = "opennms";
    private String username = "postgres";
    private String password = "";
    private String driverClass = "org.postgresql.Driver";
    private int idleTimeout = 600;
    private int loginTimeout = 3;
    private int minPool = 10;
    private int maxPool = 25;
    private int maxSize = 25;

    private DataSource dataSource;
    /** Non-null only in dedicated mode; the pool this provider created and must close. */
    private ClosableDataSource ownedPool;

    public void init() {
        try {
            this.dataSource = createDataSource();
        } catch (final Exception e) {
            // Never fail the blueprint container over a missing/invalid flow datasource: load cleanly,
            // log, and stay inert (getDataSource() == null) so the repository and query service disable
            // themselves. With datasource.url blank the internal OpenNMS datasource is used, which only
            // exists on Horizon; on Sentinel or for an external database, datasource.url (+ credentials)
            // must be set on the org.opennms.features.flows.persistence.postgres pid.
            this.dataSource = null;
            LOG.error("PostgreSQL flow persistence is DISABLED: could not obtain a flow DataSource ({}). "
                    + "Set datasource.url/username/password on the org.opennms.features.flows.persistence.postgres "
                    + "pid (required on Sentinel and for external databases); with it blank the internal OpenNMS "
                    + "datasource is used, which is only available on Horizon.", e.toString());
        }
    }

    private DataSource createDataSource() throws Exception {
        if (url == null || url.trim().isEmpty()) {
            DataSourceFactory.init(internalDataSourceName);
            LOG.info("PostgreSQL flow persistence using the internal '{}' datasource (no dedicated flow database "
                    + "configured; set datasource.url on org.opennms.features.flows.persistence.postgres to use one).",
                    internalDataSourceName);
            return DataSourceFactory.getInstance(internalDataSourceName);
        }
        final JdbcDataSource cfg = new JdbcDataSource();
        cfg.setName("opennms-flows");
        cfg.setDatabaseName(databaseName);
        cfg.setClassName(driverClass);
        cfg.setUrl(url);
        cfg.setUserName(username);
        cfg.setPassword(password);
        final HikariCPConnectionFactory pool = new HikariCPConnectionFactory(cfg);
        this.ownedPool = pool; // record early so close() can reclaim it even if a setter below fails
        pool.setIdleTimeout(idleTimeout);
        pool.setLoginTimeout(loginTimeout);
        pool.setMinPool(minPool);
        pool.setMaxPool(maxPool);
        pool.setMaxSize(maxSize);
        LOG.info("PostgreSQL flow persistence using a dedicated connection pool for {}.", url);
        return pool;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (ownedPool != null) {
            ownedPool.close();
        }
    }

    // --- config setters (blueprint) ---
    public void setInternalDataSourceName(final String internalDataSourceName) { this.internalDataSourceName = internalDataSourceName; }
    public void setUrl(final String url) { this.url = url; }
    public void setDatabaseName(final String databaseName) { this.databaseName = databaseName; }
    public void setUsername(final String username) { this.username = username; }
    public void setPassword(final String password) { this.password = password; }
    public void setDriverClass(final String driverClass) { this.driverClass = driverClass; }
    public void setIdleTimeout(final int idleTimeout) { this.idleTimeout = idleTimeout; }
    public void setLoginTimeout(final int loginTimeout) { this.loginTimeout = loginTimeout; }
    public void setMinPool(final int minPool) { this.minPool = minPool; }
    public void setMaxPool(final int maxPool) { this.maxPool = maxPool; }
    public void setMaxSize(final int maxSize) { this.maxSize = maxSize; }
}
