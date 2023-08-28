/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    @Test
    public void testAdminPasswordGate() {
        // login with "admin/admin", do not skip the password gate but instead change the password
        LOG.debug("Test admin login and password change.");
        loginAndChangePassword("index.jsp");

        // logout, then login with "admin/newPassword", should go directly to main page
        LOG.debug("Test admin login with new password.");
        logout();
        login(PASSWORD_GATE_USERNAME, ALTERNATE_ADMIN_PASSWORD, true, true, true);
        assertTrue(driver.getCurrentUrl().contains("index.jsp"));
        verifyOnMainPage();

        // Reset password back to "admin" using Rest API
        resetPassword();

        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
        LOG.debug("Test admin login with default password and skip.");
        logout();
        loginAndSkip();

        // logout and try to go to a non-login page
        // user will be redirected to login page, login with "admin/admin"
        // will get password gate page, click Skip, then should redirect to original page
        LOG.debug("Logout and login to the node page, confirm that skipping the password gate redirects there");
        logout();
        nodePage();
        waitFor("login.jsp");
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, true, true, false);
        waitFor("element/nodeList.htm");

        // logout and try to go to a non-login page
        // user will be redirected to login page, login with "admin/admin"
        // will get password gate page, change the password, then should redirect to node page
        LOG.debug("Logout and login to the node page, confirm that changing the password redirects to node page");
        logout();
        nodePage();
        waitFor("login.jsp");
        loginAndChangePassword("element/nodeList.htm");

        // Reset password back to "admin" using Rest API
        resetPassword();

        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
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
        } catch (Exception e) {
            fail("Failed to reset password to 'admin': " + e.getMessage());
        }
    }

    private void loginAndChangePassword(String expectedRedirectUrl) {
        // login with "admin/admin", do not skip past the password gate
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, false, false, true);

        if (!driver.getCurrentUrl().contains("passwordGate.jsp")) {
            fail("Failed to get password gate page after 'admin/admin' login attempt.");
        }

        // Change the admin password
        enterText(By.name("oldpass"), PASSWORD_GATE_PASSWORD);
        enterText(By.name("pass1"), ALTERNATE_ADMIN_PASSWORD);
        enterText(By.name("pass2"), ALTERNATE_ADMIN_PASSWORD);
        clickElement(By.name("btn_change_password"));

        // waiting until redirecting back to login page after successful password change
        waitFor(expectedRedirectUrl);
    }

    private void loginAndSkip() {
        // login with "admin/admin", should succeed but display passwordGate page, which is skipped
        login(PASSWORD_GATE_USERNAME, PASSWORD_GATE_PASSWORD, true, true, true);

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
