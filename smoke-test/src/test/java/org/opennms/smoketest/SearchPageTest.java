package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class SearchPageTest extends SeleneseTestCase {
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
	        selenium.click("link=Search");
	        selenium.waitForPageToLoad("30000");
	    }

	@Test
	public void testSearchPage() throws Exception {
		verifyTrue(selenium.isTextPresent("Search for Nodes"));
		verifyTrue(selenium.isTextPresent("Search Asset Information"));
		verifyTrue(selenium.isTextPresent("Search Options"));
		verifyTrue(selenium.isTextPresent("MAC Address"));
		verifyTrue(selenium.isElementPresent("link=All nodes"));
		verifyTrue(selenium.isElementPresent("link=All nodes and their interfaces"));
		verifyTrue(selenium.isElementPresent("link=All nodes with asset info"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		verifyEquals("Search", selenium.getValue("css=input[type=submit]"));
		selenium.click("link=All nodes");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Nodes"));
		verifyTrue(selenium.isElementPresent("link=Show interfaces"));
		selenium.click("//div[@id='content']/div/h2/a[2]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=All nodes and their interfaces");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Nodes and their interfaces"));
		verifyTrue(selenium.isElementPresent("link=Hide interfaces"));
		selenium.click("//div[@id='content']/div/h2/a[2]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=All nodes with asset info");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Assets"));
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
