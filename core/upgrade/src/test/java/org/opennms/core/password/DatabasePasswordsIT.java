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
package org.opennms.core.password;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/** The database path of the password CLI and the installer. Here because core/upgrade has the database setup. */
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
public class DatabasePasswordsIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private JdbcTemplate m_jdbc;
    private DataSource m_dataSource;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        m_jdbc = database.getJdbcTemplate();
        m_dataSource = m_jdbc.getDataSource();
    }

    @Before
    public void setUp() {
        m_jdbc.update("INSERT INTO users (id, username, password_hash, password_salted, created_time) VALUES (nextval('users_id_seq'), 'admin', 'old', false, NOW())");
    }

    @Test
    public void testSetsTheSaltedHash() throws Exception {
        DatabasePasswords.setPassword(m_dataSource, "admin", "n3w");
        final String hash = m_jdbc.queryForObject("SELECT password_hash FROM users WHERE username = 'admin'", String.class);
        assertTrue(new StrongPasswordEncryptor().checkPassword("n3w", hash));
        assertTrue(m_jdbc.queryForObject("SELECT password_salted FROM users WHERE username = 'admin'", Boolean.class));
    }

    @Test
    public void testUnknownUserFails() {
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> DatabasePasswords.setPassword(m_dataSource, "ghost", "x"));
        assertEquals("User \"ghost\" does not exist.", e.getMessage());
    }

    @Test
    public void testExactUsernameMatch() {
        // the DOM path used contains(), so "dmi" matched "admin"; the database path matches exactly
        assertThrows(IllegalArgumentException.class, () -> DatabasePasswords.setPassword(m_dataSource, "dmi", "x"));
    }

    @Test
    public void testSetPasswordBumpsUserVersion() throws Exception {
        final int before = m_jdbc.queryForObject("SELECT version FROM users WHERE username = 'admin'", Integer.class);
        final long usersVersion = m_jdbc.queryForObject("SELECT version FROM usermgmt_version WHERE name = 'users'", Long.class);
        DatabasePasswords.setPassword(m_dataSource, "admin", "n3w");
        assertTrue(m_jdbc.queryForObject("SELECT version FROM users WHERE username = 'admin'", Integer.class) > before);
        assertTrue(m_jdbc.queryForObject("SELECT version FROM usermgmt_version WHERE name = 'users'", Long.class) > usersVersion);
    }
}
