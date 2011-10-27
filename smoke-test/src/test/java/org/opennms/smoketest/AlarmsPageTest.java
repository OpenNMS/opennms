package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;

public class AlarmsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Alarms");
        selenium.waitForPageToLoad("30000");
    }

    @Test
    public void testAllTextIsPresent() {
        assertTrue(selenium.isTextPresent("Alarm Queries"));
        assertTrue(selenium.isTextPresent("Outstanding and acknowledged alarms"));
        assertTrue(selenium.isTextPresent("To view acknowledged alarms"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isTextPresent("Alarm ID:"));
    }

    @Test
    public void testAllLinksArePresent() { 
        assertTrue(selenium.isElementPresent("link=All alarms (summary)"));
        assertTrue(selenium.isElementPresent("link=All alarms (detail)"));
        assertTrue(selenium.isElementPresent("link=Advanced Search"));
    }

    @Test
    public void testAllLinks(){
        selenium.click("link=All alarms (summary)");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("alarm is outstanding"));
        assertTrue(selenium.isTextPresent("alarm is outstanding"));
        assertTrue(selenium.isElementPresent("//input[@value='Go']"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        selenium.click("css=a[title=Alarms System Page]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=All alarms (detail)");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent("link=First Event Time"));
        assertTrue(selenium.isElementPresent("link=Last Event Time"));
        assertTrue(selenium.isElementPresent("css=input[type=reset]"));
        assertTrue(selenium.isTextPresent("Ack"));
        selenium.click("css=a[title=Alarms System Page]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Advanced Search");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Alarm Text Contains:"));
        assertTrue(selenium.isTextPresent("Advanced Alarm Search"));
        selenium.open("/opennms/alarm/advsearch.jsp");
        assertTrue(selenium.isTextPresent("Advanced Alarm Search page"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("name=beforefirsteventtimemonth"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        selenium.waitForPageToLoad("30000");
    }

}
