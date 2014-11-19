/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReportsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        reportsPage();
    }

    private void reportsPage() throws Exception {
        frontPage();
        clickAndWait("link=Reports");
    }

    @Test
    public void a_testAllTextIsPresent() throws Exception {
        waitForText("Reports");
        waitForText("Descriptions");
        waitForText("Key SNMP Customized");
        waitForText("Name contains");
    }
     
    @Test
    public void b_testAllLinksArePresent() throws InterruptedException {
        waitForElement("link=Resource Graphs");
        waitForElement("link=KSC Performance, Nodes, Domains");
        waitForElement("link=Database Reports");
        waitForElement("link=Statistics Reports");
    }
        
     @Test
     public void c_testAllFormsArePresent() throws InterruptedException {
        waitForElement("css=input[type=submit]");
        waitForElement("//input[@value='KSC Reports']");
     }
//TODO Tak: Build report download test
     @Ignore
     @Test
     public void d_testDownloadSampleReport() throws InterruptedException {
         clickAndWait("link=Database Reports");
         waitForElement("link=Online reports");
    	 clickAndWait("link=Online reports");
    	 waitForText("Kochwurst sample JasperReport");
    	 clickAndWait("link=execute");
    	 selenium.click("id=run");
    	 selenium.waitForPageToLoad("300000");
     }
     
      @Test
      public void e_testAllLinks() throws Exception {
        clickAndWait("link=Resource Graphs");
        waitForText("Standard Resource");
        waitForText("Performance Reports");
        waitForText("Custom Resource");
        waitForText("Performance Reports");
        waitForText("Network Performance Data");
        waitForText("The Standard Performance");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=KSC Performance, Nodes, Domains");
        waitForText("Customized Reports");
        waitForText("Node & Domain Interface Reports");
        waitForText("Descriptions");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=Database Reports");
        waitForText("Database Reports");
        waitForText("Descriptions");
        waitForText("You may run or schedule");
        waitForElement("link=List reports");
        waitForElement("link=View and manage pre-run reports");
        waitForElement("link=Manage the batch report schedule");

        reportsPage();
        clickAndWait("link=Statistics Reports");
        assertEquals("Statistics Reports List | OpenNMS Web Console", selenium.getTitle());
        clickAndWait("link=Log out");
        clickAndWait("css=strong");
        selenium.type("id=input_j_username", "admin");
        selenium.type("name=j_password", "admin");
        clickAndWait("name=Login");
        clickAndWait("link=Log out");
        clickAndWait("css=strong");
    }

}
