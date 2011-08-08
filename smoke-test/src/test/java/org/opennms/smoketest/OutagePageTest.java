package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;


public class OutagePageTest extends SeleneseTestBase {
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
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Outage Menu"));
        assertTrue(selenium.isTextPresent("Outages and Service Level Availability"));
        assertTrue(selenium.isTextPresent("Outage ID"));
        assertTrue(selenium.isTextPresent("create notifications"));
    }  

    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Current outages"));
        assertTrue(selenium.isElementPresent("link=All outages"));
    }

    @Test
    public void testAllFormsArePresent() {
        assertEquals("Get details", selenium.getValue("css=input[type=submit]"));
    }
    
    @Test
    public void testAllLinks() {
        selenium.click("link=Current outages");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("name=outtype"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("link=Interface"));
        selenium.click("css=a[title=Outages System Page]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=All outages");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("name=outtype"));
        assertTrue(selenium.isTextPresent("Current Resolved Both Current & Resolved"));
        assertTrue(selenium.isTextPresent("Interface"));
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
