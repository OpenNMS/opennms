package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class AssetsPageTest extends SeleneseTestCase {
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
	        selenium.click("link=Assets");
	        selenium.waitForPageToLoad("30000");
	    }

	@Test
	public void testAssetsPage() throws Exception {
		verifyTrue(selenium.isTextPresent("Search Asset Information"));
		verifyTrue(selenium.isTextPresent("Assets Inventory"));
		verifyTrue(selenium.isTextPresent("nter the data by hand"));
		verifyTrue(selenium.isTextPresent("Assets with asset numbers"));
		verifyTrue(selenium.isTextPresent("Assets in category"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		verifyTrue(selenium.isElementPresent("name=searchvalue"));
		verifyTrue(selenium.isElementPresent("link=All nodes with asset info"));
		selenium.click("link=All nodes with asset info");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Assets"));
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
