/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

/**
 * 
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
public class HikariCPConnectionFactoryIT {
    
    @Test
    public void testMarshalDataSourceFromConfig() throws Exception {
        HikariCPConnectionFactory factory1 = null;
        HikariCPConnectionFactory factory2 = null;

        try {
            factory1 = makeFactory("opennms");
            factory2 = makeFactory("opennms2");

            Connection conn = null;
            Statement s = null;
            try {
                conn = factory2.getConnection();
                s = conn.createStatement();
                s.execute("select * from pg_proc");
            } finally {
                if (s != null) {
                    s.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
        } finally {
            Throwable t1 = null;
            Throwable t2 = null;

            if (factory1 != null) {
                try {
                    factory1.close();
                    factory1 = null;
                } catch (Throwable e1) {
                    t1 = e1;
                }
            }

            if (factory2 != null) {
                try {
                    factory2.close();
                    factory2 = null;
                } catch (Throwable e2) {
                    t2 = e2;
                }
            }

            if (t1 != null || t2 != null) {
                final StringBuilder message = new StringBuilder();
                message.append("Could not successfully close both C3P0 factories.  Future tests might fail.");

                Throwable choice;
                if (t1 != null) {
                    message.append("  First factory failed with: " + t1.getMessage() + "; see stack back trace.");
                    choice = t1;

                    if (t2 != null) {
                        System.err.println("  Both factories failed to close.  See stderr for second stack back trace.");
                        t2.printStackTrace(System.err);
                    }
                } else {
                    choice = t2;
                }
                AssertionError e = new AssertionError(message.toString());
                e.initCause(choice);
                throw e;
            }
        }
    }

    private HikariCPConnectionFactory makeFactory(String database) throws PropertyVetoException, SQLException, IOException {
        final DataSourceConfiguration config = new DataSourceConfiguration();

        final JdbcDataSource opennms = new JdbcDataSource();
        opennms.setName("opennms");
        opennms.setClassName("org.postgresql.Driver");
        opennms.setUserName("opennms");
        opennms.setPassword("opennms");
        opennms.setUrl("jdbc:postgresql://localhost:5432/template1");
        config.addJdbcDataSource(opennms);

        final JdbcDataSource opennms2 = new JdbcDataSource();
        opennms2.setName("opennms2");
        opennms2.setClassName("org.postgresql.Driver");
        opennms2.setUserName("opennms");
        opennms2.setPassword("opennms");
        opennms2.setUrl("jdbc:postgresql://localhost:5432/template1");
        config.addJdbcDataSource(opennms2);

        final String mockDbUrl = System.getProperty("mock.db.url");
        if (mockDbUrl != null && !"".equals(mockDbUrl.trim())) {
            opennms.setUrl(mockDbUrl + "template1");
            opennms2.setUrl(mockDbUrl + "template1");
        }

        final String mockDbAdminUser = System.getProperty("mock.db.adminUser");
        if (mockDbAdminUser != null && !"".equals(mockDbAdminUser.trim())) {
            opennms.setUserName(mockDbAdminUser);
            opennms2.setUserName(mockDbAdminUser);
        }

        final String mockDbAdminPassword = System.getProperty("mock.db.adminPassword");
        if (mockDbAdminPassword != null) {
            opennms.setPassword(mockDbAdminPassword);
            opennms2.setPassword(mockDbAdminPassword);
        }

        final StringWriter sw = new StringWriter();
        JaxbUtils.marshal(config, sw);
        final String configString = sw.toString();

        InputStream stream = new ByteArrayInputStream(configString.getBytes());
        final DataSourceConfigurationFactory factory = new DataSourceConfigurationFactory(stream);
        try {
            return new HikariCPConnectionFactory(factory.getJdbcDataSource(database));
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
