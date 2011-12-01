package selenium;

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver

class OpennmsSeleniumExample  {
    
    private WebDriver driver;
    private String baseUrl="http://www.papajohns.co.uk/";
    private int timeout = 30;
    private StringBuffer verificationErrors = new StringBuffer();
    
    public OpennmsSeleniumExample(String url, int timeoutInSeconds) {
        baseUrl = url;
        timeout = timeoutInSeconds;
    }
    
    @Before
    public void setUp() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    }

    @Test
    public void testSelenium() throws Exception {
        // open | / |
        driver.get(baseUrl);
        // click | link=Our Story |
        driver.findElement(By.linkText("Our Story")).click();
        
        // assertText | link=Contact Us | Contact Us
        //assertEquals("Contact Us", driver.findElement(By.linkText("Contact Us")).getText());
        assertEquals("Contact Us", driver.findElement(By.linkText("Contact Us")).getText());
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }
    
    
}
