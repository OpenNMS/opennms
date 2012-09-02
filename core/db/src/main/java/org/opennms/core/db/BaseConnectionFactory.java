/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

/**
 */
public abstract class BaseConnectionFactory implements ClosableDataSource {

    /**
     * @param stream A configuration file as an {@link InputStream}.
     * @param dsName The data source's name.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.sql.SQLException if any.
     */
    protected BaseConnectionFactory(final InputStream stream, final String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
        LogUtils.infof(this, "Setting up data source %s from input stream.", dsName);
        final JdbcDataSource ds = ConnectionFactoryUtil.marshalDataSourceFromConfig(stream, dsName);
        initializePool(ds);
    }

    /**
     * @param configFile A configuration file name.
     * @param dsName The data source's name.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.sql.SQLException if any.
     */
    protected BaseConnectionFactory(final String configFile, final String dsName) throws IOException, MarshalException, ValidationException, PropertyVetoException, SQLException {
        /*
         * Set the system identifier for the source of the input stream.
         * This is necessary so that any location information can
         * positively identify the source of the error.
         */
    	final FileInputStream fileInputStream = new FileInputStream(configFile);
        LogUtils.infof(this, "Setting up data sources from %s.", configFile);
        try {
        	final JdbcDataSource ds = ConnectionFactoryUtil.marshalDataSourceFromConfig(fileInputStream, dsName);
        	initializePool(ds);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
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
    public abstract int getLoginTimeout() throws SQLException;

    /**
     * <p>close</p>
     *
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void close() throws SQLException {
    }

    /**
     * <p>unwrap</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;  //TODO
    }

    /**
     * <p>isWrapperFor</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;  //TODO
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
            LogUtils.errorf(e, e, "Invalid JDBC URL specified: %s", e.getMessage());
            throw e;
        }
    }
}
