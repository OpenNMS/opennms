package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class InstrumentationLogReaderPageTest extends SeleneseTestCase {
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
            selenium.click("link=Instrumentation Log Reader");
            selenium.waitForPageToLoad("30000");
        }

	@Test
	public void testInstrumentationLogReaderPage() throws Exception {
		selenium.type("name=searchString", "test");
		selenium.click("css=input[type=submit]");
		selenium.waitForPageToLoad("30000");
		verifyEquals("test", selenium.getValue("name=searchString"));
		selenium.click("css=form > input[type=submit]");
		selenium.waitForPageToLoad("30000");
		verifyEquals("", selenium.getValue("name=searchString"));
		verifyTrue(selenium.isTextPresent("Service"));
		verifyTrue(selenium.isTextPresent("Threads Used:"));
		verifyTrue(selenium.isElementPresent("link=Collections"));
		verifyTrue(selenium.isElementPresent("link=Average Collection Time"));
		verifyTrue(selenium.isElementPresent("link=Unsuccessful Percentage"));
		verifyTrue(selenium.isElementPresent("link=Average Persistence Time"));
		selenium.click("link=Collections");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("link=Collections ^"));
		selenium.click("link=Collections ^");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("link=Collections v"));
		selenium.click("link=Average Successful Collection Time");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("link=Average Successful Collection Time ^"));
		selenium.click("link=Average Successful Collection Time ^");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("link=Average Successful Collection Time v"));
		selenium.click("link=Average Persistence Time");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("link=Average Persistence Time ^"));
		selenium.click("link=Average Persistence Time ^");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("link=Average Persistence Time v"));
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
