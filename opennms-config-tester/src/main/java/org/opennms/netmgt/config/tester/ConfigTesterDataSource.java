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

package org.opennms.netmgt.config.tester;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

class ConfigTesterDataSource implements DataSource {

        @Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

        @Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

        @Override
	public void setLogWriter(PrintWriter arg0) throws SQLException {
	}

        @Override
	public void setLoginTimeout(int arg0) throws SQLException {
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("getParentLogger not supported");
	}

        @Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

        @Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}

        @Override
	public Connection getConnection() throws SQLException {
            return null;
	}

        @Override
	public Connection getConnection(String arg0, String arg1)
			throws SQLException {
            return null;
	}
}