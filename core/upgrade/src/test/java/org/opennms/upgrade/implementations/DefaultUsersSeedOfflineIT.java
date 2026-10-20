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
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.upgrade.support.UpgradeStatus;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DefaultUsersSeedOfflineIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    // users.xsd requires at least one user inside <users>, so a file without users has no <users> element
    private static final String EMPTY_USERS_XML = "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"/>";

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    @Autowired
    private UserAccountDao m_userDao;

    @Autowired
    private UserGroupDao m_groupDao;

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

    private static void migrate() throws Exception {
        final UsersGroupsXmlMigratorOffline migrator = new UsersGroupsXmlMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();
    }

    private void markMigratorComplete() throws Exception {
        write(UpgradeStatus.STATUS_FILE, "UsersGroupsXmlMigratorOffline=Tue Oct 14 10\\:00\\:00 UTC 2026\n");
    }

    @Test
    @Transactional
    public void testSeedsWhenThereIsNoUsersXml() throws Exception {
        seed();
        assertEquals(2, count("users"));
        final UserAccount admin = m_userDao.findByUsername("admin");
        assertEquals("Administrator", admin.getFullName());
        assertEquals("Default administrator, do not delete", admin.getComments());
        assertTrue(admin.isPasswordSalted());
        assertTrue(admin.hasSecurityRole("ROLE_ADMIN"));
        final UserAccount rtc = m_userDao.findByUsername("rtc");
        assertEquals("RTC", rtc.getFullName());
        assertTrue(rtc.hasSecurityRole("ROLE_RTC"));
        final UserGroup group = m_groupDao.findByName("Admin");
        assertEquals("The administrators", group.getComments());
        assertTrue(group.hasMember("admin"));
    }

    @Test
    @Transactional
    public void testSeededPasswordsAreTheDefaults() throws Exception {
        seed();
        final StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        assertTrue(encryptor.checkPassword("admin", m_userDao.findByUsername("admin").getPasswordHash()));
        assertTrue(encryptor.checkPassword("rtc", m_userDao.findByUsername("rtc").getPasswordHash()));
    }

    @Test
    public void testSeedsWhenTheMigratorIsComplete() throws Exception {
        write("users.xml", EMPTY_USERS_XML);
        markMigratorComplete();
        seed();
        assertEquals(2, count("users"));
    }

    @Test
    public void testDoesNotSeedWhenTheMigrationIsPending() throws Exception {
        write("users.xml", EMPTY_USERS_XML);
        seed();
        assertEquals(0, count("users"));
        assertEquals(0, count("groups"));
    }

    @Test
    public void testDoesNotSeedWhenThereAreUsers() throws Exception {
        m_jdbc.update("INSERT INTO users (id, username, password_salted, created_time) VALUES (nextval('users_id_seq'), 'jane', false, NOW())");
        seed();
        assertEquals(1, count("users"));
        assertEquals(0, count("groups"));
    }

    @Test
    public void testSecondRunDoesNothing() throws Exception {
        seed();
        seed();
        assertEquals(2, count("users"));
        assertEquals(1, count("groups"));
    }

    @Test
    public void testDoesNotSeedAfterMigratingAUsersXmlWithoutUsers() throws Exception {
        // a site that uses only LDAP, RADIUS or SSO: an empty users.xml stays empty
        write("users.xml", EMPTY_USERS_XML);
        migrate();
        markMigratorComplete();
        seed();
        assertEquals(0, count("users"));
    }

    @Test
    public void testDoesNotSeedAfterAllUsersWereDeleted() throws Exception {
        seed();
        m_jdbc.update("DELETE FROM group_members");
        m_jdbc.update("DELETE FROM user_security_roles");
        m_jdbc.update("DELETE FROM users");
        seed();
        assertEquals(0, count("users"));
    }

    @Test
    public void testSeedMarksTheDatabaseAsInitialized() throws Exception {
        seed();
        assertEquals(1, count("usermgmt_initialized"));
    }
}
