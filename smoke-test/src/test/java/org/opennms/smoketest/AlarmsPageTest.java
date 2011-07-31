package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class AlarmsPageTest extends SeleneseTestCase {
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
	        selenium.click("link=Alarms");
	        selenium.waitForPageToLoad("30000");
	    }

	@Test
	public void testAlarmsPage() throws Exception {
		verifyTrue(selenium.isTextPresent("Alarm Queries"));
		verifyTrue(selenium.isTextPresent("Outstanding and acknowledged alarms"));
		verifyTrue(selenium.isTextPresent("To view acknowledged alarms"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		verifyTrue(selenium.isTextPresent("Alarm ID:"));
		verifyTrue(selenium.isElementPresent("link=All alarms (summary)"));
		verifyTrue(selenium.isElementPresent("link=All alarms (detail)"));
		verifyTrue(selenium.isElementPresent("link=Advanced Search"));
		selenium.click("link=All alarms (summary)");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("alarm is outstanding"));
		verifyTrue(selenium.isTextPresent("alarm is outstanding"));
		verifyTrue(selenium.isElementPresent("//input[@value='Go']"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		selenium.click("css=a[title=Alarms System Page]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=All alarms (detail)");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isElementPresent("link=First Event Time"));
		verifyTrue(selenium.isElementPresent("link=Last Event Time"));
		verifyTrue(selenium.isElementPresent("css=input[type=reset]"));
		verifyTrue(selenium.isTextPresent("Ack"));
		selenium.click("css=a[title=Alarms System Page]");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Advanced Search");
		selenium.waitForPageToLoad("30000");
		verifyTrue(selenium.isTextPresent("Alarm Text Contains:"));
		verifyTrue(selenium.isTextPresent("Advanced Alarm Search"));
		selenium.open("/opennms/alarm/advsearch.jsp");
		verifyTrue(selenium.isTextPresent("Advanced Alarm Search page"));
		verifyTrue(selenium.isElementPresent("css=input[type=submit]"));
		verifyTrue(selenium.isElementPresent("name=beforefirsteventtimemonth"));
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
