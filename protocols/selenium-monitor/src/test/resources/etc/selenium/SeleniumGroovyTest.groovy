package selenium.test.groovy;

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.opennms.core.test.MockLogAppender;
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

class SeleniumGroovyTest  {
    
    private WebDriver driver;
    private String baseUrl="http://www.papajohns.co.uk/";
    private int timeout = 30;
    private StringBuffer verificationErrors = new StringBuffer();
    
    public SeleniumGroovyTest(String url, int timeoutInSeconds) {
        System.err.println("URL: " + url);
        baseUrl = url;
        timeout = timeoutInSeconds;
    }
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        driver = new HtmlUnitDriver();
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    }

    @Test
    public void testSelenium() throws Exception {
        // open | / |
        driver.get(baseUrl + "/opennms/index.html");
        
        // assertText | link=Contact Us | Contact Us
        //assertEquals("Contact Us", driver.findElement(By.linkText("Contact Us")).getText());
        assertEquals("Testing Testing", driver.findElement(By.tagName("h2")).getText());
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
