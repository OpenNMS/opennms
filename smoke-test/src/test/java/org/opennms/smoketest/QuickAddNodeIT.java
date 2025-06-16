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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class QuickAddNodeIT extends OpenNMSSeleniumIT {

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

    private boolean textFieldsReady() {
        try {
            return REQUISITION_NAME.equals(waitForElement(By.id("foreignSource")).getAttribute("value")) &&
                    NODE_IPADDR.equals(waitForElement(By.id("ipAddress")).getAttribute("value")) &&
                    NODE_LABEL.equals(waitForElement(By.id("nodeLabel")).getAttribute("value"));
        } catch (final Exception e) {
            return false;
        }
    }

    @Test
    public void testQuickAddNode() throws Exception {
        adminPage();
        findElementByXpath("//nav//a[contains(@title, 'Quick-Add Node') and contains(@href, 'admin/ng-requisitions/quick-add-node.jsp')]").click();

        Thread.sleep(5000);

        long end = System.currentTimeMillis() + LOAD_TIMEOUT;
        do {
            // Basic fields
            findElementByCss("input#foreignSource");
            enterTextAutocomplete(By.id("foreignSource"), REQUISITION_NAME);
            enterText(By.id("ipAddress"), NODE_IPADDR);
            enterText(By.id("nodeLabel"), NODE_LABEL);
        } while (!textFieldsReady() && System.currentTimeMillis() < end);

        // Add a category to the node
        findElementById("add-category").click();
        findElementByCss("input[name='categoryName'");
        enterTextAutocomplete(By.cssSelector("input[name='categoryName']"), NODE_CATEGORY);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("provision"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Ok']"))).click();

        wait.until(new WaitForNodesInDatabase(1));
    }

    protected WebElement enterTextAutocomplete(final By selector, final CharSequence... text) throws InterruptedException {
        final WebElement element = driver.findElement(selector);
        element.clear();
        element.click();
        Thread.sleep(500);
        element.sendKeys(text);
        Thread.sleep(100);
        element.sendKeys(Keys.ENTER);
        Thread.sleep(100);
        try {
            setImplicitWait(5, TimeUnit.SECONDS);
            final List<WebElement> matching = driver.findElements(By.cssSelector("a[title='"+text+"']"));
            if (!matching.isEmpty()) {
                findElementByCss("a[title='"+text+"']").click();
            }
        } finally {
            setImplicitWait();
        }
        return element;
    }
}
