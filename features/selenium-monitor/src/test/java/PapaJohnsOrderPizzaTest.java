
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public class PapaJohnsOrderPizzaTest {
    private WebDriver driver;
    private String baseUrl="http://www.papajohns.co.uk";
    private int timeout = 30;
    private StringBuffer verificationErrors = new StringBuffer();
    private WebDriverWait m_wait;
    
    
    @Before
    public void setUp() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        
        m_wait = new WebDriverWait(driver, timeout);
    }

    @Test
    public void testPapaJohnsOrderPizza() throws Exception {
        // open | / | 
        driver.get(baseUrl);
        // click | link=sign in | 
        driver.findElement(By.linkText("sign in")).click();
        // type | id=ctl00__objContent__txtEmail | pjukolo1@papajohnsdatacenter.com
        WebElement userNameTxt = driver.findElement(By.xpath("//input[@id = 'ctl00__objContent__txtEmail']"));
        userNameTxt.clear();
        userNameTxt.sendKeys("pjukolo1@papajohnsdatacenter.com");
        // type | id=ctl00__objContent__txtPassword | Every1LikesPizza
        WebElement passwordTxt = driver.findElement(By.xpath("//input[@id = 'ctl00__objContent__txtPassword']"));
        passwordTxt.clear();
        passwordTxt.sendKeys("Every1LikesPizza");
        // click | id=ctl00__objContent__btnSignIn | 
        driver.findElement(By.id("ctl00__objContent__btnSignIn")).click();
        // select | id=ctl00__objMenuHolder__objMenuPizzas__rptMenu_ctl10__ddlVariations | label=Large
        // ERROR: Caught exception [ERROR: Unsupported command [select]]
        // click | css=option[value="739bd9fc-4ebf-4f8d-b7bc-a4ea908f800a"] | 
        driver.findElement(By.cssSelector("option[value=\"739bd9fc-4ebf-4f8d-b7bc-a4ea908f800a\"]")).click();
        // click | id=ctl00__objMenuHolder__objMenuPizzas__rptMenu_ctl10_rbOriginal | 
        driver.findElement(By.id("ctl00__objMenuHolder__objMenuPizzas__rptMenu_ctl10_rbOriginal")).click();
        // click | css=#ctl00__objMenuHolder__objMenuPizzas__rptMenu_ctl10__btnAddToBasket > img[alt="Add"] | 
        driver.findElement(By.cssSelector("#ctl00__objMenuHolder__objMenuPizzas__rptMenu_ctl10__btnAddToBasket > img[alt=\"Add\"]")).click();
        // waitForTextPresent | You have 1 items in your basket | 
        // ERROR: Caught exception [ERROR: Unsupported command [isTextPresent]]
        // click | id=ctl00__objmyBasket__btnCheckout | 
        driver.findElement(By.xpath("//p[contains(text(), 'You have 1 items in your basket')]"));
        driver.findElement(By.id("ctl00__objmyBasket__btnCheckout")).click();
        // click | id=ctl00__objContent__rdDelivery | 
        //m_wait.until(elementIsFound(By.xpath("//p[contains(@id, '_rdDelivery')]"))).click();
        driver.findElement(By.id("ctl00__objContent__rdDelivery")).click();
        
        // click | id=ctl00__objContent__rdNow | 
        m_wait.until(elementIsFound(By.xpath("//input[contains(@id, '_rdNow')]")) ).click();
        //driver.findElement(By.id("ctl00__objContent__rdNow")).click();
        // click | id=ctl00__objContent__rdCreditCard | 
        m_wait.until(elementIsFound(By.xpath("//input[contains(@id, '_rdCreditCard')]")) ).click();
        //driver.findElement(By.id("ctl00__objContent__rdCreditCard")).click();
        // waitForTextPresent | Card Number | 
        // ERROR: Caught exception [ERROR: Unsupported command [isTextPresent]]
        // assertTextPresent | Card Number | 
        // ERROR: Caught exception [ERROR: Unsupported command [isTextPresent]]
        // click | //img[@alt="Papa John's Logo"] | 
        driver.findElement(By.xpath("//img[@alt=\"Papa John's Logo\"]")).click();
        // assertTextPresent | Basket: 1 items | 
        // ERROR: Caught exception [ERROR: Unsupported command [isTextPresent]]
        // click | id=ctl00__objHeader__hypBasket | 
        driver.findElement(By.id("ctl00__objHeader__hypBasket")).click();
        // click | id=ctl00__objmyBasket__rptOrderItems_ctl00__lbRemove | 
        driver.findElement(By.id("ctl00__objmyBasket__rptOrderItems_ctl00__lbRemove")).click();
        driver.findElement(By.xpath("//p[contains(text(), 'You have 0 items in your basket')]"));
        // waitForTextPresent | You have 0 items in your basket | 
        // ERROR: Caught exception [ERROR: Unsupported command [isTextPresent]]
        // assertTextPresent | You have 0 items in your basket | 
        // ERROR: Caught exception [ERROR: Unsupported command [isTextPresent]]
        // click | link=sign out | 
        driver.findElement(By.linkText("sign out")).click();
    }

    private Function<WebDriver, WebElement> elementIsFound(final By by) {
        return new Function<WebDriver, WebElement>() {

            public WebElement apply(WebDriver driver) {
                WebElement elem = null;
                try {
                    elem = driver.findElement(by);
                    if(!elem.isDisplayed()) {
                        return null;
                    }
                    //elem.click();
                }catch(StaleElementReferenceException e) {
                    elem = driver.findElement(by);
                }
                return elem;
            }
            
        };
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
