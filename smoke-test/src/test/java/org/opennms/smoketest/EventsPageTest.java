package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class EventsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Events");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {       
        assertTrue(selenium.isTextPresent("Event Queries"));
        assertTrue(selenium.isTextPresent("Outstanding and acknowledged events"));
        assertTrue(selenium.isTextPresent("hit [Enter]"));
        assertTrue(selenium.isTextPresent("Event ID:"));
    }
    
    @Test
    public void testAllLinksArePresent() {
        assertEquals("Get details", selenium.getValue("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("link=All events"));
        assertTrue(selenium.isElementPresent("link=Advanced Search"));
    }
    @Test 
    public void testAllLinks() {
        selenium.click("link=All events");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Ack"));
        assertTrue(selenium.isTextPresent("Event(s) outstanding"));
        assertTrue(selenium.isTextPresent("Event Text"));
        assertTrue(selenium.isElementPresent("link=Interface"));
        selenium.click("css=a[title=Events System Page]");
        waitForPageToLoad();
        selenium.click("link=Advanced Search");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Advanced Event Search"));
        assertTrue(selenium.isTextPresent("Searching Instructions"));
        assertTrue(selenium.isTextPresent("Advanced Event Search"));
        assertTrue(selenium.isElementPresent("name=usebeforetime"));
        assertTrue(selenium.isElementPresent("name=limit"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
