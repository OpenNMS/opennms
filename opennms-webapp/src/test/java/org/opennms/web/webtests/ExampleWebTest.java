//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/**
 * 
 */
package org.opennms.web.webtests;

/**
 * @author mhuot
 *
 */

import java.io.File;

import org.opennms.test.mock.MockLogAppender;

import net.sourceforge.jwebunit.WebTestCase;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ExampleWebTest extends WebTestCase {
    
    public ExampleWebTest(String name) {
        super(name);
    }
    
    public void FIXMEsetUp() throws Exception {
        MockLogAppender.setupLogging();
        
	// This test needs to be run from the dist/webapps/opennms directory
        ServletRunner sr = new ServletRunner(new File("src/main/webapp/WEB-INF/web.xml"), "/opennms");
     
        ServletUnitClient sc = sr.newClient();
        getTestContext().setWebClient(sc);
        getTestContext().setAuthorization("admin","OpenNMS Administrator");
        getTestContext().setBaseUrl("http://localhost:8080/opennms");
    }
    
    public void testBogus() {
        // Empty test so JUnit doesn't complain about not having any tests to run
    }
    
    public void FIXMEtestHelpPage() {
        
        beginAt("/help/index.jsp");
        assertTitleEquals("Help | OpenNMS Web Console");
        assertLinkPresentWithText("Home");
        clickLinkWithText("About the OpenNMS Web Console");
        assertTitleEquals("About | OpenNMS Web Console");
        assertTextPresent("You should have received a copy of the ");
        assertFormPresent("bookmark");
        clickLinkWithText("Help");
        assertTitleEquals("Help | OpenNMS Web Console");
        assertLinkPresentWithText("Home");
        assertLinkPresentWithText("About the OpenNMS Web Console");
        assertLinkPresentWithText("Frequently Asked Questions");
        assertLinkPresentWithText("Online Documentation");
        assertLinkPresentWithText("Reports");
        clickLinkWithText("Reports");
        assertTitleEquals("Reports | OpenNMS Web Console");
        assertLinkPresentWithText("Performance Reports");
        assertLinkPresentWithText("KSC Performance Reports and Node Reports");
        assertLinkPresentWithText("Availability Reports");
        assertLinkPresentWithText("Response Time Reports");
        clickLinkWithText("Availability Reports");
        assertTitleEquals("Availability | OpenNMS Web Console");
        assertFormPresent("avail");
        assertFormElementPresent("format");
        assertRadioOptionSelected("format", "SVG");
        assertFormElementPresent("category");
        assertRadioOptionSelected("category", "Overall Service Availability");
        setWorkingForm("avail");
        submit();
        //getTester().dumpResponse();
        assertTextPresent("No Email Address Configured");
        
//        assertTextPresent("You should have received a copy of the GNU General Public License along with this program; if not, write to the");
//        assertLinkPresentWithText("Admin");
//        clickLinkWithText("Admin");
//        assertTitleEquals("Admin | OpenNMS Web Console");
//        clickLinkWithText("Add Interface");
//        assertTitleEquals("Admin | OpenNMS Web Console");
//        assertTextPresent("Please enter a new IP address below.");
//        setFormElement("ipAddress", "10.10.10.10");
//        submit();
//        clickLinkWithText("Search");
//        setFormElement("iplike", "10.10.10.10");        
//        submit();
    }

    public void _testAdminPage() {
        
        beginAt("/index.jsp");
        assertTitleEquals("OpenNMS Web Console");
        assertLinkPresentWithText("Admin");
        clickLinkWithText("Admin");
        assertTitleEquals("Admin | OpenNMS Web Console");
    }
    
}
