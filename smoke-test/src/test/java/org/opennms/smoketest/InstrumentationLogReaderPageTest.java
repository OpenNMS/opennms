package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class InstrumentationLogReaderPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        selenium.click("link=Instrumentation Log Reader");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testInstrumentationLogReaderPage() throws Exception {
        selenium.type("name=searchString", "test");
        selenium.click("css=input[type=submit]");
        selenium.waitForPageToLoad("30000");
        assertEquals("test", selenium.getValue("name=searchString"));
        selenium.click("css=form > input[type=submit]");
        selenium.waitForPageToLoad("30000");
        assertEquals("", selenium.getValue("name=searchString"));
        assertTrue(selenium.isTextPresent("Service"));
        assertTrue(selenium.isTextPresent("Threads Used:"));
        assertTrue(selenium.isElementPresent("link=Collections"));
        assertTrue(selenium.isElementPresent("link=Average Collection Time"));
        assertTrue(selenium.isElementPresent("link=Unsuccessful Percentage"));
        assertTrue(selenium.isElementPresent("link=Average Persistence Time"));
    }
    
    @Test
    public void testSortingLinks() {
        selenium.click("link=Collections");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Collections ^"));
        selenium.click("link=Collections ^");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Collections v"));
        selenium.click("link=Average Successful Collection Time");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Successful Collection Time ^"));
        selenium.click("link=Average Successful Collection Time ^");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Successful Collection Time v"));
        selenium.click("link=Average Persistence Time");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Persistence Time ^"));
        selenium.click("link=Average Persistence Time ^");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=Average Persistence Time v"));
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }
}
