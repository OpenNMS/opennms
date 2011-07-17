/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.smoketest;




import net.sourceforge.jwebunit.exception.ExpectedJavascriptAlertException;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;
import org.apache.log4j.BasicConfigurator;

import org.junit.Before;
import org.junit.Test;


import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;



public class InstrumentationLogReaderPageTest {

    OpenNMSWebTester web;
    
    @Before
    public void setUp() throws ExpectedJavascriptAlertException {
        
        BasicConfigurator.configure();
        
        web = new OpenNMSWebTester();
        web.setBaseUrl("http://localhost:8980/opennms");
        
        web.setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
        if (web.getTestingEngine() instanceof HtmlUnitTestingEngineImpl) {
            HtmlUnitTestingEngineImpl engine = (HtmlUnitTestingEngineImpl)web.getTestingEngine();
            engine.setRefreshHandler(new ThreadedRefreshHandler());
        }
    }
    
    public void navigateToPage() {
        web.clickLinkWithExactText("Admin");
        web.clickLinkWithExactText("Instrumentation Log Reader");
    }
    
    @SuppressWarnings("deprecation")
    @Test 
    public void testFiltering() {
        web.login();
        navigateToPage();
        web.assertFormPresent("ILRfilter");
        web.setFormElement("searchString", "test");
        web.clickButtonWithText("Submit");
        web.assertTextPresent("test");
        web.logout();
    }

    @Test 
    public void testServiceStatHeadersArePresent() {
        web.login();
        navigateToPage();
        web.assertTextPresent("Service");
        web.assertLinkPresentWithText("Collections");
        web.assertLinkPresentWithText("Average Collection Time");
        web.assertLinkPresentWithText("Average Time Between Collections");
        web.assertLinkPresentWithText("Successful Percentage");
        web.assertLinkPresentWithText("Average Successful Collection Time");
        web.assertLinkPresentWithText("Unsuccessful Collections");
        web.assertLinkPresentWithText("Unsuccessful Percentage");
        web.assertLinkPresentWithText("Average Unsuccessful Collection Time");
        web.assertLinkPresentWithText("Average Persistence Time");
        web.assertLinkPresentWithText("Total Persistence Time");
        web.logout();   
    }
    
    @Test 
    public void testSorting() {
        web.login(); 
        navigateToPage();
        web.clickLinkWithExactText("Collections");
        web.assertLinkPresentWithExactText("Collections ^");
        web.clickLinkWithExactText("Collections ^");
        web.assertLinkPresentWithExactText("Collections v");
        
        web.clickLinkWithExactText("Average Collection Time");
        web.assertLinkPresentWithExactText("Average Collection Time ^");
        web.clickLinkWithExactText("Average Collection Time ^");
        web.assertLinkPresentWithExactText("Average Collection Time v");
        
        web.clickLinkWithExactText("Average Time Between Collections");
        web.assertLinkPresentWithExactText("Average Time Between Collections ^");
        web.clickLinkWithExactText("Average Time Between Collections ^");
        web.assertLinkPresentWithExactText("Average Time Between Collections v");
        
        web.clickLinkWithExactText("Successful Percentage");
        web.assertLinkPresentWithExactText("Successful Percentage ^");
        web.clickLinkWithExactText("Successful Percentage ^");
        web.assertLinkPresentWithExactText("Successful Percentage v");
        
        web.clickLinkWithExactText("Average Successful Collection Time");
        web.assertLinkPresentWithExactText("Average Successful Collection Time ^");
        web.clickLinkWithExactText("Average Successful Collection Time ^");
        web.assertLinkPresentWithExactText("Average Successful Collection Time v");
        
        web.clickLinkWithExactText("Unsuccessful Collections");
        web.assertLinkPresentWithExactText("Unsuccessful Collections ^");
        web.clickLinkWithExactText("Unsuccessful Collections ^");
        web.assertLinkPresentWithExactText("Unsuccessful Collections v");
        
        web.clickLinkWithExactText("Unsuccessful Percentage");
        web.assertLinkPresentWithExactText("Unsuccessful Percentage ^");
        web.clickLinkWithExactText("Unsuccessful Percentage ^");
        web.assertLinkPresentWithExactText("Unsuccessful Percentage v");
        
        web.clickLinkWithExactText("Average Unsuccessful Collection Time");
        web.assertLinkPresentWithExactText("Average Unsuccessful Collection Time ^");
        web.clickLinkWithExactText("Average Unsuccessful Collection Time ^");
        web.assertLinkPresentWithExactText("Average Unsuccessful Collection Time v");
        
        web.clickLinkWithExactText("Average Persistence Time");
        web.assertLinkPresentWithExactText("Average Persistence Time ^");
        web.clickLinkWithExactText("Average Persistence Time ^");
        web.assertLinkPresentWithExactText("Average Persistence Time v");
        
        web.logout();       
    }
    
    @Test
    public void testGlobalStatHeadersArePresent() {
        web.login();
        navigateToPage();
        web.assertTextPresent("StartTime");
        web.assertTextPresent("EndTime");
        web.assertTextPresent("Duration");
        web.assertTextPresent("Total Services");
        web.assertTextPresent("Threads Used");     
        web.logout();    
    }
    
    
    @Test
    public void testAllTopLevelLinks() throws Exception {
        web.login();
        navigateToPage();
        web.assertTextPresent("Home");
        
        web.clickLinkWithExactText("Node List");
        web.assertTextPresent("Home");
        
        web.clickLinkWithText("Search", 0);
        web.assertTextPresent("Search for Nodes");
        
        web.clickLinkWithExactText("Outages");
        web.assertTextPresent("Outages and Service Level Availability");
        
        web.clickLinkWithExactText("Path Outages");
        web.assertTextPresent("All path outages");
        
        web.clickLinkWithExactText("Dashboard");
        web.assertElementPresent("surveillanceView");
        
        web.clickLinkWithExactText("Events");
        web.assertTextPresent("Outstanding and acknowledged events");
        
        web.clickLinkWithExactText("Alarms");
        web.assertTextPresent("Outstanding and acknowledged alarms");
        
        web.clickLinkWithExactText("Notifications");
        web.assertTextPresent("Outstanding and Acknowledged Notices");
        
        web.clickLinkWithExactText("Assets");
        web.assertTextPresent("Search Asset Information");
        
        web.clickLinkWithExactText("Reports");
        web.assertTextPresent("Database Reports");
        
        web.clickLinkWithExactText("Charts");
        web.assertElementPresent("include-charts");
        
        web.clickLinkWithExactText("Surveillance");
        web.assertElementPresent("content");
        
//        web.clickLinkWithExactText("Distributed Map");
//        web.assertTextPresent("Map");
        
        // Account for the distributed status refresh
        Thread.sleep(3000);

        web.clickLinkWithExactText("Map");
        web.assertTextPresent("Network Topology Maps");
        
        web.clickLinkWithExactText("Add Node");
        web.assertTextPresent("Node Quick-Add");
        
        web.clickLinkWithExactText("Admin");
        web.assertTextPresent("OpenNMS System");
        
        web.clickLinkWithExactText("Support");
        web.assertTextPresent("Commercial Support");
        
        web.logout();
    }
}