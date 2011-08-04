package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;


public class SearchPageTest extends SeleneseTestBase {
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
        selenium.click("link=Search");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testSearchPage() throws Exception {
        assertTrue(selenium.isTextPresent("Search for Nodes"));
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Search Options"));
        assertTrue(selenium.isTextPresent("MAC Address"));
        assertTrue(selenium.isElementPresent("link=All nodes"));
        assertTrue(selenium.isElementPresent("link=All nodes and their interfaces"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertEquals("Search", selenium.getValue("css=input[type=submit]"));
        selenium.click("link=All nodes");
        selenium.waitForPageToLoad("30000");
        long endTime = System.currentTimeMillis() + 30000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Nodes")){
                break;
            }
        }
        selenium.click("//div[@id='content']/div/h2/a[2]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=All nodes and their interfaces");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Nodes and their interfaces"));
        assertTrue(selenium.isElementPresent("link=Hide interfaces"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=All nodes with asset info");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Assets"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
