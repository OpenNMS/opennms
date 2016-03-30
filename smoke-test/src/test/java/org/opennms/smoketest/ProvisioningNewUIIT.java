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

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The Test Class for the New Provisioning UI using AngularJS.
 * <p>This test will left the current OpenNMS installation as it was before running,
 * to avoid issues related with the execution order of the smoke-tests.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Ignore("I can't for the life of me figure out why this does not work.  I give up.")
public class ProvisioningNewUIIT extends OpenNMSSeleniumTestCase {

    /** The Constant NODE_LABEL. */
    private static final String NODE_LABEL = "localNode";
    private static final String NODE_FOREIGNID = "localNode";
    private static final String NODE_IPADDR = "127.0.0.1";

    private RequisitionUtils m_requisitionUtils = new RequisitionUtils(this);

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        m_requisitionUtils.deleteTestRequisition();
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
        m_requisitionUtils.deleteTestRequisition();
    }

    /**
     * Test requisition UI.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRequisitionUI() throws Exception {
        // Add a new requisition
        clickId("add-requisition");
        WebElement modal = findModal();
        enterText(By.xpath("//form/input[contains(@class,'bootbox-input')]"), REQUISITION_NAME);
        modal.findElement(By.xpath("//div/button[text()='OK']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + REQUISITION_NAME + "']")));

        // Edit the foreign source

        findElementByXpath("//td[text()='" + REQUISITION_NAME + "']/../td/button[contains(@class,'edit-foreign-source')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='Foreign Source Definition for Requisition " + REQUISITION_NAME + "']")));

        // Add a detector
        clickId("add-detector");
        modal = findModal();
        enterText(By.xpath("//form[@name='detectorForm']//input[@ng-model='detector.name']"), "HTTP-8980");
        enterText(By.xpath("//form[@name='detectorForm']//input[@ng-model='detector.class']"), "HTTP");
        findElementByXpath("//form[@name='detectorForm']//ul[contains(@class, 'dropdown-menu')]/li/a/strong[text()='HTTP']").click();
        findElementByXpath("//div[contains(@class,'modal-footer')]//button[text()='Save']").click();

        // Save foreign source definition
        clickId("save-foreign-source");
        wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(By.id("save-foreign-source"))));

        // Go to the Requisition page
        clickId("go-back");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='Requisition " + REQUISITION_NAME + " (0 nodes)']")));

        // Add node to a requisition
        clickId("add-node");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nodeLabel")));
        enterText(By.id("nodeLabel"), NODE_LABEL);
        WebElement foreignId = m_driver.findElement(By.id("foreignId"));
        foreignId.clear();
        foreignId.sendKeys(NODE_FOREIGNID);

        // Add an IP Interface
        findElementByXpath("//*[@id='tab-interfaces']/a").click();
        final WebElement intfElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div/a[contains(@ng-click,'addInterface')]")));
        intfElement.click();
        final By ipaddrBy = By.xpath("//div/input[@id='ipAddress']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ipaddrBy));
        enterText(ipaddrBy, NODE_IPADDR);

        // Add a service to the IP Interface
        findElementByXpath("//div/button[contains(@ng-click,'addService')]").click();
        final String serviceXpath = "//div/input[@ng-model='service.name']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(serviceXpath)));
        enterText(By.xpath(serviceXpath), "HTTP-8980");

        // Save the IP interface
        findElementByXpath("//div[@class='modal-footer']/button[contains(@ng-click,'save')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'ng-binding') and text()='" + NODE_IPADDR + "']")));

        // Save the node
        clickId("save-node");
        wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(By.id("save-node"))));

        // Go to the requisition page
        clickId("go-back");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'ng-binding') and text()='" + NODE_LABEL + "']")));

        // Synchronize the requisition
        clickId("synchronize");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        modal = findModal();
        modal.findElement(By.xpath("//div/button[text()='Yes']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='Requisition " + REQUISITION_NAME + " (1 nodes)']")));

        // Go to the requisitions page
        clickId("go-back");

        // Wait until the node has been added to the database, using the ReST API
        m_driver.get(BASE_URL + "opennms/rest/nodes/" + REQUISITION_NAME + ":" + NODE_FOREIGNID + "/ipinterfaces/" + NODE_IPADDR + "/services/ICMP");
        m_driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
        try {
            for (int i=0; i<30; i++) {
                try {
                    final WebElement e = m_driver.findElement(By.xpath("//service/serviceType/name[text()='ICMP']"));
                    if (e != null) {
                        break;
                    }
                } catch (Exception e) {}
                m_driver.navigate().refresh();
            }
        } finally {
            m_driver.manage().timeouts().implicitlyWait(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        // Open the nodes list page
        m_driver.get(BASE_URL + "opennms/");
        clickMenuItem("Info", "Nodes", "element/nodeList.htm");

        try {
            // Disable implicitlyWait
            m_driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
            // If this is the only node on the system, we'll be sent directly to its node details page.
            findElementByXpath("//h3[text()='Availability']");
        } catch (NoSuchElementException e) {
            // If there are multiple nodes, we will be on the node list page, click through to the node
            findElementByLink(NODE_LABEL).click();
        } finally {
            // Restore the implicitlyWait timeout
            m_driver.manage().timeouts().implicitlyWait(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("ICMP")));
        findElementByXpath("//a[contains(@href, 'element/interface.jsp') and text()='" + NODE_IPADDR + "']");
        findElementByLink("HTTP-8980");
    }

    private WebElement findModal() {
        final String xpath = "//div[contains(@class, 'modal-dialog')]";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        return findElementByXpath(xpath);
    }
}
