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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

import javax.sql.DataSource;

import org.exolab.castor.jdo.conf.Database;
import org.exolab.castor.jdo.conf.Param;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0ConnectionFactory implements DbConnectionFactory {
    
    /*
     * Just hacked for now to get model-import working in OpenNMS
     * TODO: fix this with a properties file or such
     */
    private static final int DEFAULT_AQUIRE_INCREMENT = 5;
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
    
    private ComboPooledDataSource m_pool;
    private String m_user;
    private String m_password;
    private String m_url;
    private String m_className;
    private Database m_database;

    public C3P0ConnectionFactory(String configFile) throws IOException, MarshalException, ValidationException, PropertyVetoException, SQLException {
        Class dsc = Database.class;

        // Set the system identifier for the source of the input stream.
        // This is necessary so that any location information can
        // positively identify the source of the error.
        //
        FileInputStream fileInputStream = new FileInputStream(configFile);
		try {
			InputSource dbIn = new InputSource(fileInputStream);
			dbIn.setSystemId(configFile);

			m_database = (Database) Unmarshaller.unmarshal(dsc, dbIn);
			Param[] params = m_database.getDatabaseChoice().getDriver().getParam();
			for (Iterator it = Arrays.asList(params).iterator(); it.hasNext();) {
			    Param param = (Param) it.next();
			    if (param.getName().equals("user")) {
			        m_user = param.getValue();
			    } else if (param.getName().equals("password")) {
			        m_password = param.getValue();
			    }
			}

			initializePool();
		} finally {
			fileInputStream.close();
		}
        
    }

    private void initializePool() throws PropertyVetoException, SQLException {
        m_pool = new ComboPooledDataSource();
        m_pool.setPassword(m_password);
        m_pool.setUser(m_user);
        m_url = m_database.getDatabaseChoice().getDriver().getUrl();
        m_pool.setJdbcUrl(m_url);
        m_className = m_database.getDatabaseChoice().getDriver().getClassName();
        m_pool.setDriverClass(m_className);
        
        //defaults
//        m_pool.setAcquireIncrement(DEFAULT_AQUIRE_INCREMENT);
//        m_pool.setAcquireRetryAttempts(DEFAULT_RETRY_ATTEMPTS);
//        m_pool.setAcquireRetryDelay(DEFAULT_RETRY_DELAY);
//        m_pool.setAutoCommitOnClose(DEFAULT_AUTOCOMMIT_ON_CLOSE);
//        m_pool.setBreakAfterAcquireFailure(DEFAULT_BREAK_AFTER_ACQUIRE_FAILURE);
//        m_pool.setCheckoutTimeout(DEFAULT_CHECKOUT_TIMEOUT);
//        m_pool.setConnectionTesterClassName(DEFAULT_TESTER_CLASS_NAME);
//        m_pool.setDescription(DEFAULT_POOL_DESCRIPTION);
//        m_pool.setForceIgnoreUnresolvedTransactions(DEFAULT_FORCE_IGNORE_UNRESOLVED_TRANSACTION);
//        m_pool.setIdentityToken(DEFAULT_IDENTITY_TOKEN);
//        m_pool.setIdleConnectionTestPeriod(DEFAULT_IDLE_CONNECTION_TEST_PERIOD);
//        m_pool.setInitialPoolSize(DEFAULT_INITIAL_POOL_SIZE);
//        m_pool.setLoginTimeout(DEFAULT_LOGIN_TIMEOUT);
//        m_pool.setLogWriter(DEFAULT_LOG_WRITER);
//        m_pool.setMaxIdleTime(DEFAUT_MAX_IDLE_TIME);
//        m_pool.setMaxPoolSize(DEFAULT_MAX_POOL_SIZE);
//        m_pool.setMaxStatements(DEFAULT_MAX_STATEMENTS);
//        m_pool.setMaxStatementsPerConnection(DEFAULT_MAX_STATEMENTS_PER_CONNECTION);
//        m_pool.setMinPoolSize(DEFAULT_MIN_POOL_SIZE);
//        m_pool.setNumHelperThreads(DEFAULT_NUM_HELPER_THREADS);
//        m_pool.setPreferredTestQuery(DEFAULT_PERFERRED_TEST_QUERY);
//        m_pool.setPropertyCycle(DEFAULT_PROPERTY_CYCLE);
//        m_pool.setTestConnectionOnCheckin(DEFAULT_SET_TEST_CONNECTION_ON_CHECKIN);
//        m_pool.setTestConnectionOnCheckout(DEFAULT_SET_TEST_CONNECTION_ON_CHECKOUT);
//        m_pool.setUsesTraditionalReflectiveProxies(DEFAULT_USES_TRADITIONAL_REFLECTIVE_PROXIES);
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
        return m_url;
    }

    public void setUrl(String url) {
        m_url = url;
    }

    public String getUser() {
        return m_user;
    }

    public void setUser(String user) {
        m_user = user;
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

}
