package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;


public class DistributedMapTest extends SeleneseTestBase {
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
        selenium.click("link=Distributed Map");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testDistributedMap() throws Exception {
        assertEquals("Applications", selenium.getTable("css=td > table.0.2"));
        assertEquals("off", selenium.getValue("id=gwt-uid-6"));
        assertEquals("on", selenium.getValue("id=gwt-uid-1"));
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
