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
        waitForPageToLoad();
        return currentNode;
    }

    @Test
    public void a_testProvisioning() throws Exception {
        String groupName = "SeleniumTestGroup";
        
        frontPage();
        clickAndWait("link=Admin");
        clickAndWait("link=Manage Provisioning Requisitions");
        
        selenium.type("css=form[name=takeAction] > input[name=groupName]", groupName);
        clickAndWait("css=input[type=submit]");
        clickAndWait("//a[contains(@href, 'editForeignSource(\""+ groupName+"\")')]");
        clickAndWait("//input[@value='Add Detector']");
        
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8080"), select("pluginClass", "HTTP"));
        clickAndWait("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']");
        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8080"));

        clickAndWait("//input[@value='Done']");

        clickAndWait("//a[contains(@href, '" + groupName + "') and contains(@href, 'editRequisition') and text() = 'Edit']");
        clickAndWait("//input[@value='Add Node']");
        String nodeForNode = setTreeFieldsAndSave("nodeEditForm", type("nodeLabel", "localNode"));
        
        clickAndWait("//a[contains(@href, '" + nodeForNode + "') and text() = '[Add Interface]']");
        setTreeFieldsAndSave("nodeEditForm", type("ipAddr", "::1"));
        
        clickAndWait("//a[text() = 'Add Service']");
        setTreeFieldsAndSave("nodeEditForm", type("serviceName", "HTTP-8080"));
        
        clickAndWait("//input[@value='Done']");
        clickAndWait("//input[@value='Synchronize']");

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
            waitForText("ICMP service on 0:0:0:0:0:0:0:1");
            waitForText("localNode");
        } else {
            fail("Neither of the links were found. Printing page source: " + selenium.getHtmlSource());
        }

        clickAndWait("link=Admin");
        clickAndWait("link=Manage Provisioning Requisitions");
        selenium.chooseOkOnNextConfirmation();
        selenium.click("//input[@value='Delete Nodes']");
        selenium.getConfirmation();
        waitForElement("//input[@value='Synchronize']");
        clickAndWait("//input[@value='Synchronize']");
        waitForElementRefresh("//input[@value='Delete Requisition']");
        clickAndWait("//input[@value='Delete Requisition']");
    }

    @Test
    public void b_testUsersAndGroups() throws Exception {
        // go to the add user page
        clickAndWait("link=Admin");
        clickAndWait("link=Configure Users, Groups and On-Call Roles");
        clickAndWait("link=Configure Users");
        clickAndWait("link=Add new user");

        // enter user information and hit OK
        selenium.type("id=userID", "SmokeTestUser");
        selenium.type("id=pass1", "SmokeTestPassword");
        selenium.type("id=pass2", "SmokeTestPassword");
        clickAndWait("//input[@type='submit']");

        // when the page has refreshed, go to the add group page
        waitForElement("id=saveUserButton");
        clickAndWait("id=saveUserButton");
        waitForElement("id=users(SmokeTestUser).doDetails");
        clickAndWait("link=Users and Groups");
        clickAndWait("link=Configure Groups");
        clickAndWait("link=Add new group");

        // enter group information and hit OK
        waitForElement("id=groupName");
        selenium.type("id=groupName", "SmokeTestGroup");
        selenium.type("id=groupComment", "Test");
        clickAndWait("//input[@type='submit']");

        // add the SmokeTestUser to the group and finish
        waitForElement("id=users.doAdd");
        selenium.addSelection("name=availableUsers", "label=SmokeTestUser");
        selenium.click("id=users.doAdd");
        waitForElement("name=finish");
        clickAndWait("name=finish");

        // verify the user was added to the group
        clickAndWait("link=SmokeTestGroup");
        waitForText("SmokeTestUser");

        // delete the group
        clickAndWait("link=Users and Groups");
        clickAndWait("link=Configure Groups");
        waitForHtmlSource("group-SmokeTestGroup");
        selenium.chooseOkOnNextConfirmation();
        selenium.click("id=SmokeTestGroup.doDelete");
        selenium.getConfirmation();
        waitForHtmlSource("group-Admin");
        assertFalse(selenium.isTextPresent("SmokeTestGroup"));

        // delete the user
        clickAndWait("link=Users and Groups");
        clickAndWait("link=Configure Users");
        waitForHtmlSource("user-SmokeTestUser");
        selenium.chooseOkOnNextConfirmation();
        selenium.click("id=users(SmokeTestUser).doDelete");
        selenium.getConfirmation();
        waitForHtmlSource("user-admin");
        assertFalse(selenium.isTextPresent("SmokeTestUser"));
    }

}
