/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.core.utils.InetAddressUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AddNodePageIT extends OpenNMSSeleniumTestCase {
    private static final String m_unreachableIp = InetAddressUtils.str(InetAddressUtils.UNPINGABLE_ADDRESS);

    private RequisitionUtils m_requisitionUtils = new RequisitionUtils(this);

    @Before
    public void setUp() throws Exception {
        m_requisitionUtils.deleteTestRequisition();
    }

    @After
    public void tearDown() throws Exception {
        m_requisitionUtils.deleteTestRequisition();
    }

    @Test
    public void testAddNodePage() throws Exception {
        // Visit the provisioning page
        provisioningPage();

        // Add a requisition called REQUISITION_NAME
        findElementByXpath("//div/button[contains(@ng-click,'add')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        enterText(By.xpath("//form/input[contains(@class,'bootbox-input')]"), REQUISITION_NAME);
        findElementByXpath("//div/button[text()='OK']").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + REQUISITION_NAME + "']")));

        // Edit the requisition
        findElementByXpath("//td[text()='" + REQUISITION_NAME + "']/../td/button[contains(@ng-click,'edit(')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='There are no nodes on the " + REQUISITION_NAME + "']")));

        // Synchronize the empty requisition
        final String syncXpath = "//div/button[contains(@ng-click,'synchronize')]";
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(syncXpath)));
        findElementByXpath(syncXpath).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        findElementByXpath("//div/button[text()='No']").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='There are no nodes on the " + REQUISITION_NAME + "']")));

        // Wait for the requisition to be synchronized
        Thread.sleep(5000);

        frontPage();
        clickMenuItem("name=nav-admin-top", "Quick-Add Node", BASE_URL + "opennms/admin/node/add.htm");

        // Make sure there is a Provision button on the page
        final WebElement submitButton = m_driver.findElement(By.cssSelector("input[type=submit][value=Provision]"));
        assertEquals("Provision", submitButton.getAttribute("value"));

        // Select our test foreign source
        final WebElement selectElement = m_driver.findElement(By.cssSelector("select[name=foreignSource]"));
        final Select sel = new Select(selectElement);
        sel.selectByVisibleText(REQUISITION_NAME);

        // Add an unreachable IP address to the requisition
        findElementByName("ipAddress").sendKeys(m_unreachableIp);
        findElementByName("nodeLabel").sendKeys("AddNodePageTest");

        // Submit
        submitButton.click();

        // Click on the Provisioning Requisitions breadcrumb
        findElementByLink("Provisioning Requisitions").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + REQUISITION_NAME + "']")));
        wait.until(m_requisitionUtils.new WaitForNodesInDatabase(1));
    }
}
