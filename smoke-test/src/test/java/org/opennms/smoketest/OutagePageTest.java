package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;


public class OutagePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Outages");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Outage Menu"));
        assertTrue(selenium.isTextPresent("Outages and Service Level Availability"));
        assertTrue(selenium.isTextPresent("Outage ID"));
        assertTrue(selenium.isTextPresent("create notifications"));
    }  

    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Current outages"));
        assertTrue(selenium.isElementPresent("link=All outages"));
    }

    @Test
    public void testAllFormsArePresent() {
        assertEquals("Get details", selenium.getValue("css=input[type=submit]"));
    }
    
    @Test
    public void testAllLinks() {
        selenium.click("link=Current outages");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("name=outtype"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("link=Interface"));
        selenium.click("css=a[title=Outages System Page]");
        waitForPageToLoad();
        selenium.click("link=All outages");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("name=outtype"));
        assertTrue(selenium.isTextPresent("Current Resolved Both Current & Resolved"));
        assertTrue(selenium.isTextPresent("Interface"));
        selenium.click("css=a[title=Outages System Page]");
        waitForPageToLoad();
        selenium.click("css=input[type=submit]");
        assertEquals("Please enter a valid outage ID.", selenium.getAlert());
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
