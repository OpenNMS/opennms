package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;


public class UserAccountPageTest extends SeleneseTestBase {
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
        selenium.click("link=admin");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {

        assertTrue(selenium.isTextPresent("User Account Self-Service"));
        assertTrue(selenium.isTextPresent("Account Self-Service Options"));
        assertTrue(selenium.isTextPresent("require further"));
    }

    @Test 
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Change Password"));
    }

    @Test
    public void testAllLinks() {
        selenium.click("link=Change Password");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Please enter the old and new passwords and confirm."));
        assertTrue(selenium.isTextPresent("Current Password"));
        assertTrue(selenium.isElementPresent("link=Cancel"));
    }
    
    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
