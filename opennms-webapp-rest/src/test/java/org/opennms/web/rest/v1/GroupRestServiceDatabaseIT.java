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
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsUser;
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
 * The v1 group endpoints with the database store (no mock user and group managers, unlike
 * {@link GroupRestServiceIT}): the authorizations land in category_group, and every change moves the group's
 * version, so that a v2 editor holding the group gets 412. A write is validated and guarded like the v2 one and
 * answers 400 or 409 for what it refuses, never 500.
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
public class GroupRestServiceDatabaseIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private DataSource m_dataSource;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private UserGroupDao m_userGroupDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
    }

    @After
    public void restoreDestinationPaths() {
        UserMgmtRestSupport.setDestinationPaths(UserMgmtRestSupport.NOTIFD_DESTINATION_PATHS);
    }

    @Test
    public void testCategoryAuthorizationLifecycle() throws Exception {
        createGroup("dbcats");
        createCategory("Edge");
        createCategory("Core");
        int version = version("dbcats");

        sendRequest(PUT, "/groups/dbcats/categories/Edge", 204);
        assertEquals(List.of("Edge"), authorizedCategories("dbcats"));
        version = assertNewVersion("dbcats", version);

        assertEquals("Edge", JaxbUtils.unmarshal(OnmsCategory.class, sendRequest(GET, "/groups/dbcats/categories/Edge", 200)).getName());
        sendRequest(GET, "/groups/dbcats/categories/Core", 404);
        assertEquals(List.of("Edge"), categoryNames(sendRequest(GET, "/groups/dbcats/categories", 200)));

        // a second authorization for the same category changes nothing
        sendRequest(PUT, "/groups/dbcats/categories/Edge", 400);
        assertEquals(version, version("dbcats"));

        sendRequest(PUT, "/groups/dbcats/categories/Core", 204);
        assertEquals(List.of("Core", "Edge"), authorizedCategories("dbcats"));
        version = assertNewVersion("dbcats", version);

        sendRequest(DELETE, "/groups/dbcats/categories/Edge", 204);
        assertEquals(List.of("Core"), authorizedCategories("dbcats"));
        version = assertNewVersion("dbcats", version);

        sendRequest(DELETE, "/groups/dbcats/categories/Edge", 400);
        assertEquals(version, version("dbcats"));
        sendRequest(GET, "/groups/dbcats/categories/Edge", 404);
    }

    @Test
    public void testGroupWritesRightAfterACategoryChange() throws Exception {
        // a category authorization is not cached by the group manager, so its change must not make the manager's next
        // save fail with "User data was changed by another process" (a 500)
        createGroup("dbcats");
        createCategory("Edge");
        sendRequest(PUT, "/groups/dbcats/categories/Edge", 204);

        sendPut("/groups/dbcats", "comments=After a category", 204);
        assertEquals("After a category", new JdbcTemplate(m_dataSource).queryForObject("select comments from groups where name = 'dbcats'", String.class));
        assertEquals(List.of("Edge"), authorizedCategories("dbcats"));

        sendRequest(DELETE, "/groups/dbcats/categories/Edge", 204);
        sendPut("/groups/dbcats", "comments=After another", 204);
        assertTrue(authorizedCategories("dbcats").isEmpty());
    }

    @Test
    public void testGroupWritesRightAfterTheCategoryEndpointsChangeAuthorizations() throws Exception {
        // the category endpoints change category_group without the group manager
        createGroup("dbcats");
        createCategory("Edge");
        int version = version("dbcats");

        // a form update cannot set authorizedGroups (304), so it is no category_group writer
        sendPut("/categories/Edge", "authorizedGroups=dbcats", 304);
        assertTrue(authorizedCategories("dbcats").isEmpty());

        sendRequest(PUT, "/categories/Edge/groups/dbcats", 204);
        assertEquals(List.of("Edge"), authorizedCategories("dbcats"));
        version = assertNewVersion("dbcats", version);
        sendPut("/groups/dbcats", "comments=After an authorization", 204);
        version = assertNewVersion("dbcats", version);

        sendRequest(DELETE, "/categories/Edge/groups/dbcats", 204);
        version = assertNewVersion("dbcats", version);
        sendPut("/groups/dbcats", "comments=After a withdrawal", 204);
        sendRequest(PUT, "/categories/Edge/groups/dbcats", 204);
        version = version("dbcats");

        sendRequest(DELETE, "/categories/Edge", 204);
        assertTrue(authorizedCategories("dbcats").isEmpty());
        version = assertNewVersion("dbcats", version);
        sendPut("/groups/dbcats", "comments=After a delete", 204);
        assertEquals("After a delete", new JdbcTemplate(m_dataSource).queryForObject("select comments from groups where name = 'dbcats'", String.class));
    }

    @Test
    public void testCategoryUpdateKeepsTheAuthorizationsAndOnlyARenameMovesTheVersions() throws Exception {
        createGroup("dbcats");
        createGroup("dbcats2");
        createCategory("Edge");
        sendRequest(PUT, "/categories/Edge/groups/dbcats", 204);
        sendRequest(PUT, "/categories/Edge/groups/dbcats2", 204);
        final int version = version("dbcats");
        final int version2 = version("dbcats2");

        // an update of the category itself rewrites none of its authorizations, so a group editor isn't stale; the list
        // first, as a category editor reads it: it fills the category cache of this thread, which outlives the request
        sendRequest(GET, "/categories", 200);
        sendPut("/categories/Edge", "description=Edge devices", 204);
        sendPut("/categories/Edge", "description=Edge routers", 204);
        assertEquals(version, version("dbcats"));
        assertEquals(version2, version("dbcats2"));
        assertEquals(List.of("Edge"), authorizedCategories("dbcats"));

        // a rename changes the category names a v2 group shows
        sendPut("/categories/Edge", "name=Perimeter", 204);
        assertNewVersion("dbcats", version);
        assertNewVersion("dbcats2", version2);
        assertEquals(List.of("Perimeter"), authorizedCategories("dbcats"));
    }

    @Test
    public void testRenameAndDeleteRightAfterACategoryChange() throws Exception {
        createGroup("dbcats");
        createGroup("other");
        createCategory("Edge");
        sendRequest(PUT, "/groups/dbcats/categories/Edge", 204);

        sendRequest(DELETE, "/groups/dbcats", 204);
        assertTrue(authorizedCategories("dbcats").isEmpty());
        sendPut("/groups/other", "comments=After a delete", 204);
        sendRequest(PUT, "/groups/other/categories/Edge", 204);
        sendPut("/groups/other", "comments=Again", 204);
        assertEquals(List.of("Edge"), authorizedCategories("other"));
    }

    @Test
    public void testUnknownCategoryOrGroup() throws Exception {
        createGroup("dbcats");
        final int version = version("dbcats");

        sendRequest(PUT, "/groups/dbcats/categories/Nope", 400);
        sendRequest(DELETE, "/groups/dbcats/categories/Nope", 400);
        assertEquals(version, version("dbcats"));

        createCategory("Edge");
        sendRequest(PUT, "/groups/nobody/categories/Edge", 404);
        sendRequest(GET, "/groups/nobody/categories", 404);
        assertTrue(authorizedCategories("nobody").isEmpty());
        assertTrue(authorizedCategories("dbcats").isEmpty());
    }

    @Test
    public void testDeletingTheGroupClearsItsAuthorizations() throws Exception {
        createGroup("dbcats");
        createCategory("Edge");
        sendRequest(PUT, "/groups/dbcats/categories/Edge", 204);

        sendRequest(DELETE, "/groups/dbcats", 204);

        assertEquals(0, (int) new JdbcTemplate(m_dataSource).queryForObject("select count(*) from groups where name = 'dbcats'", Integer.class));
        assertTrue(authorizedCategories("dbcats").isEmpty());
    }

    @Test
    public void testCreateRefusesReservedAndInvalidNames() throws Exception {
        for (final String name : new String[] { "item", "available-categories", "admin", "ADMIN", "a/b", "&lt;b&gt;", "a b", "a%b" }) {
            sendPost("/groups", "<group><name>" + name + "</name></group>", 400);
            assertEquals(name + " must not exist", 0, count("select count(*) from groups where name = '" + name + "'"));
        }
        sendPost("/groups", "<group><comments>nameless</comments></group>", 400);
        assertEquals(0, count("select count(*) from groups"));
    }

    @Test
    public void testBadDutyScheduleIsRefused() throws Exception {
        // an OnmsGroup, the body of POST and the form of PUT, has no duty schedules: a form key for them matches no
        // property and is skipped, so no bad schedule gets in that way
        createGroup("dbduty");
        sendPut("/groups/dbduty", "dutySchedule=MoTuWeThFrSaSu8000-1700", 304);
        sendPut("/groups/dbduty", "dutySchedules=garbage", 304);
        assertEquals(0, count("select count(*) from group_duty_schedules"));
    }

    @Test
    public void testUnknownMemberIs400() throws Exception {
        createUser("jroe");

        assertEquals("ghost is not a user.", sendData(POST, "application/xml",
                "/groups", "<group><name>dbmembers</name><user>jroe</user><user>ghost</user></group>", 400).getContentAsString());
        assertEquals(0, count("select count(*) from groups where name = 'dbmembers'"));

        sendPost("/groups", "<group><name>dbmembers</name><user>jroe</user></group>", 201, "/groups/dbmembers");
        final int version = version("dbmembers");
        assertEquals("ghost is not a user.", sendData(PUT, "application/x-www-form-urlencoded",
                "/groups/dbmembers", "users=ghost", 400).getContentAsString());
        sendRequest(PUT, "/groups/dbmembers/users/ghost", 400);
        assertEquals(version, version("dbmembers"));
        assertEquals(List.of("jroe"), members("dbmembers"));
    }

    @Test
    public void testRenameThroughFormPutIs400() throws Exception {
        createGroup("dbrename");
        final int version = version("dbrename");

        assertEquals("Renaming is not supported by the deprecated v1 API; use PUT /api/v2/groups/{name}/rename.",
                sendData(PUT, "application/x-www-form-urlencoded", "/groups/dbrename", "name=other&comments=changed", 400).getContentAsString());
        assertEquals(version, version("dbrename"));
        assertEquals(0, count("select count(*) from groups where name = 'other'"));
        assertEquals(1, count("select count(*) from groups where name = 'dbrename'"));
        assertEquals(0, count("select count(*) from groups where comments = 'changed'"));
    }

    @Test
    public void testDestinationPathTargetCannotBeDeleted() throws Exception {
        createGroup("dbtarget");
        final Path path = new Path("night");
        path.addTarget(new Target("dbtarget", "javaEmail"));
        UserMgmtRestSupport.setDestinationPaths(() -> (Collection<Path>) List.of(path));

        assertEquals("Group dbtarget is a target of destination path(s) night; remove it from them first.",
                sendData(DELETE, "application/xml", "/groups/dbtarget", "", 409).getContentAsString());
        assertEquals(1, count("select count(*) from groups where name = 'dbtarget'"));

        UserMgmtRestSupport.setDestinationPaths(() -> List.<Path>of());
        sendRequest(DELETE, "/groups/dbtarget", 204);
        assertEquals(0, count("select count(*) from groups where name = 'dbtarget'"));
    }

    @Test
    public void testMembershipGroupCannotBeDeleted() throws Exception {
        createUser("boss");
        createGroup("dbonCall");
        inTx(() -> {
            m_onCallRoleDao.save(new OnCallRole("bossrole", m_userGroupDao.findByName("dbonCall"), m_userAccountDao.findByUsername("boss")));
            return null;
        });

        assertEquals("Group \"dbonCall\" is the membership group of the on-call role \"bossrole\". Change the membership group first.",
                sendData(DELETE, "application/xml", "/groups/dbonCall", "", 409).getContentAsString());
        assertEquals(1, count("select count(*) from groups where name = 'dbonCall'"));
    }

    @Test
    public void testV1SavesKeepDutySchedulesAndDefaultMap() throws Exception {
        createGroup("dbkeep");
        new JdbcTemplate(m_dataSource).update("update groups set default_map = 'Map1' where name = 'dbkeep'");
        new JdbcTemplate(m_dataSource).update("insert into group_duty_schedules (group_id, position, schedule)"
                + " select id, 0, 'MoTuWeThFr800-1700' from groups where name = 'dbkeep'");

        // the SQL change is one the group manager's cache has not seen: the save is a 409, and the retry is fine
        assertEquals("User data was changed by another process. Try again.",
                sendData(PUT, "application/x-www-form-urlencoded", "/groups/dbkeep", "comments=edited", 409).getContentAsString());
        sendPut("/groups/dbkeep", "comments=edited", 204);
        assertKept("dbkeep", "edited");

        sendPost("/groups", "<group><name>dbkeep</name><comments>overwritten</comments></group>", 201, "/groups/dbkeep");
        assertKept("dbkeep", "overwritten");
    }

    private void assertKept(final String group, final String comments) {
        final JdbcTemplate jdbc = new JdbcTemplate(m_dataSource);
        assertEquals(comments, jdbc.queryForObject("select comments from groups where name = ?", String.class, group));
        assertEquals("Map1", jdbc.queryForObject("select default_map from groups where name = ?", String.class, group));
        assertEquals(List.of("MoTuWeThFr800-1700"), jdbc.queryForList("select s.schedule from group_duty_schedules s"
                + " join groups g on g.id = s.group_id where g.name = ?", String.class, group));
    }

    @Test
    public void testGroupCrudStillWorks() throws Exception {
        createUser("jroe");
        createUser("jdoe");

        sendPost("/groups", "<group><name>dbcrud</name><comments>first</comments><user>jroe</user></group>", 201, "/groups/dbcrud");
        assertEquals(List.of("jroe"), members("dbcrud"));

        sendPut("/groups/dbcrud", "comments=second", 204);
        assertEquals("second", new JdbcTemplate(m_dataSource).queryForObject("select comments from groups where name = 'dbcrud'", String.class));
        sendRequest(PUT, "/groups/dbcrud/users/jdoe", 204);
        assertEquals(List.of("jdoe", "jroe"), members("dbcrud"));
        sendRequest(DELETE, "/groups/dbcrud/users/jroe", 204);
        assertEquals(List.of("jdoe"), members("dbcrud"));

        // a POST of a stored name overwrites the group, as before
        sendPost("/groups", "<group><name>dbcrud</name><comments>again</comments><user>jroe</user></group>", 201, "/groups/dbcrud");
        assertEquals(List.of("jroe"), members("dbcrud"));

        sendRequest(DELETE, "/groups/dbcrud", 204);
        sendRequest(GET, "/groups/dbcrud", 404);
        assertEquals(0, count("select count(*) from groups where name = 'dbcrud'"));
    }

    private void createUser(final String name) throws Exception {
        final OnmsUser user = new OnmsUser();
        user.setUsername(name);
        user.setPassword("blah");
        sendPost("/users", JaxbUtils.marshal(user), 201, "/users/" + name);
    }

    private int count(final String sql) {
        return new JdbcTemplate(m_dataSource).queryForObject(sql, Integer.class);
    }

    private List<String> members(final String group) {
        return new JdbcTemplate(m_dataSource).queryForList(
                "select u.username from group_members m join groups g on g.id = m.group_id"
                + " join users u on u.id = m.user_id where g.name = ? order by u.username", String.class, group);
    }

    private <T> T inTx(final Supplier<T> work) {
        return new TransactionTemplate(m_transactionManager).execute(status -> work.get());
    }

    private void createGroup(final String name) throws Exception {
        sendPost("/groups", "<group><name>" + name + "</name></group>", 201, "/groups/" + name);
    }

    private void createCategory(final String name) throws Exception {
        sendPost("/categories", JaxbUtils.marshal(new OnmsCategory(name)), 201, "/categories/" + name);
    }

    private int version(final String group) {
        return new JdbcTemplate(m_dataSource).queryForObject("select version from groups where name = ?", Integer.class, group);
    }

    /** Asserts that the group has a new version and returns it. */
    private int assertNewVersion(final String group, final int before) {
        final int after = version(group);
        assertTrue(group + " should have a new version, still " + before, after != before);
        return after;
    }

    private List<String> authorizedCategories(final String group) {
        return new JdbcTemplate(m_dataSource).queryForList(
                "select c.categoryname from category_group g join categories c on c.categoryid = g.categoryid"
                + " where g.groupid = ? order by c.categoryname", String.class, group);
    }

    private static List<String> categoryNames(final String xml) {
        return JaxbUtils.unmarshal(OnmsCategoryCollection.class, xml).getCategories().stream()
                .map(OnmsCategory::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
