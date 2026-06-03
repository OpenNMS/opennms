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

import org.opennms.core.db.DataSourceConfigurationFactory;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides JDBC connections to the HA coordinator before the main Spring
 * DataSource is initialised.
 *
 * <p>Connection parameters are read from the {@code opennms} entry in
 * {@code $OPENNMS_HOME/etc/opennms-datasources.xml} via
 * {@link DataSourceConfigurationFactory}, which applies the full OpenNMS
 * metadata DSL (including {@code ${env:VAR|default}} substitution) to
 * the URL, username, and password attributes.
 *
 * <p>Connections are opened with a short timeout so that a database outage
 * does not stall the HA monitor loop indefinitely.
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

    /**
     * Builds a factory by parsing {@code $OPENNMS_HOME/etc/opennms-datasources.xml}
     * and extracting the {@code opennms} data source entry with all metadata
     * expressions resolved.
     */
    public static DbConnectionFactory fromDatasourcesXml() throws Exception {
        String opennmsHome = System.getProperty("opennms.home", ".");
        File dsFile = new File(opennmsHome, "etc/opennms-datasources.xml");

        DataSourceConfigurationFactory factory = new DataSourceConfigurationFactory(dsFile);
        JdbcDataSource ds = factory.getJdbcDataSource("opennms");
        if (ds == null) {
            throw new IllegalStateException(
                    "No 'opennms' jdbc-data-source found in " + dsFile.getAbsolutePath());
        }

        return new DbConnectionFactory(ds.getUrl(), ds.getUserName(), ds.getPassword());
    }

    public Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password != null ? password : "");
        props.setProperty("loginTimeout", "5");
        props.setProperty("connectTimeout", "5");
        return DriverManager.getConnection(url, props);
    }
}
