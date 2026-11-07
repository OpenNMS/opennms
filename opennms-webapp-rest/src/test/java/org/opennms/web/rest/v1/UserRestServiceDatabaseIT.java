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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.UserMgmtRestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The v1 user endpoints with the database store (no mock user and group managers, unlike {@link UserRestServiceIT}):
 * a write is validated and guarded like the v2 one and answers 400 or 409 for what it refuses, never 500.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase(reuseDatabase = false)
public class UserRestServiceDatabaseIT extends AbstractSpringJerseyRestTestCase {
    private static final String PASSWORD = "21232F297A57A5A743894A0E4A801FC3";

    @Autowired
    private DataSource m_dataSource;

    @Autowired
    private UserManager m_userManager;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void createAdmin() {
        // the database starts without users; the REST calls run as admin, which must exist for its own protections
        inTx(() -> {
            if (m_userAccountDao.findByUsername("admin") == null) {
                final UserAccount admin = new UserAccount("admin");
                admin.setPasswordHash("");
                admin.getSecurityRoles().add(m_securityRoleDao.findByName("ROLE_ADMIN"));
                m_userAccountDao.save(admin);
            }
            return null;
        });
        setUser("admin", new String[] { "ROLE_ADMIN" });
    }

    @After
    public void restoreDestinationPaths() {
        UserMgmtRestSupport.setDestinationPaths(UserMgmtRestSupport.NOTIFD_DESTINATION_PATHS);
    }

    @Test
    public void testCreateRefusesReservedAndInvalidIds() throws Exception {
        for (final String id : new String[] { "item", "a/b", "&lt;b&gt;", "ADMIN" }) {
            sendPost("/users", userXml(id, null, null), 400);
            assertFalse(id + " must not exist", exists(id));
        }
        assertEquals(1, count("select count(*) from users"));
    }

    @Test
    public void testBadDutyScheduleIsRefused() throws Exception {
        sendPost("/users", userXml("duty", "garbage", null), 400);
        assertFalse(exists("duty"));

        createUser("duty");
        sendPut("/users/duty", "dutySchedule=garbage", 400);
        assertEquals(0, count("select count(*) from user_duty_schedules where user_id = (select id from users where username = 'duty')"));
    }

    @Test
    public void testStoredBadDutyScheduleIsGrandfathered() throws Exception {
        createUser("old");
        new JdbcTemplate(m_dataSource).update("insert into user_duty_schedules (user_id, position, schedule) select id, 0, 'garbage' from users where username = 'old'");
        // the manager's cache was loaded before this edit behind its back
        m_userManager.reload();

        sendPut("/users/old", "fullName=x", 204);
        assertEquals("x", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'old'", String.class));
    }

    @Test
    public void testUnknownRoleIs400() throws Exception {
        sendPost("/users", userXml("roley", null, "ROLE_BOGUS"), 400);
        assertFalse(exists("roley"));

        createUser("roley");
        sendPut("/users/roley", "roles=ROLE_BOGUS", 400);
        assertEquals("Unknown security role ROLE_BOGUS.", sendData(PUT, "application/x-www-form-urlencoded", "/users/roley", "roles=ROLE_BOGUS", 400).getContentAsString());
    }

    @Test
    public void testAdminKeepsRoleAdmin() throws Exception {
        final String message = "The admin user must keep the ROLE_ADMIN role.";
        assertEquals(message, sendData(POST, "application/xml", "/users", userXml("admin", null, "ROLE_USER"), 400).getContentAsString());
        assertEquals(message, sendData(DELETE, "application/xml", "/users/admin/roles/ROLE_ADMIN", "", 400).getContentAsString());
        assertEquals(message, sendData(PUT, "application/x-www-form-urlencoded", "/users/admin", "roles=ROLE_USER", 400).getContentAsString());
        assertEquals(List.of("ROLE_ADMIN"), roles("admin"));
    }

    @Test
    public void testLastAdministratorCannotBeDeletedOrDemoted() throws Exception {
        createUser("second");
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);

        // two administrators: the second can be demoted, in each of the three ways
        sendRequest(DELETE, "/users/second/roles/ROLE_ADMIN", 204);
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);
        sendPut("/users/second", "roles=ROLE_USER", 204);
        assertEquals(List.of("ROLE_USER"), roles("second"));
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);
        sendPost("/users", userXml("second", null, "ROLE_USER"), 201);
        assertEquals(List.of("ROLE_USER"), roles("second"));

        // admin is protected by its own rule, so take the role from admin behind the API's back: second is the only holder
        sendRequest(PUT, "/users/second/roles/ROLE_ADMIN", 204);
        inTx(() -> {
            m_userAccountDao.findByUsername("admin").getSecurityRoles().clear();
            return null;
        });
        final String last = "User second is the only user with the ROLE_ADMIN role and cannot lose the ROLE_ADMIN role; give the role to another user first.";
        assertEquals(last, sendData(DELETE, "application/xml", "/users/second/roles/ROLE_ADMIN", "", 409).getContentAsString());
        assertEquals(last, sendData(PUT, "application/x-www-form-urlencoded", "/users/second", "roles=ROLE_USER", 409).getContentAsString());
        assertEquals(last, sendData(POST, "application/xml", "/users", userXml("second", null, "ROLE_USER"), 409).getContentAsString());
        assertEquals("User second is the only user with the ROLE_ADMIN role and cannot be deleted; give the role to another user first.",
                sendData(DELETE, "application/xml", "/users/second", "", 409).getContentAsString());
        assertTrue(roles("second").contains("ROLE_ADMIN"));
        assertTrue(exists("second"));
    }

    @Test
    public void testSystemUsersCannotBeDeleted() throws Exception {
        inTx(() -> {
            final UserAccount rtc = new UserAccount("rtc");
            rtc.setPasswordHash("");
            m_userAccountDao.save(rtc);
            return null;
        });
        assertEquals("The system user admin cannot be deleted.", sendData(DELETE, "application/xml", "/users/admin", "", 400).getContentAsString());
        assertEquals("The system user rtc cannot be deleted.", sendData(DELETE, "application/xml", "/users/rtc", "", 400).getContentAsString());
        assertTrue(exists("admin"));
        assertTrue(exists("rtc"));
    }

    @Test
    public void testSupervisorCannotBeDeleted() throws Exception {
        createUser("boss");
        inTx(() -> {
            final UserGroup group = new UserGroup("bossgroup");
            m_userGroupDao.save(group);
            m_onCallRoleDao.save(new OnCallRole("bossrole", group, m_userAccountDao.findByUsername("boss")));
            return null;
        });

        final String message = sendData(DELETE, "application/xml", "/users/boss", "", 409).getContentAsString();
        assertTrue(message, message.contains("is the supervisor of the on-call role \"bossrole\""));
        assertTrue(exists("boss"));
    }

    @Test
    public void testDestinationPathTargetCannotBeDeleted() throws Exception {
        createUser("target");
        final Path path = new Path("night");
        path.addTarget(new Target("target", "javaEmail"));
        UserMgmtRestSupport.setDestinationPaths(() -> (Collection<Path>) List.of(path));

        assertEquals("User target is a target of destination path(s) night; remove it from them first.",
                sendData(DELETE, "application/xml", "/users/target", "", 409).getContentAsString());
        assertTrue(exists("target"));

        UserMgmtRestSupport.setDestinationPaths(() -> List.<Path>of());
        sendRequest(DELETE, "/users/target", 204);
        assertFalse(exists("target"));
    }

    @Test
    public void testRenameThroughFormPutIs400() throws Exception {
        createUser("before");
        assertEquals("Renaming is not supported by the deprecated v1 API; use PUT /api/v2/users/{userId}/rename.",
                sendData(PUT, "application/x-www-form-urlencoded", "/users/before", "username=after", 400).getContentAsString());
        assertTrue(exists("before"));
        assertFalse(exists("after"));
    }

    @Test
    public void testTooLongValueIs400() throws Exception {
        createUser("long");
        // 257 code points, two chars each
        final String tooLong = "%F0%9F%98%80".repeat(257);
        sendPut("/users/long", "fullName=" + tooLong, 400);
        assertEquals("long Full Name", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'long'", String.class));
    }

    @Test
    public void testCrudStillWorks() throws Exception {
        sendPost("/users", userXml("crud", "MoTuWeThFr800-1700", "ROLE_USER"), 201, "/users/crud");
        assertEquals("crud Full Name", new JdbcTemplate(m_dataSource).queryForObject("select full_name from users where username = 'crud'", String.class));
        assertTrue(sendRequest(GET, "/users/crud", 200).contains("<user-id>crud</user-id>"));
        assertEquals(List.of("ROLE_USER"), roles("crud"));

        sendPut("/users/crud", "fullName=Crud+Two&email=crud@example.org&dutySchedule=SaSu0-2359", 204);
        final JdbcTemplate jdbc = new JdbcTemplate(m_dataSource);
        assertEquals("Crud Two", jdbc.queryForObject("select full_name from users where username = 'crud'", String.class));
        final String xml = sendRequest(GET, "/users/crud", 200);
        assertTrue(xml, xml.contains("crud@example.org"));
        assertTrue(xml, xml.contains("SaSu0-2359"));

        sendRequest(PUT, "/users/crud/roles/ROLE_PROVISION", 204);
        assertEquals(List.of("ROLE_PROVISION", "ROLE_USER"), roles("crud"));
        sendRequest(DELETE, "/users/crud/roles/ROLE_PROVISION", 204);
        assertEquals(List.of("ROLE_USER"), roles("crud"));

        sendRequest(DELETE, "/users/crud", 204);
        assertFalse(exists("crud"));
        sendRequest(GET, "/users/crud", 404);
    }

    private void createUser(final String name) throws Exception {
        sendPost("/users", userXml(name, null, null), 201, "/users/" + name);
    }

    private static String userXml(final String id, final String dutySchedule, final String role) {
        return "<user><user-id>" + id + "</user-id><full-name>" + id + " Full Name</full-name>"
                + "<password>" + PASSWORD + "</password>"
                + (dutySchedule == null ? "" : "<duty-schedule>" + dutySchedule + "</duty-schedule>")
                + (role == null ? "" : "<role>" + role + "</role>")
                + "</user>";
    }

    private boolean exists(final String id) {
        return count("select count(*) from users where username = '" + id.replace("'", "''") + "'") > 0;
    }

    private int count(final String sql) {
        return new JdbcTemplate(m_dataSource).queryForObject(sql, Integer.class);
    }

    private List<String> roles(final String username) {
        return new JdbcTemplate(m_dataSource).queryForList(
                "select r.name from user_security_roles ur join security_roles r on r.id = ur.role_id"
                + " join users u on u.id = ur.user_id where u.username = ? order by r.name", String.class, username);
    }

    private <T> T inTx(final Supplier<T> work) {
        return new TransactionTemplate(m_transactionManager).execute(status -> work.get());
    }
}
