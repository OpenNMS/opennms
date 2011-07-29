package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class EventsPageTest extends SeleneseTestCase {
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
	        selenium.click("link=Events");
	        selenium.waitForPageToLoad("30000");
	    }

	@Test
	public void testEventsPage() throws Exception {
		verifyTrue(selenium.isTextPresent("Event Queries"));
		verifyTrue(selenium.isTextPresent("Outstanding and acknowledged events"));
		verifyTrue(selenium.isTextPresent("hit [Enter]"));
		verifyTrue(selenium.isTextPresent("Event ID:"));
		verifyEquals("Get details", selenium.getValue("css=input[type=submit]"));
		selenium.click("link=All events");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Ack"));
		verifyTrue(selenium.isTextPresent("Event(s) outstanding"));
		verifyTrue(selenium.isTextPresent("Event Text"));
		verifyTrue(selenium.isElementPresent("link=Interface"));
		selenium.click("css=a[title=Events System Page]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Advanced Search");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Advanced Event Search"));
		verifyTrue(selenium.isTextPresent("Searching Instructions"));
		verifyTrue(selenium.isTextPresent("Advanced Event Search"));
		verifyTrue(selenium.isElementPresent("name=usebeforetime"));
		verifyTrue(selenium.isElementPresent("name=limit"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		selenium.click("//div[@id='content']/div/h2/a[2]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
