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
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationsPageIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationsPageIT.class);

    @Before
    public void setUp() throws Exception {
        notificationsPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(3, countElementsMatchingCss("div.card-header"));
        findElementByXpath("//span[text()='Notification queries']");
        findElementByXpath("//span[text()='Outstanding and Acknowledged Notices']");
        findElementByXpath("//span[text()='Notification Escalation']");
    }

    @Test
    public void testAllLinksArePresent() {
        findElementByLink("Your outstanding notices");
        findElementByLink("All outstanding notices");
        findElementByLink("All acknowledged notices");
    }

    @Test
    public void testAllFormsArePresent() {
        findElementByXpath("//button[@id='btn_search_by_notice' and @type='submit']");
        findElementByXpath("//button[@id='btn_search_by_user' and @type='submit']");
    }

    @Test
    public void testAllLinks() {
        findElementByLink("Your outstanding notices").click();
        findElementByXpath("//span[@class='label label-default' and contains(text(), 'admin was notified')]");
        findElementByLink("[Remove all]");
        findElementByLink("Sent Time");
        findElementByXpath("//button[@type='button' and text()='Acknowledge Notices']");

        notificationsPage();
        findElementByLink("All outstanding notices").click();
        findElementByXpath("//p//strong[text()='outstanding']");
        findElementByLink("[Show acknowledged]");
        findElementByLink("Respond Time");
        assertElementDoesNotHaveText(By.xpath("//span[@class='label label-default']"), "admin was notified [-]");

        notificationsPage();
        findElementByLink("All acknowledged notices").click();
        findElementByXpath("//p//strong[text()='acknowledged']");
        findElementByLink("[Show outstanding]");
        findElementByLink("Respond Time");
        assertElementDoesNotHaveText(By.xpath("//span[@class='label label-default']"), "admin was notified [-]");
    }

    @Test
    public void testAddEscalationButton() {
        adminPage();
        findElementByLink("Configure Notifications").click();
        findElementByLink("Configure Destination Paths").click();
        findElementByXpath("//input[@value='New Path']").click();
        findElementByXpath("//input[@value='Add Escalation']").click();
        try {
            final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
        } catch (final Exception e) {
            LOG.debug("Got an exception waiting for a 'specify path name first' alert.", e);
            throw e;
        }
        enterText(By.xpath("//input[@name='name']"), "Foo");
        findElementByXpath("//input[@value='Add Escalation']").click();
        assertElementHasText(By.xpath("//h2"), "Editing path: Foo");
    }

    @Test
    public void testEditButton() {
        adminPage();
        findElementByLink("Configure Notifications").click();
        findElementByLink("Configure Destination Paths").click();
        findElementByXpath("//input[@value='New Path']").click();
        findElementByXpath("//input[@value='Edit']").click();
        try {
            final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
        } catch (final Exception e) {
            LOG.debug("Got an exception waiting for a 'specify path name first' alert.", e);
            throw e;
        }
        enterText(By.xpath("//input[@name='name']"), "Foo");
        findElementByXpath("//input[@value='Edit']").click();
        assertElementHasText(By.xpath("//h2"), "Editing path: Foo");
    }
}
