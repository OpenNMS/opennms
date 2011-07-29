package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class NotificationsPageTest extends SeleneseTestCase {
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
	        selenium.click("link=Notifications");
	        selenium.waitForPageToLoad("30000");
	    }

	@Test
	public void testNotificationsPage() throws Exception {
		verifyTrue(selenium.isTextPresent("Notification queries"));
		verifyTrue(selenium.isTextPresent("Outstanding and Acknowledged Notices"));
		verifyTrue(selenium.isTextPresent("Notification Escalation"));
		verifyTrue(selenium.isTextPresent("Check your outstanding notices"));
		verifyTrue(selenium.isTextPresent("Once a notice is sent"));
		verifyTrue(selenium.isTextPresent("User:"));
		verifyTrue(selenium.isTextPresent("Notice:"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		verifyTrue(selenium.isElementPresent("//input[@value='Get details']"));
		verifyTrue(selenium.isElementPresent("link=Your outstanding notices"));
		verifyTrue(selenium.isElementPresent("link=All outstanding notices"));
		verifyTrue(selenium.isElementPresent("link=All acknowledged notices"));
		selenium.click("link=Your outstanding notices");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("admin was notified"));
		verifyTrue(selenium.isElementPresent("link=[Remove all]"));
		verifyTrue(selenium.isElementPresent("link=Sent Time"));
		verifyTrue(selenium.isElementPresent("//input[@value='Acknowledge Notices']"));
		selenium.click("link=Notices");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=All outstanding notices");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("only outstanding notices"));
		verifyTrue(selenium.isElementPresent("link=Respond Time"));
		verifyTrue(selenium.isElementPresent("css=input[type=button]"));
		selenium.click("link=Notices");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=All acknowledged notices");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("only acknowledged notices"));
		verifyTrue(selenium.isElementPresent("link=Node"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		selenium.click("link=Notices");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
