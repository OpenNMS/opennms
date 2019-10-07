/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.opsboard;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.selenium.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpsBoardAdminPageIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(OpsBoardAdminPageIT.class);

    private OpsBoardAdminPage adminPage;

    @Before
    public void setUp() {
        this.adminPage = new OpsBoardAdminPage(this).open();
        this.adminPage.removeAll();
    }

    @After
    public void tearDown() {
        LOG.debug("Tearing down. Removing all boards.");
        this.adminPage.open(); // reload page to reset any invalid state
        this.adminPage.removeAll();
    }

    // See NMS-12166
    @Test(timeout = 300000)
    public void testHeaderHiddenForTopologyUI() {
        final OpsBoardAdminEditorPage testBoard = adminPage.createNew("testBoard");
        testBoard.addDashlet(new DashletBuilder()
                .withDashlet("Topology")
                .withTitle("Test Dashlet")
                .withDuration(300).build());

        // Hit preview button
        testBoard.preview();

        try {
            setImplicitWait(1, TimeUnit.SECONDS);
            new WebDriverWait(driver, 5).until(not(pageContainsText("Access denied")));
            new WebDriverWait(driver, 5).until(pageContainsText("Topology"));

            // Verify that the header is hidden
            // This method can throw StateElementReference exceptions, so we try multiple times
            await().atMost(1, TimeUnit.MINUTES)
                    .ignoreExceptionsInstanceOf(WebDriverException.class)
                    .until(() -> driver.switchTo().parentFrame()
                            .switchTo().frame(findElementByXpath("//div[@id = 'opsboard-topology-iframe']//iframe"))
                            .findElement(By.id("header")).isDisplayed(), equalTo(false));
        } finally {
            setImplicitWait();
        }
    }

    // See NMS-12166
    @Test(timeout = 300000)
    public void testHeaderHiddenForNodeMap() {
        final OpsBoardAdminEditorPage testBoard = adminPage.createNew("testBoard");
        testBoard.addDashlet(new DashletBuilder()
                .withDashlet("Map")
                .withTitle("Test Dashlet")
                .withDuration(300).build());

        // Hit preview button
        testBoard.preview();

        try {
            setImplicitWait(1, TimeUnit.SECONDS);
            new WebDriverWait(driver, 5).until(not(pageContainsText("Access denied")));
            new WebDriverWait(driver, 5).until(pageContainsText("Map"));

            // Verify that the header is hidden
            // This method can throw StateElementReference exceptions, so we try multiple times
            await().atMost(1, TimeUnit.MINUTES)
                    .ignoreExceptionsInstanceOf(WebDriverException.class)
                    .until(() -> driver.switchTo().parentFrame()
                            .switchTo().frame(findElementByXpath("//div[@id = 'opsboard-map-iframe']//iframe"))
                            .findElement(By.id("header")).isDisplayed(), equalTo(false));
        } finally {
            setImplicitWait();
        }
    }

    // See NMS-9678
    @Test
    public void canCreateAndPreview() {
        final OpsBoardAdminEditorPage testBoard = adminPage.createNew("testBoard");
        testBoard.addDashlet(new DashletBuilder()
                .withDashlet("Surveillance")
                .withTitle("Test Dashlet")
                .withDuration(300).build());

        // Hit preview button
        testBoard.preview();

        // Now ensure that access was NOT denied
        try {
            setImplicitWait(1, TimeUnit.SECONDS);
            new WebDriverWait(driver, 5).until(not(pageContainsText("Access denied")));
            new WebDriverWait(driver, 5).until(pageContainsText("Surveillance view"));
        } finally {
            setImplicitWait();
        }
    }

    // See NMS-10515
    @Test
    @org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
    public void canCreateAndUseDeepLink() {
        final OpsBoardAdminEditorPage testBoard = adminPage.createNew("My-Wallboard");
        testBoard.addDashlet(new DashletBuilder()
                .withDashlet("Alarms")
                .withTitle("My-Alarms")
                .withDuration(300).build());

        adminPage.open("/vaadin-wallboard#!wallboard/My-Wallboard");
        waitUntil(pageContainsText("Ops Panel"));
        waitUntil(pageContainsText("Alarms: My-Alarms"));
    }

    private static class OpsBoardAdminPage extends AbstractPage {

        OpsBoardAdminPage(AbstractOpenNMSSeleniumHelper testCase) {
            super(testCase);
        }

        public OpsBoardAdminPage open() {
            get("/admin/wallboardConfig.jsp");
            getDriver().switchTo().frame(0);
            return this;
        }

        public OpsBoardAdminPage open(final String path) {
            get(path);
            getDriver().switchTo().parentFrame();
            return this;
        }

        public OpsBoardAdminPage removeAll() {
            List<WebElement> elements = findElements(By.xpath("//*[@class='v-button-caption' and contains(text(), 'Remove')]"));
            while(elements.size() > 0) {
                elements.get(0).click();
                elements = findElements(By.xpath("//*[@class='v-button-caption' and contains(text(), 'Remove')]"));
            }
            return this;
        }

        public OpsBoardAdminEditorPage createNew(String name) {
            // Create new
            getDriver().findElement(By.xpath("//div[@class='v-captiontext' and contains(text(), '+')]")).click();
            waitUntil(pageContainsText("New Ops Board"));

            // Set name
            final WebElement element = findElement(By.id("newopsboard.name"));
            element.sendKeys(name);

            // click save
            findElement(By.id("newopsboard.save")).click();
            return new OpsBoardAdminEditorPage(testCase);
        }
    }

    private static class OpsBoardAdminEditorPage extends AbstractPage {
        OpsBoardAdminEditorPage(AbstractOpenNMSSeleniumHelper testCase) {
            super(testCase);
        }

        public OpsBoardAdminEditorPage addDashlet(DashletConfig dashletConfig) {
            findElement(By.id("opsboard.action.addDashlet")).click();
            if (dashletConfig.getDuration() != null) {
                findElement(By.id("opsboard.duration")).clear();
                findElement(By.id("opsboard.duration")).sendKeys("" + dashletConfig.getDuration());
            }
            if (dashletConfig.getTitle() != null) {
                findElement(By.id("opsboard.title")).sendKeys(dashletConfig.getTitle());
            }
            if (dashletConfig.getType() != null) {
                new Select(findElement(By.xpath("//*[@id='opsboard.type']//select"))).selectByVisibleText(dashletConfig.getType());
            }
            return this;
        }

        public OpsBoardPreviewPage preview() {
            return new OpsBoardPreviewPage(testCase).open();
        }
    }

    private static class OpsBoardPreviewPage extends AbstractPage {
        OpsBoardPreviewPage(AbstractOpenNMSSeleniumHelper testCase) {
            super(testCase);
        }

        public OpsBoardPreviewPage open() {
            findElement(By.id("opsboard.action.preview")).click();

            waitUntil(driver -> driver.findElements(By.tagName("iframe")).size() == 2);
            getDriver().switchTo().frame(1);
            return this;
        }

        public OpsBoardAdminEditorPage close() {
            findElement(By.id("//span[@class='v-button-caption' and contains(text(), 'Close')]")).click();
            getDriver().switchTo().parentFrame();
            return new OpsBoardAdminEditorPage(testCase);
        }
    }
}
