package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;


public class SurveillancePageTest extends SeleneseTestBase {
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
        selenium.click("link=Surveillance");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testSurveillancePage() throws Exception {
        long endTime = System.currentTimeMillis() + 30000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Surveillance View:")){
                break;
            }
        }
        assertTrue(selenium.isTextPresent("Routers"));
        assertTrue(selenium.isTextPresent("Nodes Down"));
        assertTrue(selenium.isTextPresent("DEV"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
