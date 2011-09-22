package org.opennms.smoketest;

import org.junit.Test;


public class UserAccountPageTest extends OpenNMSSeleniumTestCase {
    @Test
    public void testAllTextIsPresent() throws Exception {

        assertTrue(selenium.isTextPresent("User Account Self-Service"));
        assertTrue(selenium.isTextPresent("Account Self-Service Options"));
        assertTrue(selenium.isTextPresent("require further"));
    }

    @Test 
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Change Password"));
    }

    @Test
    public void testAllLinks() {
        selenium.click("link=Change Password");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Please enter the old and new passwords and confirm."));
        assertTrue(selenium.isTextPresent("Current Password"));
        assertTrue(selenium.isElementPresent("link=Cancel"));
    }

}
