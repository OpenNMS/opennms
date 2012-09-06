/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
