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

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
        assertEquals("Account page should have 3 panels", 3, headers.size());
        assertEquals("Account page should have \"User Account Self-Service\" panel", "User Account Self-Service", headers.get(0).getText());
        assertEquals("Account page should have \"User Account Self-Service Options\" panel", "Account Self-Service Options", headers.get(1).getText());
        assertEquals("Account page should have \"Browser Notifications\" panel", "Browser Notifications", headers.get(2).getText());
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
}
