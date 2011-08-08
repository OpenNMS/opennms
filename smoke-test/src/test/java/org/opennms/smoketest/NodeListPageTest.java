package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;




public class NodeListPageTest extends SeleneseTestBase {
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
        selenium.click("link=Node List");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Nodes"));
    }
    
    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("//a[@href='element/nodeList.htm?listInterfaces=true']"));
    }
    
    @Test
    public void testAllLinks() {
        selenium.click("link=Show interfaces");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("interfaces"));
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
