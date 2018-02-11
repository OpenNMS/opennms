/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class BaseConnectionFactory implements ClosableDataSource {
	
	private static final Logger LOG = LoggerFactory.getLogger(BaseConnectionFactory.class);

    /**
     * @param stream A configuration file as an {@link InputStream}.
     * @param dsName The data source's name.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.sql.SQLException if any.
     */
    protected BaseConnectionFactory(final JdbcDataSource ds) throws SQLException {
        initializePool(ds);
    }

    protected abstract void initializePool(final JdbcDataSource ds) throws SQLException;

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getUrl();

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public abstract void setUrl(final String url);

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getUser();

    /**
     * <p>setUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public abstract void setUser(final String user);

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public abstract DataSource getDataSource();

    /**
     * <p>getLoginTimeout</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public abstract int getLoginTimeout() throws SQLException;

    /**
     * <p>close</p>
     */
    @Override
    public void close() {
    }

    /**
     * <p>unwrap</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;  //TODO
    }

    /**
     * <p>isWrapperFor</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @return a boolean.
     */
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false; //TODO
    }

    protected static void validateJdbcUrl(String url) {
        try {
            if (url == null) {
                throw new IllegalArgumentException("Null JDBC URL");
            } else if (url.length() == 0) {
                throw new IllegalArgumentException("Blank JDBC URL");
            } else if (url.matches("\\$\\{.*\\}")) {
                throw new IllegalArgumentException("JDBC URL cannot contain replacement tokens");
            }
        } catch (IllegalArgumentException e) {
        	LOG.error("Invalid JDBC URL specified: {}", e.getMessage(), e);
            throw e;
        }
    }
}
