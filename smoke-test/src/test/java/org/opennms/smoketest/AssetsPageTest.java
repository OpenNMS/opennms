package org.opennms.smoketest;

import java.net.URL;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;


public class AssetsPageTest extends SeleneseTestBase {
    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capability = DesiredCapabilities.firefox();
        WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
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
    public void testAllTextIsPresent() { 
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Assets Inventory"));
        assertTrue(selenium.isTextPresent("nter the data by hand"));
        assertTrue(selenium.isTextPresent("Assets with asset numbers"));
        assertTrue(selenium.isTextPresent("Assets in category"));
    }    

    @Test 
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("name=searchvalue"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
    }
    @Test
    public void testAllLinks() {
        selenium.click("link=All nodes with asset info");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Assets"));
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
