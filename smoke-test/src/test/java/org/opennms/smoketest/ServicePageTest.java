/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;


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

    @Test
    public void testProvisioning() throws Exception {
        createProvisioningGroup();
        checkProvisioningGroupWasCreated();
        deleteProvisioningNodesAndGroups();
    }

    public void createProvisioningGroup() throws Exception {
        String groupName = "SeleniumTestGroup";

        frontPage();
        clickAndWait("link=Admin");

        clickAndWait("link=Manage Provisioning Requisitions");
        waitForPageToLoad();

        selenium.type("css=form[name=takeAction] > input[name=groupName]", groupName);
        clickAndWait("css=input[type=submit]");
        clickAndWait("//a[contains(@href, 'editForeignSource(\""+ groupName+"\")')]");
        clickAndWait("//input[@value='Add Detector']");

        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8080"), select("pluginClass", "HTTP"));
        waitForPageToLoad();

        clickAndWait("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']");

        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8080"));
        waitForPageToLoad();

        clickAndWait("//input[@value='Done']");
        clickAndWait("//a[contains(@href, '" + groupName + "') and contains(@href, 'editRequisition') and text() = 'Edit']");
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
    }
    
    public void checkProvisioningGroupWasCreated() throws Exception {
        frontPage();
        clickAndWait("link=Node List");

        // wait 60-ish seconds to make sure the new node is created
        int count = 60;
        while(selenium.isTextPresent("None found.") && count > 0) {
            Thread.sleep(1000);
            count--;
            selenium.refresh();
            waitForPageToLoad();
        }

        assertFalse(selenium.isTextPresent("None found."));

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

    public void deleteProvisioningNodesAndGroups() throws Exception {
        frontPage();
        clickAndWait("link=Admin");
        clickAndWait("link=Manage Provisioning Requisitions");
        clickAndWait("//input[@value='Delete Nodes']");
        clickAndWait("//input[@value='Synchronize']");

        /*
         *  we need to reload this page several times if the 'Delete Group' button doesn't exist
         *  in case the nodes hadn't been deleted by the time the page was reloaded
         */

        long end = System.currentTimeMillis() + 300000;
        while (!selenium.isElementPresent("//input[@value='Delete Requisition']") && System.currentTimeMillis() < end) {

            Thread.sleep(10000);

            if (System.currentTimeMillis() >= end) {
                throw new NoSuchElementException("Could not find the 'Delete Requisition' button after refreshing for 5 minutes");
            } else {
                selenium.refresh();
                waitForPageToLoad();
            }
        }        

        clickAndWait("//input[@value='Delete Requisition']");
    }

    @Test
    public void testUsersAndGroups() throws Exception {
        createUser();
        createGroup();
        deleteUsersAndGroups();
    }

    public void createUser() throws Exception { 
        frontPage();
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Users");
        clickAndWait("link=Add new user");
        selenium.type("id=userID", "SmokeTestUser");
        selenium.type("id=pass1", "SmokeTestPassword");
        selenium.type("id=pass2", "SmokeTestPassword");
        clickAndWait("id=doOK");
        waitForElement("id=saveUserButton");
        clickAndWait("id=saveUserButton");
        waitForElement("id=users(SmokeTestUser).doDetails");
    }

    public void createGroup() throws Exception {
        frontPage();
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Groups");
        clickAndWait("link=Add new group");
        waitForElement("id=groupName");
        selenium.type("id=groupName", "SmokeTestGroup");
        selenium.type("id=groupComment", "Test");
        Thread.sleep(1000);
        clickAndWait("id=doOK");
        System.err.println(selenium.getHtmlSource());
        waitForElement("name=finish");
        clickAndWait("name=finish");
        clickAndWait("//div[@id='content']/form/table/tbody/tr[4]/td[2]/a/i");
        selenium.addSelection("name=availableUsers", "label=SmokeTestUser");
        selenium.click("xpath=/html/body/div[2]/form/table[2]/tbody/tr[2]/td/table/tbody/tr[2]/td/p/input[2]");
        waitForElement("name=finish");
        clickAndWait("name=finish");
        clickAndWait("link=SmokeTestGroup");
        waitForText("SmokeTestUser");
    }

    public void deleteUsersAndGroups() throws Exception {
        frontPage();
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Groups");
        waitForText("group-SmokeTestGroup");
        selenium.chooseOkOnNextConfirmation();
        selenium.click("id=delete-SmokeTestGroup");
        selenium.getConfirmation();
        waitForText("group-Admin");
        assertFalse(selenium.isTextPresent("SmokeTestGroup"));

        frontPage();
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Users");
        waitForText("user-SmokeTestUser");
        selenium.chooseOkOnNextConfirmation();
        selenium.click("id=users(SmokeTestUser).doDelete");
        selenium.getConfirmation();
        waitForText("user-admin");
        assertFalse(selenium.isTextPresent("SmokeTestUser"));
    }

}
