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

import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * The v1 category endpoints of a group with the database store (no mock user and group managers, unlike
 * {@link GroupRestServiceIT}): the authorizations land in category_group, and every change moves the group's
 * version, so that a v2 editor holding the group gets 412.
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

    @Override
    protected void beforeServletStart() {
        MockLogAppender.setupLogging();
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
