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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the Admin Password Gate functionality, when user enters the default 'admin' password.
 */
@SuppressWarnings("java:S2068")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminPasswordGateIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPasswordGateIT.class);

    private static final String ALTERNATE_ADMIN_PASSWORD = "Admin!admin1";
    private static final String INVALID_ADMIN_PASSWORD = "jwV5wddWoSCFS6Vn7JTRZVGvVGfgBaEL";

    // pages asked for before login; after login, the browser must come back to them
    private static final String REQUESTED_PAGE = "outage/index.jsp?x=a:b";
    private static final String VUE_HASH_ROUTE = "ui/index.html#/nodes?x=a:b";

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/login.jsp");
    }

    /**
     * Tests:
     * - logging in as "admin/admin", then getting the Password Change gate.
     * - skipping the gate, changing password to something else, redirecting either to main page or previously-attempted page
     * - login and skipping with redirect, to either main page or previously-attempted page
     *
     * Resets password back to the standard admin password.
     * Since we change the password, it's easier to do all these tests within the same test method,
     * otherwise there are issues when the AbstractOpenNMSSeleniumHelper.m_watcher TestWatcher Rule
     * fires.
     */
    @Test
    public void testAdminPasswordGate() {
        // login with "admin/admin", do not skip the password gate but instead change the password
        LOG.debug("Test admin login and password change");
        logout();
        loginAndChangePassword("index.jsp", false);

        // logout, then login with "admin/newPassword", should go directly to main page
        LOG.debug("Test admin login with new password");
        logout();
        // skip cookie deletion, we need to retain for authorization for resetPassword Rest API call
        // do not navigate to login page, we should already be there
        login(PASSWORD_GATE_USERNAME, ALTERNATE_ADMIN_PASSWORD, true, true, false, true);
        assertTrue(driver.getCurrentUrl().contains("index.jsp"));
        verifyOnMainPage();

        // Reset password back to "admin" using Rest API
        resetPassword();

        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
        LOG.debug("Test admin login with default password and skip");
        logout();
        loginAndSkip();
    }

    /**
     * Tests:
     * - logging in as "admin/admin", then getting the Password Change gate.
     * - attempting to change password to one which does not contain a special character
     * - should be rejected on client side
     */
    @Test
    public void testAdminPasswordGateWithInvalidPasswordHavingNoSpecialCharacters() {
        // login with "admin/admin", do not skip the password gate but instead change the password
        LOG.debug("Test admin login and password change with invalid password having no special characters.");
        logout();

        // login with "admin/admin", do not skip past the password gate, skip cookie deletion
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, false, false, true, true);

        if (!driver.getCurrentUrl().contains("passwordGate.jsp")) {
            fail("Failed to get password gate page after 'admin/admin' login attempt.");
        }

        // Change the admin password to an invalid one
        enterText(By.name("oldpass"), PASSWORD_GATE_PASSWORD);
        enterText(By.name("pass1"), INVALID_ADMIN_PASSWORD);
        enterText(By.name("pass2"), INVALID_ADMIN_PASSWORD);
        clickElement(By.name("btn_change_password"));

        // should stay on page and get an alert message
        handleAlert("Password complexity is not correct!", false);
    }

    @Test
    public void testAdminPasswordGateRetainsRequestedPage() {
        // logout and try to go to a non-login page, with a query string that contains ':'
        // user will be redirected to login page, login with "admin/admin"
        // will get password gate page, click Skip, then should redirect to original page
        LOG.debug("Test logout and login to the outage page, confirm that skipping the password gate redirects there");
        logout();
        driver.get(getBaseUrlInternal() + "opennms/" + REQUESTED_PAGE);
        waitFor("login.jsp");
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, true, false, false, false);
        waitFor(REQUESTED_PAGE);

        // logout and try to go to a non-login page
        // user will be redirected to login page, login with "admin/admin"
        // will get password gate page, change the password, then should redirect to outage page
        LOG.debug("Test logout and login to the outage page, confirm that changing the password redirects to outage page");
        logout();
        driver.get(getBaseUrlInternal() + "opennms/" + REQUESTED_PAGE);
        waitFor("login.jsp");
        loginAndChangePassword(REQUESTED_PAGE, false);

        // the admin password is not the default now, so there is no password gate
        LOG.debug("Test login to a Vue hash route, without the password gate");
        loginThroughVueHashRoute(ALTERNATE_ADMIN_PASSWORD, false);

        // Reset password back to "admin" using Rest API
        resetPassword();

        LOG.debug("Test login to a Vue hash route, skipping the password gate");
        loginThroughVueHashRoute(PASSWORD_GATE_PASSWORD, true);

        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
        LOG.debug("Test final logout and login to the outage page and skip");
        // logout uses the menu of a JSP page
        driver.get(getBaseUrlInternal() + "opennms/index.jsp");
        logout();
        loginAndSkip();
    }

    /**
     * The password gate action is a POST that changes the password, so it must require the CSRF token.
     */
    @Test
    public void testPasswordGateActionRequiresCsrfToken() {
        login();

        final Object status = ((JavascriptExecutor) driver).executeAsyncScript(
            "const done = arguments[arguments.length - 1];"
            + "fetch('/opennms/account/selfService/passwordGateAction?skip=1',"
            + " {method: 'POST', redirect: 'manual', credentials: 'same-origin'})"
            + ".then(r => done(r.status)).catch(e => done('error: ' + e));");

        assertEquals(403L, status);
    }

    /**
     * Go to a Vue hash route while not logged in, login (and skip the password gate if expected),
     * then confirm that the browser is back at the same hash route.
     */
    private void loginThroughVueHashRoute(final String password, final boolean expectGate) {
        // start with no session
        driver.manage().deleteAllCookies();
        driver.get("about:blank");

        driver.get(getBaseUrlInternal() + "opennms/" + VUE_HASH_ROUTE);
        waitFor("login.jsp");

        // not login(): it waits for elements of JSP pages, which the Vue app does not have
        enterText(By.name("j_username"), PASSWORD_GATE_USERNAME);
        enterText(By.name("j_password"), password);
        clickElement(By.name("Login"));

        if (expectGate) {
            waitFor("passwordGate.jsp");
            clickElement(By.id("btn_skip"));
        }

        wait.until((WebDriver d) -> decodedUrl(d).contains(VUE_HASH_ROUTE));
        assertTrue(decodedUrl(driver).contains(VUE_HASH_ROUTE));
    }

    private static String decodedUrl(final WebDriver d) {
        return URLDecoder.decode(d.getCurrentUrl(), StandardCharsets.UTF_8);
    }

    private void resetPassword() {
        LOG.debug("Resetting password back to default via Rest API");

        final String url = "/rest/users/admin";
        final String body = "password=admin&hashPassword=true";

        try {
            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(PASSWORD_GATE_USERNAME, ALTERNATE_ADMIN_PASSWORD);
            sendPut(url, body, 204, credentials);
            LOG.debug("Password reset successfully");
        } catch (Exception e) {
            fail("Failed to reset password to 'admin': " + e.getMessage());
        }
    }

    private void loginAndChangePassword(String expectedRedirectUrl, boolean navigateToLoginPage) {
        // login with "admin/admin", do not skip past the password gate, skip cookie deletion
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, false, false, navigateToLoginPage, true);

        if (!driver.getCurrentUrl().contains("passwordGate.jsp")) {
            fail("Failed to get password gate page after 'admin/admin' login attempt.");
        }

        // Change the admin password
        enterText(By.name("oldpass"), PASSWORD_GATE_PASSWORD);
        enterText(By.name("pass1"), ALTERNATE_ADMIN_PASSWORD);
        enterText(By.name("pass2"), ALTERNATE_ADMIN_PASSWORD);
        clickElement(By.name("btn_change_password"));

        // waiting until redirecting back to expectedRedirectUrl after successful password change
        waitFor(expectedRedirectUrl);
    }

    private void loginAndSkip() {
        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, true, true, true, false);

        waitFor("index.jsp");
    }

    private void waitFor(String pageUrl) {
        wait.until((WebDriver driver) -> {
            return driver.getCurrentUrl().contains(pageUrl);
        });

        assertTrue(driver.getCurrentUrl().contains(pageUrl));
    }

    private void verifyOnMainPage() {
        // should be on main index.jsp page, verify some elements in that page
        final WebElement contentMiddleElement = getDriver().findElement(By.id("index-contentmiddle"));
        assertNotNull(contentMiddleElement);

        final WebElement statusOverviewElement = findElementByXpath("//div[contains(@class, 'card-header')]//span[text()='Status Overview']");
        assertNotNull(statusOverviewElement);
    }
}
