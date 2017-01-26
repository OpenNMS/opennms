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
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BSMAdminIT extends OpenNMSSeleniumTestCase {

    private static final String BSM_ADMIN_URL = BASE_URL + "opennms/admin/bsm/adminpage.jsp";
    private static final Logger LOG = LoggerFactory.getLogger(BSMAdminIT.class);

    /**
     * Class to control the inputs of the "Business Service Edit"-Window
     */
    private class BsmAdminPageEditWindow {
        private final String businessServiceName;

        private BsmAdminPageEditWindow(final String businessServiceName) {
            this.businessServiceName = businessServiceName;
        }

        public BsmAdminPage save() {
            findElementById("saveButton").click();
            wait.until(pageContainsText(businessServiceName));
            return new BsmAdminPage();
        }

        public BsmAdminPage cancel() {
            findElementById("cancelButton").click();
            return new BsmAdminPage();
        }

        public BsmAdminPageEditWindow name(String newName) {
            WebElement nameField = findElementById("nameField");
            nameField.clear();
            nameField.sendKeys(newName);
            return new BsmAdminPageEditWindow(newName);
        }

        public BsmAdminPageEdgeEditWindow newEdgeWindow() {
            findElementById("addEdgeButton").click();
            wait.until(pageContainsText("Business Service Edge Edit"));
            return new BsmAdminPageEdgeEditWindow();
        }

        public BsmAdminPageEdgeEditWindow addChildEdge(String childServiceText, String mapFunctionText, int weight) throws InterruptedException {
            BsmAdminPageEdgeEditWindow editPage = newEdgeWindow()
                    .selectChildService(childServiceText)
                    .selectMapFunction(mapFunctionText)
                    .weight(weight)
                    .confirm();
            Thread.sleep(250);
            return editPage;
        }

        public BsmAdminPageEditWindow addReductionKeyEdge(String reductionKeyText, String mapFunctionText, int weight) throws InterruptedException {
            newEdgeWindow()
                    .reductionKey(reductionKeyText)
                    .selectMapFunction(mapFunctionText)
                    .weight(weight)
                    .confirm();
            Thread.sleep(250);
            return this;
        }

        public BsmAdminPageEditWindow addIpServiceEdge(String ipServiceText, String mapFunctionText, int weight) throws InterruptedException {
            newEdgeWindow()
                    .selectIpService(ipServiceText)
                    .selectMapFunction(mapFunctionText)
                    .weight(weight)
                    .confirm();
            Thread.sleep(250);
            return this;
        }

        public BsmAdminPageEditWindow removeEdge(String edgeValueString) {
            new Select(findElementByXpath("//*[@id=\"edgeList\"]/select")).selectByVisibleText(edgeValueString);
            findElementById("removeEdgeButton").click();
            return this;
        }

        public BsmAdminPageEditWindow setReductionFunction(final String reductionFunctionValueString) {
            new Select(findElementByXpath("//*[@id=\"reduceFunctionNativeSelect\"]/select")).selectByVisibleText(reductionFunctionValueString);
            return this;
        }

        public BsmAdminPageEditWindow setThreshold(final float threshold) {
            findElementById("thresholdTextField").clear();
            findElementById("thresholdTextField").sendKeys(String.valueOf(threshold));
            return this;
        }
    }

    /**
     * Class to control the inputs and workflow of the "Business Service Admin" Page
     */
    private class BsmAdminPage {

        public BsmAdminPage open() {
            m_driver.get(BSM_ADMIN_URL);
            switchToVaadinFrame();
            return this;
        }

        public BsmAdminPageEditWindow openNewDialog(String businessServiceName) throws InterruptedException {
            WebElement createTextField = findElementById("createTextField");
            createTextField.sendKeys(businessServiceName);
            Thread.sleep(250); // wait before continuuing
            findElementById("createButton").click();
            wait.until(pageContainsText("Business Service Edit")); // we wait until the edit dialog appears
            return new BsmAdminPageEditWindow(businessServiceName);
        }

        public BsmAdminPageEditWindow openEditDialog(String businessServiceName) {
            findElementById("editButton-" + businessServiceName).click();
            wait.until(pageContainsText("Business Service Edit"));
            return new BsmAdminPageEditWindow(businessServiceName);
        }

        public void delete(String businessServiceName) {
            delete(businessServiceName, false);
        }

        public void rename(String serviceName, String newServiceName) {
            openEditDialog(serviceName).name(newServiceName).save();
        }

        public void delete(String serviceName, boolean withConfirmDialog) {
            findDeleteButton(serviceName).click();
            if (withConfirmDialog) { // we remove the parent element first, the confirm dialog must be present
                findElementById("confirmationDialog.button.ok").click();
            }
            verifyElementNotPresent(By.id("deleteButton-" + serviceName));
        }
    }

    private class BsmAdminPageEdgeEditWindow {
        private BsmAdminPageEdgeEditWindow selectEdgeType(String edgeType) {
            new Select(findElementByXpath("//*[@id=\"edgeTypeSelector\"]/select")).selectByVisibleText(edgeType);
            return this;
        }

        public BsmAdminPageEdgeEditWindow selectIpService(String ipServiceText) {
            selectEdgeType("IP Service");
            new Select(findElementByXpath("//*[@id=\"ipServiceList\"]/select")).selectByVisibleText(ipServiceText);
            return this;
        }

        public BsmAdminPageEdgeEditWindow selectChildService(String childServiceText) {
            selectEdgeType("Child Service");
            new Select(findElementByXpath("//*[@id=\"childServiceList\"]/select")).selectByVisibleText(childServiceText);
            return this;
        }

        public BsmAdminPageEdgeEditWindow selectMapFunction(String mapFunctionText) {
            new Select(findElementByXpath("//*[@id=\"mapFunctionSelector\"]/select")).selectByVisibleText(mapFunctionText);
            return this;
        }

        public BsmAdminPageEdgeEditWindow reductionKey(String reductionKey) throws InterruptedException {
            selectEdgeType("Reduction Key");
            findElementById("reductionKeyField").clear();
            findElementById("reductionKeyField").sendKeys(reductionKey);
            return this;
        }

        public BsmAdminPageEdgeEditWindow weight(int weight) {
            findElementById("weightField").clear();
            findElementById("weightField").sendKeys(String.valueOf(weight));
            return this;
        }

        public BsmAdminPageEdgeEditWindow confirm() {
            findElementById("saveEdgeButton").click();
            wait.until(pageContainsText("Business Service Edit"));
            return new BsmAdminPageEdgeEditWindow();
        }
    }

    private final RequisitionUtils requisitionUtils = new RequisitionUtils(this);

    private BsmAdminPage bsmAdminPage;

    private void createTestSetup() throws Exception {
        String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">" +
                "   <node foreign-id=\"NodeA\" node-label=\"NodeA\">" +
                "       <interface ip-addr=\"::1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"AAA\"/>" +
                "           <monitored-service service-name=\"BBB\"/>" +
                "       </interface>" +
                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"CCC\"/>" +
                "           <monitored-service service-name=\"DDD\"/>" +
                "       </interface>" +
                "   </node>" +
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

    @Before
    public void before() throws Exception {
        bsmAdminPage = new BsmAdminPage();
        bsmAdminPage.open();
    }

    @After
    public void after() throws Exception {
        switchToDefaultFrame();
    }

    @Test
    public void testCanCreateAndDeleteBusinessService() throws InterruptedException {
        final String businessServiceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(businessServiceName).save();
        bsmAdminPage.delete(businessServiceName);
    }

    @Test
    public void testCanCreateAndDeleteBusinessServiceWithThreshold() throws InterruptedException {
        final String businessServiceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(businessServiceName)
                    .setReductionFunction("Threshold")
                    .setThreshold(0.25f)
                    .save();

        bsmAdminPage.delete(businessServiceName);
    }

    @Test
    public void testCanRenameBusinessService() throws InterruptedException {
        final String serviceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(serviceName).save();

        // rename business Service
        final String RENAMED_SERVICE_NAME = "renamed_service" + createUniqueBusinessServiceName();
        bsmAdminPage.rename(serviceName, RENAMED_SERVICE_NAME);

        // verify that element was deleted
        verifyElementNotPresent(By.id("deleteButton-" + serviceName));
        pageContainsText(RENAMED_SERVICE_NAME);
        bsmAdminPage.delete(RENAMED_SERVICE_NAME);
    }

    @Test
    public void testCanAddIpServiceEdge() throws Exception {
        try {
            createTestSetup();

            // Create a BusinessService and open editor
            final String serviceName = createUniqueBusinessServiceName();
            BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName);

            // Check that the ipServices are known
            BsmAdminPageEdgeEditWindow bsmAdminPageEdgeEditWindow = bsmAdminPageEditWindow.newEdgeWindow();
            bsmAdminPageEdgeEditWindow.selectEdgeType("IP Service");
            final String IP_SERVICE_1 = "NodeA /0:0:0:0:0:0:0:1 AAA";
            final String IP_SERVICE_2 = "NodeA /0:0:0:0:0:0:0:1 BBB";
            final String IP_SERVICE_3 = "NodeA /127.0.0.1 CCC";
            final String IP_SERVICE_4 = "NodeA /127.0.0.1 DDD";
            wait.until(pageContainsText(IP_SERVICE_1));
            wait.until(pageContainsText(IP_SERVICE_2));
            wait.until(pageContainsText(IP_SERVICE_3));
            wait.until(pageContainsText(IP_SERVICE_4));

            // Locate relevant components
            bsmAdminPageEdgeEditWindow.selectIpService(IP_SERVICE_3);
            bsmAdminPageEdgeEditWindow.selectMapFunction("Increase");
            bsmAdminPageEdgeEditWindow.confirm();

            // save
            bsmAdminPageEditWindow.save();

            // Verify
            bsmAdminPage.openEditDialog(serviceName);
            wait.until(pageContainsText("IPSvc: NodeA /127.0.0.1 CCC, Map: Increase, Weight: 1"));

            // Close dialog and delete BusinessService
            bsmAdminPageEditWindow.cancel();
            bsmAdminPage.delete(serviceName);
        } finally {
            removeTestSetup();
        }
    }

    @Test
    public void testCanAddBusinessServiceEdge() throws Exception {
        // Create a bunch of business services
        final String serviceName = createUniqueBusinessServiceName();
        final String[] serviceNames = new String[]{
                serviceName + "_1",
                serviceName + "_2",
                serviceName + "_3"};
        for (String eachServiceName : serviceNames) {
            bsmAdminPage.openNewDialog(eachServiceName).save();
        }

        // Open Edit Dialog for 1st Business Service which was just created
        final BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceNames[0]);
        bsmAdminPageEditWindow.addChildEdge(serviceNames[1], "Decrease", 2);
        bsmAdminPageEditWindow.addChildEdge(serviceNames[2], "Ignore", 3);

        // verify
        wait.until(pageContainsText(String.format("Child: %s, Map: Decrease, Weight: 2", serviceNames[1])));
        wait.until(pageContainsText(String.format("Child: %s, Map: Ignore, Weight: 3", serviceNames[2])));
        bsmAdminPageEditWindow.save();
        Thread.sleep(500); // pause

        // Verify that business service are gone
        // we have to delete backwards
        for (int i = 0; i < serviceNames.length; i++) {
            final String eachServiceName = serviceNames[i];
            bsmAdminPage.delete(eachServiceName, i==0);
        }
    }

    @Test
    public void testCanAddReductionKeyEdge() throws InterruptedException {
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName);
        bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.1", "Increase", 17);

        // verify edit
        wait.until(pageContainsText("ReKey: test.rk.1, Map: Increase, Weight: 17"));
        bsmAdminPageEditWindow.save();

        // verify save
        bsmAdminPage.openEditDialog(serviceName);
        wait.until(pageContainsText("ReKey: test.rk.1, Map: Increase, Weight: 17"));
        bsmAdminPageEditWindow.cancel();

        // clean up
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanAddMixedTypeEdges() throws Exception {
        try {
            // Initialize services
            createTestSetup();
            final String parentServiceName = createUniqueBusinessServiceName();
            final String child1 = parentServiceName + "_child1";
            final String child2 = parentServiceName + "_child2";
            bsmAdminPage.openNewDialog(parentServiceName).save();
            bsmAdminPage.openNewDialog(child1).save();
            bsmAdminPage.openNewDialog(child2).save();

            // define edges
            final BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(parentServiceName);
            bsmAdminPageEditWindow.addChildEdge(child1, "Identity", 1);
            bsmAdminPageEditWindow.addChildEdge(child2, "Ignore", 1);
            bsmAdminPageEditWindow.addIpServiceEdge("NodeA /0:0:0:0:0:0:0:1 BBB", "Identity", 1);
            bsmAdminPageEditWindow.addIpServiceEdge("NodeA /127.0.0.1 CCC", "Ignore", 1);
            bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.1", "Decrease", 1);
            bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.2", "Increase", 1);

            // verify edit dialog
            wait.until(pageContainsText(String.format("Child: %s, Map: Identity, Weight: 1", child1)));
            wait.until(pageContainsText(String.format("Child: %s, Map: Ignore, Weight: 1", child2)));
            wait.until(pageContainsText("IPSvc: NodeA /0:0:0:0:0:0:0:1 BBB, Map: Identity, Weight: 1"));
            wait.until(pageContainsText("IPSvc: NodeA /127.0.0.1 CCC, Map: Ignore, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.1, Map: Decrease, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.2, Map: Increase, Weight: 1"));

            // verify after save
            bsmAdminPageEditWindow.save();
            bsmAdminPage.openEditDialog(parentServiceName);
            wait.until(pageContainsText(String.format("Child: %s, Map: Identity, Weight: 1", child1)));
            wait.until(pageContainsText(String.format("Child: %s, Map: Ignore, Weight: 1", child2)));
            wait.until(pageContainsText("IPSvc: NodeA /0:0:0:0:0:0:0:1 BBB, Map: Identity, Weight: 1"));
            wait.until(pageContainsText("IPSvc: NodeA /127.0.0.1 CCC, Map: Ignore, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.1, Map: Decrease, Weight: 1"));
            wait.until(pageContainsText("ReKey: test.rk.2, Map: Increase, Weight: 1"));
            bsmAdminPageEditWindow.cancel();

            // cleanup
            bsmAdminPage.delete(child2, true);
            bsmAdminPage.delete(child1, true);
            bsmAdminPage.delete(parentServiceName);
        } finally {
            removeTestSetup();
        }
    }

    @Test
    public void testCanRemovePersistedEdge() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1);
        wait.until(pageContainsText("ReKey: some.reduction.key, Map: Increase, Weight: 1"));
        bsmAdminPageEditWindow.save();

        // remove persisted edge
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        bsmAdminPageEditWindow.removeEdge("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        verifyElementNotPresent("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        bsmAdminPageEditWindow.save();

        // clean up afterwards
        bsmAdminPage.delete(serviceName);
    }

    @Test
    public void testCanRemoveTransientEdge() throws InterruptedException {
        // create Business Service with one Edge and persist
        final String serviceName = createUniqueBusinessServiceName();
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openNewDialog(serviceName).addReductionKeyEdge("some.reduction.key", "Increase", 1);
        wait.until(pageContainsText("ReKey: some.reduction.key, Map: Increase, Weight: 1"));
        // remove transient edge
        bsmAdminPageEditWindow.removeEdge("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        verifyElementNotPresent("ReKey: some.reduction.key, Map: Increase, Weight: 1");
        bsmAdminPageEditWindow.cancel();

        verifyElementNotPresent(serviceName);
    }

    @Test
    @Ignore
    public void testCanCancelEdit() throws InterruptedException {
        // create service to edit
        final String serviceName = createUniqueBusinessServiceName();
        bsmAdminPage.openNewDialog(serviceName).save();

        // edit
        BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.1", "Identity", 1);
        bsmAdminPageEditWindow.addReductionKeyEdge("test.rk.2", "Identity", 1);

        // cancel
        bsmAdminPageEditWindow.cancel();

        // verify that cancel worked
        bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(serviceName);
        verifyElementNotPresent("ReKey: test.rk.1, Map: Identity, Weight: 1");
        verifyElementNotPresent("ReKey: test.rk.2, Map: Identity, Weight: 1");
        bsmAdminPageEditWindow.cancel();

        // clean up
        bsmAdminPage.delete(serviceName);
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

    private void verifyElementNotPresent(String text) {
        final String escapedText = text.replace("\'", "\\\'");
        final String xpathExpression = "//*[contains(., \'" + escapedText + "\')]";
        verifyElementNotPresent(By.xpath(xpathExpression));
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
