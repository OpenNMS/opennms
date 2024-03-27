/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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