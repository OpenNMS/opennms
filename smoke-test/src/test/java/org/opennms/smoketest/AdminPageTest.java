package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Selenium;

@SuppressWarnings("deprecation")
public class AdminPageTest extends SeleneseTestCase {
       
        //private Selenium selenium;

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
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
        }

        @Test
        public void testAllTextIsPresent() throws Exception {
                verifyTrue(selenium.isTextPresent("OpenNMS System"));
                verifyTrue(selenium.isTextPresent("Operations"));
                verifyTrue(selenium.isTextPresent("Nodes"));
                verifyTrue(selenium.isTextPresent("Distributed Monitoring"));
                verifyTrue(selenium.isTextPresent("Descriptions"));
                verifyTrue(selenium.isTextPresent("Scheduled Outages: Add"));
                verifyTrue(selenium.isTextPresent("Notification Status:"));
        }
        
        @Test
        public void testAllLinksArePresent() throws Exception  {
            
                verifyTrue(selenium.isElementPresent("link=Configure Users, Groups and Roles"));
                verifyTrue(selenium.isElementPresent("link=System Information"));
                verifyTrue(selenium.isElementPresent("link=Instrumentation Log Reader"));
                verifyTrue(selenium.isElementPresent("link=Configure Discovery"));
                verifyTrue(selenium.isElementPresent("link=Configure SNMP Community Names by IP"));
                verifyTrue(selenium.isElementPresent("link=Configure SNMP Data Collection per Interface"));
                verifyTrue(selenium.isElementPresent("link=Manage and Unmanage Interfaces and Services"));
                verifyTrue(selenium.isElementPresent("link=Manage Thresholds"));
                verifyTrue(selenium.isElementPresent("link=Configure Notifications"));
                verifyTrue(selenium.isElementPresent("link=Scheduled Outages"));
                verifyTrue(selenium.isElementPresent("link=Add Interface"));
                verifyTrue(selenium.isElementPresent("link=Manage Provisioning Groups"));
                verifyTrue(selenium.isElementPresent("link=Import and Export Asset Information"));
                verifyTrue(selenium.isElementPresent("link=Manage Surveillance Categories"));
                verifyTrue(selenium.isElementPresent("link=Delete Nodes"));
                verifyTrue(selenium.isElementPresent("link=Manage Applications"));
                verifyTrue(selenium.isElementPresent("link=Manage Location Monitors"));
                verifyTrue(selenium.isElementPresent("link=the OpenNMS wiki"));
        }
        
        @Test
        public void testLinkGroupOne() throws Exception {
                selenium.click("link=Configure Users, Groups and Roles");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Users and Groups"));
                verifyTrue(selenium.isTextPresent("Users"));
                verifyTrue(selenium.isTextPresent("Groups"));
                verifyTrue(selenium.isTextPresent("Roles"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=System Information");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("OpenNMS Configuration"));
                verifyTrue(selenium.isTextPresent("System Configuration"));
                verifyTrue(selenium.isTextPresent("Reports directory:"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
        }
        
        @Test
        public void testLinkGroupTwo() throws Exception {
                selenium.click("link=Configure Discovery");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("General settings"));
                verifyTrue(selenium.isTextPresent("Specifics"));
                verifyTrue(selenium.isTextPresent("Include URLs"));
                verifyTrue(selenium.isTextPresent("Include Ranges"));
                verifyTrue(selenium.isTextPresent("Exclude Ranges"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Configure SNMP Community Names by IP");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Please enter an IP or a range of IPs and the read community string below"));
                verifyTrue(selenium.isTextPresent("Updating SNMP Community Names"));
                verifyTrue(selenium.isTextPresent("optimize this list"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Configure SNMP Data Collection per Interface");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Manage SNMP Data Collection per Interface"));
                verifyTrue(selenium.isTextPresent("datacollection-config.xml file"));
                verifyTrue(selenium.isTextPresent("Node Label"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Manage and Unmanage Interfaces and Services");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Manage and Unmanage Interfaces and Services"));
                verifyTrue(selenium.isTextPresent("unchecked meaning"));
                verifyTrue(selenium.isTextPresent("mark each service"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Manage Thresholds");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Threshold Configuration"));
                verifyTrue(selenium.isTextPresent("Name"));
                verifyTrue(selenium.isTextPresent("RRD Repository"));
                selenium.click("//div[@id='content']/div/h2/a[2]");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Configure Notifications");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Configure Notifications"));
                verifyTrue(selenium.isTextPresent("Event Notifications"));
                verifyTrue(selenium.isTextPresent("Destination Paths"));
                verifyTrue(selenium.isTextPresent("Path Outages"));
                selenium.click("//div[@id='content']/div/h2/a[2]");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Scheduled Outages");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Affects..."));
                verifyTrue(selenium.isTextPresent("Notifications"));
                verifyTrue(selenium.isTextPresent("Data collection"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
        }
        
        @Test
        public void testLinkGroupThree() throws Exception {
                selenium.click("link=Add Interface");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Enter IP address"));
                verifyTrue(selenium.isTextPresent("Add Interface"));
                verifyTrue(selenium.isTextPresent("valid IP address"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Manage Provisioning Groups");
                selenium.waitForPageToLoad("30000");
                verifyEquals("Add New Group", selenium.getValue("css=input[type=submit]"));
                verifyEquals("Edit Default Foreign Source", selenium.getValue("css=input[type=button]"));
                verifyEquals("Reset Default Foreign Source", selenium.getValue("//input[@value='Reset Default Foreign Source']"));
                selenium.click("//div[@id='content']/div/h2/a[2]");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Import and Export Asset Information");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Import and Export Assets"));
                verifyTrue(selenium.isTextPresent("Importing Asset Information"));
                verifyTrue(selenium.isTextPresent("Exporting Asset Information"));
                selenium.click("//div[@id='content']/div/h2/a[2]");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Manage Surveillance Categories");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Surveillance Categories"));
                verifyTrue(selenium.isTextPresent("Category"));
                verifyEquals("Add New Category", selenium.getValue("css=input[type=submit]"));
                selenium.click("//div[@id='content']/div/h2/a[2]");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Delete Nodes");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Delete Nodes"));
                verifyTrue(selenium.isTextPresent("Node Label"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
        }
        
        @Test
        public void testLinkGroupFour() throws Exception {
                selenium.click("link=Manage Applications");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Applications"));
                verifyTrue(selenium.isTextPresent("Edit"));
                verifyEquals("Add New Application", selenium.getValue("css=input[type=submit]"));
                selenium.click("//div[@id='content']/div/h2/a[2]");
                selenium.waitForPageToLoad("30000");
                selenium.click("link=Manage Location Monitors");
                selenium.waitForPageToLoad("30000");
                verifyTrue(selenium.isTextPresent("Distributed Poller Status"));
                verifyTrue(selenium.isTextPresent("Hostname"));
                verifyEquals("Resume All", selenium.getValue("//input[@value='Resume All']"));
                selenium.click("link=Admin");
                selenium.waitForPageToLoad("30000");
        }
        
        @Test
        public void testLinkGroupFive() throws Exception {
                selenium.click("link=the OpenNMS wiki");
                verifyTrue(selenium.isTextPresent("Events"));
                verifyTrue(selenium.isTextPresent("license keys"));
                verifyTrue(selenium.isTextPresent("Stay Connected"));
                // goBack
                selenium.click("link=Log out");
                selenium.waitForPageToLoad("30000");
        }

        @After
        public void tearDown() throws Exception {
                selenium.stop();
        }

    public void setSelenium(Selenium s) {
        selenium = s;
    }

    public Selenium getSelenium() {
        return selenium;
    }
}