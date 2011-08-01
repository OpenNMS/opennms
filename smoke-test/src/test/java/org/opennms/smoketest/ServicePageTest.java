package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class ServicePageTest extends SeleneseTestCase {
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
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if ("localNode".equals(selenium.getText("link=localNode"))) break; } catch (Exception e) {}
            Thread.sleep(1000);
            selenium.refresh();
        }
        selenium.click("link=localNode");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=HTTP-8980");
        selenium.waitForPageToLoad("30000");
        verifyTrue(selenium.isTextPresent("Polling Status 	Managed"));
        verifyTrue(selenium.isTextPresent("Interface 	0000:0000:0000:0000:0000:0000:0000:0001"));
        verifyTrue(selenium.isTextPresent("Node 	localNode"));
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Manage Provisioning Groups");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Edit");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=li > a > img");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=input[type=button]");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@value='Synchronize']");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=span > input[type=button]");
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
        verifyTrue(selenium.isElementPresent("id=users(SmokeTestUser).doDetails"));
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
        verifyTrue(selenium.isTextPresent("SmokeTestUser"));
    }

    @Test
    public void testAllTopLevelLinks () {
        selenium.click("link=Node List");
        selenium.waitForPageToLoad("30000");
        verifyTrue(selenium.isTextPresent("Nodes"));
        verifyTrue(selenium.isElementPresent("link=Show interfaces"));
        selenium.click("link=Search");
        selenium.waitForPageToLoad("30000");
        verifyTrue(selenium.isTextPresent("Search for Nodes"));
        verifyTrue(selenium.isTextPresent("Search Asset Information"));
        verifyTrue(selenium.isTextPresent("Search Options"));
        verifyTrue(selenium.isElementPresent("link=All nodes with asset info"));
        selenium.click("link=Outages");
        selenium.waitForPageToLoad("30000");
        verifyTrue(selenium.isElementPresent("link=Current outages"));
        verifyTrue(selenium.isTextPresent("Outages and Service Level Availability"));
        verifyTrue(selenium.isTextPresent("All path outages"));
        verifyTrue(selenium.isTextPresent("Critical Path Service"));
        verifyTrue(selenium.isTextPresent("Alarms"));
        verifyTrue(selenium.isTextPresent("Resource Graphs"));
        verifyTrue(selenium.isTextPresent("Event Queries"));
        verifyTrue(selenium.isTextPresent("Outstanding and acknowledged events"));
        verifyTrue(selenium.isTextPresent("Alarm Queries"));
        verifyTrue(selenium.isTextPresent("Outstanding and acknowledged alarms"));
        verifyTrue(selenium.isTextPresent("Notification queries"));
        verifyTrue(selenium.isTextPresent("Outstanding and Acknowledged Notices"));
        verifyTrue(selenium.isTextPresent("Notification Escalation"));
        verifyTrue(selenium.isTextPresent("Search Asset Information"));
        verifyTrue(selenium.isTextPresent("Assets with asset numbers"));
        verifyTrue(selenium.isTextPresent("Assets Inventory"));
        verifyTrue(selenium.isTextPresent("Reports"));
        verifyTrue(selenium.isTextPresent("Descriptions"));
        verifyTrue(selenium.isElementPresent("css=img[alt=sample-bar-chart]"));
        verifyTrue(selenium.isElementPresent("css=img[alt=sample-bar-chart2]"));
        verifyTrue(selenium.isElementPresent("css=img[alt=sample-bar-chart3]"));
        verifyTrue(selenium.isTextPresent("Nodes Down"));
        verifyTrue(selenium.isTextPresent("TEST"));
        verifyTrue(selenium.isTextPresent("Surveillance View: default"));
        verifyTrue(selenium.isElementPresent("id=TabPanelGroup__0"));
        verifyTrue(selenium.isTextPresent("Network Topology Maps"));
        verifyTrue(selenium.isTextPresent("Node Quick-Add"));
        verifyTrue(selenium.isTextPresent("will override"));
        verifyTrue(selenium.isTextPresent("OpenNMS System"));
        verifyTrue(selenium.isTextPresent("Operations"));
        verifyTrue(selenium.isTextPresent("Nodes"));
        verifyTrue(selenium.isTextPresent("Distributed Monitoring"));
        verifyTrue(selenium.isTextPresent("Descriptions"));
        verifyTrue(selenium.isTextPresent("Commercial Support"));
        verifyTrue(selenium.isTextPresent("About"));
        verifyTrue(selenium.isTextPresent("Other Support Options"));
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
        selenium.click("css=img[alt=Delete SmokeTestUser]");
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
