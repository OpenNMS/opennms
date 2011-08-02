package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class SupportPageTest extends SeleneseTestCase {
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
            selenium.click("link=Support");
            selenium.waitForPageToLoad("30000");
        }

	@Test
	public void testAllTextIsPresent() throws Exception {
		verifyTrue(selenium.isTextPresent("Commercial Support"));
		verifyTrue(selenium.isTextPresent("About"));
		verifyTrue(selenium.isTextPresent("Other Support Options"));
	}
	
	@Test
	public void testAllLinksArePresent() {		
		verifyTrue(selenium.isElementPresent("link=About the OpenNMS Web Console"));
		verifyTrue(selenium.isElementPresent("link=Release Notes"));
		verifyTrue(selenium.isElementPresent("link=Online Documentation"));
		verifyTrue(selenium.isElementPresent("link=Generate a System Report"));
		verifyTrue(selenium.isElementPresent("link=Open a Bug or Enhancement Request"));
		verifyTrue(selenium.isElementPresent("link=Chat with Developers on IRC"));
		verifyTrue(selenium.isElementPresent("link=the OpenNMS.com support page"));
	}
	@Test
	public void testAllFormsArePresent() {
		verifyTrue(selenium.isTextPresent("Username:"));
		verifyTrue(selenium.isTextPresent("Password:"));
		verifyTrue(selenium.isElementPresent("css=input[type=reset]"));
		verifyEquals("Log In", selenium.getValue("css=input[type=submit]"));
	}
	@Test
	public void testAllLinks() {
		selenium.click("link=About the OpenNMS Web Console");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("OpenNMS Web Console"));
		verifyTrue(selenium.isTextPresent("License and Copyright"));
		verifyTrue(selenium.isTextPresent("OSI Certified Open Source Software"));
		verifyTrue(selenium.isTextPresent("Version:"));
		selenium.click("//div[@id='content']/div/h2/a[2]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Release Notes");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("OpenNMS Release Notes"));
		verifyTrue(selenium.isTextPresent("OpenNMS Development Team"));
		verifyTrue(selenium.isTextPresent("Copyright"));
		selenium.goBack();
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Online Documentation");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Welcome to OpenNMS"));
		verifyTrue(selenium.isTextPresent("Try Out OpenNMS"));
		verifyTrue(selenium.isElementPresent("link=exact:http://demo.opennms.org/opennms/"));
		selenium.goBack();
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Generate a System Report");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Plugins"));
		verifyTrue(selenium.isTextPresent("Report Type"));
		verifyTrue(selenium.isElementPresent("name=formatter"));
		verifyEquals("", selenium.getValue("css=input[type=submit]"));
		verifyTrue(selenium.isTextPresent("Output"));
		verifyTrue(selenium.isTextPresent("Choose which plugins to enable:"));
		selenium.goBack();
		selenium.waitForPageToLoad("30000");
//		selenium.click("link=Open a Bug or Enhancement Request");
//                selenium.waitForPageToLoad("30000");
//                assertEquals("System Dashboard - The OpenNMS Issue Tracker", selenium.getTitle());
//                selenium.goBack();
//                selenium.waitForPageToLoad("30000");
                selenium.click("link=Log out");
                selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
