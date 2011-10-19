package selenium.test.groovy;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class OpennnmsSeleniumExample {
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
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
	}
    
    
	@Test
	public void test() throws Exception {
		// open | / | 
		driver.get("/");
		// click | link=About OpenNMS | 
		driver.findElement(By.linkText("About OpenNMS")).click();
		// assertTextPresent | OpenNMS is an award winning network management application platform | 
		// ERROR: Caught exception [ERROR: Unsupported command [isTextPresent]]
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
