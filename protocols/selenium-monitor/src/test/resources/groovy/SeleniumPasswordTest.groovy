package selenium.test.groovy;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

import org.junit.*
import org.openqa.selenium.*
import org.openqa.selenium.firefox.FirefoxDriver

public class SeleniumPasswordTest {
    private WebDriver driver;
    private String baseUrl="";
    private StringBuffer verificationErrors = new StringBuffer();
    @Before
    public void setUp() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void testSeleniumPassword() throws Exception {
        // open | / | 
        driver.get("/");
        // click | link=sign in | 
        driver.findElement(By.linkText("sign in")).click();
        // click | link=exact:Forgotten your password? | 
        driver.findElement(By.linkText("exact:Forgotten your password?")).click();
        // type | id=ctl00__objContent__txtEmail | pjukolo2@papajohnsdatacenter.com
        driver.findElement(By.id("ctl00__objContent__txtEmail")).clear();
        driver.findElement(By.id("ctl00__objContent__txtEmail")).sendKeys("pjukolo2@papajohnsdatacenter.com");
        // click | id=ctl00__objContent__btnForgottenPassword | 
        driver.findElement(By.id("ctl00__objContent__btnForgottenPassword")).click();
        // assertText | id=ctl00__objContent__pnlError | Your password has been emailed to you
        assertEquals("Your password has been emailed to you", driver.findElement(By.id("ctl00__objContent__pnlError")).getText());
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
