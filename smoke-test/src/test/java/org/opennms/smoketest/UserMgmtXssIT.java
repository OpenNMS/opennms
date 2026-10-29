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
package org.opennms.smoketest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.rest.AbstractRestIT;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.google.common.collect.ImmutableMap;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Stores markup and quotes in the text fields of a user, a group and an on-call role, in the name of a destination
 * path and in the fields of an event notification, then opens the legacy JSP pages and the Vue pages that show them.
 * No script may run, and the text must appear as text.
 *
 * <p>The v2 REST API refuses markup in names and comments. It still accepts it in full names, e-mail addresses and
 * role descriptions, so those objects are created through the API. The names that reach JavaScript strings,
 * {@code javascript:} links and {@code id} attributes are created another way. The user ID and the role name go through
 * the classic pages: the role pages do not check the name, and a user ID only may not hold quotes or markup. The
 * classic group pages refuse quotes and markup in a group name and comments, but groups migrated from
 * {@code groups.xml} can hold them, so the group gets its name and comments in the database.</p>
 *
 * <p>The objects are created once for the class and removed when it is done. Every test only reads them, except
 * {@link #testDeleteLinksReachTheRightObject()}, which creates and deletes its own.</p>
 */
public class UserMgmtXssIT extends AbstractRestIT {
    private static final String BASE = "/opennms/api/v2/";
    private static final String DESTINATION_PATHS = BASE + "notification-config/destination-paths";
    private static final String EVENT_NOTIFICATIONS = BASE + "notification-config/event-notifications";

    private static final String USER = "xss1";
    private static final String GROUP = "xssgroup";
    private static final String GROUP_COMMENTS = "Group comments";
    private static final String ROLE = "xss role";

    private static final String FULL_NAME = "<img src=x onerror=window.__xss=1>\"'";
    private static final String EMAIL = "\"><script>window.__xss=2</script>@x.org";
    // Names the v2 API refuses. They break out of a JS string ('), an attribute (") and the markup (<>), and they carry a
    // percent-encoded quote, which a javascript: URL decodes before the script runs.
    private static final String CLASSIC_GROUP = "xssg');window.__xss=5;//<u>\"%27);window.__xss=6;//";
    // the plain name the group is created with, before it gets CLASSIC_GROUP in the database
    private static final String CLASSIC_GROUP_SEED = "xssclassicgroup";
    private static final String CLASSIC_ROLE = "xssr');window.__xss=5;//<u>\"%27);window.__xss=6;//";
    // a user ID may not contain quotes or markup; a percent-encoded quote and a semicolon get through
    private static final String CLASSIC_USER = "xssu%27);window.__xss=7;//";
    private static final String CLASSIC_COMMENTS = "<img src=x onerror=window.__xss=8> \"quoted\" 'single'";

    // the same kind of names for the objects the delete test removes; none is a part of another name
    private static final String DELETE_GROUP = "xssdg');window.__xss=5;//<u>\"%27);window.__xss=6;//";
    private static final String DELETE_GROUP_SEED = "xssdeletegroup";
    private static final String DELETE_ROLE = "xssdr');window.__xss=5;//<u>\"%27);window.__xss=6;//";
    private static final String DELETE_USER = "xssdu%27);window.__xss=7;//";

    private static final String DESCRIPTION = "<script>window.__xss=3</script> \"quoted\" 'single' <img src=x onerror=window.__xss=4>";

    // A destination path name with tags, an ampersand, both quotes and JS-string breakouts. The wizard refuses spaces
    // in a path name and the API refuses /, \, % and control characters, so the tags have no attributes. "&amp" without
    // the ; is still a character reference to a browser, followed by a character that is neither = nor a letter or
    // digit: a page that decodes the name once more shows "&", one that encodes it twice shows "&amp;amp", and a page
    // that does not encode it loses the tags. The name has no ; or +, so the path of an API request holds it as is.
    private static final String DESTINATION_PATH_PREFIX = "xssp";
    private static final String DESTINATION_PATH = DESTINATION_PATH_PREFIX
            + "&<u>&amp'-(window.__xss=10)-'\"-(window.__xss=11)-\"<b>x";

    // An event notification for notifsForUEI.jsp. Its name breaks out of a JS string (') and an attribute ("), holds a
    // tag and an ampersand, and carries a percent-encoded quote, which a javascript: URL decodes before the script runs.
    // The API refuses the % and checks the rule, so the notification is created with the seed name and gets the other
    // values from the classic wizard, which checks neither. Both names start with the prefix, the path's with the
    // destination path prefix, so the clean-up finds them.
    private static final String NOTIFICATION_PREFIX = "xssn";
    private static final String NOTIFICATION_SEED = NOTIFICATION_PREFIX + "seed";
    private static final String NOTIFICATION = NOTIFICATION_PREFIX + "');window.__xss=12;//<u>\"&amp%27);window.__xss=13;//";
    private static final String NOTIFICATION_UEI = "uei.opennms.org/smoketest/xssNotification";
    private static final String NOTIFICATION_PATH = DESTINATION_PATH_PREFIX + "notification";
    // the rule is never run: no event has the UEI
    private static final String NOTIFICATION_RULE = "IPADDR IPLIKE *.*.*.* & (nodelabel == '<img src=x onerror=window.__xss=14>&amp')";
    private static final String VARBIND_NAME = "<img src=x onerror=window.__xss=15>";
    private static final String VARBIND_VALUE = "<u>v</u>&amp\"'";

    // the shared objects are created by the first test and removed after the last one
    private static boolean fixturesAttempted;
    private static Throwable fixtureError;

    public UserMgmtXssIT() {
        super(Version.V2, "");
    }

    @Before
    public void createFixturesOnce() throws Exception {
        if (fixtureError != null) {
            throw new AssertionError("The objects of the test could not be created", fixtureError);
        }
        if (fixturesAttempted) {
            return;
        }
        fixturesAttempted = true;
        try {
            cleanUp();
            createApiObjects();
            createClassicGroup(CLASSIC_GROUP, CLASSIC_GROUP_SEED);
            createClassicUser(CLASSIC_USER);
            createClassicRole(CLASSIC_ROLE);
        } catch (final Exception | AssertionError e) {
            fixtureError = e;
            throw e;
        }
    }

    /** Creates the user, the group and the role the v2 API accepts. */
    private void createApiObjects() {
        final Map<String, Object> user = ImmutableMap.of(
                "userId", USER,
                "fullName", FULL_NAME,
                "userComments", "x",
                "email", EMAIL,
                "password", "SmokeTestPassword1");
        given().basePath(BASE + "users").contentType(ContentType.JSON).body(user).post().then().statusCode(201);

        final Map<String, Object> group = ImmutableMap.of(
                "name", GROUP,
                "comments", GROUP_COMMENTS,
                "users", new String[] { USER });
        given().basePath(BASE + "groups").contentType(ContentType.JSON).body(group).post().then().statusCode(201);

        final Map<String, Object> role = ImmutableMap.of(
                "name", ROLE,
                "description", DESCRIPTION,
                "membershipGroup", GROUP,
                "supervisor", USER,
                "schedules", new Object[] { ImmutableMap.of(
                        "user", USER,
                        "type", "daily",
                        "times", new Object[] { ImmutableMap.of("begins", "00:00:00", "ends", "23:59:59") }) });
        given().basePath(BASE + "oncall-roles").contentType(ContentType.JSON).body(role).post().then().statusCode(201);
    }

    /**
     * No page or API takes a group name with quotes or markup, so create a plain group and rename it in the database.
     * The group manager sees the change on its next check, within a second.
     */
    private void createClassicGroup(final String name, final String seed) throws Exception {
        given().basePath(BASE + "groups").contentType(ContentType.JSON).body(ImmutableMap.of("name", seed))
                .post().then().statusCode(201);
        sql("UPDATE groups SET name = ?, comments = ? WHERE name = ?", name, CLASSIC_COMMENTS, seed);
        open(base() + "admin/userGroupView/groups/list.htm");
        wait.until(d -> {
            d.navigate().refresh();
            return ((JavascriptExecutor) d).executeScript("return document.getElementById(arguments[0]) !== null", name + ".doModify");
        });
    }

    /** The page checks the user ID in the browser only; submit the form without that check. */
    private void createClassicUser(final String userId) throws Exception {
        open(base() + "admin/userGroupView/users/newUser.jsp");
        setValue("userID", userId);
        setValue("pass1", "SmokeTestPassword1");
        setValue("pass2", "SmokeTestPassword1");
        submitForm("newUserForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fullName")));
        setValue("fullName", FULL_NAME);
        setValue("userComments", CLASSIC_COMMENTS);
        findElementById("saveUserButton").click();
        waitForId("users(" + userId + ").doModify");
    }

    /** The role pages do not check the name. The role has USER as its supervisor and GROUP as its membership group. */
    private void createClassicRole(final String name) throws Exception {
        open(base() + "admin/userGroupView/roles");
        driver.findElement(By.xpath("//button[text()='Add New On-Call Role']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input_roleName")));
        setValue("input_roleName", name);
        setValue("input_roleDescr", DESCRIPTION);
        new Select(driver.findElement(By.id("input_roleUser"))).selectByVisibleText(USER);
        new Select(driver.findElement(By.id("input_roleGroup"))).selectByVisibleText(GROUP);
        clickToLoad(driver.findElement(By.name("save")));
        assertBodyContains(name);
        assertNothingRan();
    }

    @AfterClass
    public static void removeFixtures() throws SQLException {
        // a test ran, so RestAssured points at the stack; the paths go even if the other objects could not
        final boolean testsRan = fixturesAttempted;
        fixturesAttempted = false;
        fixtureError = null;
        if (testsRan) {
            try {
                cleanUp();
            } finally {
                deleteDestinationPaths();
            }
        }
    }

    /** Removes every object of the test that exists; an object that does not exist is not an error. */
    private static void cleanUp() throws SQLException {
        RestAssured.authentication = preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
        // The API refuses the classic names, so delete those objects in the database. The classic roles name USER
        // and GROUP, so they go first.
        sql("DELETE FROM oncall_roles WHERE name IN (?, ?)", CLASSIC_ROLE, DELETE_ROLE);
        sql("DELETE FROM groups WHERE name IN (?, ?, ?, ?)", CLASSIC_GROUP, CLASSIC_GROUP_SEED, DELETE_GROUP, DELETE_GROUP_SEED);
        sql("DELETE FROM users WHERE username IN (?, ?)", CLASSIC_USER, DELETE_USER);
        given().basePath(BASE + "oncall-roles").delete(ROLE);
        given().basePath(BASE + "groups").delete(GROUP);
        given().basePath(BASE + "users").delete(USER);
    }

    /** Deletes the destination paths whose names start with the prefix of the test, including a changed name. */
    private static void deleteDestinationPaths() {
        RestAssured.authentication = preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
        for (final String name : destinationPathNames()) {
            if (name.startsWith(DESTINATION_PATH_PREFIX)) {
                given().basePath(DESTINATION_PATHS).pathParam("name", name).delete("{name}");
            }
        }
    }

    /** The names of all destination paths. */
    private static List<String> destinationPathNames() {
        final Response response = given().basePath(DESTINATION_PATHS).accept(ContentType.JSON).get();
        assertEquals(response.asString(), 200, response.getStatusCode());
        final List<String> names = response.jsonPath().getList("path.name", String.class);
        return names == null ? Collections.emptyList() : names;
    }

    /** Runs one statement on the OpenNMS database of the stack. */
    private static void sql(final String statement, final String... args) throws SQLException {
        final PostgreSQLContainer postgres = stack.postgres();
        final String url = "jdbc:postgresql://" + postgres.getContainerIpAddress() + ":"
                + postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) + "/opennms";
        try (Connection connection = DriverManager.getConnection(url, "opennms", "opennms");
             PreparedStatement ps = connection.prepareStatement(statement)) {
            for (int i = 0; i < args.length; i++) {
                ps.setString(i + 1, args[i]);
            }
            ps.executeUpdate();
        }
    }

    @Test
    public void testUserPages() throws Exception {
        // users list: the full name and the email of each user
        open(base() + "admin/userGroupView/users/list.jsp");
        assertText(By.id("users(" + USER + ").fullName"), FULL_NAME);
        assertText(By.id("users(" + USER + ").email"), EMAIL);
        assertNothingRan();

        // user detail
        open(base() + "admin/userGroupView/users/userDetail.jsp?userID=" + USER);
        assertBodyContains(FULL_NAME);
        assertBodyContains(EMAIL);
        assertNothingRan();

        // user modify page, reached from the list. The fields hold the values, so compare the inputs.
        open(base() + "admin/userGroupView/users/list.jsp");
        clickToLoad(findElementById("users(" + USER + ").doModify"));
        assertEquals(FULL_NAME, driver.findElement(By.id("fullName")).getAttribute("value"));
        assertEquals(EMAIL, driver.findElement(By.id("email")).getAttribute("value"));
        assertNothingRan();

        // the classic user ID is in onclick handlers and in javascript: URLs of the list
        open(base() + "admin/userGroupView/users/list.jsp");
        assertBodyContains(CLASSIC_USER);
        trapXss();
        clickToLoad(byId("users(" + CLASSIC_USER + ").doModify"));
        assertEquals(FULL_NAME, driver.findElement(By.id("fullName")).getAttribute("value"));
        assertNothingRan();

        open(base() + "admin/userGroupView/users/list.jsp");
        trapXss();
        clickToLoad(byId("users(" + CLASSIC_USER + ").doDetails"));
        assertBodyContains(CLASSIC_USER);
        assertNothingRan();
    }

    @Test
    public void testGroupPages() throws Exception {
        // groups list: the group name and the comments, not the members
        open(base() + "admin/userGroupView/groups/list.htm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("group-" + GROUP)));
        assertBodyContains(GROUP);
        assertBodyContains(GROUP_COMMENTS);
        assertNothingRan();

        // GET modifyGroup?groupName= without an operation shows the group's detail page (read-only)
        open(base() + "admin/userGroupView/groups/modifyGroup?groupName=" + GROUP);
        assertBodyContains("Details for Group: " + GROUP);
        assertBodyContains(GROUP_COMMENTS);
        assertBodyContains(USER);
        assertNothingRan();

        // group modify page, reached from the list; the name is in the hidden input, the member in the user list
        open(base() + "admin/userGroupView/groups/list.htm");
        clickToLoad(findElementById(GROUP + ".doModify"));
        assertEquals(GROUP, driver.findElement(By.name("groupName")).getAttribute("value"));
        assertBodyContains(USER);
        assertNothingRan();

        // the classic group name is in id attributes, in onclick handlers and in javascript: URLs of the list
        open(base() + "admin/userGroupView/groups/list.htm");
        assertBodyContains(CLASSIC_GROUP);
        assertNothingRan();
        trapXss();
        clickToLoad(byId(CLASSIC_GROUP + ".doModify"));
        assertEquals(CLASSIC_GROUP, driver.findElement(By.name("groupName")).getAttribute("value"));
        assertNothingRan();
    }

    @Test
    public void testRolePages() throws Exception {
        // on-call role list (admin) and the role view
        open(base() + "admin/userGroupView/roles");
        assertBodyContains(DESCRIPTION);
        assertNothingRan();
        clickToLoad(driver.findElement(By.linkText(ROLE)));
        assertBodyContains(DESCRIPTION);
        assertNothingRan();

        // on-call role list (everyone) and its role view
        open(base() + "roles");
        assertBodyContains(DESCRIPTION);
        assertNothingRan();
        clickToLoad(driver.findElement(By.linkText(ROLE)));
        assertBodyContains(DESCRIPTION);
        assertNothingRan();

        // The full-name tooltips on the role pages: the supervisor and the scheduled user are USER, whose full name is markup.
        for (final String page : new String[] { "admin/userGroupView/roles", "roles" }) {
            open(base() + page);
            assertBodyContains(CLASSIC_ROLE);
            assertTitleAttribute(FULL_NAME);
            assertNothingRan();
            // the role link is a javascript: URL that holds the name; it must open the right role
            trapXss();
            clickToLoad(driver.findElement(By.linkText(CLASSIC_ROLE)));
            assertBodyContains(DESCRIPTION);
            assertBodyContains(CLASSIC_ROLE);
            // only the admin role view has the tooltips; the role view for everyone shows the user ids as plain text
            if (page.startsWith("admin/")) {
                assertTitleAttribute(FULL_NAME);
            }
            assertNothingRan();
        }

        // the role edit page: the name is in an attribute
        open(base() + "admin/userGroupView/roles");
        trapXss();
        clickToLoad(driver.findElement(By.linkText(CLASSIC_ROLE)));
        clickToLoad(driver.findElement(By.xpath("//button[text()='Value Details']")));
        assertEquals(CLASSIC_ROLE, driver.findElement(By.id("input_roleName")).getAttribute("value"));
        assertNothingRan();
    }

    /**
     * The delete links hold the names in a confirm() string and in a javascript: URL. The dialog must show the name as
     * stored, and the delete must reach the right object. The test deletes objects of its own, so the other tests keep
     * theirs whatever the order.
     */
    @Test
    public void testDeleteLinksReachTheRightObject() throws Exception {
        createClassicGroup(DELETE_GROUP, DELETE_GROUP_SEED);
        createClassicUser(DELETE_USER);
        createClassicRole(DELETE_ROLE);

        open(base() + "admin/userGroupView/groups/list.htm");
        trapXss();
        deleteAndConfirm(byId(DELETE_GROUP + ".doDelete"), "Are you sure you want to delete the group " + DELETE_GROUP + "?");
        assertBodyDoesNotContain(DELETE_GROUP);
        assertBodyContains(CLASSIC_GROUP);
        assertNothingRan();

        open(base() + "admin/userGroupView/users/list.jsp");
        trapXss();
        deleteAndConfirm(byId("users(" + DELETE_USER + ").doDelete"), "Are you sure you want to delete the user " + DELETE_USER + "?");
        assertBodyDoesNotContain(DELETE_USER);
        assertBodyContains(CLASSIC_USER);
        assertNothingRan();

        open(base() + "admin/userGroupView/roles");
        trapXss();
        final WebElement row = driver.findElement(By.xpath("//tr[.//a[text()=" + xpathLiteral(DELETE_ROLE) + "]]"));
        deleteAndConfirm(row.findElement(By.xpath(".//a[contains(@href, 'doDelete')]")), "Are you sure you want to delete the role " + DELETE_ROLE + "?");
        assertBodyDoesNotContain(DELETE_ROLE);
        assertBodyContains(CLASSIC_ROLE);
        assertNothingRan();
    }

    @Test
    public void testVuePages() throws Exception {
        // Manage Users
        open(base() + "ui/index.html#/admin/users");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='page-title' and text()='Manage Users']")));
        pageToRow("[data-test='users-table']", USER);
        assertBodyContains(FULL_NAME);
        assertBodyContains(EMAIL);
        assertNothingRan();
        pageToRow("[data-test='users-table']", CLASSIC_USER);
        assertNothingRan();

        // Manage Groups. GROUP_COMMENTS are plain text, so the members are what the page shows.
        open(base() + "ui/index.html#/admin/groups");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='page-title' and text()='Manage Groups']")));
        pageToRow("[data-test='groups-table']", GROUP);
        assertBodyContains(USER);
        assertNothingRan();
        pageToRow("[data-test='groups-table']", CLASSIC_GROUP);
        assertBodyContains(CLASSIC_COMMENTS);
        assertNothingRan();
    }

    /**
     * Walks a destination path with markup in its name through the wizard: the list, the outline, the targets, the
     * group intervals and the commands. Every page shows the name as text, and finishing the wizard keeps the name.
     */
    @Test
    public void testDestinationPathPages() throws Exception {
        deleteDestinationPaths();
        // the Admin group, with the only command of the smoke stack's notificationCommands.xml
        final Map<String, Object> path = ImmutableMap.of(
                "name", DESTINATION_PATH,
                "target", new Object[] { ImmutableMap.of("name", "Admin", "command", new String[] { "browser" }) },
                "escalate", new Object[0]);
        given().basePath(DESTINATION_PATHS).contentType(ContentType.JSON).body(path).post().then().statusCode(204);
        try {
            // the list: the name is the text and the value of an option
            open(base() + "admin/notification/destinationPaths.jsp");
            selectDestinationPath();
            assertNothingRan();
            clickToLoad(driver.findElement(By.cssSelector("form[name='path'] input[type='button'][value='Edit']")));

            // the outline: the name is in the heading and in the name field
            assertDestinationPathOutline();
            clickToLoad(driver.findElement(By.cssSelector("form[name='outline'] input[type='button'][value='Edit']")));

            // the targets: the Admin group is chosen, so the next step is the group intervals
            assertBodyContains("Editing path: " + DESTINATION_PATH);
            final List<String> groups = new Select(driver.findElement(By.cssSelector("form[name='targets'] select[name='groups']")))
                    .getAllSelectedOptions().stream().map(WebElement::getText).map(String::trim).collect(Collectors.toList());
            assertEquals(Collections.singletonList("Admin"), groups);
            assertNothingRan();
            clickToLoad(driver.findElement(By.partialLinkText("Next Step")));

            // the group intervals
            assertTrue("Not on the group intervals page: " + driver.getCurrentUrl(), driver.getCurrentUrl().contains("groupIntervals.jsp"));
            assertBodyContains("Editing path: " + DESTINATION_PATH);
            assertNothingRan();
            clickToLoad(driver.findElement(By.partialLinkText("Next Step")));

            // the commands
            assertTrue("Not on the commands page: " + driver.getCurrentUrl(), driver.getCurrentUrl().contains("chooseCommands.jsp"));
            assertBodyContains("Editing path: " + DESTINATION_PATH);
            assertNothingRan();
            clickToLoad(driver.findElement(By.partialLinkText("Next Step")));

            // back on the outline; finishing saves the path under the name of the name field
            assertDestinationPathOutline();
            clickToLoad(driver.findElement(By.cssSelector("form[name='outline'] input[type='submit'][value='Finish']")));

            // the list again, and the stored path: same name, same target, and no second path with a changed name
            assertTrue("Not on the destination paths page: " + driver.getCurrentUrl(), driver.getCurrentUrl().contains("destinationPaths.jsp"));
            selectDestinationPath();
            assertNothingRan();
            final List<String> names = destinationPathNames().stream()
                    .filter(name -> name.startsWith(DESTINATION_PATH_PREFIX)).collect(Collectors.toList());
            assertEquals(Collections.singletonList(DESTINATION_PATH), names);
            final Response stored = given().basePath(DESTINATION_PATHS).pathParam("name", DESTINATION_PATH).accept(ContentType.JSON).get("{name}");
            assertEquals(stored.asString(), 200, stored.getStatusCode());
            assertEquals(DESTINATION_PATH, stored.jsonPath().getString("name"));
            assertEquals(Collections.singletonList("Admin"), stored.jsonPath().getList("target.name", String.class));
        } finally {
            deleteDestinationPaths();
        }
    }

    /**
     * notifsForUEI.jsp lists the notifications of a UEI. It shows the name, the description, the rule and the varbind of
     * each as text, and its Edit link holds the name in a javascript: URL. The link must open the wizard on that
     * notification.
     */
    @Test
    public void testNotificationsForUeiPage() throws Exception {
        deleteNotifications();
        deleteDestinationPaths();
        final Map<String, Object> path = ImmutableMap.of(
                "name", NOTIFICATION_PATH,
                "target", new Object[] { ImmutableMap.of("name", "Admin", "command", new String[] { "browser" }) },
                "escalate", new Object[0]);
        given().basePath(DESTINATION_PATHS).contentType(ContentType.JSON).body(path).post().then().statusCode(204);
        try {
            final Map<String, Object> notification = ImmutableMap.<String, Object>builder()
                    .put("name", NOTIFICATION_SEED)
                    .put("status", "off")
                    .put("uei", NOTIFICATION_UEI)
                    .put("rule", ImmutableMap.of("value", "IPADDR IPLIKE *.*.*.*"))
                    .put("destinationPath", NOTIFICATION_PATH)
                    .put("text-message", "smoke test")
                    .build();
            given().basePath(EVENT_NOTIFICATIONS).contentType(ContentType.JSON).body(notification).post().then().statusCode(204);

            // the classic wizard: edit the seed, set the rule, then save it with the other values under the new name
            open(base() + "admin/notification/noticeWizard/notifsForUEI.jsp?uei=" + NOTIFICATION_UEI);
            postToNotificationWizard(ImmutableMap.of("sourcePage", "notifsForUEI.jsp", "userAction", "edit", "notice", NOTIFICATION_SEED));
            postToNotificationWizard(ImmutableMap.of("sourcePage", "validateRule.jsp", "newRule", NOTIFICATION_RULE));
            postToNotificationWizard(ImmutableMap.<String, String>builder()
                    .put("sourcePage", "choosePath.jsp")
                    .put("path", NOTIFICATION_PATH)
                    .put("name", NOTIFICATION)
                    .put("description", DESCRIPTION)
                    .put("textMsg", "smoke test")
                    .put("varbindName", VARBIND_NAME)
                    .put("varbindValue", VARBIND_VALUE)
                    .build());
            assertEquals(Collections.singletonList(NOTIFICATION), notificationNames());

            // the list: every field is text
            open(base() + "admin/notification/noticeWizard/notifsForUEI.jsp?uei=" + NOTIFICATION_UEI);
            final WebElement row = driver.findElement(By.xpath("//tr[td[1]=" + xpathLiteral(NOTIFICATION) + "]"));
            final List<String> cells = row.findElements(By.tagName("td")).stream()
                    .map(WebElement::getText).map(String::trim).collect(Collectors.toList());
            assertEquals(Arrays.asList(NOTIFICATION, DESCRIPTION, NOTIFICATION_RULE, NOTIFICATION_PATH,
                    VARBIND_NAME + "=" + VARBIND_VALUE, "Edit"), cells);
            assertNothingRan();

            // the Edit link opens the wizard on this notification, which the wizard names in its heading
            trapXss();
            clickToLoad(row.findElement(By.linkText("Edit")));
            assertTrue("Not on the choose UEIs page: " + driver.getCurrentUrl(), driver.getCurrentUrl().contains("chooseUeis.htm"));
            assertEquals("Editing notice: " + NOTIFICATION,
                    driver.findElement(By.xpath("//h2[starts-with(., 'Editing notice: ')]")).getText().trim());
            assertEquals(NOTIFICATION_UEI, driver.findElement(By.id("regexp")).getAttribute("value"));
            assertNothingRan();
        } finally {
            try {
                deleteNotifications();
            } finally {
                deleteDestinationPaths();
            }
        }
    }

    /** The names of the event notifications of the test. */
    private static List<String> notificationNames() {
        final Response response = given().basePath(EVENT_NOTIFICATIONS).accept(ContentType.JSON).get();
        assertEquals(response.asString(), 200, response.getStatusCode());
        final List<String> names = response.jsonPath().getList("notification.name", String.class);
        return names == null ? Collections.emptyList()
                : names.stream().filter(name -> name.startsWith(NOTIFICATION_PREFIX)).collect(Collectors.toList());
    }

    /** Deletes the event notifications of the test through the classic wizard; the API cannot address a name with %. */
    private void deleteNotifications() {
        for (final String name : notificationNames()) {
            postToNotificationWizard(ImmutableMap.of("sourcePage", "eventNotices.htm", "userAction", "delete", "notice", name));
        }
        assertEquals(Collections.emptyList(), notificationNames());
    }

    /**
     * Posts the fields to the notification wizard the way its pages do, and waits for the page it redirects to. The
     * wizard keeps the notification it edits in the browser's session.
     */
    private void postToNotificationWizard(final Map<String, String> fields) {
        final WebElement page = driver.findElement(By.tagName("html"));
        ((JavascriptExecutor) driver).executeScript(
                "var form = document.createElement('form'); form.method = 'post'; form.action = arguments[0];"
                + " var fields = arguments[1];"
                + " Object.keys(fields).forEach(function(name) {"
                + " var input = document.createElement('input'); input.type = 'hidden'; input.name = name; input.value = fields[name];"
                + " form.appendChild(input); });"
                + " document.body.appendChild(form); form.submit();",
                base() + "admin/notification/noticeWizard/notificationWizard", fields);
        wait.until(ExpectedConditions.stalenessOf(page));
        waitForLoad();
    }

    /** Selects DESTINATION_PATH in the list of destinationPaths.jsp, whose option must show the name as text. */
    private void selectDestinationPath() {
        final Select paths = new Select(driver.findElement(By.cssSelector("form[name='path'] select[name='paths']")));
        final List<WebElement> options = paths.getOptions();
        final List<String> values = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            final String value = options.get(i).getAttribute("value");
            values.add(value);
            if (DESTINATION_PATH.equals(value)) {
                assertEquals(DESTINATION_PATH, options.get(i).getText().trim());
                paths.selectByIndex(i);
                return;
            }
        }
        throw new AssertionError("No destination path \"" + DESTINATION_PATH + "\" in the list " + values);
    }

    /** pathOutline.jsp shows the name in its heading and holds it in the name field. */
    private void assertDestinationPathOutline() {
        assertTrue("Not on the path outline page: " + driver.getCurrentUrl(), driver.getCurrentUrl().contains("pathOutline.jsp"));
        assertBodyContains("Editing path: " + DESTINATION_PATH);
        assertEquals(DESTINATION_PATH, driver.findElement(By.cssSelector("form[name='outline'] input[name='name']")).getAttribute("value"));
        assertNothingRan();
    }

    private String base() {
        return getBaseUrlInternal() + "opennms/";
    }

    /**
     * The Vue tables have no filter, so page forward from the first page until the row with the given text shows.
     * Each click waits until the paginator marks another page as current.
     */
    private void pageToRow(final String tableSelector, final String text) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(tableSelector)));
        assertNotNull("The paginator of " + tableSelector + " shows no current page", currentPage(tableSelector));
        if (!"1".equals(currentPage(tableSelector))) {
            clickPaginatorButton(tableSelector, driver.findElement(By.cssSelector(tableSelector + " .p-paginator-first")));
        }
        int pages = 1;
        while (!driver.findElement(By.cssSelector(tableSelector)).getText().contains(text)) {
            final WebElement next = driver.findElement(By.cssSelector(tableSelector + " .p-paginator-next"));
            if (!next.isEnabled() || next.getAttribute("class").contains("p-disabled")) {
                throw new AssertionError("The row \"" + text + "\" is not in the table on any of " + pages + " pages");
            }
            clickPaginatorButton(tableSelector, next);
            pages++;
        }
    }

    /** Clicks a button of the paginator of a Vue table and waits until another page is the current one. */
    private void clickPaginatorButton(final String tableSelector, final WebElement button) {
        final String page = currentPage(tableSelector);
        button.click();
        wait.until(d -> !Objects.equals(page, currentPage(tableSelector)));
    }

    /** The number of the page a Vue table shows, from the page link its paginator marks as current. */
    private String currentPage(final String tableSelector) {
        return (String) ((JavascriptExecutor) driver).executeScript(
                "var e = document.querySelector(arguments[0]); return e === null ? null : e.textContent.trim();",
                tableSelector + " .p-paginator-page[aria-current='page']");
    }

    /** Sets the value of the field with the given id or name, without the browser's typing and without client-side checks. */
    private void setValue(final String idOrName, final String value) {
        ((JavascriptExecutor) driver).executeScript(
                "var e = document.getElementById(arguments[0]) || document.getElementsByName(arguments[0])[0]; e.value = arguments[1];",
                idOrName, value);
    }

    /** Submits the form the way a script does, which skips its onsubmit check. */
    private void submitForm(final String formName) {
        ((JavascriptExecutor) driver).executeScript("document.forms[arguments[0]].submit()", formName);
    }

    /** Waits for the element with an id that holds markup or quotes, which a locator cannot hold. */
    private void waitForId(final String id) {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.getElementById(arguments[0]) !== null", id));
    }

    /** Finds an element by an id that holds markup or quotes, which a locator cannot hold. */
    private WebElement byId(final String id) {
        final Object element = ((JavascriptExecutor) driver).executeScript("return document.getElementById(arguments[0])", id);
        assertTrue("No element with the id \"" + id + "\" on " + driver.getCurrentUrl(), element != null);
        return (WebElement) element;
    }

    /** An XPath string literal for text with quotes. */
    private static String xpathLiteral(final String text) {
        return "concat('" + text.replace("'", "', \"'\", '") + "')";
    }

    /**
     * Clicks an element that loads another page, and waits until that page replaced this one and finished loading, so
     * an onerror handler of the new page had its turn. A wait for content alone can pass on the old page, which often
     * shows the same text.
     */
    private void clickToLoad(final WebElement element) {
        final WebElement page = driver.findElement(By.tagName("html"));
        element.click();
        wait.until(ExpectedConditions.stalenessOf(page));
        waitForLoad();
    }

    private void waitForLoad() {
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    /** Clicks a delete link, checks the text of its confirm() dialog, accepts it and waits for the next page. */
    private void deleteAndConfirm(final WebElement link, final String expectedText) {
        final WebElement page = driver.findElement(By.tagName("html"));
        link.click();
        final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals(expectedText, alert.getText());
        alert.accept();
        wait.until(ExpectedConditions.stalenessOf(page));
        waitForLoad();
    }

    /** At least one element shows the text as its title attribute. */
    private void assertTitleAttribute(final String title) {
        assertEquals("No element has the title \"" + title + "\" on " + driver.getCurrentUrl(), Boolean.TRUE,
                ((JavascriptExecutor) driver).executeScript(
                        "var title = arguments[0];"
                        + " return Array.from(document.querySelectorAll('[title]')).some(function(e) { return e.getAttribute('title') === title; })", title));
    }

    /**
     * Makes a script that sets window.__xss leave a mark that survives the navigation of a javascript: link, which
     * would otherwise take the page, and the mark, away. A hit on the current page is reported first, so that the mark
     * belongs to the page the link opens.
     */
    private void trapXss() {
        assertNothingRan();
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(window, '__xss', { configurable: true, set: function(v) { sessionStorage.setItem('xss', String(v)); } })");
    }

    /**
     * Opens a page with window.__xss and the mark of {@link #trapXss()} cleared first. A Vue route is only a change of
     * the hash, which keeps the window, and the mark lives as long as the tab, so without this a hit would be reported
     * on a later page.
     */
    private void open(final String url) throws InterruptedException {
        ((JavascriptExecutor) driver).executeScript(
                "try { delete window.__xss; sessionStorage.removeItem('xss'); } catch (e) { /* a page without storage */ }");
        driver.get(url);
        // Give an onerror handler time to fire; a page load alone would not catch it.
        Thread.sleep(500);
    }

    private void assertBodyContains(final String text) {
        final String body = driver.findElement(By.tagName("body")).getText();
        assertTrue("Expected the page to show \"" + text + "\" as text, but it showed:\n" + body, body.contains(text));
    }

    private void assertBodyDoesNotContain(final String text) {
        final String body = driver.findElement(By.tagName("body")).getText();
        assertFalse("Expected the page not to show \"" + text + "\", but it showed:\n" + body, body.contains(text));
    }

    private void assertText(final By by, final String text) {
        assertEquals(text, driver.findElement(by).getText().trim());
    }

    /** No script from the payloads ran, and no element was injected. */
    private void assertNothingRan() {
        final JavascriptExecutor js = (JavascriptExecutor) driver;
        assertEquals("A payload script ran on " + driver.getCurrentUrl(), "undefined", js.executeScript("return typeof window.__xss"));
        assertEquals("A payload script ran before " + driver.getCurrentUrl(), Boolean.TRUE, js.executeScript("return sessionStorage.getItem('xss') === null"));
        assertEquals("A payload element was injected on " + driver.getCurrentUrl(), 0L,
                js.executeScript("return document.querySelectorAll(\"img[src='x']\").length"));
    }
}
