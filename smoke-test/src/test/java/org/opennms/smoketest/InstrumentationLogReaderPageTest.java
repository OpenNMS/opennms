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


public class InstrumentationLogReaderPageTest extends SeleneseTestBase {
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
        assertEquals("test", selenium.getValue("name=searchString"));
        selenium.click("css=form > input[type=submit]");
        selenium.waitForPageToLoad("30000");
        assertEquals("", selenium.getValue("name=searchString"));
        assertTrue(selenium.isTextPresent("Service"));
        assertTrue(selenium.isTextPresent("Threads Used:"));
        assertTrue(selenium.isElementPresent("link=Collections"));
        assertTrue(selenium.isElementPresent("link=Average Collection Time"));
        assertTrue(selenium.isElementPresent("link=Unsuccessful Percentage"));
        assertTrue(selenium.isElementPresent("link=Average Persistence Time"));
    }
    
    @Test
    public void testSortingLinks() {
        selenium.click("link=Collections");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Collections ^"));
        selenium.click("link=Collections ^");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Collections v"));
        selenium.click("link=Average Successful Collection Time");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Successful Collection Time ^"));
        selenium.click("link=Average Successful Collection Time ^");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Successful Collection Time v"));
        selenium.click("link=Average Persistence Time");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Persistence Time ^"));
        selenium.click("link=Average Persistence Time ^");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Persistence Time v"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }


    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
