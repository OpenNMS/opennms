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
package org.opennms.upgrade.implementations;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.schema.Migrator;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabasePostgreSQL;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.HtmlEncoding;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * The 37.0.0 changeset that decodes the HTML-encoded contact info of old usersnotified rows. The temporary database
 * ran it on an empty table, so the test forgets that it ran, adds rows, and runs the migration again.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class UsersNotifiedContactInfoDecodeIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private static final String CHANGESET = "37.0.0-usersnotified-decode-contactinfo";

    private JdbcTemplate m_jdbc;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        m_jdbc = database.getJdbcTemplate();
    }

    @Test
    public void testDecodesOldContactInfoOnce() throws Exception {
        assertEquals(1, changesetRuns());
        m_jdbc.update("DELETE FROM databasechangelog WHERE id = ?", CHANGESET);

        // user id, stored contact info, expected contact info after the migration
        final String[][] rows = {
            {"obrien", "o&#39;brien@example.org", "o'brien@example.org"},
            {"amp", "a&amp;b@example.org", "a&b@example.org"},
            // a destination path's email target: the address is the user id, and setInfo encoded it as the contact
            {"o'brien@example.org", "o&#39;brien@example.org", "o'brien@example.org"},
            {"all", "&lt;&gt;&#34;&#39;&amp;", "<>\"'&"},
            // &amp; is decoded last, so its result is not decoded again
            {"order", "x&amp;lt;y", "x&lt;y"},
            {"twice", "x&amp;#39;y", "x&#39;y"},
            {"ampamp", "&amp;amp;", "&amp;"},
            {"ampentity", "&&#39;", "&'"},
            {"trailing", "a&", "a&"},
            {"truncated", "a&#3", "a&#3"},
            // Encode.forHtml never wrote named quotes, and the decoder leaves them
            {"quot", "&quot;", "&quot;"},
            {"raw", "a&b", "a&b"},
            {"plain", "plain@example.org", "plain@example.org"}
        };
        // the changeset must decode exactly like the users migration
        for (final String[] row : rows) {
            assertEquals(row[1], row[2], HtmlEncoding.decodeHtmlEncoded(row[1]));
        }
        for (final String[] row : rows) {
            m_jdbc.update("INSERT INTO usersnotified (id, userid, media, contactinfo) VALUES (nextval('usernotifnxtid'), ?, 'email', ?)", row[0], row[1]);
        }

        migrate();
        assertEquals(1, changesetRuns());
        final Map<String, String> expected = new LinkedHashMap<>();
        for (final String[] row : rows) {
            expected.put(row[0], row[2]);
        }
        assertEquals(expected, contactInfo());

        // Liquibase runs the changeset once: a second migration leaves the decoded values alone
        migrate();
        assertEquals(1, changesetRuns());
        assertEquals(expected, contactInfo());
    }

    private int changesetRuns() {
        return m_jdbc.queryForObject("SELECT count(*) FROM databasechangelog WHERE id = ?", Integer.class, CHANGESET);
    }

    private Map<String, String> contactInfo() {
        final Map<String, String> contactInfo = new LinkedHashMap<>();
        m_jdbc.query("SELECT userid, contactinfo FROM usersnotified ORDER BY id", rs -> {
            contactInfo.put(rs.getString("userid"), rs.getString("contactinfo"));
        });
        return contactInfo;
    }

    /** Runs the production changelog on the temporary database, as the installer does. */
    private void migrate() throws Exception {
        final String adminUser = System.getProperty(TemporaryDatabase.ADMIN_USER_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_USER);
        final String adminPassword = System.getProperty(TemporaryDatabase.ADMIN_PASSWORD_PROPERTY, TemporaryDatabase.DEFAULT_ADMIN_PASSWORD);
        // the changelogs are not on the test classpath: load them from the source tree, as TemporaryDatabasePostgreSQL
        // does, and run only that root changelog
        final File liquibase = new File(ConfigurationTestUtils.getTopProjectDirectory(), TemporaryDatabasePostgreSQL.LIQUIBASE_RELATIVE_PATH);
        final String sourceTree = liquibase.toURI().toString();
        try (final URLClassLoader classLoader = new URLClassLoader(new URL[] {liquibase.toURI().toURL()}, getClass().getClassLoader())) {
            final GenericApplicationContext context = new GenericApplicationContext();
            context.setClassLoader(classLoader);
            final Migrator migrator = new Migrator();
            migrator.setApplicationContext(context);
            migrator.setLiquibaseChangelogFilter(changelog -> {
                try {
                    return changelog.getURI().toString().startsWith(sourceTree);
                } catch (final IOException e) {
                    return false;
                }
            });
            migrator.setAdminUser(adminUser);
            migrator.setAdminPassword(adminPassword);
            migrator.setDatabaseUser(adminUser);
            migrator.setDatabasePassword(adminPassword);
            migrator.setDataSource(m_jdbc.getDataSource());
            migrator.setAdminDataSource(m_jdbc.getDataSource());
            migrator.setValidateDatabaseVersion(false);
            final Collection<Resource> changelogs = migrator.getLiquibaseChangelogs(true);
            assertEquals(changelogs.toString(), 1, changelogs.size());
            migrator.migrate(changelogs.iterator().next());
        }
    }
}
