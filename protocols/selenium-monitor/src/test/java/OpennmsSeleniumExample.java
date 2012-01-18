
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class OpennmsSeleniumExample {
    private WebDriver driver;
    private String baseUrl="";
    private int timeout = 3;
    private StringBuffer verificationErrors = new StringBuffer();

    public OpennmsSeleniumExample(String url, int timeoutInSeconds) {
         baseUrl = url;
         timeout = timeoutInSeconds;
    }
    
    @Before
    public void setUp() throws Exception {
        System.err.println("Before is being called in Groovy Script: " + this.hashCode());
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    }

    @Test
    public void testSelenium() throws Exception {
        System.err.println("Test is being called in Groovy Script: " + this.hashCode());
        // open | / |
        assertNotNull(driver);
        driver.get(baseUrl);
        // click | link=Our Story |
        driver.findElement(By.linkText("Our Story")).click();

        // assertText | link=Contact Us | Contact Us
        //assertEquals("Contact Us", driver.findElement(By.linkText("Contact Us")).getText());
        assertEquals("Contact Us", driver.findElement(By.linkText("Contact Us")).getText());
    }

    @After
    public void tearDown() throws Exception {
        System.err.println("After is being called in Groovy Script: " + this.hashCode());
        assertNotNull("Driver is null, it should not be null", driver);
        driver.quit();
        
        
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

}
