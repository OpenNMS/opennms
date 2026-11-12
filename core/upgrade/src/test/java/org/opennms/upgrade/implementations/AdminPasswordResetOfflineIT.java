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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.password.DatabasePasswords;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class AdminPasswordResetOfflineIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    private JdbcTemplate m_jdbc;
    private String m_previousHome;
    private File m_etc;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        m_jdbc = database.getJdbcTemplate();
    }

    @Before
    public void setUp() throws Exception {
        BeanUtils.assertAutowiring(this);
        m_previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", m_home.getRoot().getAbsolutePath());
        m_etc = m_home.newFolder("etc");
        // AbstractOnmsUpgrade reads these in its constructor
        write("opennms.properties", "");
        write("rrd-configuration.properties", "");
    }

    @After
    public void tearDown() {
        System.clearProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY);
        if (m_previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", m_previousHome);
        }
    }

    private void write(final String name, final String content) throws Exception {
        Files.write(new File(m_etc, name).toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    private int count(final String table) {
        return m_jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private static void seed() throws Exception {
        final DefaultUsersSeedOffline seed = new DefaultUsersSeedOffline();
        seed.preExecute();
        seed.execute();
        seed.postExecute();
    }

    private static void reset() throws Exception {
        final AdminPasswordResetOffline reset = new AdminPasswordResetOffline();
        reset.preExecute();
        reset.execute();
        reset.postExecute();
    }

    private String adminHash() {
        return m_jdbc.queryForObject("SELECT password_hash FROM users WHERE username = 'admin'", String.class);
    }

    @Test
    public void testDeferredResetSetsThePasswordAfterTheSeed() throws Exception {
        seed();
        System.setProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY, "secret");
        reset();
        final StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        assertTrue(encryptor.checkPassword("secret", adminHash()));
        assertFalse(encryptor.checkPassword("admin", adminHash()));
        assertNull(System.getProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY));
    }

    @Test
    public void testNoPropertyDoesNothing() throws Exception {
        seed();
        final String before = adminHash();
        reset();
        assertEquals(before, adminHash());
    }

    @Test
    public void testNoAdminAfterTheUserDataIsInitializedLogsAndDoesNotFail() throws Exception {
        // admin was deleted or renamed: no task will create it
        m_jdbc.update("INSERT INTO usermgmt_initialized (initialized_by) VALUES ('test')");
        System.setProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY, "secret");
        reset();
        assertEquals(0, count("users"));
        assertNull(System.getProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY));
    }

    @Test
    public void testNoAdminBeforeTheUserDataIsInitializedFailsTheUpgrade() throws Exception {
        // an earlier task failed (e.g. the migrator), so the seed did not create admin: the rerun needs -R again
        System.setProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY, "secret");
        final AdminPasswordResetOffline reset = new AdminPasswordResetOffline();
        reset.preExecute();
        final OnmsUpgradeException e = assertThrows(OnmsUpgradeException.class, reset::execute);
        assertTrue(e.getMessage(), e.getMessage().contains("-R"));
        assertTrue(reset.isCritical());
        assertEquals(0, count("users"));
        assertNull(System.getProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY));
    }

    @Test
    public void testRunsAfterTheSeedAndIsCriticalOnlyWithAPassword() throws Exception {
        final AdminPasswordResetOffline reset = new AdminPasswordResetOffline();
        assertTrue(reset.getOrder() > new DefaultUsersSeedOffline().getOrder());
        assertFalse(reset.runOnlyOnce());
        assertFalse(reset.isCritical());

        System.setProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY, "secret");
        assertTrue(reset.isCritical());
        // skipped while OpenNMS runs, the password would not be set: that fails the upgrade
        assertTrue(reset.isPending());
    }

    @Test
    public void testNotPendingWhenNothingWillCreateTheAdminUser() throws Exception {
        System.setProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY, "secret");
        final AdminPasswordResetOffline reset = new AdminPasswordResetOffline();
        // the user data is not initialized yet: the seed creates admin, so skipping the task would drop the password
        assertTrue(reset.isPending());

        // initialized without admin (deleted or renamed): the task has nothing to do, so skipping it fails nothing
        m_jdbc.update("INSERT INTO usermgmt_initialized (initialized_by) VALUES ('test')");
        assertFalse(reset.isPending());

        // initialized with admin: the task sets the password
        m_jdbc.update("INSERT INTO users (id, username, password_hash) VALUES (nextval('users_id_seq'), 'admin', '')");
        assertTrue(reset.isPending());
    }

    @Test
    public void testAFailedUpdateFailsTheUpgrade() throws Exception {
        seed();
        final String before = adminHash();
        // checked on every update from now on, but not on the rows that exist
        m_jdbc.execute("ALTER TABLE users ADD CONSTRAINT test_no_update CHECK (password_hash IS NULL) NOT VALID");
        System.setProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY, "secret");

        final AdminPasswordResetOffline reset = new AdminPasswordResetOffline();
        reset.preExecute();
        assertThrows(OnmsUpgradeException.class, reset::execute);
        // the upgrade asks after execute(), which cleared the property
        assertTrue(reset.isCritical());
        assertNull(System.getProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY));
        assertEquals(before, adminHash());
    }
}
