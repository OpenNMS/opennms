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

        selenium.click("link=Admin");

        waitForPageToLoad();

        selenium.click("link=Manage Provisioning Requisitions");
        waitForPageToLoad();

        selenium.type("css=form[name=takeAction] > input[name=groupName]", groupName);
        selenium.click("css=input[type=submit]");
        waitForPageToLoad();

        selenium.click("//a[contains(@href, 'editForeignSource(\""+ groupName+"\")')]");
        waitForPageToLoad();
        
        selenium.click("//input[@value='Add Detector']");
        waitForPageToLoad();
        
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8080"), select("pluginClass", "HTTP"));
        waitForPageToLoad();
        
        selenium.click("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']");
        waitForPageToLoad();
        
        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8080"));
        
        waitForPageToLoad();
        
        selenium.click("//input[@value='Done']");
        waitForPageToLoad();

        selenium.click("//a[contains(@href, '" + groupName + "') and contains(@href, 'editRequisition') and text() = 'Edit']");
        waitForPageToLoad();

        selenium.click("//input[@value='Add Node']");
        waitForPageToLoad();
        
        String nodeForNode = setTreeFieldsAndSave("nodeEditForm", type("nodeLabel", "localNode"));
        waitForPageToLoad();
        
        selenium.click("//a[contains(@href, '" + nodeForNode + "') and text() = '[Add Interface]']");
        waitForPageToLoad();

        setTreeFieldsAndSave("nodeEditForm", type("ipAddr", "::1"));
        waitForPageToLoad();

        selenium.click("//a[text() = 'Add Service']");
        waitForPageToLoad();
        
        setTreeFieldsAndSave("nodeEditForm", type("serviceName", "HTTP-8080"));
        waitForPageToLoad();

        selenium.click("//input[@value='Done']");
        waitForPageToLoad();

        selenium.click("//input[@value='Synchronize']");
        waitForPageToLoad();

        selenium.click("link=Log out");
        waitForPageToLoad();
        
        // Yo dawg, I heard you liked hacks
        Thread.sleep(10000);
    }

    @Test
    public void testCreateUser() { 
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Configure Users, Groups and On-Call Roles");
        waitForPageToLoad();
        selenium.click("link=Configure Users");
        waitForPageToLoad();
        selenium.click("link=Add New User");
        waitForPageToLoad();
        selenium.type("id=userID", "SmokeTestUser");
        selenium.type("id=pass1", "SmokeTestPassword");
        selenium.type("id=pass2", "SmokeTestPassword");
        selenium.click("id=doOK");
        waitForPageToLoad();
        selenium.click("id=saveUserButton");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("id=users(SmokeTestUser).doDetails"));
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

    @Test  
    public void testCreateGroup() {
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Configure Users, Groups and On-Call Roles");
        waitForPageToLoad();
        selenium.click("link=Configure Groups");
        waitForPageToLoad();
        selenium.click("link=Add new group");
        waitForPageToLoad();
        selenium.type("id=groupName", "SmokeTestGroup");
        selenium.type("id=groupComment", "Test");
        selenium.click("id=doOK");
        waitForPageToLoad();
        selenium.click("name=finish");
        waitForPageToLoad();
        selenium.click("//div[@id='content']/form/table/tbody/tr[4]/td[2]/a/img");
        waitForPageToLoad();
        selenium.addSelection("name=availableUsers", "label=SmokeTestUser");
        selenium.click("xpath=/html/body/div[2]/form/table[2]/tbody/tr[2]/td/table/tbody/tr[2]/td/p/input[2]");
        selenium.click("name=finish");
        waitForPageToLoad();
        selenium.click("link=SmokeTestGroup");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("SmokeTestUser"));
    }

    @Test
    public void testProvisioningGroupWasCreated() throws InterruptedException {
        selenium.click("link=Node List");
        waitForPageToLoad();
        if(selenium.isElementPresent("link=localNode")) {
            // if there's more than 1 node discovered, it will give a list
            selenium.click("link=localNode");
            waitForPageToLoad();
        }
        // otherwise it will go straight to the only node's page

        if(selenium.isElementPresent("link=ICMP")){
            selenium.click("link=ICMP");
            waitForPageToLoad();
            assertTrue("Managed text not found", selenium.isTextPresent("regexp:(Managed|Not Monitored)"));
            assertTrue("IP text not found", selenium.isTextPresent("regexp:0+\\:0+\\:0+\\:0+\\:0+\\:0+\\:0+\\:0*1"));
            assertTrue("localNode text not found", selenium.isTextPresent("localNode"));
        } else {
            fail("Neither of the links were found. Printing page source: " + selenium.getHtmlSource());
        }
    }

    @Test
    public void testAllTopLevelLinks() throws InterruptedException {
        selenium.click("link=Node List");
        waitForPageToLoad();
        long endTime = System.currentTimeMillis() + 60000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Availability") || selenium.isElementPresent("link=localNode")){
                break;
            }
            selenium.refresh();
            waitForPageToLoad();
            if(endTime - System.currentTimeMillis() < 5000){
                fail ("55 second timeout trying to reach \"Node List/localNode\" Page");
            }
        }
        selenium.click("link=Search");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Search for Nodes"));
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Search Options"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
        selenium.click("link=Outages");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Current outages"));
        assertTrue(selenium.isTextPresent("Outages and Service Level Availability"));
        assertTrue(selenium.isTextPresent("Outage Menu"));
        selenium.click("link=Path Outages");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("All path outages"));
        assertTrue(selenium.isTextPresent("Critical Path Service"));
        selenium.click("link=Dashboard");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Node Status"));
        assertTrue(selenium.isTextPresent("24 Hour Availability"));
        selenium.click("link=Alarms");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Alarm Queries"));
        assertTrue(selenium.isTextPresent("Outstanding and acknowledged alarms"));
        assertTrue(selenium.isTextPresent("Alarm ID:"));
        selenium.click("link=Events");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Event Queries"));
        assertTrue(selenium.isTextPresent("Outstanding and acknowledged events"));     
        selenium.click("link=Notifications");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Notification queries"));
        assertTrue(selenium.isTextPresent("Outstanding and Acknowledged Notices"));
        assertTrue(selenium.isTextPresent("Notification Escalation"));
        selenium.click("link=Assets");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Assets with asset numbers"));
        assertTrue(selenium.isTextPresent("Assets Inventory"));
        selenium.click("link=Reports");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Reports"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        selenium.click("link=Charts");
        waitForPageToLoad();
        selenium.click("link=Surveillance");
        endTime = System.currentTimeMillis() + 60000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Surveillance View:")){
                break;
            }
        }
        assertTrue(selenium.isTextPresent("Routers"));
        assertTrue(selenium.isTextPresent("Surveillance View: default"));
        selenium.click("//a[@href='maps.htm']");
        waitForPageToLoad();
        selenium.click("//div[@id='content']//a[contains(@href,'RemotePollerMap')]");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Applications"));
        selenium.goBack();
        waitForPageToLoad();
        selenium.click("link=Add Node");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Node Quick-Add"));
        assertTrue(selenium.isTextPresent("CLI Authentication Parameters"));
        selenium.click("link=Admin");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("OpenNMS System"));
        assertTrue(selenium.isTextPresent("Operations"));
        assertTrue(selenium.isTextPresent("Nodes"));
        assertTrue(selenium.isTextPresent("Distributed Monitoring"));
        assertTrue(selenium.isTextPresent("Descriptions"));
    }

    @Test
    public void testDeleteProvisioningNodesAndGroups() throws Exception {
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Manage Provisioning Requisitions");
        waitForPageToLoad();
        selenium.click("//input[@value='Delete Nodes']");
        waitForPageToLoad();
        selenium.click("//input[@value='Synchronize']");
        waitForPageToLoad();
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

        selenium.click("//input[@value='Delete Requisition']");
        waitForPageToLoad();
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

	@Test
    public void testDeleteUsersAndGroups() {
        waitForPageToLoad();
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Configure Users, Groups and On-Call Roles");
        waitForPageToLoad();
        selenium.click("link=Configure Groups");
        waitForPageToLoad();
        selenium.click("//div[@id='content']/form/table/tbody/tr[4]/td/a/img");
        selenium.click("link=Users and Groups");
        waitForPageToLoad();
        selenium.click("link=Configure Users");
        waitForPageToLoad();
        selenium.click("xpath=//html/body/div[2]/form/table/tbody/tr[2]/td/a/img");  
        selenium.click("link=Log out");
        waitForPageToLoad();
    }
    
}
