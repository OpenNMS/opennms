/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * <p>DataSourceDbConnectionFactory class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DataSourceDbConnectionFactory implements DbConnectionFactory {
    private DataSource m_dataSource;

    /**
     * <p>Constructor for DataSourceDbConnectionFactory.</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public DataSourceDbConnectionFactory(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
    /**
     * <p>destroy</p>
     *
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void destroy() throws SQLException {
    }

    /**
     * <p>getConnection</p>
     *
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public Connection getConnection() throws SQLException {
        return m_dataSource.getConnection();
    }

    /** {@inheritDoc} */
    @Override
    public void init(String dbUrl, String dbDriver, String username, String password) throws ClassNotFoundException, SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        connection.close();
    }
}
