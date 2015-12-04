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

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BSMAdminIT extends OpenNMSSeleniumTestCase {

    private final String RENAMED_SERVICE_NAME = "renamed_service";
    private final String BSM_ADMIN_URL = BASE_URL + "opennms/admin/bsm/adminpage.jsp";
    private final String BASIC_SERVICE_NAME = "BasicService";

    private final String ADD_BUTTON_XPATH = "/html/body/div[3]/div[5]/div/div/div[5]/div/div/div/div[3]/div/div[2]/div/div[2]/div[1]/span/span";
    private final String REMOVE_BUTTON_XPATH = "/html/body/div[3]/div[5]/div/div/div[5]/div/div/div/div[3]/div/div[2]/div/div[2]/div[3]/span/span";

    private final String IP_SERVICE_1 = "NodeA//0:0:0:0:0:0:0:1/AAA";
    private final String IP_SERVICE_2 = "NodeA//0:0:0:0:0:0:0:1/BBB";
    private final String IP_SERVICE_3 = "NodeA//127.0.0.1/CCC";
    private final String IP_SERVICE_4 = "NodeA//127.0.0.1/DDD";

    private final String IP_SERVICE_1_XPATH_SELECTED = "/html/body/div[3]/div[5]/div/div/div[5]/div/div/div/div[3]/div/div[2]/div/select[2]/option[1]";
    private final String IP_SERVICE_1_XPATH_SELECTED_NOT = "/html/body/div[3]/div[5]/div/div/div[5]/div/div/div/div[3]/div/div[2]/div/select[1]/option[1]";

    private final String IP_SERVICE_2_XPATH_SELECTED = "/html/body/div[3]/div[5]/div/div/div[5]/div/div/div/div[3]/div/div[2]/div/select[2]/option[2]";
    private final String IP_SERVICE_2_XPATH_SELECTED_NOT = "/html/body/div[3]/div[5]/div/div/div[5]/div/div/div/div[3]/div/div[2]/div/select[1]/option[2]";

    private final RequisitionUtils requisitionUtils = new RequisitionUtils(this);

    private void buildNodes() throws Exception {
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
    }

    private void removeNodes() throws Exception {
        requisitionUtils.deleteNode("NodeA");
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
    public void testCanCreateMinimalBusinessService() {
        findElementById("createTextField").sendKeys(BASIC_SERVICE_NAME);
        findElementById("createButton").click();

        wait.until(pageContainsText("Save"));
        findElementById("saveButton").click();
        wait.until(pageContainsText(BASIC_SERVICE_NAME));
    }

    @Test
    public void testCanDeleteBusinessService() {
        findElementById("deleteButton-" + BASIC_SERVICE_NAME).click();
        ExpectedConditions.invisibilityOfElementLocated(By.id("deleteButton-" + BASIC_SERVICE_NAME));
    }

    @Test
    public void testCanChangeNameOfBusinessService() {
        testCanCreateMinimalBusinessService();
        findElementById("editButton-" + BASIC_SERVICE_NAME).click();
        wait.until(pageContainsText("Business Service Edit"));

        WebElement nameField = findElementById("nameField");
        BASIC_SERVICE_NAME.equals(nameField.getText());
        nameField.clear();
        nameField.sendKeys(RENAMED_SERVICE_NAME);

        findElementById("saveButton").click();

        ExpectedConditions.invisibilityOfElementLocated(By.id("deleteButton-" + BASIC_SERVICE_NAME));
        pageContainsText(RENAMED_SERVICE_NAME);
        findElementById("deleteButton-" + RENAMED_SERVICE_NAME).click();
        ExpectedConditions.invisibilityOfElementLocated(By.id("deleteButton-" + RENAMED_SERVICE_NAME));
    }

    @Test
    public void testCanHandelIpServicesDuringEdit() throws Exception {
        //Create test data and wait for it to show up
        buildNodes();
        wait.until(requisitionUtils.new WaitForNodesInDatabase(1));

        //Create BusinessService open editor
        testCanCreateMinimalBusinessService();
        findElementById("editButton-" + BASIC_SERVICE_NAME).click();
        wait.until(pageContainsText("Business Service Edit"));

        //Check that the ipServices are known
        wait.until(pageContainsText(IP_SERVICE_1));
        wait.until(pageContainsText(IP_SERVICE_2));
        wait.until(pageContainsText(IP_SERVICE_3));
        wait.until(pageContainsText(IP_SERVICE_4));

        //Locate relevant components
        findElementById("ipServiceSelect");
        WebElement addButton = findElementByXpath(ADD_BUTTON_XPATH);
        findElementByXpath(REMOVE_BUTTON_XPATH);

        //Check for not selected ipServices
        findElementByXpath(IP_SERVICE_1_XPATH_SELECTED_NOT);
        findElementByXpath(IP_SERVICE_2_XPATH_SELECTED_NOT);

        //Add ipServices to selection
        addButton.click();
        addButton.click();
        addButton.click();

        //Check for selected ipServices
        findElementByXpath(IP_SERVICE_1_XPATH_SELECTED);
        findElementByXpath(IP_SERVICE_2_XPATH_SELECTED);

        findElementById("saveButton").click();

        //Open the BusinessService and check the ipServices are in place
        wait.until(pageContainsText("edit"));
        findElementById("editButton-BasicService").click();

        //Check for selected ipServices
        wait.until(pageContainsText("Cancel"));
        findElementByXpath(IP_SERVICE_1_XPATH_SELECTED);
        findElementByXpath(IP_SERVICE_2_XPATH_SELECTED);

        //Close dialog an delete BusinessService
        findElementById("cancelButton").click();
        findElementById("deleteButton-BasicService").click();

        removeNodes();
    }

    // switches to the embedded vaadin iframe
    private void switchToVaadinFrame() {
        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
    }

    // go back to the content "frame"
    private void switchToDefaultFrame() {
        m_driver.switchTo().defaultContent();
    }
}
