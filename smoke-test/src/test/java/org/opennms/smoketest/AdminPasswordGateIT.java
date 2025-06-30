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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
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
    // @Test
    @Ignore("Need to fix this to work with the new menu.")
    public void testAdminPasswordGate() {
        // login with "admin/admin", do not skip the password gate but instead change the password
        LOG.debug("Test admin login and password change");
        logout();
        loginAndChangePassword("index.jsp", true);

        // logout, then login with "admin/newPassword", should go directly to main page
        LOG.debug("Test admin login with new password");
        logout();
        // skip cookie deletion, we need to retain for authorization for resetPassword Rest API call
        login(PASSWORD_GATE_USERNAME, ALTERNATE_ADMIN_PASSWORD, true, true, true, true);
        assertTrue(driver.getCurrentUrl().contains("index.jsp"));
        verifyOnMainPage();

        // Reset password back to "admin" using Rest API
        resetPassword();

        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
        LOG.debug("Test admin login with default password and skip");
        logout();
        loginAndSkip();

        // logout and try to go to a non-login page
        // user will be redirected to login page, login with "admin/admin"
        // will get password gate page, click Skip, then should redirect to original page
        LOG.debug("Test logout and login to the node page, confirm that skipping the password gate redirects there");
        logout();
        nodePage();
        waitFor("login.jsp");
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, true, true, false, false);
        waitFor("element/nodeList.htm");

        // logout and try to go to a non-login page
        // user will be redirected to login page, login with "admin/admin"
        // will get password gate page, change the password, then should redirect to node page
        LOG.debug("Test logout and login to the node page, confirm that changing the password redirects to node page");
        logout();
        nodePage();
        waitFor("login.jsp");
        loginAndChangePassword("element/nodeList.htm", false);

        // Reset password back to "admin" using Rest API
        resetPassword();

        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
        LOG.debug("Test final logout and login to the node page and skip");
        logout();
        loginAndSkip();
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
