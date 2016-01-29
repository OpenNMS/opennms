/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BSMAdminIT extends OpenNMSSeleniumTestCase {

    private static final String BSM_ADMIN_URL = BASE_URL + "opennms/admin/bsm/adminpage.jsp";
    private static final Logger LOG = LoggerFactory.getLogger(BSMAdminIT.class);

    private final RequisitionUtils requisitionUtils = new RequisitionUtils(this);

    private void createTestSetup() throws Exception {
        String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">" +
                "<node foreign-id=\"NodeA\" node-label=\"NodeA\">" +
                "<interface ip-addr=\"::1\" status=\"1\" snmp-primary=\"N\">" +
                "<monitored-service service-name=\"AAA\"/>" +
                "<monitored-service service-name=\"BBB\"/>" +
                "</interface>" +
                "<interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                "<monitored-service service-name=\"CCC\"/>" +
                "<monitored-service service-name=\"DDD\"/>" +
                "</interface>" +
                "</node>" +
                "</model-import>";

        String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        requisitionUtils.setupTestRequisition(requisitionXML, foreignSourceXML);
        wait.until(requisitionUtils.new WaitForNodesInDatabase(1));
    }

    private void removeTestSetup() throws Exception {
        requisitionUtils.deleteNode("NodeA");
        requisitionUtils.deleteForeignSource();
    }

    private void createBusinessService(String businessServiceName) throws InterruptedException {
        LOG.info("Trying to create BS with name {}", businessServiceName);
        WebElement createTextField = findElementById("createTextField");
        createTextField.sendKeys(businessServiceName);
        Thread.sleep(250); // wait before continuuing
        findElementById("createButton").click();
        wait.until(pageContainsText("Business Service Edit")); // we wait until the edit dialog appears
        findElementById("saveButton").click();
        wait.until(pageContainsText(businessServiceName));
    }

    @Before
    public void before() throws Exception {
        m_driver.get(BSM_ADMIN_URL);
        switchToVaadinFrame();
    }

    @After
    public void after() throws Exception {
        switchToDefaultFrame();
    }

    @Test
    public void testCanCreateAndDeleteBusinessService() throws InterruptedException {
        final String businessServiceName = createUniqueBusinessServiceName();
        createBusinessService(businessServiceName);
        findDeleteButton(businessServiceName).click();
        verifyElementNotPresent(By.id("deleteButton-" + businessServiceName));
    }

    @Test
    public void testCanChangeNameOfBusinessService() throws InterruptedException {
        final String serviceName = createUniqueBusinessServiceName();
        createBusinessService(serviceName);

        // rename business Service
        final String RENAMED_SERVICE_NAME = "renamed_service" + createUniqueBusinessServiceName();
        findElementById("editButton-" + serviceName).click();
        wait.until(pageContainsText("Business Service Edit"));
        WebElement nameField = findElementById("nameField");
        serviceName.equals(nameField.getText());
        nameField.clear();
        nameField.sendKeys(RENAMED_SERVICE_NAME);
        findElementById("saveButton").click();

        // verify that element was deleted
        verifyElementNotPresent(By.id("deleteButton-" + serviceName));
        pageContainsText(RENAMED_SERVICE_NAME);
        findElementById("deleteButton-" + RENAMED_SERVICE_NAME).click();
        verifyElementNotPresent(By.id("deleteButton-" + RENAMED_SERVICE_NAME));
    }

    @Test
    public void testCanHandleIpServicesDuringEdit() throws Exception {
        final String IP_Service_Select_XPATH = "//*[@id=\"ipServiceSelect\"]";
        final String ADD_BUTTON_XPATH = IP_Service_Select_XPATH + "/div[2]/div[1]/span/span";
        final String REMOVE_BUTTON_XPATH =  IP_Service_Select_XPATH + "/div[2]/div[3]/span/span";
        final String IP_SERVICE_1_XPATH_SELECTED = IP_Service_Select_XPATH + "/select[2]/option[1]";
        final String IP_SERVICE_1_XPATH_SELECTED_NOT = IP_Service_Select_XPATH + "/select[1]/option[1]";
        final String IP_SERVICE_2_XPATH_SELECTED = IP_Service_Select_XPATH + "/select[2]/option[2]";
        final String IP_SERVICE_2_XPATH_SELECTED_NOT = IP_Service_Select_XPATH + "/select[1]/option[2]";

        try {
            createTestSetup();

            // Create a BusinessService and open editor
            final String serviceName = createUniqueBusinessServiceName();
            createBusinessService(serviceName);
            findEditButton(serviceName).click();
            wait.until(pageContainsText("Business Service Edit"));

            // Check that the ipServices are known
            final String IP_SERVICE_1 = "NodeA//0:0:0:0:0:0:0:1/AAA";
            final String IP_SERVICE_2 = "NodeA//0:0:0:0:0:0:0:1/BBB";
            final String IP_SERVICE_3 = "NodeA//127.0.0.1/CCC";
            final String IP_SERVICE_4 = "NodeA//127.0.0.1/DDD";
            wait.until(pageContainsText(IP_SERVICE_1));
            wait.until(pageContainsText(IP_SERVICE_2));
            wait.until(pageContainsText(IP_SERVICE_3));
            wait.until(pageContainsText(IP_SERVICE_4));

            // Locate relevant components
            findElementById("ipServiceSelect");
            WebElement addButton = findElementByXpath(ADD_BUTTON_XPATH);
            findElementByXpath(REMOVE_BUTTON_XPATH);

            // Check for not selected ipServices
            findElementByXpath(IP_SERVICE_1_XPATH_SELECTED_NOT);
            findElementByXpath(IP_SERVICE_2_XPATH_SELECTED_NOT);

            // Add ipServices to selection
            addButton.click();
            addButton.click();
            addButton.click();

            // Check for selected ipServices
            findElementByXpath(IP_SERVICE_1_XPATH_SELECTED);
            findElementByXpath(IP_SERVICE_2_XPATH_SELECTED);

            // save
            findElementById("saveButton").click();

            // Open the BusinessService and check the ipServices are in place
            wait.until(pageContainsText("edit"));
            Thread.sleep(200); // wait for vaadin
            findEditButton(serviceName).click();

            // Verify that the previously added ip services are still selected
            wait.until(pageContainsText("Business Service Edit"));
            findElementByXpath(IP_SERVICE_1_XPATH_SELECTED);
            findElementByXpath(IP_SERVICE_2_XPATH_SELECTED);

            // Close dialog and delete BusinessService
            findElementById("cancelButton").click();
            findDeleteButton(serviceName).click();
            verifyElementNotPresent(By.id("deleteButton-" + serviceName));

        } finally {
            removeTestSetup();
        }
    }

    @Test
    public void testCanHandleBusinessServicesDuringEdit() throws Exception {
        final String Business_Service_Select_XPATH = "//*[@id=\"businessServiceSelect\"]";
        final String BUSINESS_SERVICE_ADD_BUTTON_XPATH = Business_Service_Select_XPATH + "/div[2]/div[1]/span/span";
        final String BUSINESS_SERVICE_REMOVE_BUTTON_XPATH =  Business_Service_Select_XPATH + "/div[2]/div[3]/span/span";
        final String BUSINESS_SERVICE_1_XPATH_SELECTED = Business_Service_Select_XPATH + "/select[2]/option[1]";
        final String BUSINESS_SERVICE_1_XPATH_SELECTED_NOT = Business_Service_Select_XPATH + "/select[1]/option[1]";
        final String BUSINESS_SERVICE_2_XPATH_SELECTED = Business_Service_Select_XPATH + "/select[2]/option[2]";
        final String BUSINESS_SERVICE_2_XPATH_SELECTED_NOT = Business_Service_Select_XPATH + "/select[1]/option[2]";

        // Create a bunch of business services
        final String serviceName = createUniqueBusinessServiceName();
        final String[] serviceNames = new String[]{
                serviceName + "_1",
                serviceName + "_2",
                serviceName + "_3"};
        for (String eachServiceName : serviceNames) {
            createBusinessService(eachServiceName);
        }

        // Open Edit Dialog for 1st Business Service which was just created
        findEditButton(serviceNames[0]).click();
        wait.until(pageContainsText("Business Service Edit"));

        // Locate relevant components
        findElementById("businessServiceSelect");
        WebElement addButton = findElementByXpath(BUSINESS_SERVICE_ADD_BUTTON_XPATH);
        findElementByXpath(BUSINESS_SERVICE_REMOVE_BUTTON_XPATH);

        // Check for not selected business services
        findElementByXpath(BUSINESS_SERVICE_1_XPATH_SELECTED_NOT);
        findElementByXpath(BUSINESS_SERVICE_2_XPATH_SELECTED_NOT);

        // Add Business Services to selection
        addButton.click();
        addButton.click();
        addButton.click();

        // Check for selected ipServices
        findElementByXpath(BUSINESS_SERVICE_1_XPATH_SELECTED);
        findElementByXpath(BUSINESS_SERVICE_2_XPATH_SELECTED);
        findElementById("saveButton").click();
        Thread.sleep(500); // pause

        // Verify that business service are gone
        // we have to delete backwards
        for (int i = 0; i < serviceNames.length; i++) {
            String eachServiceName = serviceNames[i];
            findDeleteButton(eachServiceName).click();
            if (i == 0) { // we remove the parent element first, the confirm dialog must be present
                findElementById("confirmationDialog.button.ok").click();
            }
            verifyElementNotPresent(By.id("deleteButton-" + eachServiceName));
        }
    }

    // switches to the embedded vaadin iframe
    private void switchToVaadinFrame() {
        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
    }

    // go back to the content "frame"
    private void switchToDefaultFrame() {
        m_driver.switchTo().defaultContent();
    }

    // If we use the same name over and over again tests may pass, even if we did not create/delete items,
    // therefore we create a unique business service name all the time
    private String createUniqueBusinessServiceName() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    }

    private WebElement findDeleteButton(String serviceName) {
        return findElementById("deleteButton-" + serviceName);
    }

    private WebElement findEditButton(String serviceName) {
        return findElementById("editButton-" + serviceName);
    }

    /**
     * Verifies that the provided element is not present.
     * @param by
     */
    private void verifyElementNotPresent(final By by) {
        new WebDriverWait(m_driver, 5 /* seconds */).until(
                ExpectedConditions.not(new ExpectedCondition<Boolean>() {
                    @Nullable
                    @Override
                    public Boolean apply(@Nullable WebDriver input) {
                        try {
                            // the default implicit wait timeout is too long, make it shorter
                            input.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                            WebElement elementFound = input.findElement(by);
                            return elementFound != null;
                        } catch (NoSuchElementException ex) {
                            return false;
                        } finally {
                            // set the implicit wait timeout back to the value it has been before
                            input.manage().timeouts().implicitlyWait(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
                        }
                    }
                })
        );
    }
}
