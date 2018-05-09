/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static org.openqa.selenium.support.ui.ExpectedConditions.not;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.AbstractPage;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OpsBoardAdminPageIT extends OpenNMSSeleniumTestCase {

    private OpsBoardAdminPage adminPage;

    @Before
    public void setUp() {
        this.adminPage = new OpsBoardAdminPage(this).open();
        this.adminPage.removeAll();
    }

    @After
    public void tearDown() {
        this.adminPage.open(); // reload page to reset any invalid state
        this.adminPage.removeAll();
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
            new WebDriverWait(m_driver, 5).until(not(pageContainsText("Access denied")));
            new WebDriverWait(m_driver, 5).until(pageContainsText("Surveillance view"));
        } finally {
            setImplicitWait();
        }
    }

    private static class OpsBoardAdminPage extends AbstractPage {

        OpsBoardAdminPage(OpenNMSSeleniumTestCase testCase) {
            super(testCase);
        }

        public OpsBoardAdminPage open() {
            get("/admin/wallboardConfig.jsp");
            getDriver().switchTo().frame(0);
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
        OpsBoardAdminEditorPage(OpenNMSSeleniumTestCase testCase) {
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
        OpsBoardPreviewPage(OpenNMSSeleniumTestCase testCase) {
            super(testCase);
        }

        public OpsBoardPreviewPage open() {
            findElement(By.id("opsboard.action.preview")).click();
            waitUntil(driver -> driver.findElements(By.tagName("iframe")).size() == 3);
            getDriver().switchTo().frame(2); // first 2 frames are either empty or javascript
            return this;
        }

        public OpsBoardAdminEditorPage close() {
            findElement(By.id("//span[@class='v-button-caption' and contains(text(), 'Close')]")).click();
            getDriver().switchTo().parentFrame();
            return new OpsBoardAdminEditorPage(testCase);
        }
    }
}
