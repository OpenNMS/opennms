package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class ReportsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        selenium.click("link=Reports");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Reports"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        assertTrue(selenium.isTextPresent("Key SNMP Customized"));
        assertTrue(selenium.isTextPresent("Name contains"));
    }
     
    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Resource Graphs"));
        assertTrue(selenium.isElementPresent("link=KSC Performance, Nodes, Domains"));
        assertTrue(selenium.isElementPresent("link=Database Reports"));
        assertTrue(selenium.isElementPresent("link=Statistics Reports"));
    }
        
     @Test
     public void testAllFormsArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("//input[@value='KSC Reports']"));
     }
//TODO Tak: Build report download test
     @Ignore
     @Test
     public void testDownloadSampleReport() {
    	 selenium.click("link=Database Reports");
         assertTrue(selenium.isElementPresent("link=Online reports"));
    	 selenium.click("link=Online reports");
    	 assertTrue(selenium.isTextPresent("Kochwurst sample JasperReport"));
    	 selenium.click("link=execute");
    	 selenium.click("id=run");
    	 selenium.waitForPageToLoad("300000");
    	 selenium.goBack();
     }
     
      @Test
      public void testAllLinks() {
        selenium.click("link=Resource Graphs");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Standard Resource"));
        assertTrue(selenium.isTextPresent("Performance Reports"));
        assertTrue(selenium.isTextPresent("Custom Resource"));
        assertTrue(selenium.isTextPresent("Performance Reports"));
        assertTrue(selenium.isTextPresent("Network Performance Data"));
        assertTrue(selenium.isTextPresent("The Standard Performance"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=KSC Performance, Nodes, Domains");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Customized Reports"));
        assertTrue("no Node & Domain Interface Reports found, content is: " + selenium.getHtmlSource(), selenium.isTextPresent("Node & Domain Interface Reports"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Database Reports");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Database Reports"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        assertTrue(selenium.isTextPresent("You may run or schedule"));
        assertTrue(selenium.isElementPresent("link=List reports"));
        assertTrue(selenium.isElementPresent("link=View and manage pre-run reports"));
        assertTrue(selenium.isElementPresent("link=Manage the batch report schedule"));
        selenium.goBack();
        
        waitForPageToLoad();
        selenium.click("link=Statistics Reports");
        waitForPageToLoad();
        assertEquals("Statistics Reports List | OpenNMS Web Console", selenium.getTitle());
        selenium.click("link=Log out");
        waitForPageToLoad();
        selenium.click("css=strong");
        waitForPageToLoad();
        selenium.type("id=input_j_username", "admin");
        selenium.type("name=j_password", "admin");
        selenium.click("name=Login");
        waitForPageToLoad();
        selenium.click("link=Log out");
        waitForPageToLoad();
        selenium.click("css=strong");
        waitForPageToLoad();
    }

}
