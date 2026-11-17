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
package org.opennms.config.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.config.httpdatacollection.Uri;
import org.opennms.netmgt.config.httpdatacollection.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.FileSystemUtils;

import liquibase.exception.LiquibaseException;

/**
 * Runs the shipped CM changeSet that moves http-datacollection-config.xml into Config Management.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-config-service.xml"})
@JUnitTemporaryDatabase
public class HttpDatacollectionConfigUpgradeIT implements TemporaryDatabaseAware<TemporaryDatabase> {
    private static final String CHANGELOG = "changelog-cm/37.0.0/changelog-cm.xml";
    private static final String CONFIG_NAME = HttpDatacollectionConfig.CM_CONFIG_NAME;
    private static final String FILE_NAME = "http-datacollection-config.xml";
    private static final String SYSTEM_PROP_OPENNMS_HOME = "opennms.home";

    @Autowired
    private ConfigurationManagerService cm;

    private DataSource dataSource;
    private Path opennmsHome;
    private String opennmsHomeOrg;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        this.dataSource = database;
    }

    @Before
    public void setUp() throws IOException {
        opennmsHome = Files.createTempDirectory(getClass().getSimpleName());
        opennmsHomeOrg = System.getProperty(SYSTEM_PROP_OPENNMS_HOME);
        System.setProperty(SYSTEM_PROP_OPENNMS_HOME, opennmsHome.toString());
        Files.createDirectories(opennmsHome.resolve("etc"));
        assertTrue(cm.getRegisteredConfigDefinition(CONFIG_NAME).isEmpty());
    }

    @After
    public void tearDown() throws SQLException {
        // forget that the changeSet ran, so the next test runs it again against the reused database
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOG);
            statement.execute("DROP TABLE IF EXISTS " + LiquibaseUpgrader.TABLE_NAME_DATABASECHANGELOGLOCK);
        }
        if (cm.getRegisteredConfigDefinition(CONFIG_NAME).isPresent()) {
            cm.unregisterSchema(CONFIG_NAME);
        }
        FileSystemUtils.deleteRecursively(opennmsHome.toFile());
        if (opennmsHomeOrg != null) {
            System.setProperty(SYSTEM_PROP_OPENNMS_HOME, opennmsHomeOrg);
        } else {
            System.clearProperty(SYSTEM_PROP_OPENNMS_HOME);
        }
    }

    @Test
    public void importsTheShippedDefaultConfig() throws Exception {
        assertImported(Path.of("../../../opennms-base-assembly/src/main/filtered/etc/" + FILE_NAME));
    }

    @Test
    public void importsACustomizedConfig() throws Exception {
        assertImported(Path.of("../test/src/test/resources/" + FILE_NAME));
    }

    private void assertImported(final Path source) throws IOException, LiquibaseException, SQLException {
        final Path etcFile = opennmsHome.resolve("etc").resolve(FILE_NAME);
        Files.copy(source, etcFile);
        final HttpDatacollectionConfig expected = JaxbUtils.unmarshal(HttpDatacollectionConfig.class, etcFile.toFile());

        new LiquibaseUpgrader(cm).runChangelog(CHANGELOG, dataSource.getConnection());

        assertTrue(cm.getRegisteredConfigDefinition(CONFIG_NAME).isPresent());
        final String json = cm.getJSONStrConfiguration(CONFIG_NAME, DEFAULT_CONFIG_ID).orElseThrow();
        // CM fills in the XSD defaults on import, so compare what the collector sees rather than the raw fields
        assertEquals(effective(expected), effective(ConfigConvertUtil.jsonToObject(json, HttpDatacollectionConfig.class)));

        assertFalse("the file must be moved out of etc", Files.exists(etcFile));
        try (Stream<Path> archived = Files.list(opennmsHome.resolve("etc_archive"))) {
            assertEquals("the file must be archived", 1L, archived.map(Path::getFileName).map(Path::toString).filter(n -> n.matches(FILE_NAME.replace(".", "\\.") + "\\.[0-9]+")).count());
        }
    }

    private static List<Object> effective(final HttpDatacollectionConfig config) {
        final List<Object> values = new ArrayList<>();
        values.add(config.getRrdRepository());
        for (final HttpCollection collection : config.getHttpCollection()) {
            values.addAll(List.of(collection.getName(), collection.getRrd().getStep(), collection.getRrd().getRra()));
            for (final Uri uri : collection.getUris()) {
                final Url url = uri.getUrl();
                values.addAll(List.of(uri.getName(), url.getMethod(), url.getHttpVersion(), url.getUserAgent(), url.getVirtualHost(),
                        url.getScheme(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getFragment(),
                        url.getMatches(), url.getResponseRange(), url.isCanonicalEquivalence(), url.isCaseInsensitive(), url.isComments(),
                        url.isDotall(), url.isLiteral(), url.isMultiline(), url.isUnicodeCase(), url.isUnixLines(),
                        url.getParameters(), url.getHeaders(), uri.getAttributes()));
            }
        }
        return values;
    }
}
