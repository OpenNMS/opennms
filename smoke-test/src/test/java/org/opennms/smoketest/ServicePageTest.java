/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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



import static org.junit.Assert.assertTrue;
import net.sourceforge.jwebunit.exception.ExpectedJavascriptAlertException;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;

import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;


/**
 * ServicePageTest
 *
 * @author brozow
 */
public class ServicePageTest {
    private static final String IP6_LOCAL_HOST = "0000:0000:0000:0000:0000:0000:0000:0001";
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

    @Test
    public void testGetVersion() throws Exception {
        web.login();

        final Double version = web.getVersionNumber();
        assertTrue(version >= 1.8);
        
        web.logout();
    }
    
    @Test
    public void provisionIPv6AddressAndVerifyElementPages() throws Exception {
        web.login();

        final Double version = web.getVersionNumber();
        LogUtils.debugf(this, "version = %.1f", version);
        Assume.assumeTrue(version >= 1.9);

        String groupName = "JWebUnitServicePageTest";

        web.createProvisiongGroup(groupName);

        web.add8980DetectorToForeignSource(groupName);
        
        web.addIPv6LocalhostToGroup(groupName, "localNode-" + groupName);
        
        web.synchronizeGroup(groupName);
        
        verifyServiceManagedOnInterface("localNode-" + groupName, groupName, IP6_LOCAL_HOST);
        
        web.deleteProvisionedNodes(groupName);
        
        web.deleteProvisioningGroup(groupName);
        
        web.logout();
    }
    
    @Test
    public void addUser() throws Exception{
        web.login();
        
        String userName = "smokeAddUser";
        String password = "smokeAddUserPassword";
        
        web.createUser(userName, password);
        web.logout();
    }
    
    @Test
    public void addUserToGroup() throws Exception{
        web.login();
        
        String userName = "smokeAddUserToGroup";
        String password = "smokeAddUserToGroupPassword";
        String groupName = "smokeAddUserToGroupGroup";
        
        web.createUser(userName, password);
        
        web.createGroup(groupName);
        
        web.addUserToGroup(groupName, userName);
        
        web.logout();
        
    }
    
    @Test
    public void testAllTopLevelLinks() throws Exception {
        web.login();
        web.assertTextPresent("Home");
        
        web.clickLinkWithExactText("Node List");
        web.assertTextPresent("Nodes");
        
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
        
        web.clickLinkWithExactText("Distributed Status");
        web.assertTextPresent("Distributed Status Summary");
        
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
    
    private void verifyServiceManagedOnInterface(String node, String foreignSource, String iface) {
        web.gotoPage("element/nodeList.htm?listInterfaces=true&nodename="+ node);
        
        web.assertTextPresent("Node: " + node);
        web.assertTextPresent("Foreign Source: " + foreignSource);
        
        web.clickLinkWithExactText(iface);
        
        web.assertTextPresent("Interface: "+iface);
        web.assertTextPresent(node);
        
        web.clickLinkWithExactText("HTTP-8980");

        web.assertTextPresent("HTTP-8980 service on " +iface);
        
        web.assertTableEquals("", new String[][] { 
                { "Node", node },
                { "Interface", iface },
                { "Polling Status", "Managed" }
        });
        
    }

}
