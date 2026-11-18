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

import java.lang.reflect.Constructor;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.core.db.ClosableDataSource;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class ConfigFactory {

    // Same values as the cm:default-properties in blueprint.xml. A key left blank in the
    // .cfg file overrides those defaults with an empty string, so apply them again here
    // rather than fall back to ConnectionPool's defaults, which are sized for Core.
    static final int DEFAULT_IDLE_TIMEOUT = 600;
    static final int DEFAULT_LOGIN_TIMEOUT = 3;
    static final int DEFAULT_MIN_POOL = 50;
    static final int DEFAULT_MAX_POOL = 50;
    static final int DEFAULT_MAX_SIZE = 50;

    public DataSource createDataSource(ConnectionPool connectionPool, JdbcDataSource jdbcDataSource) throws Exception {
        Objects.requireNonNull(connectionPool);
        Objects.requireNonNull(jdbcDataSource);

        // Resolve every setting before the pool exists: creating it opens connections,
        // and nothing would close them if an invalid value failed afterwards.
        final int idleTimeout = connectionPool.getIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        final int loginTimeout = connectionPool.getLoginTimeout(DEFAULT_LOGIN_TIMEOUT);
        final int minPool = connectionPool.getMinPool(DEFAULT_MIN_POOL);
        final int maxPool = connectionPool.getMaxPool(DEFAULT_MAX_POOL);
        final int maxSize = connectionPool.getMaxSize(DEFAULT_MAX_SIZE);

        final String connectionPoolFactoryClassName = connectionPool.getFactory();
        final Class<?> clazz = Class.forName(connectionPoolFactoryClassName);
        final Constructor<?> constructor = clazz.getConstructor(new Class<?>[] { JdbcDataSource.class });
        final ClosableDataSource dataSource = (ClosableDataSource)constructor.newInstance(new Object[] { jdbcDataSource });
        try {
            dataSource.setIdleTimeout(idleTimeout);
            dataSource.setLoginTimeout(loginTimeout);
            dataSource.setMinPool(minPool);
            dataSource.setMaxPool(maxPool);
            dataSource.setMaxSize(maxSize);
        } catch (final Exception | Error e) {
            // The pool rejected a value (e.g. HikariCP refuses a maxPool below 1); don't leave it running
            try {
                dataSource.close();
            } catch (final Exception closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
        return dataSource;
    }
}
