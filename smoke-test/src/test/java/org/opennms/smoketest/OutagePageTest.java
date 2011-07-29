package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class OutagePageTest extends SeleneseTestCase {
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
	        selenium.click("link=Outages");
	        selenium.waitForPageToLoad("30000");
	    }

	@Test
	public void testOutagePage() throws Exception {
		verifyTrue(selenium.isTextPresent("Outage Menu"));
		verifyTrue(selenium.isTextPresent("Outages and Service Level Availability"));
		verifyTrue(selenium.isTextPresent("Outage ID"));
		verifyTrue(selenium.isTextPresent("create notifications"));
		verifyEquals("Get details", selenium.getValue("css=input[type=submit]"));
		verifyTrue(selenium.isElementPresent("link=Current outages"));
		verifyTrue(selenium.isElementPresent("link=All outages"));
		selenium.click("link=Current outages");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("name=outtype"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		verifyTrue(selenium.isElementPresent("link=Interface"));
		selenium.click("css=a[title=Outages System Page]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=All outages");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("name=outtype"));
		verifyTrue(selenium.isTextPresent("Current Resolved Both Current & Resolved"));
		verifyTrue(selenium.isTextPresent("Interface"));
		selenium.click("css=a[title=Outages System Page]");
		selenium.waitForPageToLoad("30000");
		selenium.click("css=input[type=submit]");
		assertEquals("Please enter a valid outage ID.", selenium.getAlert());
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
