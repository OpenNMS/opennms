/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.distributed.datasource;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.core.db.ClosableDataSource;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.api.DatabaseSchemaConfig;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class ConfigFactory {

    private static DatabaseSchemaConfigFactory factory;

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

    public DatabaseSchemaConfig createDatabaseSchemaConfig() throws IOException {
        if (factory == null) {
            factory = new DatabaseSchemaConfigFactory();
            factory.init();
        }
        return factory.getInstance();
    }

}