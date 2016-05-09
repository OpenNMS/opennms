/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class QuickAddNodeIT extends OpenNMSSeleniumTestCase {

    /** The Constant NODE_LABEL. */
    private static final String NODE_LABEL = "localNode";
    private static final String NODE_IPADDR = "127.0.0.1";
    private static final String NODE_CATEGORY = "Test";

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
        createTestRequisition();
        provisioningPage();
    }

    /**
     * Tears down the test.
     * <p>Be 100% sure that there are no left-overs on the testing OpenNMS installation.</p>
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        deleteTestRequisition();
    }

    @Test
    public void testQuickAddNode() throws Exception {
        adminPage();
        clickMenuItem("name=nav-admin-top", "Quick-Add Node", "admin/ng-requisitions/app/quick-add-node.jsp");

        // Basic fields
        findElementByCss("input#foreignSource");
        Thread.sleep(1000);
        enterTextAutocomplete(By.id("foreignSource"), REQUISITION_NAME);
        enterText(By.id("ipAddress"), NODE_IPADDR);
        enterText(By.id("nodeLabel"), NODE_LABEL);

        // Add a category to the node
        findElementById("add-category").click();
        findElementByCss("input[name='categoryName'");
        enterTextAutocomplete(By.cssSelector("input[name='categoryName']"), NODE_CATEGORY, Keys.ENTER);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("provision"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog button[data-bb-handler='main']"))).click();

        wait.until(new WaitForNodesInDatabase(1));
    }

    protected WebElement enterTextAutocomplete(final By selector, final CharSequence... text) throws InterruptedException {
        final WebElement element = m_driver.findElement(selector);
        element.click();
        Thread.sleep(200);
        element.sendKeys(text);
        Thread.sleep(10);
        element.sendKeys(Keys.ENTER);
        Thread.sleep(10);
        try {
            setImplicitWait(2, TimeUnit.SECONDS);
            final List<WebElement> matching = m_driver.findElements(By.cssSelector("a[title='"+text+"']"));
            if (!matching.isEmpty()) {
                findElementByCss("a[title='"+text+"']").click();
            }
        } finally {
            setImplicitWait();
        }
        return element;
    }
}