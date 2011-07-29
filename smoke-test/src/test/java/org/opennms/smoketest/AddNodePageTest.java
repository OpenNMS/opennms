package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

@SuppressWarnings("deprecation")
public class AddNodePageTest extends SeleneseTestCase {
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
	        selenium.click("link=Add Node");
	        selenium.waitForPageToLoad("30000");
	    }
	@Test
	public void setupProvisioningGroup() throws Exception {
	    selenium.open("/opennms/admin/node/add.htm");
            selenium.click("link=Admin");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Manage Provisioning Groups");
            selenium.waitForPageToLoad("30000");
            selenium.type("css=form[name=takeAction] > input[name=groupName]", "test");
            selenium.click("css=input[type=submit]");
            selenium.waitForPageToLoad("30000");
            selenium.click("//input[@value='Synchronize']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Log out");
            selenium.waitForPageToLoad("30000");
	}
	@Test
	public void testAddNodePage() throws Exception {
	      
		verifyTrue(selenium.isTextPresent("Category:"));
		verifyEquals("Provision", selenium.getValue("css=input[type=submit]"));
		verifyTrue(selenium.isElementPresent("css=input[type=reset]"));
		verifyTrue(selenium.isTextPresent("Enable Password:"));
		verifyTrue(selenium.isTextPresent("Node Quick-Add"));
		verifyTrue(selenium.isTextPresent("CLI Authentication Parameters (optional)"));
		verifyTrue(selenium.isTextPresent("SNMP Parameters (optional)"));
		verifyTrue(selenium.isTextPresent("Surveillance Category Memberships (optional)"));
		verifyTrue(selenium.isTextPresent("Basic Attributes (required)"));
		selenium.click("link=Log out");
		selenium.waitForPageToLoad("30000");
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}
