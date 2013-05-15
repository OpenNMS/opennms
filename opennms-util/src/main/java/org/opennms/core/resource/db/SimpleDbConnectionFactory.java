/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.resource.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A trivial implementation of <code>DbConnectionFactory</code> that creates a
 * new connection for every request and does no caching or connection sharing.
 *
 * <p>
 * Note that no real connection pooling is occurring within this class. This
 * factory will be an inefficient connection management scheme for most
 * real-world applications. It was designed only for development/debugging
 * purposes and for applications that are <strong>very </strong> infrequently
 * used. In those cases, caching idle database connections for long periods of
 * time can be wasteful, and this pooling scheme would be appropriate.
 * </p>
 *
 * <p>
 * This manager simply initializes the JDBC driver and then stores the database
 * credential information (if any). Then when a connection is requested, a new
 * connection is made with the stored credentials (if any).
 * </p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public class SimpleDbConnectionFactory extends Object implements DbConnectionFactory {
    protected String url = null;

    protected String username = null;

    protected String password = null;

    /**
     * {@inheritDoc}
     *
     * Initialize a new database pool with the given database username and
     * password. This method will load the JDBC driver and store the given
     * database credentials. When a connection is requested, a new connection
     * will be made using the credentials.
     */
    @Override
    public void init(String dbUrl, String dbDriver, String username, String password) throws ClassNotFoundException, SQLException {
        if (dbUrl == null || dbDriver == null || username == null || password == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Class.forName(dbDriver);

        this.url = dbUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Clear all database URL and credential information so no more connections
     * can be requested.
     */
    @Override
    public void destroy() {
        this.url = null;
        this.username = null;
        this.password = null;
    }

    /**
     * Create a new connection for the given database URL. This method will
     * check to ensure that the URL has already been registered as a valid
     * database pool. If any database credentials or properties were registered,
     * those will be used when creating the new connection.
     *
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (this.url == null) {
            throw new IllegalArgumentException("This database factory has not been initialized or has been destroyed.");
        }

        Connection connection = null;

        if (this.username != null && this.password != null) {
            connection = DriverManager.getConnection(this.url, this.username, this.password);
        } else {
            connection = DriverManager.getConnection(this.url);
        }

        return (connection);
    }

    /**
     * {@inheritDoc}
     *
     * Close the given connection.
     */
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        connection.close();
    }

}
