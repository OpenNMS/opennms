package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class AssetsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Assets");
        selenium.waitForPageToLoad("30000");
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
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Assets"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

}
