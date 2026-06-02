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
package org.opennms.netmgt.ha;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides JDBC connections to the HA coordinator before the main Spring
 * DataSource is initialised.
 *
 * <p>Connection parameters are read from the same system properties used by
 * the existing {@code opennms-datasources.xml} loader:
 * <ul>
 *   <li>{@code opennms.db.url} — JDBC URL (default: {@code jdbc:postgresql://localhost:5432/opennms})
 *   <li>{@code opennms.db.username} — database username (default: {@code opennms})
 *   <li>{@code opennms.db.password} — database password (default: empty)
 * </ul>
 *
 * These are always set before {@code Starter} is invoked by the OpenNMS
 * bootstrap scripts.
 */
public class DbConnectionFactory {

    private final String url;
    private final String username;
    private final String password;

    DbConnectionFactory(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DbConnectionFactory fromSystemProperties() {
        String url = System.getProperty("opennms.db.url", "jdbc:postgresql://localhost:5432/opennms");
        String username = System.getProperty("opennms.db.username", "opennms");
        String password = System.getProperty("opennms.db.password", "");
        return new DbConnectionFactory(url, username, password);
    }

    public Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        // Short connect timeout so a DB outage doesn't stall the monitor loop indefinitely
        props.setProperty("loginTimeout", "5");
        props.setProperty("connectTimeout", "5");
        return DriverManager.getConnection(url, props);
    }
}
