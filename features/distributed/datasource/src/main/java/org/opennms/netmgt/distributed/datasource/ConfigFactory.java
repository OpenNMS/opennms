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

    public DataSource createDataSource(ConnectionPool connectionPool, JdbcDataSource jdbcDataSource) throws Exception {
        Objects.requireNonNull(connectionPool);
        Objects.requireNonNull(jdbcDataSource);

        final String connectionPoolFactoryClassName = connectionPool.getFactory();
        final Class<?> clazz = Class.forName(connectionPoolFactoryClassName);
        final Constructor<?> constructor = clazz.getConstructor(new Class<?>[] { JdbcDataSource.class });
        final ClosableDataSource dataSource = (ClosableDataSource)constructor.newInstance(new Object[] { jdbcDataSource });
        dataSource.setIdleTimeout(connectionPool.getIdleTimeout());
        dataSource.setLoginTimeout(connectionPool.getLoginTimeout());
        dataSource.setMinPool(connectionPool.getMinPool());
        dataSource.setMaxPool(connectionPool.getMaxPool());
        dataSource.setMaxSize(connectionPool.getMaxSize());
        return dataSource;
    }
}