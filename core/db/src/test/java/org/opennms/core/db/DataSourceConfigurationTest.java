/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
