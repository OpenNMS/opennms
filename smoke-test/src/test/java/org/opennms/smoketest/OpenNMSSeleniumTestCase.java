package org.opennms.smoketest;

import org.junit.After;
import org.junit.Before;
import org.opennms.core.test.MockLogAppender;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.thoughtworks.selenium.SeleneseTestBase;

public class OpenNMSSeleniumTestCase extends SeleneseTestBase {

    protected static final String LOAD_TIMEOUT = "60000";

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        // Google Chrome
        // System.setProperty("webdriver.chrome.driver", "/Users/ranger/Downloads/chromedriver");
        // WebDriver driver = new ChromeDriver();

        // Selenium remote server
        // DesiredCapabilities capability = DesiredCapabilities.firefox();
        // WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);

        // Firefox
        WebDriver driver = new FirefoxDriver();

        String baseUrl = "http://localhost:8980/";
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        selenium.open("/opennms/login.jsp");
        selenium.type("name=j_username", "admin");
        selenium.type("name=j_password", "admin");
        selenium.click("name=Login");
        waitForPageToLoad();
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }

    protected void waitForPageToLoad() {
        selenium.waitForPageToLoad(LOAD_TIMEOUT);
    }

}
