/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.resource.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A <code>DbConnectionFactory</code> allocates and deallocates connections
 * from a database. The concrete implementations of this interface specify a
 * particular allocation/deallocation policy.
 *
 * <p>
 * For example, an implementation might use and reuse connections from a
 * persistent pool of connections, while another might dynamically allocate and
 * deallocate connections for each request.
 * </p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public interface DbConnectionFactory {
    /**
     * Initialize a database factory with the given URL, driver classname, and
     * database credentials. Will guarantee that the JDBC driver is loaded and
     * that connections will be available.
     *
     * <p>
     * Only one <code>init</code> method should be called.
     *
     * @param dbUrl
     *            the JDBC URL used to retrieve connections
     * @param dbDriver
     *            a fully qualified class name for the JDBC driver that will
     *            handle this JDBC URL
     * @param username
     *            the name to use to authenticate us with the database
     * @param password
     *            the credentials use to authenticate the username
     * @throws java.lang.ClassNotFoundException if any.
     */
    public void init(String dbUrl, String dbDriver, String username, String password) throws ClassNotFoundException;

    /**
     * Deallocate all the resources that may have been allocated to this
     * database connection factory. Makes this factory unavailable for new
     * connection requests.
     *
     */
    public void destroy();

    /**
     * Retrieve a connection from the given database connection pool.
     *
     * @throws java.lang.IllegalStateException
     *             If the factory has not been initialized or has been
     *             destroyed.
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public Connection getConnection() throws SQLException;

    /**
     * Replace a database connection back in the pool of available connections
     * for its parent pool.
     *
     * @param connection
     *            the connection to release
     * @throws java.lang.IllegalStateException
     *             If the factory has not been initialized or has been
     *             destroyed.
     * @throws java.sql.SQLException if any.
     */
    public void releaseConnection(Connection connection) throws SQLException;

}
