package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class AssetsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Assets");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() { 
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Assets Inventory"));
        assertTrue(selenium.isTextPresent("nter the data by hand"));
        assertTrue(selenium.isTextPresent("Assets with asset numbers"));
        assertTrue(selenium.isTextPresent("Assets in category"));
    }    

    @Test 
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("name=searchvalue"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
    }
    @Test
    public void testAllLinks() {
        selenium.click("link=All nodes with asset info");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Assets"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
