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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.opennms.config.upgrade.LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOG;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.DBUtils;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.exception.ValidationFailedException;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"})
@JUnitTemporaryDatabase
public class LiquibaseUpgraderIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private DataSource dataSource;
    private Connection connection;
    private DBUtils db;

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        this.dataSource = database;
    }

    @Before
    public void setUp() throws SQLException {
        this.db = new DBUtils();
        this.connection = dataSource.getConnection();
        db.watch(connection);
    }

    @Test
    public void shouldRunChangelog() throws LiquibaseException, IOException, SQLException, JAXBException {
        try {
            ConfigurationManagerService cm = Mockito.mock(ConfigurationManagerService.class);
            LiquibaseUpgrader liqui = new LiquibaseUpgrader(cm);
            liqui.runChangelog("org/opennms/config/upgrade/LiquibaseUpgraderIT-changelog.xml", dataSource.getConnection());

            // check if CM was called
            verify(cm).registerSchema(anyString(),
                    eq(new ConfigurationManagerService.Version(1, 0, 0)),
                    eq(ProvisiondConfiguration.class));

            // check if liquibase table names where set correctly
            checkIfTableExists(TABLE_NAME_DATABASECHANGELOG);
            checkIfTableExists(LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOGLOCK);
        } finally {
            this.db.cleanUp();
        }
    }

    @Test
    public void shouldAbortInCaseOfValidationError() {
        try {
            ConfigurationManagerService cm = Mockito.mock(ConfigurationManagerService.class);
            LiquibaseUpgrader liqui = new LiquibaseUpgrader(cm);
            assertThrowsException(ValidationFailedException.class,
                    () -> liqui.runChangelog("org/opennms/config/upgrade/LiquibaseUpgraderIT-changelog-faulty.xml", connection));
        } finally {
            this.db.cleanUp();
        }
    }

    @Test
    public void shouldAbortInCaseOfErrorDuringRun() {
        try {
            ConfigurationManagerService cm = null; // will lead to Nullpointer
            LiquibaseUpgrader liqui = new LiquibaseUpgrader(cm);
            assertThrowsException(MigrationFailedException.class,
                    () -> liqui.runChangelog("org/opennms/config/upgrade/LiquibaseUpgraderIT-changelog-ok2.xml", connection));
        } finally {
            this.db.cleanUp();
        }
    }

    private void checkIfTableExists(final String tableName) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = ?)");
        db.watch(ps);
        ps.setString(1, tableName);
        ResultSet result = ps.executeQuery();
        db.watch(result);
        result.next();
        assertTrue(result.getBoolean(1));
    }

    // similar to JUnit5
    public static void assertThrowsException(Class<? extends Throwable> expectedException, RunnableWithCheckedException function) {
        try {
            function.run();
        } catch (Exception e) {
            if (!expectedException.isAssignableFrom(e.getClass())) {
                fail(String.format("Expected exception: %s but was %s", expectedException.getName(), e.getClass().getName()));
            }
            return;
        }
        fail(String.format("Expected exception: %s but none was thrown.", expectedException.getName()));
    }

    public interface RunnableWithCheckedException {
        void run() throws Exception;
    }
}
