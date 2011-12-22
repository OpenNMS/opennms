package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;


public class ReportsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        selenium.click("link=Reports");
        selenium.waitForPageToLoad("30000");
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
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Standard Resource"));
        assertTrue(selenium.isTextPresent("Performance Reports"));
        assertTrue(selenium.isTextPresent("Custom Resource"));
        assertTrue(selenium.isTextPresent("Performance Reports"));
        assertTrue(selenium.isTextPresent("Network Performance Data"));
        assertTrue(selenium.isTextPresent("The Standard Performance"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=KSC Performance, Nodes, Domains");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Customized Reports"));
        assertTrue(selenium.isTextPresent("Node SNMP Interface Reports"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Database Reports");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Database Reports"));
        assertTrue(selenium.isTextPresent("Descriptions"));
        assertTrue(selenium.isTextPresent("You may run or schedule"));
        assertTrue(selenium.isElementPresent("link=Batch reports"));
        assertTrue(selenium.isElementPresent("link=Online reports"));
        assertTrue(selenium.isElementPresent("//div[@id='content']/div[2]/div/ul/li[3]"));
        assertTrue(selenium.isElementPresent("link=Manage the batch report schedule"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Statistics Reports");
        selenium.waitForPageToLoad("30000");
        assertEquals("Statistics Reports List | OpenNMS Web Console", selenium.getTitle());
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=strong");
        selenium.waitForPageToLoad("30000");
        selenium.type("id=input_j_username", "admin");
        selenium.type("name=j_password", "admin");
        selenium.click("name=Login");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Log out");
        selenium.waitForPageToLoad("30000");
        selenium.click("css=strong");
        selenium.waitForPageToLoad("30000");
    }

}
