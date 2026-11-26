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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.FileSystemUtils;

/**
 * {@code importConfigDirectory}: every xml file of {@code etc/<directory>} becomes one configuration, named after the
 * file, and the directory is archived as a whole.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-config-service.xml"})
@JUnitTemporaryDatabase(reuseDatabase = false)
public class ImportConfigDirectoryIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private static final String CHANGELOG = "org/opennms/config/upgrade/ImportConfigDirectoryIT-changelog.xml";
    private static final String SCHEMA = "provisiond-multi";
    private static final String DIRECTORY = "provisiond-dir";
    private static final Path PROVISIOND_XML = Path.of("../../../opennms-base-assembly/src/main/filtered/etc/provisiond-configuration.xml");

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
        opennmsHomeOrg = System.getProperty("opennms.home");
        System.setProperty("opennms.home", opennmsHome.toString());
        Files.createDirectories(opennmsHome.resolve("etc"));
    }

    @After
    public void tearDown() throws IOException {
        if (cm.getRegisteredConfigDefinition(SCHEMA).isPresent()) {
            cm.unregisterSchema(SCHEMA);
        }
        FileSystemUtils.deleteRecursively(opennmsHome);
        if (opennmsHomeOrg != null) {
            System.setProperty("opennms.home", opennmsHomeOrg);
        }
    }

    private void run() throws Exception {
        new LiquibaseUpgrader(cm).runChangelog(CHANGELOG, dataSource.getConnection());
    }

    private static void copyProvisiond(final Path dir, final String... names) throws IOException {
        Files.createDirectories(dir);
        for (final String name : names) {
            Files.copy(PROVISIOND_XML, dir.resolve(name));
        }
    }

    private List<Path> archives() throws IOException {
        final Path archive = opennmsHome.resolve("etc_archive");
        if (!Files.isDirectory(archive)) {
            return List.of();
        }
        try (Stream<Path> entries = Files.list(archive)) {
            return entries.filter(p -> p.getFileName().toString().matches(DIRECTORY + "\\.\\d{14}")).collect(Collectors.toList());
        }
    }

    @Test
    public void importsEveryFileAndArchivesTheDirectory() throws Exception {
        final Path dir = opennmsHome.resolve("etc").resolve(DIRECTORY);
        copyProvisiond(dir, "first.xml", "second.one.xml");
        Files.writeString(dir.resolve(".readme"), "not a configuration", StandardCharsets.UTF_8);

        run();

        assertThat(cm.getConfigIds(SCHEMA), equalTo(Set.of("first", "second.one")));
        assertTrue(cm.getJSONStrConfiguration(SCHEMA, "first").isPresent());
        assertFalse("the directory is archived", Files.exists(dir));
        final List<Path> archives = archives();
        assertThat(archives.size(), equalTo(1));
        assertTrue(Files.exists(archives.get(0).resolve("first.xml")));
        assertTrue(Files.exists(archives.get(0).resolve("second.one.xml")));
        assertTrue("other files are archived along", Files.exists(archives.get(0).resolve(".readme")));

        // the changeSet ran, a second start does nothing
        copyProvisiond(dir, "third.xml");
        run();
        assertThat(cm.getConfigIds(SCHEMA), equalTo(Set.of("first", "second.one")));
        assertTrue(Files.exists(dir.resolve("third.xml")));
    }

    @Test
    public void missingDirectoryImportsNothing() throws Exception {
        run();

        assertTrue(cm.getRegisteredConfigDefinition(SCHEMA).isPresent());
        assertThat(cm.getConfigIds(SCHEMA), equalTo(Set.of()));
        assertThat(archives().size(), equalTo(0));
    }

    @Test
    public void fallsBackToTheLatestArchive() throws Exception {
        final Path archive = opennmsHome.resolve("etc_archive");
        copyProvisiond(archive.resolve(DIRECTORY + ".20200101000000"), "old.xml");
        copyProvisiond(archive.resolve(DIRECTORY + ".20250101000000"), "newer.xml");

        run();

        assertThat(cm.getConfigIds(SCHEMA), equalTo(Set.of("newer")));
        assertThat("archives stay where they are", archives().size(), equalTo(2));
    }
}
