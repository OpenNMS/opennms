/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KSCEditorIT extends OpenNMSSeleniumIT {

    protected void goToMainPage() {
        driver.get(getBaseUrlInternal() + "opennms/KSC/index.jsp");
    }

    @Before
    public void setUp() throws Exception {
        // Create the test requisition, this will block until the test node is actually created
        createRequisition();
    }

    @Test
    public void testKSCReports() throws Exception {
        goToMainPage();
        checkMainPage();

        goToMainPage();
        createReport();

        goToMainPage();
        editExistingReport();

        goToMainPage();
        deleteExistingReport();
    }

    protected void checkMainPage() throws Exception {
        // main KSC page
        assertEquals(3, countElementsMatchingCss("div.card-header"));
        findElementByXpath("//div[@class='card-header']/span[text()='Customized Reports']");
        findElementByXpath("//div[@class='card-header']/span[text()='Node & Domain Interface Reports']");
        findElementByXpath("//div[@class='card-header']/span[text()='Descriptions']");

        assertElementDoesNotExist(By.name("report:Smoke Test Report 1"));
        assertElementDoesNotExist(By.name("report:Smoke Test Report Uno"));
        assertEquals("TestMachine1", findElementByName("resource:TestMachine1").getText());
    }

    protected void createReport() throws Exception {
        // create a new report
        clickElementByXpath("//button[text()='Create New']");

        // set the title
        enterText(By.name("report_title"), "Smoke Test Report 1");

        // add the ICMP graph
        clickElementByXpath("//button[text()='Add New Graph']");

        // select the first subresource (TestMachine1)
        clickElementByName("subresource:Node:TestMachine1");
        clickElementByXpath("//button[text()='View Child Resource']");

        // select the first subresource (127.0.0.1)
        clickElementByName("subresource:Response Time:127.0.0.1");
        clickElementByXpath("//button[text()='View Child Resource']");

        // choose the resource
        clickElementByXpath("//button[text()='Choose this resource']");

        // name the graph
        enterText(By.name("title"), "Smoke Test Graph Title 1");

        // finish up
        clickElementByXpath("//button[text()='Done with edits to this graph']");
        clickElementByXpath("//button[text()='Save Report']");

        assertEquals("Smoke Test Report 1", findElementByName("report:Smoke Test Report 1").getText());        

        // view the report to confirm it's right
        waitForElement(By.name("report:Smoke Test Report 1"));
        Thread.sleep(100);
        clickElementByName("report:Smoke Test Report 1");
        clickElementByXpath("//button[text()='View']");
        findElementByXpath("//div[@class='card-header']/span[text()='Custom View: Smoke Test Report 1']");
        findElementByXpath("//div[contains(@class, 'graph-container')]");
        findElementByXpath("//div[contains(@class, 'graph-container')]//canvas");
    }

    protected void editExistingReport() throws Exception {
        // edit report 0 (should be the Smoke Test Report 1 from b_test*)
        clickElementByName("report:Smoke Test Report 1");
        clickElementByXpath("//button[text()='Customize']");

        // check that the defaults are set as expected
        assertFalse(findElementByName("show_timespan").isSelected());
        assertFalse(findElementByName("show_graphtype").isSelected());
        final Select gpl = new Select(findElementByName("graphs_per_line"));
        assertEquals("default", gpl.getFirstSelectedOption().getText());

        // change graphs per line to 3, check "show timespan" and "show graphtype", and change the title
        gpl.selectByVisibleText("3");
        clickElementByName("show_timespan");
        clickElementByName("show_graphtype");
        enterText(By.name("report_title"), "Smoke Test Report Uno");

        // now confirm that the checkboxes got checked
        assertTrue(findElementByName("show_timespan").isSelected());
        assertTrue(findElementByName("show_graphtype").isSelected());

        // modify the graph and give it a new title
        clickElementByXpath("//button[text()='Modify']");
        enterText(By.name("title"), "Smoke Test Graph Title I");

        // update the timespan
        final Select timespan = new Select(findElementByName("timespan"));
        timespan.selectByVisibleText("3 month");

        // then finish modifying the graph
        clickElementByXpath("//button[text()='Done with edits to this graph']");

        // then finish modifying the report
        clickElementByXpath("//button[text()='Save Report']");
    }

    protected void deleteExistingReport() throws Exception {
        // edit report 0 (should be the Smoke Test Report 1 from b_test*)
        clickElementByName("report:Smoke Test Report Uno");
        clickElementByXpath("//button[text()='Delete']");
        assertElementDoesNotExist(By.name("report:Smoke Test Report Uno"));
    }

    private void clickElementByName(final String name) {
        findElementByName(name).click();
    }

    private void clickElementByXpath(final String xpath) {
        findElementByXpath(xpath).click();
    }

    private void createRequisition() throws Exception {
        final String req = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"" + REQUISITION_NAME + "\">" +
                "<node node-label=\"TestMachine1\" foreign-id=\"TestMachine1\">" +
                "<interface ip-addr=\"127.0.0.1\" snmp-primary=\"P\" descr=\"localhost\">" +
                "<monitored-service service-name=\"ICMP\"/>" +
                "<monitored-service service-name=\"HTTP\"/>" +
                "</interface>" +
                "</node>" +
                "</model-import>";
        createRequisition(REQUISITION_NAME, req, 1);
    }
}
