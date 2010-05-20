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
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;
import org.opennms.netmgt.dao.castor.CastorUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0ConnectionFactory implements ClosableDataSource {

    /*    private static final int DEFAULT_AQUIRE_INCREMENT = 5;
    private static final int DEFAULT_RETRY_ATTEMPTS = 1;
    private static final int DEFAULT_RETRY_DELAY = 0;  //TODO: need to learn this one
    private static final boolean DEFAULT_AUTOCOMMIT_ON_CLOSE = false;
    private static final boolean DEFAULT_BREAK_AFTER_ACQUIRE_FAILURE = false;
    private static final int DEFAULT_CHECKOUT_TIMEOUT = 0;
    private static final String DEFAULT_TESTER_CLASS_NAME = null;
    private static final String DEFAULT_POOL_DESCRIPTION = "OpenNMS C3P0 Connection Pool";

    private static final boolean DEFAULT_FORCE_IGNORE_UNRESOLVED_TRANSACTION = false;
    private static final String DEFAULT_IDENTITY_TOKEN = null;
    private static final int DEFAULT_IDLE_CONNECTION_TEST_PERIOD = 1800;
    private static final int DEFAULT_INITIAL_POOL_SIZE = 25;
    private static final int DEFAULT_LOGIN_TIMEOUT = 3000;
    private static final PrintWriter DEFAULT_LOG_WRITER = null;
    private static final int DEFAUT_MAX_IDLE_TIME = 1800;
    private static final int DEFAULT_MAX_POOL_SIZE = 256;
    private static final int DEFAULT_MAX_STATEMENTS = 0;
    private static final int DEFAULT_MAX_STATEMENTS_PER_CONNECTION = 0;
    private static final int DEFAULT_MIN_POOL_SIZE = 10;
    private static final int DEFAULT_NUM_HELPER_THREADS = 0;
    private static final String DEFAULT_PERFERRED_TEST_QUERY = null;
    private static final int DEFAULT_PROPERTY_CYCLE = 0;
    private static final boolean DEFAULT_SET_TEST_CONNECTION_ON_CHECKIN = false;
    private static final boolean DEFAULT_SET_TEST_CONNECTION_ON_CHECKOUT = false;
    private static final boolean DEFAULT_USES_TRADITIONAL_REFLECTIVE_PROXIES = false;
     */    
    private ComboPooledDataSource m_pool;

    protected C3P0ConnectionFactory(InputStream stream, String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
        log().info("C3P0ConnectionFactory: setting up data sources from input stream.");
        JdbcDataSource ds = marshalDataSourceFromConfig(stream, dsName);
        initializePool(ds);
    }

    protected C3P0ConnectionFactory(Reader rdr, String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
        log().info("C3P0ConnectionFactory: setting up data sources from reader argument.");
        JdbcDataSource ds = marshalDataSourceFromConfig(rdr, dsName);
        initializePool(ds);
    }

    protected C3P0ConnectionFactory(String configFile, String dsName) throws IOException, MarshalException, ValidationException, PropertyVetoException, SQLException {
        /*
         * Set the system identifier for the source of the input stream.
         * This is necessary so that any location information can
         * positively identify the source of the error.
         */
        FileInputStream fileInputStream = new FileInputStream(configFile);
        log().info("C3P0ConnectionFactory: setting up data sources from:"+configFile);
        try {
            JdbcDataSource ds = marshalDataSourceFromConfig(fileInputStream, dsName);
            initializePool(ds);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    public static JdbcDataSource marshalDataSourceFromConfig(final InputStream stream, String dsName) throws MarshalException, ValidationException {
        DataSourceConfiguration dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, stream);
        return validateDataSourceConfiguration(dsName, dsc);
    }

    @SuppressWarnings("deprecation")
    public static JdbcDataSource marshalDataSourceFromConfig(final Reader rdr, String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
        DataSourceConfiguration dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, rdr);
        return validateDataSourceConfiguration(dsName, dsc);
    }

    private static JdbcDataSource validateDataSourceConfiguration(String dsName, DataSourceConfiguration dsc) {
        for (JdbcDataSource jdbcDs : dsc.getJdbcDataSourceCollection()) {
            if (jdbcDs.getName().equals(dsName)) {
                return jdbcDs;
            }
        }
        
        throw new IllegalArgumentException("C3P0ConnectionFactory: DataSource: "+dsName+" is not defined.");
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    private void initializePool(JdbcDataSource ds) throws PropertyVetoException, SQLException {
        m_pool = new ComboPooledDataSource();
        m_pool.setPassword(ds.getPassword());
        m_pool.setUser(ds.getUserName());
        m_pool.setJdbcUrl(ds.getUrl());
        m_pool.setDriverClass(ds.getClassName());

        Properties props = new Properties();
        for (Param p : ds.getParamCollection()) {
            props.put(p.getName(), p.getValue());
        }
        if (!props.isEmpty()) {
            m_pool.setProperties(props);
        }
    }

    public Connection getConnection() throws SQLException {
        return m_pool.getConnection();
    }

    public ComboPooledDataSource getPool() {
        return m_pool;
    }

    public void setPool(ComboPooledDataSource pool) {
        m_pool = pool;
    }

    public String getUrl() {
        return m_pool.getJdbcUrl();
    }

    public void setUrl(String url) {
        m_pool.setJdbcUrl(url);
    }

    public String getUser() {
        return m_pool.getUser();
    }

    public void setUser(String user) {
        m_pool.setUser(user);
    }

    public DataSource getDataSource() {
        return m_pool;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return m_pool.getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return m_pool.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        m_pool.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        m_pool.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return m_pool.getLoginTimeout();
    }

    public void close() throws SQLException {
        log().info("Closing c3p0 pool");
        m_pool.close();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //TODO
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //TODO
    }
}
