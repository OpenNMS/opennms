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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.opennms.config.upgrade.LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOG;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import org.junit.After;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.exception.ValidationFailedException;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-config-service.xml"})

@JUnitTemporaryDatabase
public class LiquibaseUpgraderIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private final static String SCHEMA_NAME = "provisiond";
    private final static String CONFIG_ID = "provisiond";

    private DataSource dataSource;
    private Connection connection;
    private DBUtils db;
    @Autowired
    private ConfigurationManagerService cm;
    private ConfigurationManagerService cmSpy;
    private List<Path> pathsToDelete = new ArrayList<>();

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        this.dataSource = database;
    }

    @Before
    public void setUp() throws SQLException, IOException {
        this.db = new DBUtils();
        this.connection = dataSource.getConnection();
        db.watch(connection);
        cmSpy = spy(cm);
        assertTrue(this.cm.getRegisteredSchema(SCHEMA_NAME).isEmpty());
        assertTrue(this.cm.getXmlConfiguration(SCHEMA_NAME, CONFIG_ID).isEmpty());
    }

    @After
    public void tearDown() throws SQLException, IOException {
        if(cm.getXmlConfiguration(SCHEMA_NAME, CONFIG_ID).isPresent()) {
            this.cm.unregisterConfiguration(SCHEMA_NAME, CONFIG_ID);
        }
        if(cm.getRegisteredSchema(SCHEMA_NAME).isPresent()) {
            this.cm.unregisterSchema(SCHEMA_NAME);
        }
        for(Path path: pathsToDelete) {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void shouldRunChangelog() throws LiquibaseException, IOException, SQLException, JAXBException {
        try {
            LiquibaseUpgrader liqui = new LiquibaseUpgrader(cmSpy);
            liqui.runChangelog("org/opennms/config/upgrade/LiquibaseUpgraderIT-changelog.xml", dataSource.getConnection());

            // check if CM was called for schema
            verify(cmSpy).registerSchema(anyString(),
                    eq("provisiond-configuration.xsd"),
                    eq("provisiond-configuration"));

            // check if CM was called for config
            verify(cmSpy).registerConfiguration(eq(SCHEMA_NAME), eq("provisiond"), any());

            // check if liquibase table names where set correctly
            checkIfTableExists(TABLE_NAME_DATABASECHANGELOG);
            checkIfTableExists(LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOGLOCK);

            // check for the data itself
            assertTrue(this.cm.getRegisteredSchema(SCHEMA_NAME).isPresent());
            assertTrue(this.cm.getXmlConfiguration(SCHEMA_NAME, CONFIG_ID).isPresent());

            // check if CM was called for schema
            verify(cmSpy).upgradeSchema(anyString(),
                    eq("provisiond-configuration_v2.xsd"),
                    eq("provisiond-configuration"));
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
            LiquibaseUpgrader liqui = new LiquibaseUpgrader(null);
            assertThrowsException(MigrationFailedException.class,
                    () -> liqui.runChangelog("org/opennms/config/upgrade/LiquibaseUpgraderIT-changelog2.xml", connection));
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
