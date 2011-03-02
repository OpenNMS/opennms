//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 May 31: Make marshalDataSourceFromConfig public and static and inline
//              a few one-line methods that don't need to be there since
//              Castor classes are now genericized. - dj@opennms.org 
// 2007 Aug 02: Prepare for Castor 1.0.5, Java 5 generics and loops. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

/**
 * <p>C3P0ConnectionFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
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
     * @param rdr A configuration file as a {@link Reader}.
     * @param dsName The data source's name.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.sql.SQLException if any.
     */
    protected BaseConnectionFactory(final Reader rdr, final String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
    	LogUtils.infof(this, "Setting up data source %s from reader.", dsName);
    	final JdbcDataSource ds = ConnectionFactoryUtil.marshalDataSourceFromConfig(rdr, dsName);
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
     * <p>getConnection</p>
     *
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public abstract Connection getConnection() throws SQLException;

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

    /** {@inheritDoc} */
    public abstract Connection getConnection(final String username, final String password) throws SQLException;

    /**
     * <p>getLogWriter</p>
     *
     * @return a {@link java.io.PrintWriter} object.
     * @throws java.sql.SQLException if any.
     */
    public abstract PrintWriter getLogWriter() throws SQLException;

    /** {@inheritDoc} */
    public abstract void setLogWriter(PrintWriter out) throws SQLException;

    /** {@inheritDoc} */
    public abstract void setLoginTimeout(final int seconds) throws SQLException;

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
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;  //TODO
    }
}
