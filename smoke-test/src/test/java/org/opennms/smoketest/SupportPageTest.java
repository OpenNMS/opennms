package org.opennms.smoketest;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestBase;



public class SupportPageTest extends SeleneseTestBase {
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
        selenium.click("link=Support");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Commercial Support"));
        assertTrue(selenium.isTextPresent("About"));
        assertTrue(selenium.isTextPresent("Other Support Options"));
    }

    @Test
    public void testAllLinksArePresent() {		
        assertTrue(selenium.isElementPresent("link=About the OpenNMS Web Console"));
        assertTrue(selenium.isElementPresent("link=Release Notes"));
        assertTrue(selenium.isElementPresent("link=Online Documentation"));
        assertTrue(selenium.isElementPresent("link=Generate a System Report"));
        assertTrue(selenium.isElementPresent("link=Open a Bug or Enhancement Request"));
        assertTrue(selenium.isElementPresent("link=Chat with Developers on IRC"));
        assertTrue(selenium.isElementPresent("link=the OpenNMS.com support page"));
    }
    @Test
    public void testAllFormsArePresent() {
        assertTrue(selenium.isTextPresent("Username:"));
        assertTrue(selenium.isTextPresent("Password:"));
        assertTrue(selenium.isElementPresent("css=input[type=reset]"));
        assertEquals("Log In", selenium.getValue("css=input[type=submit]"));
    }
    @Test
    public void testAllLinks() {
        selenium.click("link=About the OpenNMS Web Console");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("OpenNMS Web Console"));
        assertTrue(selenium.isTextPresent("License and Copyright"));
        assertTrue(selenium.isTextPresent("OSI Certified Open Source Software"));
        assertTrue(selenium.isTextPresent("Version:"));
        selenium.goBack();
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("//a[@href='http://www.opennms.org/documentation/ReleaseNotesStable.html#whats-new']"));
        assertTrue(selenium.isElementPresent("//a[@href='http://www.opennms.org/wiki/']"));
        selenium.click("link=Generate a System Report");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Plugins"));
        assertTrue(selenium.isTextPresent("Report Type"));
        assertTrue(selenium.isElementPresent("name=formatter"));
        assertEquals("", selenium.getValue("css=input[type=submit]"));
        assertTrue(selenium.isTextPresent("Output"));
        assertTrue(selenium.isTextPresent("Choose which plugins to enable:"));
        selenium.goBack();
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("//a[@href='http://issues.opennms.org/']"));
        assertTrue(selenium.isElementPresent("//a[@href='irc://irc.freenode.net/%23opennms']"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
    }
}
