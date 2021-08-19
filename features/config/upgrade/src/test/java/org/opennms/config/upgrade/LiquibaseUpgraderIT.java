/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.config.upgrade;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.utils.DBUtils;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import liquibase.exception.LiquibaseException;

public class LiquibaseUpgraderIT {

    private final static Logger LOG = LoggerFactory.getLogger(LiquibaseUpgraderIT.class);

    public static GenericContainer<?> container;

    private static DataSource dataSource;

    @BeforeClass
    public static void setUpContainer() throws SQLException, LiquibaseException {
        container = new GenericContainer<>("postgres:11.4")
                .withExposedPorts(5432)
                .withEnv("POSTGRES_PASSWORD", "password")
                .withEnv("TIMESCALEDB_TELEMETRY", "off")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
                .withLogConsumer(new Slf4jLogConsumer(LOG));
        container.start();
        dataSource = createDatasource();
    }

    @AfterClass
    public static void tearDown() {
        container.stop();
    }

    private static DataSource createDatasource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://localhost:%s/", container.getFirstMappedPort()));
        config.setUsername("postgres");
        config.setPassword("password");
        return new HikariDataSource(config);
    }

    @Test
    public void shouldRunChangelog() throws LiquibaseException, IOException, ClassNotFoundException, SQLException {
        ConfigurationManagerService cm = Mockito.mock(ConfigurationManagerService.class);
        LiquibaseUpgrader liqui = new LiquibaseUpgrader(cm);
        liqui.runChangelog("/org/opennms/config/upgrade/LiquibaseUpgraderIT-changelog.xml", dataSource.getConnection());

        // check if CM was called
        verify(cm).registerSchema(anyString(),eq(1), eq(0), eq(0), eq(String.class)); // TODO: Patrick: fixme: String.class

        // check if liquibase table names where set correctly
        checkIfTableExists(LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOG);
        checkIfTableExists(LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOGLOCK);

    }

    private void checkIfTableExists(final String tableName) throws SQLException {
        DBUtils db = new DBUtils();
        try {
            Connection con = dataSource.getConnection();
            db.watch(con);
            PreparedStatement ps = con.prepareStatement("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = ?)");
            db.watch(ps);
            ps.setString(1, tableName);
            ResultSet result = ps.executeQuery();
            db.watch(result);
            result.next();
            assertTrue(result.getBoolean(1));
        } finally {
            db.cleanUp();
        }
    }
}
