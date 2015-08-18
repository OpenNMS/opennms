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

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The Test Class for the New Provisioning UI using AngularJS.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ProvisioningNewUIIT extends OpenNMSSeleniumTestCase {

    /** The Constant NODE_LABEL. */
    private static final String NODE_LABEL = "localNode";
    private static final String NODE_FOREIGNID = "localNode";
    private static final String NODE_IPADDR = "127.0.0.1";

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
        provisioningPage();
    }

    /* (non-Javadoc)
     * @see org.opennms.smoketest.OpenNMSSeleniumTestCase#provisioningPage()
     */
    @Override
    protected void provisioningPage() {
        m_driver.get(BASE_URL + "opennms/admin/index.jsp");
        m_driver.findElement(By.linkText("Manage Provisioning Requisitions [New UI]")).click();
    }

    /* (non-Javadoc)
     * @see org.opennms.smoketest.OpenNMSSeleniumTestCase#deleteTestRequisition()
     */
    @Override
    protected void deleteTestRequisition() throws Exception {
        // TODO This can be done using the new UI as well
        final Integer responseCode = doRequest(new HttpGet(BASE_URL + "/opennms/rest/requisitions/" + REQUISITION_NAME));
        if (responseCode == 404 || responseCode == 204) {
            return;
        }
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/requisitions/" + REQUISITION_NAME));
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/requisitions/deployed/" + REQUISITION_NAME));
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/foreignSources/" + REQUISITION_NAME));
        doRequest(new HttpDelete(BASE_URL + "/opennms/rest/foreignSources/deployed/" + REQUISITION_NAME));
        doRequest(new HttpGet(BASE_URL + "/opennms/rest/requisitions"));
    }

    /**
     * Test requisition UI.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRequisitionUI() throws Exception {
        // Add a new requisition
        findElementByXpath("//div/ul/li/a[contains(@ng-click,'add')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        enterText(By.xpath("//form/input[contains(@class,'bootbox-input')]"), REQUISITION_NAME);
        findElementByXpath("//div/button[text()='OK']").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + REQUISITION_NAME + "']")));

        // Edit the foreign source
        findElementByXpath("//td[text()='" + REQUISITION_NAME + "']/../td/a[contains(@ng-click,'editForeignSource')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Foreign Source Definition (" + REQUISITION_NAME + ")']")));

        // Add a detector
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div/a[contains(@ng-click,'addDetector')]")));
        Thread.sleep(10000); // Inducing a delay to be sure that the page and all the asynchronous operations have been executed
        findElementByXpath("//div/a[contains(@ng-click,'addDetector')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        enterText(By.xpath("//input[@ng-model='detector.name']"), "HTTP-8980");
        enterText(By.xpath("//input[@ng-model='detector.class']"), "HTTP");
        findElementByXpath("//li/a/strong[text()='HTTP']").click();
        findElementByXpath("//div[contains(@class,'modal-footer')]/button[text()='Save']").click();

        // Save foreign source definition
        final String saveFSXpath = "//ul/li/button[contains(text(),'Save')]";
        findElementByXpath(saveFSXpath).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(By.xpath(saveFSXpath))));

        // Go to the Requisition page
        findElementByXpath("//ul/li/a[contains(text(),'Return')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='" + REQUISITION_NAME + " (0 nodes)']")));

        // Add node to a requisition
        findElementByXpath("//ul/li/a[contains(@ng-click,'addNode')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nodeLabel")));
        enterText(By.id("nodeLabel"), NODE_LABEL);
        WebElement foreignId = m_driver.findElement(By.id("foreignId"));
        foreignId.clear();
        foreignId.sendKeys(NODE_FOREIGNID);

        // Add an IP Interface
        findElementByXpath("//ul/li/a[text()='Interfaces']").click();
        final String addIntfXpath = "//div/a[contains(@ng-click,'addInterface')]";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(addIntfXpath)));
        findElementByXpath(addIntfXpath).click();
        final String ipAddrXpath = "//div/input[@id='ipAddress']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ipAddrXpath)));
        enterText(By.xpath(ipAddrXpath), NODE_IPADDR);
        findElementByXpath("//div/button[contains(@ng-click,'addService')]").click();

        // Add a service to the IP Interface
        final String serviceXpath = "//div/input[@ng-model='service.name']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(serviceXpath)));
        enterText(By.xpath(serviceXpath), "HTTP-8980");

        // Save the IP interface
        findElementByXpath("//div/button[contains(@ng-click,'save')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'ng-binding') and text()='" + NODE_IPADDR + "']")));

        // Save the node
        final String saveNodeXpath = "//div/ul/li/button[contains(@ng-click,'save')]";
        findElementByXpath(saveNodeXpath).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(By.xpath(saveNodeXpath))));
        findElementByXpath("//div/ul/li/a[contains(@ng-click,'goBack')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'ng-binding') and text()='" + NODE_LABEL + "']")));

        // Synchronize the requisition
        findElementByXpath("//div/ul/li/a[contains(@ng-click,'synchronize')]").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        findElementByXpath("//div/button[text()='Yes']").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='" + REQUISITION_NAME + " (1 nodes)']")));
        findElementByXpath("//div/ul/li/a[contains(@ng-click,'goBack')]").click();

        // Wait until the node has been added to the database, using the ReST API
        for (int i=0; i<5; i++) {
            Thread.sleep(10000); // Give enough time before trying again.
            try {
                m_driver.get(BASE_URL + "opennms/rest/nodes/" + REQUISITION_NAME + ":" + NODE_FOREIGNID + "/ipinterfaces/" + NODE_IPADDR + "/services/ICMP");
                WebElement e = m_driver.findElement(By.xpath("//service/serviceType/name[text()='ICMP']"));
                if (e != null) {
                    break;
                }
            } catch (Exception e) {}
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
    }
}
