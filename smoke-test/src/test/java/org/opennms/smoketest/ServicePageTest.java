package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;


public class ServicePageTest extends SeleneseTestBase{
    @Before
    public void setUp() throws Exception {
        WebDriver driver = new FirefoxDriver();
        String baseUrl = "http://localhost:8980/";
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        //selenium.start();
        selenium.open("/opennms/login.jsp");
        selenium.type("name=j_username", "admin");
        selenium.type("name=j_password", "admin");
        selenium.click("name=Login");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testPrvoisioningGroupSetup() throws Exception {
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Provisioning Groups");
        selenium.waitForPageToLoad("30000");
        selenium.type("css=form[name=takeAction] > input[name=groupName]", "SeleniumTestGroup");
        selenium.click("css=input[type=submit]");
        selenium.waitForPageToLoad("30000");
        selenium.click("//div[@id='content']/table/tbody/tr[2]/td[2]/a");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=h4 > input[type=button]");
        selenium.waitForPageToLoad("30000");
        selenium.type("id=formData.detectors13.name", "HTTP-8980");
        selenium.select("id=formData.detectors13.pluginClass", "label=HTTP");
        selenium.click("//input[@value='Save']");
        selenium.waitForPageToLoad("30000");
        selenium.click("//form[@id='foreignSourceEditForm']/ul/li[14]/a[3]");
        selenium.waitForPageToLoad("30000");
        selenium.select("id=formData.detectors13.parameters0.key", "label=port");
        selenium.type("id=formData.detectors13.parameters0.value", "8980");
        selenium.click("css=li > input[type=button]");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=input[type=button]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Edit");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Add Node']");
        selenium.waitForPageToLoad("30000");
        selenium.type("id=formData.node0.nodeLabel", "localNode");
        selenium.click("css=li > input[type=button]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=[Add Interface]");
        selenium.waitForPageToLoad("30000");
        selenium.type("id=formData.node0.interface0.ipAddr", "::1");
        selenium.click("css=li > input[type=button]");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=input[type=button]");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Synchronize']");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testProvisioningGroupWasCreated() throws InterruptedException {
        selenium.click("link=Node List");
        selenium.waitForPageToLoad("30000");
        while(!selenium.isElementPresent("link=localNode")){
            selenium.refresh();
        }
        selenium.click("link=localNode");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=HTTP-8980");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Managed"));
        assertTrue(selenium.isTextPresent("0000:0000:0000:0000:0000:0000:0000:0001"));
        assertTrue(selenium.isTextPresent("localNode"));
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
    public void testAllTopLevelLinks () throws InterruptedException {
        selenium.click("link=Node List");
        selenium.waitForPageToLoad("30000");
        long endTime = System.currentTimeMillis() + 60000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isElementPresent("link=localNode")){
                break;
            }else
                selenium.refresh();
                selenium.waitForPageToLoad("30000");
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
    public void testDeleteProvisioningNodesAndGroups() {
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Provisioning Groups");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Delete Nodes']");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Synchronize']");
        selenium.waitForPageToLoad("30000");
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

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
