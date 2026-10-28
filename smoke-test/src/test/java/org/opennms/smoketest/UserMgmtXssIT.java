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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.rest.AbstractRestIT;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.collect.ImmutableMap;

import io.restassured.http.ContentType;

/**
 * Stores markup and quotes in the text fields of a user, a group and an on-call role, then opens the legacy JSP
 * pages and the Vue pages that show them. No script may run, and the text must appear as text.
 *
 * <p>The v2 REST API refuses markup in names and comments. It still accepts it in full names, e-mail addresses and
 * role descriptions, so those objects are created through the API. The names that reach JavaScript strings,
 * {@code javascript:} links and {@code id} attributes are created another way. The user ID and the role name go through
 * the classic pages: the role pages do not check the name, and a user ID only may not hold quotes or markup. The
 * classic group pages refuse quotes and markup in a group name and comments, but groups migrated from
 * {@code groups.xml} can hold them, so the group gets its name and comments in the database.</p>
 */
public class UserMgmtXssIT extends AbstractRestIT {
    private static final String BASE = "/opennms/api/v2/";

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

    private static final String DESCRIPTION = "<script>window.__xss=3</script> \"quoted\" 'single' <img src=x onerror=window.__xss=4>";

    public UserMgmtXssIT() {
        super(Version.V2, "");
    }

    @Before
    public void createObjects() throws Exception {
        cleanUp();

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

        createClassicObjects();
    }

    /** Creates the group, the user and the role whose names the v2 API refuses. */
    private void createClassicObjects() throws Exception {
        final String base = getBaseUrlInternal() + "opennms/";

        // No page or API takes this group name, so create a plain group and rename it in the database. The group
        // manager sees the change on its next check, within a second.
        given().basePath(BASE + "groups").contentType(ContentType.JSON).body(ImmutableMap.of("name", CLASSIC_GROUP_SEED))
                .post().then().statusCode(201);
        sql("UPDATE groups SET name = ?, comments = ? WHERE name = ?", CLASSIC_GROUP, CLASSIC_COMMENTS, CLASSIC_GROUP_SEED);
        open(base + "admin/userGroupView/groups/list.htm");
        wait.until(d -> {
            d.navigate().refresh();
            return ((JavascriptExecutor) d).executeScript("return document.getElementById(arguments[0]) !== null", CLASSIC_GROUP + ".doModify");
        });

        // The page checks the user ID in the browser only; submit the form without that check.
        open(base + "admin/userGroupView/users/newUser.jsp");
        setValue("userID", CLASSIC_USER);
        setValue("pass1", "SmokeTestPassword1");
        setValue("pass2", "SmokeTestPassword1");
        submitForm("newUserForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fullName")));
        setValue("fullName", FULL_NAME);
        setValue("userComments", CLASSIC_COMMENTS);
        findElementById("saveUserButton").click();
        waitForId("users(" + CLASSIC_USER + ").doModify");

        open(base + "admin/userGroupView/roles");
        driver.findElement(By.xpath("//button[text()='Add New On-Call Role']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input_roleName")));
        setValue("input_roleName", CLASSIC_ROLE);
        setValue("input_roleDescr", DESCRIPTION);
        new org.openqa.selenium.support.ui.Select(driver.findElement(By.id("input_roleUser"))).selectByVisibleText(USER);
        new org.openqa.selenium.support.ui.Select(driver.findElement(By.id("input_roleGroup"))).selectByVisibleText(GROUP);
        clickToLoad(driver.findElement(By.name("save")));
        assertBodyContains(CLASSIC_ROLE);
        assertNothingRan();
    }

    @After
    public void cleanUp() throws SQLException {
        applyDefaultCredentials();
        // The API refuses the classic names, so delete those objects in the database. The classic role names USER
        // and GROUP, so it goes first.
        sql("DELETE FROM oncall_roles WHERE name = ?", CLASSIC_ROLE);
        sql("DELETE FROM groups WHERE name IN (?, ?)", CLASSIC_GROUP, CLASSIC_GROUP_SEED);
        sql("DELETE FROM users WHERE username = ?", CLASSIC_USER);
        given().basePath(BASE + "oncall-roles").delete(ROLE);
        given().basePath(BASE + "groups").delete(GROUP);
        given().basePath(BASE + "users").delete(USER);
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
    public void testUserTextIsRenderedAsText() throws Exception {
        final String base = getBaseUrlInternal() + "opennms/";

        // JSP: users list. The list shows the full name and the email of each user.
        open(base + "admin/userGroupView/users/list.jsp");
        assertText(By.id("users(" + USER + ").fullName"), FULL_NAME);
        assertText(By.id("users(" + USER + ").email"), EMAIL);
        assertNothingRan();

        // JSP: user detail
        open(base + "admin/userGroupView/users/userDetail.jsp?userID=" + USER);
        assertBodyContains(FULL_NAME);
        assertBodyContains(EMAIL);
        assertNothingRan();

        // JSP: user modify page, reached from the list. The fields hold the values, so compare the inputs.
        open(base + "admin/userGroupView/users/list.jsp");
        findElementById("users(" + USER + ").doModify").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fullName")));
        assertEquals(FULL_NAME, driver.findElement(By.id("fullName")).getAttribute("value"));
        assertEquals(EMAIL, driver.findElement(By.id("email")).getAttribute("value"));
        assertNothingRan();

        // JSP: groups list. It shows the group name and the comments, not the members.
        open(base + "admin/userGroupView/groups/list.htm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("group-" + GROUP)));
        assertBodyContains(GROUP);
        assertBodyContains(GROUP_COMMENTS);
        assertNothingRan();

        // JSP: GET modifyGroup?groupName= without an operation shows the group's detail page (read-only)
        open(base + "admin/userGroupView/groups/modifyGroup?groupName=" + GROUP);
        assertBodyContains("Details for Group: " + GROUP);
        assertBodyContains(GROUP_COMMENTS);
        assertBodyContains(USER);
        assertNothingRan();

        // JSP: group modify page, reached from the list; the name is in the hidden input, the member in the user list
        open(base + "admin/userGroupView/groups/list.htm");
        clickToLoad(findElementById(GROUP + ".doModify"));
        assertEquals(GROUP, driver.findElement(By.name("groupName")).getAttribute("value"));
        assertBodyContains(USER);
        assertNothingRan();

        // JSP: on-call role list (admin) and the role view
        open(base + "admin/userGroupView/roles");
        assertBodyContains(DESCRIPTION);
        assertNothingRan();
        clickToLoad(driver.findElement(By.linkText(ROLE)));
        assertBodyContains(DESCRIPTION);
        assertNothingRan();

        // JSP: on-call role list (everyone) and its role view
        open(base + "roles");
        assertBodyContains(DESCRIPTION);
        assertNothingRan();
        clickToLoad(driver.findElement(By.linkText(ROLE)));
        assertBodyContains(DESCRIPTION);
        assertNothingRan();

        // Vue: Manage Users
        open(base + "ui/index.html#/admin/users");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='page-title' and text()='Manage Users']")));
        pageToRow("[data-test='users-table']", USER);
        assertBodyContains(FULL_NAME);
        assertBodyContains(EMAIL);
        assertNothingRan();

        // Vue: Manage Groups. The group comments are plain text, so the members are what the page shows.
        open(base + "ui/index.html#/admin/groups");
        pageToRow("[data-test='groups-table']", GROUP);
        assertBodyContains(USER);
        assertNothingRan();
    }

    @Test
    public void testClassicNamesInScriptsLinksAndAttributes() throws Exception {
        final String base = getBaseUrlInternal() + "opennms/";

        // The full-name tooltips on the role pages: the supervisor and the scheduled user are USER, whose full name is markup.
        for (final String page : new String[] { "admin/userGroupView/roles", "roles" }) {
            open(base + page);
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
        open(base + "admin/userGroupView/roles");
        trapXss();
        clickToLoad(driver.findElement(By.linkText(CLASSIC_ROLE)));
        clickToLoad(driver.findElement(By.xpath("//button[text()='Value Details']")));
        assertEquals(CLASSIC_ROLE, driver.findElement(By.id("input_roleName")).getAttribute("value"));
        assertNothingRan();

        // Groups list: the name is in id attributes, in onclick handlers and in javascript: URLs.
        open(base + "admin/userGroupView/groups/list.htm");
        assertBodyContains(CLASSIC_GROUP);
        assertNothingRan();
        trapXss();
        clickToLoad(byId(CLASSIC_GROUP + ".doModify"));
        assertEquals(CLASSIC_GROUP, driver.findElement(By.name("groupName")).getAttribute("value"));
        assertNothingRan();

        // Users list: the user ID is in onclick handlers and in javascript: URLs.
        open(base + "admin/userGroupView/users/list.jsp");
        assertBodyContains(CLASSIC_USER);
        assertNothingRan();
        trapXss();
        clickToLoad(byId("users(" + CLASSIC_USER + ").doModify"));
        assertEquals(FULL_NAME, driver.findElement(By.id("fullName")).getAttribute("value"));
        assertNothingRan();
        open(base + "admin/userGroupView/users/list.jsp");
        trapXss();
        clickToLoad(byId("users(" + CLASSIC_USER + ").doDetails"));
        assertBodyContains(CLASSIC_USER);
        assertNothingRan();

        // The Vue pages show the same objects as text.
        open(base + "ui/index.html#/admin/groups");
        pageToRow("[data-test='groups-table']", CLASSIC_GROUP);
        assertNothingRan();

        // The delete links hold the names in a confirm() string and in a javascript: URL. The dialog must show the
        // name as stored, and the delete must reach the right object.
        open(base + "admin/userGroupView/groups/list.htm");
        trapXss();
        deleteAndConfirm(byId(CLASSIC_GROUP + ".doDelete"), "Are you sure you want to delete the group " + CLASSIC_GROUP + "?");
        assertBodyDoesNotContain(CLASSIC_GROUP);
        assertNothingRan();

        open(base + "admin/userGroupView/users/list.jsp");
        trapXss();
        deleteAndConfirm(byId("users(" + CLASSIC_USER + ").doDelete"), "Are you sure you want to delete the user " + CLASSIC_USER + "?");
        assertBodyDoesNotContain(CLASSIC_USER);
        assertNothingRan();

        open(base + "admin/userGroupView/roles");
        trapXss();
        final org.openqa.selenium.WebElement row = driver.findElement(By.xpath("//tr[.//a[text()=" + xpathLiteral(CLASSIC_ROLE) + "]]"));
        deleteAndConfirm(row.findElement(By.xpath(".//a[contains(@href, 'doDelete')]")), "Are you sure you want to delete the role " + CLASSIC_ROLE + "?");
        assertBodyDoesNotContain(CLASSIC_ROLE);
        assertNothingRan();
    }

    /** The Vue tables have no filter, so page forward until the row with the given text shows. */
    private void pageToRow(final String tableSelector, final String text) throws InterruptedException {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(tableSelector)));
        int pages = 0;
        while (true) {
            if (driver.findElement(By.cssSelector(tableSelector)).getText().contains(text)) {
                return;
            }
            final java.util.List<org.openqa.selenium.WebElement> next = driver.findElements(By.cssSelector(".p-paginator-next"));
            if (next.isEmpty() || !next.get(0).isEnabled() || next.get(0).getAttribute("class").contains("p-disabled")) {
                throw new AssertionError("The row \"" + text + "\" is not in the table on any of " + (pages + 1) + " pages");
            }
            next.get(0).click();
            pages++;
            Thread.sleep(300);
        }
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
    private org.openqa.selenium.WebElement byId(final String id) {
        final Object element = ((JavascriptExecutor) driver).executeScript("return document.getElementById(arguments[0])", id);
        assertTrue("No element with the id \"" + id + "\" on " + driver.getCurrentUrl(), element != null);
        return (org.openqa.selenium.WebElement) element;
    }

    /** An XPath string literal for text with quotes. */
    private static String xpathLiteral(final String text) {
        return "concat('" + text.replace("'", "', \"'\", '") + "')";
    }

    /**
     * Clicks an element that loads another page, and waits until that page replaced this one. A wait for content
     * alone can pass on the old page, which often shows the same text.
     */
    private void clickToLoad(final org.openqa.selenium.WebElement element) {
        final org.openqa.selenium.WebElement page = driver.findElement(By.tagName("html"));
        element.click();
        wait.until(ExpectedConditions.stalenessOf(page));
    }

    /** Clicks a delete link, checks the text of its confirm() dialog, accepts it and waits for the next page. */
    private void deleteAndConfirm(final org.openqa.selenium.WebElement link, final String expectedText) {
        final org.openqa.selenium.WebElement page = driver.findElement(By.tagName("html"));
        link.click();
        final org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals(expectedText, alert.getText());
        alert.accept();
        wait.until(ExpectedConditions.stalenessOf(page));
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
     * would otherwise take the page, and the mark, away.
     */
    private void trapXss() {
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(window, '__xss', { configurable: true, set: function(v) { sessionStorage.setItem('xss', String(v)); } })");
    }

    private void open(final String url) throws InterruptedException {
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
