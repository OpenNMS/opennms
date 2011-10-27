package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;



public class NotificationsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Notifications");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Notification queries"));
        assertTrue(selenium.isTextPresent("Outstanding and Acknowledged Notices"));
        assertTrue(selenium.isTextPresent("Notification Escalation"));
        assertTrue(selenium.isTextPresent("Check your outstanding notices"));
        assertTrue(selenium.isTextPresent("Once a notice is sent"));
        assertTrue(selenium.isTextPresent("User:"));
        assertTrue(selenium.isTextPresent("Notice:"));
    }

    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Your outstanding notices"));
        assertTrue(selenium.isElementPresent("link=All outstanding notices"));
        assertTrue(selenium.isElementPresent("link=All acknowledged notices"));
    }

    @Test 
    public void testAllFormsArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("//input[@value='Get details']"));
    }

    @Test
    public void testAllLinks() {
        selenium.click("link=Your outstanding notices");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("admin was notified"));
        assertTrue(selenium.isElementPresent("link=[Remove all]"));
        assertTrue(selenium.isElementPresent("link=Sent Time"));
        assertTrue(selenium.isElementPresent("//input[@value='Acknowledge Notices']"));
        selenium.click("link=Notices");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=All outstanding notices");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("only outstanding notices"));
        assertTrue(selenium.isElementPresent("link=Respond Time"));
        assertTrue(selenium.isElementPresent("css=input[type=button]"));
        selenium.click("link=Notices");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=All acknowledged notices");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("only acknowledged notices"));
        assertTrue(selenium.isElementPresent("link=Node"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        selenium.click("link=Notices");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
    }

}
