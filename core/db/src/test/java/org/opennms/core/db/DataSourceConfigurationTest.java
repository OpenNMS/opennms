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
package org.opennms.core.db;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;

public class DataSourceConfigurationTest extends XmlTestNoCastor<DataSourceConfiguration> {

    public DataSourceConfigurationTest(DataSourceConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/opennms-datasources.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        DataSourceConfiguration config = new DataSourceConfiguration();

        ConnectionPool connectionPool = new ConnectionPool();
        connectionPool.setFactory("org.opennms.core.db.HikariCPConnectionFactory");
        connectionPool.setIdleTimeout(600);
        connectionPool.setLoginTimeout(3);
        connectionPool.setMinPool(50);
        connectionPool.setMaxPool(50);
        connectionPool.setMaxSize(50);
        config.setConnectionPool(connectionPool);

        JdbcDataSource opennmsDs = new JdbcDataSource();
        opennmsDs.setName("opennms");
        opennmsDs.setClassName("org.postgresql.Driver");
        opennmsDs.setUrl(System.getProperty("mock.db.url", "jdbc:postgresql://localhost:5432/") + "template1");
        opennmsDs.setUserName("opennms");
        opennmsDs.setPassword("opennms");
        config.addJdbcDataSource(opennmsDs);

        JdbcDataSource opennmsDeuceDs = new JdbcDataSource();
        opennmsDeuceDs.setName("opennms2");
        opennmsDeuceDs.setClassName("org.postgresql.Driver");
        opennmsDeuceDs.setUrl(System.getProperty("mock.db.url", "jdbc:postgresql://localhost:5432/") + "template1");
        opennmsDeuceDs.addParam(new Param("user", "opennms"));
        opennmsDeuceDs.addParam(new Param("password", "opennms"));
        config.addJdbcDataSource(opennmsDeuceDs);

        return Arrays.asList(new Object[][] {
                {
                    config,
                    new File("src/test/resources/org/opennms/core/db/opennms-datasources.xml")
                }
        });
    }

}
