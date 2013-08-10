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
    public void testProvisioningGroupSetup() throws Exception {

        String groupName = "SeleniumTestGroup";

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
        selenium.click("link=Log out");
        waitForPageToLoad();

        // Yo dawg, I heard you liked hacks
        Thread.sleep(10000);
    }

    @Test
    public void testCreateUser() throws InterruptedException { 
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Users");
        clickAndWait("link=Add New User");
        selenium.type("id=userID", "SmokeTestUser");
        selenium.type("id=pass1", "SmokeTestPassword");
        selenium.type("id=pass2", "SmokeTestPassword");
        clickAndWait("id=doOK");
        clickAndWait("id=saveUserButton");
        waitForElement("id=users(SmokeTestUser).doDetails");
    }

    @Test  
    public void testCreateGroup() throws InterruptedException {
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Groups");
        clickAndWait("link=Add new group");
        selenium.type("id=groupName", "SmokeTestGroup");
        selenium.type("id=groupComment", "Test");
        clickAndWait("id=doOK");
        clickAndWait("name=finish");
        clickAndWait("//div[@id='content']/form/table/tbody/tr[4]/td[2]/a/img");
        selenium.addSelection("name=availableUsers", "label=SmokeTestUser");
        selenium.click("xpath=/html/body/div[2]/form/table[2]/tbody/tr[2]/td/table/tbody/tr[2]/td/p/input[2]");
        clickAndWait("name=finish");
        clickAndWait("link=SmokeTestGroup");
        waitForText("SmokeTestUser");
    }

    @Test
    public void testProvisioningGroupWasCreated() throws InterruptedException {
        clickAndWait("link=Node List");
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

    @Test
    public void testDeleteProvisioningNodesAndGroups() throws Exception {
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
    public void testDeleteUsersAndGroups() {
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Groups");
        selenium.click("//div[@id='content']/form/table/tbody/tr[4]/td/a/img");
        clickAndWait("link=Users and Groups");
        clickAndWait("link=Configure Users");
        selenium.click("xpath=//html/body/div[2]/form/table/tbody/tr[2]/td/a/img");  
    }

}
