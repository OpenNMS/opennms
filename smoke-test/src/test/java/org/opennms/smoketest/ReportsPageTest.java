package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class ReportsPageTest extends SeleneseTestCase {
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
	        selenium.click("link=Reports");
	        selenium.waitForPageToLoad("30000");
	    }

	@Test
	public void testReportsPage() throws Exception {
		verifyTrue(selenium.isTextPresent("Reports"));
		verifyTrue(selenium.isTextPresent("Descriptions"));
		verifyTrue(selenium.isTextPresent("Key SNMP Customized"));
		verifyTrue(selenium.isTextPresent("Name contains"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		verifyTrue(selenium.isElementPresent("//input[@value='KSC Reports']"));
		verifyTrue(selenium.isElementPresent("link=Resource Graphs"));
		verifyTrue(selenium.isElementPresent("link=KSC Performance, Nodes, Domains"));
		verifyTrue(selenium.isElementPresent("link=Database Reports"));
		verifyTrue(selenium.isElementPresent("link=Statistics Reports"));
		selenium.click("link=Resource Graphs");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Standard Resource"));
		verifyTrue(selenium.isTextPresent("Performance Reports"));
		verifyTrue(selenium.isTextPresent("Custom Resource"));
		verifyTrue(selenium.isTextPresent("Performance Reports"));
		verifyTrue(selenium.isTextPresent("Network Performance Data"));
		verifyTrue(selenium.isTextPresent("The Standard Performance"));
		verifyTrue(selenium.isElementPresent("id=ext-gen110"));
		selenium.click("//div[@id='content']/div/h2/a[2]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=KSC Performance, Nodes, Domains");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("id=ext-gen189"));
		verifyTrue(selenium.isTextPresent("Customized Reports"));
		verifyTrue(selenium.isTextPresent("Node SNMP Interface Reports"));
		verifyTrue(selenium.isTextPresent("Descriptions"));
		selenium.click("//div[@id='content']/div/h2/a[2]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Database Reports");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Database Reports"));
		verifyTrue(selenium.isTextPresent("Descriptions"));
		verifyTrue(selenium.isTextPresent("You may run or schedule"));
		verifyTrue(selenium.isElementPresent("link=Batch reports"));
		verifyTrue(selenium.isElementPresent("link=Online reports"));
		verifyTrue(selenium.isElementPresent("//div[@id='content']/div[2]/div/ul/li[3]"));
		verifyTrue(selenium.isElementPresent("link=Manage the batch report schedule"));
		selenium.click("//div[@id='content']/div/h2/a[2]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Statistics Reports");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Statistics Report List"));
		verifyTrue(selenium.isElementPresent("css=img[alt=Filter]"));
		verifyTrue(selenium.isElementPresent("css=img[alt=Clear]"));
		verifyTrue(selenium.isElementPresent("name=reportList_f_duration"));
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
		selenium.click("css=strong");
		selenium.waitForPageToLoad("30000");
		selenium.type("id=input_j_username", "admin");
		selenium.type("name=j_password", "admin");
		selenium.click("name=Login");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
		selenium.click("css=strong");
		selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
