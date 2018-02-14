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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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
public class ProvisioningNewUIIT extends OpenNMSSeleniumTestCase {
    private static final String NODE_LABEL = "localNode";
    private static final String NODE_FOREIGNID = "localNode";
    private static final String NODE_IPADDR = "127.0.0.1";
    private static final String NODE_SERVICE = "HTTP-8980";
    private static final String NODE_CATEGORY = "Test";

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

    /**
     * Tears down the test.
     * <p>Be 100% sure that there are no left-overs on the testing OpenNMS installation.</p>
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        //deleteTestRequisition();
    }

    /**
     * Test requisition UI.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRequisitionUI() throws Exception {
        setImplicitWait(2, TimeUnit.SECONDS);

        // Add a new requisition
        clickId("add-requisition", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form.bootbox-form > input.bootbox-input")));
        enterText(By.cssSelector("form.bootbox-form > input.bootbox-input"), REQUISITION_NAME);
        findElementByXpath("//div/button[text()='OK']").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + REQUISITION_NAME + "']")));

        // trigger dropdown menu
        final String moreActionsButton = "button.btn[uib-tooltip='More actions for requisition "+REQUISITION_NAME+"']";
        clickElement(By.cssSelector(moreActionsButton));

        // Edit the foreign source
        wait.until(ExpectedConditions.elementToBeClickable(By.id("editForeignSource-"+REQUISITION_NAME))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.nav-tabs > li > a.nav-link")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='Foreign Source Definition for Requisition " + REQUISITION_NAME + "']")));

        // Add a detector
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-detector")));
        clickId("add-detector", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form[name='detectorForm']")));
        enterText(By.xpath("//form[@name='detectorForm']//input[@ng-model='detector.name']"), NODE_SERVICE);
        enterText(By.xpath("//form[@name='detectorForm']//input[@ng-model='detector.class']"), "HTTP");
        findElementByXpath("//form[@name='detectorForm']//ul[contains(@class, 'dropdown-menu')]/li/a/strong[text()='HTTP']").click();
        waitForDropdownClose();

        // Add a parameter to the detector
        clickId("add-detector-parameter", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='paramName']")));
        enterText(By.cssSelector("input[name='paramName']"), "po");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@title='port']"))).click();
        enterText(By.cssSelector("input[name='paramValue']"), "8980");
        //enterText(By.cssSelector("input[name='paramValue']"), Keys.ENTER);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("save-detector"))).click();
        waitForModalClose();
        enterText(By.cssSelector("input[placeholder='Search/Filter Detectors'][ng-model='filters.detector']"), "HTTP-8980");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='"+NODE_SERVICE+"']")));

        // Add a policy to the detector
        findElementByCss("#tab-policies .ng-binding").click();
        clickId("add-policy", false);
        findElementByCss("form[name='policyForm']");
        enterText(By.cssSelector("input#name"), "No IPs");
        enterText(By.cssSelector("input#clazz"), "Match IP Interface");
        enterText(By.cssSelector("input#clazz"), Keys.ENTER);
        enterText(By.xpath("(//input[@name='paramValue'])[1]"), "DO_NOT_PERSIST");
        enterText(By.xpath("(//input[@name='paramValue'])[1]"), Keys.ENTER);
        enterText(By.xpath("(//input[@name='paramValue'])[2]"), "NO_PARAMETERS");
        enterText(By.xpath("(//input[@name='paramValue'])[2]"), Keys.ENTER);
        clickId("save-policy", false);
        waitForModalClose();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='No IPs']")));

        // Save foreign source definition
        clickId("save-foreign-source", false);
        wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(By.id("save-foreign-source"))));

        // Go to the Requisition page
        clickId("go-back", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='Requisition " + REQUISITION_NAME + " (0 defined, 0 deployed)']")));

        // Add node to a requisition
        clickId("add-node", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nodeLabel"))).clear();
        enterText(By.id("nodeLabel"), NODE_LABEL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("foreignId"))).clear();
        enterText(By.id("foreignId"), NODE_FOREIGNID);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("location"))).clear();
        saveNode();

        // Add an IP Interface
        clickId("tab-interfaces", false);
        findElementById("add-interface").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form[name='intfForm']")));
        final By ipaddrBy = By.cssSelector("input#ipAddress");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ipaddrBy));
        enterText(ipaddrBy, NODE_IPADDR);

        // Add a service to the IP Interface
        findElementById("add-service").click();
        final By xpath = By.cssSelector("input[name='serviceName']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(xpath));
        Thread.sleep(100);
        enterText(xpath, "HTTP-89");
        findElementByXpath("//a[@title='HTTP-8980']/strong").click();

        // Save the IP interface
        clickId("save-interface", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'ng-binding') and text()='" + NODE_IPADDR + "']")));

        // Add an asset to the node
        clickId("tab-assets", false);
        clickId("add-asset", false);
        findElementByCss("form[name='assetForm']");
        enterText(By.id("asset-name"), "countr");
        findElementByXpath("//a[@title='country']/strong").click();
        enterText(By.id("asset-value"), "USA");
        clickId("save-asset", false);
        waitForModalClose();

        // Add a category to the node
        clickId("tab-categories", false);
        clickId("add-category", false);
        Thread.sleep(100);
        enterText(By.cssSelector("input[name='categoryName']"), NODE_CATEGORY);
        findElementByXpath("//a[@title='"+NODE_CATEGORY+"']/strong").click();

        saveNode();

        // Go to the requisition page
        clickId("go-back", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'ng-binding') and text()='" + NODE_LABEL + "']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'ng-binding') and text()='" + NODE_FOREIGNID + "']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(@class,'ng-binding') and text()='" + NODE_IPADDR + " (P)']")));

        // Synchronize the requisition
        clickId("synchronize", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-dialog button.btn.btn-primary")));
        WebElement modal = findModal();
        modal.findElement(By.xpath("//div/button[text()='Yes']")).click();
        waitForModalClose();
        wait.until(new WaitForNodesInRequisition(REQUISITION_NAME, 1));
        wait.until(new WaitForNodesInDatabase(REQUISITION_NAME, 1));
        clickId("refresh", false);
        clickId("refreshDeployedStats", false);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='Requisition " + REQUISITION_NAME + " (1 defined, 1 deployed)']")));

        // Go to the requisitions page
        clickId("go-back", false);

        // Wait until the node has been added to the database, using the ReST API
        m_driver.get(getBaseUrl() + "opennms/rest/nodes/" + REQUISITION_NAME + ":" + NODE_FOREIGNID + "/ipinterfaces/" + NODE_IPADDR + "/services/ICMP");
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
        m_driver.get(getBaseUrl() + "opennms/");
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

    protected void saveNode() throws InterruptedException {
        clickId("save-node", false);
        wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(By.id("save-node"))));
    }

    protected void waitForDropdownClose() {
        waitForClose(By.cssSelector(".modal-dialog ul.dropdown-menu"));
    }

    protected void waitForModalClose() {
        System.err.println("waitForModalClose()");
        waitForClose(By.cssSelector(".modal-dialog"));
    }

    protected WebElement findModal() {
        final String xpath = "//div[contains(@class, 'modal-dialog')]";
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }
}
