package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.Selenium;

public class AdminPageTest extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Admin");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("OpenNMS System"));
        assertTrue(selenium.isTextPresent("Operations"));
        assertTrue(selenium.isTextPresent("Nodes"));
        assertTrue(selenium.isTextPresent("Distributed Monitoring"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        assertTrue(selenium.isTextPresent("Scheduled Outages: Add"));
        assertTrue(selenium.isTextPresent("Notification Status:"));
    }

    @Test
    public void testAllLinksArePresent() throws Exception  {

        assertTrue(selenium.isElementPresent("link=Configure Users, Groups and On-Call Roles"));
        assertTrue(selenium.isElementPresent("link=System Information"));
        assertTrue(selenium.isElementPresent("link=Instrumentation Log Reader"));
        assertTrue(selenium.isElementPresent("link=Configure Discovery"));
        assertTrue(selenium.isElementPresent("link=Configure SNMP Community Names by IP"));
        assertTrue(selenium.isElementPresent("link=Configure SNMP Data Collection per Interface"));
        assertTrue(selenium.isElementPresent("link=Manage and Unmanage Interfaces and Services"));
        assertTrue(selenium.isElementPresent("link=Manage Thresholds"));
        assertTrue(selenium.isElementPresent("link=Configure Notifications"));
        assertTrue(selenium.isElementPresent("link=Scheduled Outages"));
        assertTrue(selenium.isElementPresent("link=Add Interface for Scanning"));
        assertTrue(selenium.isElementPresent("link=Manage Provisioning Requisitions"));
        assertTrue(selenium.isElementPresent("link=Import and Export Asset Information"));
        assertTrue(selenium.isElementPresent("link=Manage Surveillance Categories"));
        assertTrue(selenium.isElementPresent("link=Delete Nodes"));
        assertTrue(selenium.isElementPresent("link=Manage Applications"));
        assertTrue(selenium.isElementPresent("link=Manage Location Monitors"));
        assertTrue(selenium.isElementPresent("link=the OpenNMS wiki"));
    }

    @Test
    public void testLinkGroupOne() throws Exception {
        selenium.click("link=Configure Users, Groups and On-Call Roles");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Users and Groups"));
        assertTrue(selenium.isTextPresent("Users"));
        assertTrue(selenium.isTextPresent("Groups"));
        assertTrue(selenium.isTextPresent("Roles"));
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=System Information");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("OpenNMS Configuration"));
        assertTrue(selenium.isTextPresent("System Configuration"));
        assertTrue(selenium.isTextPresent("Reports directory:"));
        selenium.click("link=Admin");
        waitForPageToLoad();
    }

    @Test
    public void testLinkGroupTwo() throws Exception {
        selenium.click("link=Configure Discovery");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("General settings"));
        assertTrue(selenium.isTextPresent("Specifics"));
        assertTrue(selenium.isTextPresent("Include URLs"));
        assertTrue(selenium.isTextPresent("Include Ranges"));
        assertTrue(selenium.isTextPresent("Exclude Ranges"));
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Configure SNMP Community Names by IP");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Please enter an IP or a range of IPs and the read community string below"));
        assertTrue(selenium.isTextPresent("Updating SNMP Community Names"));
        assertTrue(selenium.isTextPresent("optimize this list"));
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Configure SNMP Data Collection per Interface");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Manage SNMP Data Collection per Interface"));
        assertTrue(selenium.isTextPresent("datacollection-config.xml file"));
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Manage and Unmanage Interfaces and Services");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Manage and Unmanage Interfaces and Services"));
        assertTrue(selenium.isTextPresent("unchecked meaning"));
        assertTrue(selenium.isTextPresent("mark each service"));
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Manage Thresholds");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Threshold Configuration"));
        assertTrue(selenium.isTextPresent("Name"));
        assertTrue(selenium.isTextPresent("RRD Repository"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Configure Notifications");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Configure Notifications"));
        assertTrue(selenium.isTextPresent("Event Notifications"));
        assertTrue(selenium.isTextPresent("Destination Paths"));
        assertTrue(selenium.isTextPresent("Path Outages"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Scheduled Outages");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Affects..."));
        assertTrue(selenium.isTextPresent("Notifications"));
        assertTrue(selenium.isTextPresent("Data collection"));
        selenium.click("link=Admin");
        waitForPageToLoad();
    }

    @Test
    public void testLinkGroupThree() throws Exception {
        selenium.click("link=Add Interface for Scanning");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Enter IP address"));
        assertTrue(selenium.isTextPresent("Add Interface"));
        assertTrue(selenium.isTextPresent("valid IP address"));
        selenium.click("link=Admin");
        waitForPageToLoad();
        selenium.click("link=Manage Provisioning Requisitions");
        waitForPageToLoad();
        assertEquals("Add New Requisition", selenium.getValue("css=input[type=submit]"));
        assertEquals("Edit Default Foreign Source Definition", selenium.getValue("css=input[type=button]"));
        assertEquals("Reset Default Foreign Source Definition", selenium.getValue("//input[@value='Reset Default Foreign Source Definition']"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Import and Export Asset Information");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Import and Export Assets"));
        assertTrue(selenium.isTextPresent("Importing Asset Information"));
        assertTrue(selenium.isTextPresent("Exporting Asset Information"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Manage Surveillance Categories");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Surveillance Categories"));
        assertTrue(selenium.isTextPresent("Category"));
        assertEquals("Add New Category", selenium.getValue("css=input[type=submit]"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Delete Nodes");
        waitForPageToLoad();
        long endTime = System.currentTimeMillis() + 30000;
        while(System.currentTimeMillis() < endTime){
            if("Delete Nodes | Admin | OpenNMS Web Console".equals(selenium.getTitle())){
                break;
            }
            if(endTime - System.currentTimeMillis() < 5000){
                fail ("25 second timeout trying to reach \"Admin/Delete Nodes\" Page");
            }
        }
        assertTrue(selenium.isTextPresent("Delete Nodes"));
        assertEquals("Delete Nodes | Admin | OpenNMS Web Console", selenium.getTitle());
        selenium.click("link=Admin");
        waitForPageToLoad();
    }

    @Test
    public void testLinkGroupFour() throws Exception {
        selenium.click("link=Manage Applications");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Applications"));
        assertTrue(selenium.isTextPresent("Edit"));
        assertEquals("Add New Application", selenium.getValue("css=input[type=submit]"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Manage Location Monitors");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Distributed Poller Status"));
        assertTrue(selenium.isTextPresent("Hostname"));
        assertEquals("Resume All", selenium.getValue("//input[@value='Resume All']"));
        selenium.click("link=Admin");
        waitForPageToLoad();
    }

    @Test
    public void testLinkGroupFive() throws Exception {
        assertTrue(selenium.isElementPresent("//a[@href='http://www.opennms.org']"));
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

    public void setSelenium(Selenium s) {
        selenium = s;
    }

    public Selenium getSelenium() {
        return selenium;
    }
}