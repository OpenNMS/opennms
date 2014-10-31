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

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServicePageTest extends OpenNMSSeleniumTestCase {

    interface Setter {
        public void setField(String prefix);
    }

    Setter type(final String suffix, final String value) {
        return new Setter() {

            public void setField(String prefix) {
                selenium.type("name=" + prefix + "." + suffix,  value);
            }

        };
    }

    Setter select(final String suffix, final String value) {
        return new Setter() {

            public void setField(String prefix) {
                selenium.select("name=" + prefix + "." + suffix, "label=" + value);
            }

        };
    }

    String setTreeFieldsAndSave(String formName, Setter... setters) throws InterruptedException {
        String currentNode = selenium.getAttribute("//input[@name='currentNode']@value");
        assertTrue(currentNode.startsWith(formName+"."));
        String prefix = currentNode.replace(formName+".", "");

        for(Setter setter : setters) {
            setter.setField(prefix);
        }

        selenium.click("//input[contains(@onclick, '" + currentNode + "') and @value='Save']");
        return currentNode;
    }

    @After
    public void tearDown() throws Exception {
        // if selenium is not initialized, do not clean up
        if (selenium != null) {
            goToMainPage();

            clickAndWait("link=Admin");
            clickAndWait("link=Manage Provisioning Requisitions");

            if (selenium.isElementPresent("//input[@value='Delete Nodes']")) {
                clickAndWait("//input[@value='Delete Nodes']");
                clickAndWait("//input[@value='Synchronize']");
            }

            int loop = 0;
            while (loop < 20) {
                clickAndWait("link=Provisioning Requisitions");
                Thread.sleep(1000);
                if (selenium.isTextPresent("0 nodes defined, 0 nodes in database")) {
                    break;
                }
                loop++;
            }

            clickAndWait("//input[@value='Delete Requisition']");

            deleteTestRequisition();
            deleteTestUser();
            deleteTestGroup();

        }

        super.tearDown();
    }

    @Test
    public void testRequisitionUI() throws Exception {
        goToMainPage();

        clickAndWait("link=Admin");

        clickAndWait("link=Manage Provisioning Requisitions");
        waitForPageToLoad();

        selenium.type("css=form[name=takeAction] > div > input[name=groupName]", REQUISITION_NAME);
        clickAndWait("css=input[type=submit]");
        clickAndWait("//button[contains(@onclick, 'editForeignSource(\""+ REQUISITION_NAME+"\")')]");
        clickAndWait("//input[@value='Add Detector']");

        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8080"), select("pluginClass", "HTTP"));
        waitForPageToLoad();

        clickAndWait("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']");

        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8080"));
        waitForPageToLoad();

        clickAndWait("//input[@value='Done']");
        String rcOfEditAnchor = "id=edit_req_anchor_" + REQUISITION_NAME;
        clickAndWait(rcOfEditAnchor);
        clickAndWait("//input[@value='Add Node']");
        String nodeForNode = setTreeFieldsAndSave("nodeEditForm", type("nodeLabel", "localNode"));
        waitForPageToLoad();

        clickAndWait("//a[contains(@href, '" + nodeForNode + "') and text() = '[Add Interface]']");
        setTreeFieldsAndSave("nodeEditForm", type("ipAddr", "::1"));
        waitForPageToLoad();

        clickAndWait("//a[text() = 'Add Service']");
        setTreeFieldsAndSave("nodeEditForm", type("serviceName", "HTTP-8080"));
        waitForPageToLoad();

        clickAndWait("//input[@value='Done']");
        clickAndWait("//input[@value='Synchronize']");
        waitForPageToLoad();

        goToMainPage();

        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Users");
        clickAndWait("link=Add new user");
        selenium.type("id=userID", USER_NAME);
        selenium.type("id=pass1", "SmokeTestPassword");
        selenium.type("id=pass2", "SmokeTestPassword");
        clickAndWait("id=doOK");
        waitForElement("id=saveUserButton");
        clickAndWait("id=saveUserButton");
        waitForElement("id=users(" + USER_NAME + ").doDetails");

        goToMainPage();

        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Groups");
        clickAndWait("link=Add new group");
        selenium.type("id=groupName", GROUP_NAME);
        selenium.type("id=groupComment", "Test");
        clickAndWait("id=doOK");
        waitForElement("name=finish");
        clickAndWait("name=finish");
        clickAndWait("//div[@id='content']/form/table/tbody/tr[4]/td[2]/a/i");
        selenium.addSelection("name=availableUsers", "label=" + USER_NAME);
        selenium.click("xpath=/html/body/div[2]/form/table[2]/tbody/tr[2]/td/table/tbody/tr[2]/td/p/input[2]");
        waitForElement("name=finish");
        clickAndWait("name=finish");
        clickAndWait("link=" + GROUP_NAME);
        waitForText(USER_NAME);

        goToMainPage();

        clickAndWait("link=Node List");

        int loop = 0;
        while (loop < 20) {
            selenium.refresh();
            Thread.sleep(1000);
            if (selenium.isTextPresent("localNode")) {
                break;
            }
            loop++;
        }

        if(selenium.isElementPresent("link=localNode")) {
            // if there's more than 1 node discovered, it will give a list
            clickAndWait("link=localNode");
        }
        // otherwise it will go straight to the only node's page

        if(selenium.isElementPresent("link=ICMP")){
            clickAndWait("link=ICMP");
            waitForText("regexp:(Managed|Not Monitored)");
            waitForText("regexp:0+\\:0+\\:0+\\:0+\\:0+\\:0+\\:0+\\:0*1");
            waitForText("localNode");
        } else {
            fail("Neither of the links were found. Printing page source: " + selenium.getHtmlSource());
        }
    }

}
