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
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(UserIT.class);

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/account/selfService/index.jsp");
    }

    @Test
    public void testExpectedTextAndLinksArePresent() throws Exception {
        final List<WebElement> headers = driver.findElements(By.xpath("//div[@class='card-header']/span"));
        assertEquals("Account page should have 2 panels", 2, headers.size());
        assertEquals("Account page should have \"User Account Self-Service\" panel", "User Account Self-Service", headers.get(0).getText());
        assertEquals("Account page should have \"User Account Self-Service Options\" panel", "Account Self-Service Options", headers.get(1).getText());
    }

    @Test
    public void testSubmitWithWrongPassword() throws InterruptedException {
        driver.findElement(By.linkText("Change Password")).click();
        enterText(By.cssSelector("input[type=password][name=oldpass]"), "12345");
        enterText(By.cssSelector("input[type=password][name=pass1]"), "23456");
        enterText(By.cssSelector("input[type=password][name=pass2]"), "34567");
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        try {
            final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
        } catch (final Exception e) {
            LOG.debug("Got an exception waiting for a 'wrong password' alert.", e);
            throw e;
        }
    }

    @Test
    public void testUsersAndGroups() throws Exception {
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();
        findElementByLink("Add new user").click();

        enterText(By.id("userID"), USER_NAME);
        enterText(By.id("pass1"), "SmokeTestPassword");
        enterText(By.id("pass2"), "SmokeTestPassword");
        findElementByXpath("//button[@type='submit' and text()='OK']").click();

        WebElement saveUserButton = scrollToElement(By.id("saveUserButton"));
        clickElementUsingScript(saveUserButton);
        findElementById("users(" + USER_NAME + ").doDetails");

        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Groups").click();
        findElementByLink("Add new group").click();

        enterText(By.id("groupName"), GROUP_NAME);
        enterText(By.id("groupComment"), "Test");
        findElementByXpath("//button[@type='submit' and text()='OK']").click();

        final Select select = new Select(findElementByName("availableUsers"));
        select.selectByVisibleText(USER_NAME);
        findElementById("users.doAdd").click();

        findElementByName("finish").click();

        findElementByLink(GROUP_NAME).click();
        driver.findElement(By.xpath("//div[@class='card-header']/span[text()='Details for Group: " + GROUP_NAME + "']"));

        findElementByLink("Group List").click();
        findElementById(GROUP_NAME + ".doDelete").click();
        handleAlert("Are you sure you want to delete the group " + GROUP_NAME + "?");
        assertElementDoesNotExist(By.id(GROUP_NAME));

        findElementByLink("Users and Groups").click();
        findElementByLink("Configure Users").click();
        findElementById("user-" + USER_NAME);
        findElementById("users(" + USER_NAME + ").doDelete").click();
        handleAlert("Are you sure you want to delete the user " + USER_NAME + "?");
        assertElementDoesNotExist(By.id(USER_NAME));
    }

    @Test
    public void testChangeAdminPasswordWithDifferentPasswords() throws Exception {

        driver.get(getBaseUrlInternal() + "opennms/account/selfService/newPasswordEntry");
        enterText(By.cssSelector("input[type=password][name=oldpass]"), "admin");
        enterText(By.cssSelector("input[type=password][name=pass1]"), "OpenNMS");
        enterText(By.cssSelector("input[type=password][name=pass2]"), "OpenNM");
        driver.findElement(By.cssSelector("button[type=submit]")).click();
        try {
            final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
        } catch (final Exception e) {
            LOG.debug("Got an exception waiting for a 'wrong password' alert.", e);
            throw e;
        }
    }

    // "ZZ" so this test runs last, since we can't change the password back to "admin"
    @Test
    public void testZZChangeAdminPassword() throws Exception {

        driver.get(getBaseUrlInternal() + "opennms/account/selfService/newPasswordEntry");
        enterText(By.cssSelector("input[type=password][name=oldpass]"), "admin");
        enterText(By.cssSelector("input[type=password][name=pass1]"), "OpenNMS.!123");
        enterText(By.cssSelector("input[type=password][name=pass2]"), "OpenNMS.!123");
        driver.findElement(By.cssSelector("button[type=submit]")).click();
        assertTrue(wait.until(pageContainsText("Password successfully changed")));
    }

    @Test
    public void testInvalidUserIds() {
        checkInvalidUserId("John<b>Doe</b>",true);
        checkInvalidUserId("Jane'Doe'",true);
        checkInvalidUserId("John&Doe",true);
        checkInvalidUserId("Jane\"\"Doe",true);
    }

    @Test
    public void testValidUserIds() {
        checkInvalidUserId("John-Doe",false);
        checkInvalidUserId("Jane/Doe",false);
        checkInvalidUserId("John.Doe",false);
        checkInvalidUserId("Jane#Doe", false);
        checkInvalidUserId("John@Döe.com", false);
        checkInvalidUserId("JohnDoé", false);
    }

    @Test
    public void testInvalidGroupIds() {
        checkInvalidGroupId("John<b>Doe</b>",true);
        checkInvalidGroupId("Jane'Doe'",true);
        checkInvalidGroupId("John&Doe",true);
        checkInvalidGroupId("Jane\"\"Doe",true);
    }

    @Test
    public void testValidGroupIds() {
        checkInvalidGroupId("John-Doe",false);
        checkInvalidGroupId("Jane/Doe",false);
        checkInvalidGroupId("John.Doe",false);
        checkInvalidGroupId("Jane#Doe", false);
        checkInvalidGroupId("John@Döe.com", false);
        checkInvalidGroupId("JohnDoé", false);
    }

    public void checkInvalidUserId(final String userId, final boolean mustFail) {
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();
        findElementByLink("Add new user").click();

        enterText(By.id("userID"), userId);
        enterText(By.id("pass1"), "SmokeTestPassword");
        enterText(By.id("pass2"), "SmokeTestPassword");
        findElementByXpath("//button[@type='submit' and text()='OK']").click();

        if (mustFail) {
            try {
                final Alert alert = wait.withTimeout(Duration.of(5, ChronoUnit.SECONDS)).until(ExpectedConditions.alertIsPresent());
                alert.dismiss();
            } catch (final Exception e) {
                LOG.debug("Got an exception waiting for a 'invalid user ID' alert.", e);
                throw e;
            }
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(By.name("finish")));
        }
    }

    public void checkInvalidGroupId(final String groupId, final boolean mustFail) {
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Groups").click();
        findElementByLink("Add new group").click();

        enterText(By.id("groupName"), groupId);
        enterText(By.id("groupComment"), "SmokeTestComment");
        findElementByXpath("//button[@type='submit' and text()='OK']").click();

        if (mustFail) {
            try {
                final Alert alert = wait.withTimeout(Duration.of(5, ChronoUnit.SECONDS)).until(ExpectedConditions.alertIsPresent());
                alert.dismiss();
            } catch (final Exception e) {
                LOG.debug("Got an exception waiting for a 'invalid group ID' alert.", e);
                throw e;
            }
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(By.name("finish")));
        }
    }

    /**
     * see NMS-13124
     */
    @Test
    public void testCsrfPrivilegeEscalation() {
        // visit the admin's user page
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();
        findElementByLink("Add new user").click();

        // add a new user 'user'
        enterText(By.id("userID"), "user");
        enterText(By.id("pass1"), "pass");
        enterText(By.id("pass2"), "pass");
        findElementByXpath("//button[@type='submit' and text()='OK']").click();

        // assign just the ROLE_USER
        final Select select = new Select(driver.findElement(By.name("availableRoles")));
        select.selectByVisibleText("ROLE_USER");
        findElementById("roles.doAdd").click();
        findElementById("saveUserButton").click();

        // assert that this is correctly set
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();
        findElementById("users(user).doDetails").click();

        assertTrue(wait.until(pageContainsText("ROLE_USER")));

        // now construct an exploit to set ROLE_ADMIN for user 'user'
        final String html = "<form action='" + stack.opennms().getBaseUrlInternal() + "opennms/admin/userGroupView/users/updateUser' method='POST' enctype='application/x-www-form-urlencoded'>" +
                "<input type='hidden' name='userID' value='user' />" +
                "<input type='hidden' name='password' value=' ' />" +
                "<input type='hidden' name='redirect' value='/admin/userGroupView/users/saveUser' /> <input type='hidden' name='fullName' value=' ' />" +
                "<input type='hidden' name='userComments' value=' ' />" +
                "<input type='hidden' name='configuredRoles' value='ROLE_ADMIN' />" +
                "<input type='hidden' name='email' value=' ' />" +
                "<input type='hidden' name='pemail' value=' ' />" +
                "<input type='hidden' name='xmppAddress' value=' ' />" +
                "<input type='hidden' name='microblog' value=' ' />" +
                "<input type='hidden' name='numericalService' value=' ' />" +
                "<input type='hidden' name='numericalPin' value=' ' />" +
                "<input type='hidden' name='textService' value=' ' />" +
                "<input type='hidden' name='textPin' value=' ' />" +
                "<input type='hidden' name='workPhone' value=' ' />" +
                "<input type='hidden' name='mobilePhone' value=' ' />" +
                "<input type='hidden' name='homePhone' value=' ' />" +
                "<input type='hidden' name='tuiPin' value=' ' />" +
                "<input type='hidden' name='timeZoneId' value=' ' />" +
                "<input type='hidden' name='dutySchedules' value='0' />" +
                "<input type='hidden' name='numSchedules' value='1' />" +
                "<input type='submit' id='submitIt' />" +
                "</form>";
        String script = "var foo = document.createElement('div'); " +
                "foo.innerHTML=\"" + html + "\"; " +
                "document.body.appendChild(foo)";

        // ...and execute it
        driver.executeScript(script);
        findElementById("submitIt").click();

        // this should be denied due to CSRF protection
        assertTrue(wait.until(pageContainsText("Access denied")));

        // assure that the user's role is still ROLE_USER
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();
        findElementById("users(user).doDetails").click();
        assertTrue(wait.until(pageContainsText("ROLE_USER")));

        // delete the user
        adminPage();
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();

        WebElement deleteGroupLink = findElementById("users(user).doDelete");
        clickElementUsingScript(deleteGroupLink);

        // findElementById("users(user).doDelete").click();
        handleAlert("Are you sure you want to delete the user user?");
    }
}
