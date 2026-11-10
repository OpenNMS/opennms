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

import static org.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 1. Verifies that the scheduled outage text is correctly displayed. See LTS-233.
 * 2. Verifies that special characters can be used in scheduled outage names. See LTS-234.
 */
@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class ScheduledOutageIT extends OpenNMSSeleniumIT {

    @Before
    public void before() throws Exception {
        final String node = "<node type=\"A\" label=\"TestMachine\" foreignSource=\"" + REQUISITION_NAME + "\" foreignId=\"TestMachine\">" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("/rest/nodes", node, 201);

        String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<hostName>test-machine1.local</hostName>" +
                "</ipInterface>";

        sendPost("rest/nodes/"+REQUISITION_NAME+":TestMachine/ipinterfaces", ipInterface, 201);
    }

    @After
    public void after() throws Exception {
        deleteTestRequisition();
    }

    @Test
    public void testWeekly() throws Exception {
        testOption("Weekly", "Every Sunday, From 00:00:00 Through 23:59:59");
    }

    @Test
    public void testMonthly() throws Exception {
        testOption("Monthly", "Every Sunday, From 00:00:00 Through 23:59:59");
    }

    @Test
    public void testDaily() throws Exception {
        testOption("Daily", "Daily, From 00:00:00 Through 23:59:59");
    }

    @Test
    public void testSpecific() throws Exception {
        final String dateString = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
        testOption("Specific", "One-Time, From " + dateString + " 00:00:00 Through " + dateString + " 23:59:59");
    }

    private void testOption(final String option, final String text) throws Exception {
        // Visit the scheduled outage page.
        getDriver().get(getBaseUrlInternal() + "opennms/admin/sched-outages/index.jsp");
        // Enter the name...
        enterText(By.xpath("//form[@action='admin/sched-outages/editoutage.jsp']//input[@name='newName']"), "My-Scheduled-Outage");
        // ...and hit the button.
        findElementByXpath("//form[@action='admin/sched-outages/editoutage.jsp']//button[@name='newOutage']").click();

        // Wait till the editor page appears.
        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText("Editing Outage: My-Scheduled-Outage").apply(driver));
        // Now add all nodes and interfaces...
        findElementByXpath("//form[@id='matchAnyForm']//input[@name='matchAny']").click();
        // ...and confirm the alert box.
        getDriver().switchTo().alert().accept();

        final WebDriverWait webDriverWait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
        final String outageTypeSelectorXPath = "//select[@id='outageTypeSelector']";

        try {
            webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(outageTypeSelectorXPath)));
        } catch (final StaleElementReferenceException e) {
            webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(outageTypeSelectorXPath)));
        }

        // Set the specified outage type...
        new Select(findElementByXpath(outageTypeSelectorXPath)).selectByVisibleText(option);
        // ...and apply.
        findElementByXpath("//input[@name='setOutageType']").click();
        // now add the outage
        findElementByXpath("//input[@name='addOutage']").click();

        // check whether the outage text appears correctly...
        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText(text).apply(driver));
    }

    private void testCharactersInName(String name) throws Exception {
        try {
            getDriver().get(getBaseUrlInternal() + "opennms/admin/sched-outages/index.jsp");
            enterText(By.xpath("//form[@action='admin/sched-outages/editoutage.jsp']//input[@name='newName']"), name);
            findElementByXpath("//form[@action='admin/sched-outages/editoutage.jsp']//button[@name='newOutage']").click();
            with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText("Editing Outage: "+name).apply(driver));
            findElementByXpath("//form[@id='matchAnyForm']//input[@name='matchAny']").click();
            getDriver().switchTo().alert().accept();

            final WebDriverWait webDriverWait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
            final String outageTypeSelectorXPath = "//select[@id='outageTypeSelector']";

            try {
                webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(outageTypeSelectorXPath)));
            } catch (final StaleElementReferenceException e) {
                webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(outageTypeSelectorXPath)));
            }

            new Select(findElementByXpath(outageTypeSelectorXPath)).selectByVisibleText("Daily");
            findElementByXpath("//input[@name='setOutageType']").click();
            findElementByXpath("//input[@name='addOutage']").click();
            findElementByXpath("//input[@name='saveButton']").click();
            getDriver().get(getBaseUrlInternal() + "opennms/element/node.jsp?node=" + REQUISITION_NAME + ":TestMachine");
            findElementByXpath("//a[text()='"+name+"']").click();
            with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText("Editing Outage: "+name).apply(driver));
        } finally {
            getDriver().get(getBaseUrlInternal() + "opennms/admin/sched-outages/index.jsp");

            final WebDriverWait webDriverWait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
            final String deleteLink = "//a[@id='" + name + ".delete']";

            try {
                webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(deleteLink)));
            } catch (final StaleElementReferenceException e) {
                webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(deleteLink)));
            }

            findElementByXpath(deleteLink).click();
            getDriver().switchTo().alert().accept();
        }
    }

    @Ignore("flapping")
    @Test
    public void testNormalOutageName() throws Exception {
        testCharactersInName("My-Outage-123");
    }

    @Ignore("flapping")
    @Test
    public void testWeirdOutageName() throws Exception {
        testCharactersInName("M?y#O;u.t-a&amp;g&e 1 2 3*");
    }

    @Ignore("flapping")
    @Test
    public void testOutageTypeChange() throws Exception {
        // Visit the scheduled outage page.
        getDriver().get(getBaseUrlInternal() + "opennms/admin/sched-outages/index.jsp");
        // Enter the name...
        enterText(By.xpath("//form[@action='admin/sched-outages/editoutage.jsp']//input[@name='newName']"), "My-Scheduled-Outage");
        // ...and hit the button.
        findElementByXpath("//form[@action='admin/sched-outages/editoutage.jsp']//button[@name='newOutage']").click();

        // Wait till the editor page appears.
        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText("Editing Outage: My-Scheduled-Outage").apply(driver));
        // Now add all nodes and interfaces...
        findElementByXpath("//form[@id='matchAnyForm']//input[@name='matchAny']").click();
        // ...and confirm the alert box.
        getDriver().switchTo().alert().accept();

        final WebDriverWait webDriverWait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
        final String outageTypeSelectorXPath = "//select[@id='outageTypeSelector']";

        try {
            webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(outageTypeSelectorXPath)));
        } catch (final StaleElementReferenceException e) {
            webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(outageTypeSelectorXPath)));
        }

        // Set the specified outage type...
        new Select(findElementByXpath(outageTypeSelectorXPath)).selectByVisibleText("Daily");

        // ...and apply.
        findElementByXpath("//input[@name='setOutageType']").click();

        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText("daily").apply(driver));

        // This is not working since it's an <input type='image'>
        findElementByXpath("//input[@id='deleteOutageTypeBtn']").click();

        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> findElementByXpath("//select[@id='outageTypeSelector']").isDisplayed());

        // Set another outage type...
        new Select(findElementByXpath("//select[@id='outageTypeSelector']")).selectByVisibleText("Weekly");

        // ...and apply.
        findElementByXpath("//input[@name='setOutageType']").click();

        // check whether the outage type text appears correctly...
        with().pollInterval(1, SECONDS).await().atMost(10, SECONDS).until(() -> pageContainsText("weekly").apply(driver));
    }
}
