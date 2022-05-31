/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opennms.config.upgrade.LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOG;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;
import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
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
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.FileSystemUtils;

import io.swagger.v3.oas.models.OpenAPI;
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

    private final static String SCHEMA_NAME_PROVISIOND = "provisiond";
    private final static String SCHEMA_NAME_EVENTD = "eventd";
    private final static String SCHEMA_NAME_PROPERTIES = "propertiesTest";
    private final static String SCHEMA_NAME_GRAPHML = "org.opennms.netmgt.graph.provider.graphml";
    private final static String SYSTEM_PROP_OPENNMS_HOME = "opennms.home";

    private DataSource dataSource;
    private Connection connection;
    private DBUtils db;
    @Autowired
    private ConfigurationManagerService cm;
    private ConfigurationManagerService cmSpy;
    private Path opennmsHome;
    private String opennmsHomeOrg;

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        this.dataSource = database;
    }

    @Before
    public void setUp() throws IOException, URISyntaxException, JAXBException, SQLException {
        this.opennmsHome = Files.createTempDirectory(this.getClass().getSimpleName());
        opennmsHomeOrg = System.getProperty(SYSTEM_PROP_OPENNMS_HOME);
        System.setProperty(SYSTEM_PROP_OPENNMS_HOME, this.opennmsHome.toString());
        Path etcDir = Files.createDirectories(Paths.get(this.opennmsHome + "/etc"));
        Files.copy(Path.of("../../../opennms-base-assembly/src/main/filtered/etc/" + SCHEMA_NAME_PROVISIOND + "-configuration.xml"),
                Path.of(etcDir + "/" + SCHEMA_NAME_PROVISIOND + "-configuration.xml"));
        Files.copy(Path.of("../../../opennms-base-assembly/src/main/filtered/etc/" + SCHEMA_NAME_EVENTD + "-configuration.xml"),
                Path.of(etcDir + "/" + SCHEMA_NAME_EVENTD + "-configuration.xml"));
        Files.copy(Path.of("../../../opennms-config-model/src/main/resources/defaults/org.opennms.features.datachoices.cfg"),
                Path.of(etcDir + "/org.opennms.features.datachoices.cfg"));
        FileUtils.writeStringToFile(new File(etcDir + "/"+SCHEMA_NAME_GRAPHML+"-a.cfg"), "graphLocation=a", StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File(etcDir + "/"+SCHEMA_NAME_GRAPHML+"-b.cfg"), "graphLocation=b", StandardCharsets.UTF_8);

        this.db = new DBUtils();
        this.connection = dataSource.getConnection();
        db.watch(connection);
        cmSpy = spy(cm);
        assertTrue(this.cm.getRegisteredConfigDefinition(SCHEMA_NAME_PROVISIOND).isEmpty());
        assertTrue(this.cm.getJSONConfiguration(SCHEMA_NAME_PROVISIOND, DEFAULT_CONFIG_ID).isEmpty());
        assertTrue(this.cm.getRegisteredConfigDefinition(SCHEMA_NAME_EVENTD).isEmpty());
        assertTrue(this.cm.getJSONConfiguration(SCHEMA_NAME_EVENTD, DEFAULT_CONFIG_ID).isEmpty());
        assertTrue(this.cm.getRegisteredConfigDefinition(SCHEMA_NAME_GRAPHML).isEmpty());
        assertTrue(this.cm.getJSONConfiguration(SCHEMA_NAME_GRAPHML, "a").isEmpty());
        assertTrue(this.cm.getJSONConfiguration(SCHEMA_NAME_GRAPHML, "b").isEmpty());
    }

    @After
    public void tearDown() throws IOException, JAXBException {

        if (cm.getRegisteredConfigDefinition(SCHEMA_NAME_PROVISIOND).isPresent()) {
            this.cm.unregisterSchema(SCHEMA_NAME_PROVISIOND);
        }

        if (cm.getRegisteredConfigDefinition(SCHEMA_NAME_EVENTD).isPresent()) {
            this.cm.unregisterSchema(SCHEMA_NAME_EVENTD);
        }

        if (cm.getRegisteredConfigDefinition(SCHEMA_NAME_GRAPHML).isPresent()) {
            this.cm.unregisterSchema(SCHEMA_NAME_GRAPHML);
        }

        FileSystemUtils.deleteRecursively(this.opennmsHome.toFile());
        if (this.opennmsHomeOrg != null) {
            System.setProperty(SYSTEM_PROP_OPENNMS_HOME, this.opennmsHomeOrg);
        }
    }

    @Test
    public void shouldRunChangelog() throws LiquibaseException, ValidationException, SQLException {
        try {
            assertFalse(this.cm.getRegisteredConfigDefinition(SCHEMA_NAME_PROPERTIES).isPresent());

            LiquibaseUpgrader liqui = new LiquibaseUpgrader(cmSpy);
            liqui.runChangelog("org/opennms/config/upgrade/LiquibaseUpgraderIT-changelog.xml", dataSource.getConnection());

            // check if CM was called for schemas
            verify(cmSpy, times(5)).registerConfigDefinition(anyString(), any());

            // check if CM was called for config
            verify(cmSpy).registerConfiguration(eq(SCHEMA_NAME_PROVISIOND), eq(DEFAULT_CONFIG_ID), any());

            // check if liquibase table names where set correctly
            checkIfTableExists(TABLE_NAME_DATABASECHANGELOG);
            checkIfTableExists(LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOGLOCK);

            // check for the data itself
            assertTrue(this.cm.getRegisteredConfigDefinition(SCHEMA_NAME_PROVISIOND).isPresent());
            assertTrue(this.cm.getJSONConfiguration(SCHEMA_NAME_PROVISIOND, DEFAULT_CONFIG_ID).isPresent());
            assertTrue(this.cm.getRegisteredConfigDefinition(SCHEMA_NAME_EVENTD).isPresent());
            assertTrue(this.cm.getJSONConfiguration(SCHEMA_NAME_EVENTD, DEFAULT_CONFIG_ID).isPresent());

            // check if CM was called for schema
            verify(cmSpy, times(5)).changeConfigDefinition(anyString(), any(ConfigDefinition.class));

            // check if xml file was moved into archive folder
            assertFalse(Files.exists(Path.of(this.opennmsHome + "/etc/" + SCHEMA_NAME_EVENTD + "-configuration.xml"))); // should be gone since we moved the file
            assertTrue(checkFileWithDateTimeSuffix(this.opennmsHome + "/etc_archive", SCHEMA_NAME_EVENTD + "-configuration.xml"));
            assertFalse(Files.exists(Path.of(this.opennmsHome + "/etc/" + SCHEMA_NAME_PROVISIOND + "-configuration.xml"))); // should be gone since we moved the file
            assertTrue(checkFileWithDateTimeSuffix(this.opennmsHome + "/etc_archive", SCHEMA_NAME_PROVISIOND + "-configuration.xml"));

            // check if schema changes work properly
            Optional<ConfigDefinition> configDefinition = this.cm.getRegisteredConfigDefinition(SCHEMA_NAME_PROPERTIES);
            assertTrue(configDefinition.isPresent());

            OpenAPI openApi = configDefinition.get().getSchema();
            ConfigItem schema = OpenAPIBuilder.createBuilder(SCHEMA_NAME_PROPERTIES, SCHEMA_NAME_PROPERTIES, "", openApi).getRootConfig();
            assertNotNull(schema);
            assertEquals(2, schema.getChildren().size());
            assertEquals("property1", schema.getChildren().get(0).getName());
            assertEquals(".*", schema.getChildren().get(0).getPattern());
            assertEquals(ConfigItem.Type.BOOLEAN, schema.getChildren().get(1).getType());
            assertEquals(Boolean.FALSE, schema.getChildren().get(1).getDefaultValue());

            // check for org.opennms.features.datachoices.cfg
            Optional<JSONObject> config = this.cm.getJSONConfiguration("org.opennms.features.datachoices", "default");
            // only fields with a default value will auto fill
            assertEquals(4, config.get().keySet().size());
            // boolean in openable is impossible to be null
            assertEquals(false, config.get().get("enabled"));
            assertEquals(BigDecimal.valueOf(86400000), config.get().get("interval"));
            assertEquals("http://stats.opennms.org/datachoices/", config.get().get("url"));

            // check for graphml a + b
            config = this.cm.getJSONConfiguration(SCHEMA_NAME_GRAPHML, "a");
            assertTrue(config.isPresent());
            assertEquals("a", config.get().get("graphLocation"));
            config = this.cm.getJSONConfiguration(SCHEMA_NAME_GRAPHML, "b");
            assertTrue(config.isPresent());
            assertEquals("b", config.get().get("graphLocation"));
            assertFalse(Files.exists(Path.of(this.opennmsHome + "/etc/" + SCHEMA_NAME_GRAPHML + "-a.cfg"))); // should be gone since we moved the file
            assertTrue(checkFileWithDateTimeSuffix(this.opennmsHome + "/etc_archive", SCHEMA_NAME_GRAPHML + "-a.cfg"));
            assertFalse(Files.exists(Path.of(this.opennmsHome + "/etc/" + SCHEMA_NAME_GRAPHML + "-b.cfg"))); // should be gone since we moved the file
            assertTrue(checkFileWithDateTimeSuffix(this.opennmsHome + "/etc_archive", SCHEMA_NAME_GRAPHML + "-b.cfg"));
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
    public void shouldAbortInCaseOfErrorDuringRun() throws SQLException, URISyntaxException, IOException {
        try {
            // Make sure it trigger Liquibase logic
            PreparedStatement statement = connection.prepareStatement("DROP TABLE IF EXISTS " + TABLE_NAME_DATABASECHANGELOG );
            statement.execute();
            ConfigurationManagerService cm = Mockito.mock(ConfigurationManagerService.class); // will lead to Exception
            LiquibaseUpgrader liqui = new LiquibaseUpgrader(cm);
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

    private boolean checkFileWithDateTimeSuffix(String path, String fileName){
        List<File> files = Stream.of(new File(path).listFiles())
                .filter(file -> file.getName().matches(String.format("%s\\.[0-9]+", fileName)))
                .collect(Collectors.toList());
        return files.size() == 1 && files.get(0).isFile();
    }
}
