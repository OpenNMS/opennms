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
    public void testPrvoisioningGroupSetup() throws Exception {
    	
        String groupName = "SeleniumTestGroup";

        selenium.click("link=Admin");

        selenium.waitForPageToLoad("30000");

        selenium.waitForPageToLoad("30000");

        selenium.click("link=Manage Provisioning Groups");
        selenium.waitForPageToLoad("30000");

        selenium.type("css=form[name=takeAction] > input[name=groupName]", groupName);
        selenium.click("css=input[type=submit]");
        selenium.waitForPageToLoad("30000");

        selenium.click("//a[contains(@href, 'editForeignSource(\""+ groupName+"\")')]");
        selenium.waitForPageToLoad("30000");
        
        selenium.click("//input[@value='Add Detector']");
        selenium.waitForPageToLoad("30000");
        
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", type("name", "HTTP-8980"), select("pluginClass", "HTTP"));
        selenium.waitForPageToLoad("30000");
        
        selenium.click("//a[contains(@href, '"+detectorNode+"') and text() = '[Add Parameter]']");
        selenium.waitForPageToLoad("30000");
        
        setTreeFieldsAndSave("foreignSourceEditForm", select("key", "port"), type("value", "8980"));
        
        selenium.waitForPageToLoad("30000");
        
        selenium.click("//input[@value='Done']");
        selenium.waitForPageToLoad("30000");

        selenium.click("//a[contains(@href, '" + groupName + "') and contains(@href, 'editRequisition') and text() = 'Edit']");
        selenium.waitForPageToLoad("30000");

        selenium.click("//input[@value='Add Node']");
        selenium.waitForPageToLoad("30000");
        
        String nodeForNode = setTreeFieldsAndSave("nodeEditForm", type("nodeLabel", "localNode"));
        selenium.waitForPageToLoad("30000");
        
        selenium.click("//a[contains(@href, '" + nodeForNode + "') and text() = '[Add Interface]']");
        selenium.waitForPageToLoad("30000");

        setTreeFieldsAndSave("nodeEditForm", type("ipAddr", "::1"));
        selenium.waitForPageToLoad("30000");

        selenium.click("//input[@value='Done']");
        selenium.waitForPageToLoad("30000");

        selenium.click("//input[@value='Synchronize']");
        selenium.waitForPageToLoad("30000");

        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testCreateUser() { 
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Configure Users, Groups and Roles");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Configure Users");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Add New User");
        selenium.waitForPageToLoad("30000");
        selenium.type("id=userID", "SmokeTestUser");
        selenium.type("id=pass1", "SmokeTestPassword");
        selenium.type("id=pass2", "SmokeTestPassword");
        selenium.click("id=doOK");
        selenium.waitForPageToLoad("30000");
        selenium.click("id=saveUserButton");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("id=users(SmokeTestUser).doDetails"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

    @Test  
    public void testCreateGroup() {
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Configure Users, Groups and Roles");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Configure Groups");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Add new group");
        selenium.waitForPageToLoad("30000");
        selenium.type("id=groupName", "SmokeTestGroup");
        selenium.type("id=groupComment", "Test");
        selenium.click("id=doOK");
        selenium.waitForPageToLoad("30000");
        selenium.click("name=finish");
        selenium.waitForPageToLoad("30000");
        selenium.click("//div[@id='content']/form/table/tbody/tr[4]/td[2]/a/img");
        selenium.waitForPageToLoad("30000");
        selenium.addSelection("name=availableUsers", "label=SmokeTestUser");
        selenium.click("xpath=/html/body/div[2]/form/table[2]/tbody/tr[2]/td/table/tbody/tr[2]/td/p/input[2]");
        selenium.click("name=finish");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=SmokeTestGroup");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("SmokeTestUser"));
    }

    @Test
    public void testProvisioningGroupWasCreated() throws InterruptedException {
        selenium.click("link=Node List");
        selenium.waitForPageToLoad("30000");
        if(selenium.isElementPresent("link=localNode")) {
            selenium.click("link=localNode");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=HTTP-8980");
            selenium.waitForPageToLoad("30000");
            assertTrue("Managed text not found",selenium.isTextPresent("Managed"));
            assertTrue("IP text not found",selenium.isTextPresent("0000:0000:0000:0000:0000:0000:0000:0001"));
            assertTrue("localNode text not found", selenium.isTextPresent("localNode"));
        }else if(selenium.isElementPresent("link=HTTP-8980")){
            selenium.click("link=HTTP-8980");
            selenium.waitForPageToLoad("30000");
            assertTrue("Managed text not found", selenium.isTextPresent("regexp:(Managed|Not Monitored)"));
            assertTrue("IP text not found", selenium.isTextPresent("regexp:0+\\:0+\\:0+\\:0+\\:0+\\:0+\\:0+\\:0*1"));
            assertTrue("localNode text not found", selenium.isTextPresent("localNode"));
        }else {
            fail("Neither of the links were found. Printing page source: " + selenium.getHtmlSource());
        }
    }

    @Test
    public void testAllTopLevelLinks () throws InterruptedException {
        selenium.click("link=Node List");
        selenium.waitForPageToLoad("30000");
        long endTime = System.currentTimeMillis() + 60000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Availability") || selenium.isElementPresent("link=localNode")){
                break;
            }
            selenium.refresh();
            selenium.waitForPageToLoad("30000");
            if(endTime - System.currentTimeMillis() < 5000){
                fail ("55 second timeout trying to reach \"Node List/localNode\" Page");
            }
        }
        selenium.click("link=Search");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Search for Nodes"));
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Search Options"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
        selenium.click("link=Outages");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Current outages"));
        assertTrue(selenium.isTextPresent("Outages and Service Level Availability"));
        assertTrue(selenium.isTextPresent("Outage Menu"));
        selenium.click("link=Path Outages");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("All path outages"));
        assertTrue(selenium.isTextPresent("Critical Path Service"));
        selenium.click("link=Dashboard");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Node Status"));
        assertTrue(selenium.isTextPresent("24 Hour Availability"));
        selenium.click("link=Alarms");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Alarm Queries"));
        assertTrue(selenium.isTextPresent("Outstanding and acknowledged alarms"));
        assertTrue(selenium.isTextPresent("Alarm ID:"));
        selenium.click("link=Events");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Event Queries"));
        assertTrue(selenium.isTextPresent("Outstanding and acknowledged events"));     
        selenium.click("link=Notifications");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Notification queries"));
        assertTrue(selenium.isTextPresent("Outstanding and Acknowledged Notices"));
        assertTrue(selenium.isTextPresent("Notification Escalation"));
        selenium.click("link=Assets");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Assets with asset numbers"));
        assertTrue(selenium.isTextPresent("Assets Inventory"));
        selenium.click("link=Reports");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Reports"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        selenium.click("link=Charts");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Surveillance");
        endTime = System.currentTimeMillis() + 60000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Surveillance View:")){
                break;
            }
        }
        assertTrue(selenium.isTextPresent("Routers"));
        assertTrue(selenium.isTextPresent("Surveillance View: default"));
        selenium.click("link=Distributed Map");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Applications"));
        selenium.goBack();
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Map");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Network Topology Maps"));
        selenium.click("link=Add Node");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Node Quick-Add"));
        assertTrue(selenium.isTextPresent("CLI Authentication Parameters"));
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("OpenNMS System"));
        assertTrue(selenium.isTextPresent("Operations"));
        assertTrue(selenium.isTextPresent("Nodes"));
        assertTrue(selenium.isTextPresent("Distributed Monitoring"));
        assertTrue(selenium.isTextPresent("Descriptions"));
    }

    @Test
    public void testDeleteProvisioningNodesAndGroups() throws Exception {
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Provisioning Groups");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Delete Nodes']");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Synchronize']");
        selenium.waitForPageToLoad("30000");
        /*
         *  we need to reload this page several times if the 'Delete Group' button doesn't exist
         *  in case the nodes hadn't been deleted by the time the page was reloaded
         */

        long end = System.currentTimeMillis() + 300000;
        while (!selenium.isElementPresent("//input[@value='Delete Group']") && System.currentTimeMillis() < end) {
        	
        	Thread.sleep(10000);
        	
        	if (System.currentTimeMillis() >= end) {
        		throw new NoSuchElementException("Could not find the 'Delete Group' button after refreshing for 5 minutes");
        	} else {
        		selenium.refresh();
        		selenium.waitForPageToLoad("30000");
        	}
        }        

        selenium.click("//input[@value='Delete Group']");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

	@Test
    public void testDeleteUsersAndGroups() {
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Configure Users, Groups and Roles");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Configure Groups");
        selenium.waitForPageToLoad("30000");
        selenium.click("//div[@id='content']/form/table/tbody/tr[4]/td/a/img");
        selenium.click("link=Users and Groups");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Configure Users");
        selenium.waitForPageToLoad("30000");
        selenium.click("xpath=//html/body/div[2]/form/table/tbody/tr[2]/td/a/img");  
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }
    
}
