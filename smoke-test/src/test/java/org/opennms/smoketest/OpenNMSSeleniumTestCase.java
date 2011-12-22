package org.opennms.smoketest;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.thoughtworks.selenium.SeleneseTestBase;

public class OpenNMSSeleniumTestCase extends SeleneseTestBase {

    @Before
    public void setUp() throws Exception {
    	System.setProperty("webdriver.chrome.driver", "/tmp/chromedriver");
    	assertEquals("/tmp/chromedriver", System.getProperty("webdriver.chrome.driver"));
        DesiredCapabilities capability = DesiredCapabilities.firefox();
//      WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
//		WebDriver driver = new FirefoxDriver();
        WebDriver driver = new ChromeDriver();

        String baseUrl = "http://localhost:8980/";
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        selenium.open("/opennms/login.jsp");
        selenium.type("name=j_username", "admin");
        selenium.type("name=j_password", "admin");
        selenium.click("name=Login");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=admin");
        selenium.waitForPageToLoad("30000");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }

}
