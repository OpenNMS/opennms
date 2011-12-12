package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;


public class SearchPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        selenium.click("link=Search");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Search for Nodes"));
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Search Options"));
        assertTrue(selenium.isTextPresent("MAC Address"));
    }
        
     @Test
     public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=All nodes"));
        assertTrue(selenium.isElementPresent("link=All nodes and their interfaces"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
     }
      
     @Test 
     public void testAllFormsArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertEquals("Search", selenium.getValue("css=input[type=submit]"));
     }
      
     @Test
     public void testAllLinks() {
        selenium.click("link=All nodes");
        waitForPageToLoad();
        long endTime = System.currentTimeMillis() + 30000;
        while(System.currentTimeMillis() < endTime){
            if(selenium.isTextPresent("Nodes")){
                break;
            }
            if(endTime - System.currentTimeMillis() < 5000){
                fail ("25 second timeout trying to reach \"Search/All nodes\" Page");
            }
        }
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=All nodes and their interfaces");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Nodes and their interfaces"));
        assertTrue(selenium.isElementPresent("link=Hide interfaces"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=All nodes with asset info");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Assets"));
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
