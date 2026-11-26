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
package org.opennms.features.config.service.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.config.upgrade.LiquibaseUpgrader;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.netmgt.dao.jaxb.JdbcDataCollectionConfigDaoCm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.FileSystemUtils;

/**
 * The upgrade imports the JDBC data collection files through the configuration manager, which runs the
 * {@link org.opennms.netmgt.dao.jaxb.JdbcDataCollectionConfigValidator} although no DAO has started: a file the
 * collector can't use is not imported and stays in etc/, so nothing is stored that later edits would be rejected for.
 * The DAO in the context registers no validation of its own. The upgrade module itself can't test this, it can't
 * depend on the jdbc-collector.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:mock-dao.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitTemporaryDatabase
public class JdbcDataCollectionUpgradeIT implements TemporaryDatabaseAware<TemporaryDatabase> {
    private static final String CONFIG_NAME = JdbcDataCollectionConfigDaoCm.CONFIG_NAME;
    // registered by the same 37.0.0 changelog, so its file must be present and its schema cleaned up
    private static final String HTTP_CONFIG_NAME = "http-datacollection-config";
    private static final String CHANGELOG = "changelog-cm/37.0.0/changelog-cm.xml";
    private static final String ASSEMBLY_ETC = "../../../opennms-base-assembly/src/main/filtered/etc/";
    private static final String OPENNMS_HOME = "opennms.home";
    private static final String TOO_LONG_ALIAS = "an-alias-longer-than-rrdtool-allows";

    @Autowired
    private ConfigurationManagerService cm;

    private DataSource dataSource;
    private Path opennmsHome;
    private String opennmsHomeOrg;
    private Path etcDir;
    private Path includeDir;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        this.dataSource = database;
    }

    @Before
    public void setUp() throws IOException, SQLException {
        opennmsHome = Files.createTempDirectory(getClass().getSimpleName());
        opennmsHomeOrg = System.getProperty(OPENNMS_HOME);
        System.setProperty(OPENNMS_HOME, opennmsHome.toString());
        etcDir = Files.createDirectories(opennmsHome.resolve("etc"));
        includeDir = Files.createDirectories(etcDir.resolve("jdbc-datacollection.d"));
        Files.copy(Path.of(ASSEMBLY_ETC + HTTP_CONFIG_NAME + ".xml"), etcDir.resolve(HTTP_CONFIG_NAME + ".xml"));
        // the changelog tables survive between tests, so the changeset would be skipped
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOG);
            statement.execute("DROP TABLE IF EXISTS " + LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOGLOCK);
        }
    }

    @After
    public void tearDown() throws IOException {
        for (final String configName : List.of(CONFIG_NAME, HTTP_CONFIG_NAME)) {
            if (cm.getRegisteredConfigDefinition(configName).isPresent()) {
                cm.unregisterSchema(configName);
            }
        }
        if (opennmsHomeOrg == null) {
            System.clearProperty(OPENNMS_HOME);
        } else {
            System.setProperty(OPENNMS_HOME, opennmsHomeOrg);
        }
        FileSystemUtils.deleteRecursively(opennmsHome);
    }

    @Test
    public void filesTheCollectorCannotUseAreNotImported() throws Exception {
        Files.writeString(etcDir.resolve("jdbc-datacollection-config.xml"),
                withTooLongAlias(ASSEMBLY_ETC + "jdbc-datacollection-config.xml", "OnmsEventCount"));
        Files.writeString(includeDir.resolve("postgresql.xml"),
                withTooLongAlias(ASSEMBLY_ETC + "jdbc-datacollection.d/postgresql.xml", "spcname"));
        Files.copy(Path.of(ASSEMBLY_ETC + "jdbc-datacollection.d/mysql.xml"), includeDir.resolve("mysql.xml"));

        runUpgrade();

        assertThat(cm.getConfigIds(CONFIG_NAME), equalTo(Set.of("mysql")));
        assertThat(Files.exists(etcDir.resolve("jdbc-datacollection-config.xml")), equalTo(true));
        assertThat(Files.exists(includeDir.resolve("postgresql.xml")), equalTo(true));
        assertThat(Files.exists(includeDir.resolve("mysql.xml")), equalTo(false));

        // what was imported can be edited
        cm.updateConfiguration(CONFIG_NAME, "mysql", new JsonAsString(cm.getJSONStrConfiguration(CONFIG_NAME, "mysql").get()), true);
    }

    /**
     * A file whose name is not an id the REST API accepts is not imported and stays in etc/, the others are imported.
     */
    @Test
    public void fileWithInvalidIdIsNotImported() throws Exception {
        Files.copy(Path.of(ASSEMBLY_ETC + "jdbc-datacollection-config.xml"), etcDir.resolve("jdbc-datacollection-config.xml"));
        Files.copy(Path.of(ASSEMBLY_ETC + "jdbc-datacollection.d/postgresql.xml"), includeDir.resolve("postgresql.xml"));
        Files.copy(Path.of(ASSEMBLY_ETC + "jdbc-datacollection.d/mysql.xml"), includeDir.resolve("-mysql.xml"));

        runUpgrade();

        assertThat(cm.getConfigIds(CONFIG_NAME), equalTo(Set.of(ConfigDefinition.DEFAULT_CONFIG_ID, "postgresql")));
        assertThat(Files.exists(includeDir.resolve("-mysql.xml")), equalTo(true));
        assertThat(Files.exists(includeDir.resolve("postgresql.xml")), equalTo(false));
    }

    private void runUpgrade() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            new LiquibaseUpgrader(cm).runChangelog(CHANGELOG, connection);
        }
    }

    private static String withTooLongAlias(final String file, final String alias) throws IOException {
        final String xml = Files.readString(Path.of(file), StandardCharsets.UTF_8);
        final String attribute = "alias=\"" + alias + "\"";
        assertThat(xml, containsString(attribute));
        return xml.replace(attribute, "alias=\"" + TOO_LONG_ALIAS + "\"");
    }
}
