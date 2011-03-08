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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WaitingRefreshHandler;

import net.sourceforge.jwebunit.api.IElement;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.junit.WebTester;


/**
 * ServicePageTest
 *
 * @author brozow
 */
public class ServicePageTest {
    private static final String IP6_LOCAL_HOST = "0000:0000:0000:0000:0000:0000:0000:0001";
    WebTester web;
    
    @Before
    public void setUp() {
        
        BasicConfigurator.configure();
        
        web = new WebTester();
        web.setBaseUrl("http://localhost:8980/opennms");
        //web.setBaseUrl("http://demo.opennms.org/opennms");
        
        if (web.getTestingEngine() instanceof HtmlUnitTestingEngineImpl) {
            HtmlUnitTestingEngineImpl engine = (HtmlUnitTestingEngineImpl)web.getTestingEngine();
            engine.setRefreshHandler(new ThreadedRefreshHandler());
        }
    }
    
    private interface Setter {
        public void setField(String prefix);
    }
    
    @Test
    public void provisionIPv6AddressAndVerifyElementPages() throws Exception {
        login();
        
        String groupName = "JWebUnitServicePageTest";

        createProvisiongGroup(groupName);

        add8980DetectorToForeignSource(groupName);
        
        addIPv6LocalhostToGroup(groupName);
        
        synchronizeGroup(groupName);
        
        verifyElementPages(groupName);
        
        deleteProvisionedNodes(groupName);
        
        deleteProvisioningGroup(groupName);
        
        logout();
    }
    
    private void verifyElementPages(String groupName) {
        web.gotoPage("element/nodeList.htm?listInterfaces=true&nodename=localNode-"+groupName);
        
        web.assertTextPresent("Node: localNode-"+groupName);
        web.assertTextPresent("Foreign Source: " + groupName);
        
        web.clickLinkWithExactText(IP6_LOCAL_HOST);
        
        web.assertTextPresent("Interface: "+IP6_LOCAL_HOST);
        web.assertTextPresent("localNode-"+groupName);
        
        web.clickLinkWithExactText("HTTP-8980");

        web.assertTextPresent("HTTP-8980 service on " +IP6_LOCAL_HOST);
        
        web.assertTableEquals("", new String[][] { 
                { "Node", "localNode-"+groupName },
                { "Interface", IP6_LOCAL_HOST },
                { "Polling Status", "Managed" }
        });
        
    }

    private void synchronizeGroup(String groupName) throws InterruptedException {
        // go the managing provisiong group page
        web.gotoPage("admin/provisioningGroups.htm");
        
        // click the synchronize button for the group
        web.clickElementByXPath("//input[contains(@onclick, '" + groupName + "') and @value='Synchronize']");
        
        // wait until nodes define and nodes in database match
        waitForGroupToSynchronize(groupName);
        
    }

    private void waitForGroupToSynchronize(String groupName) throws InterruptedException {

        Pattern re = Pattern.compile("(\\d+) nodes defined,\\s+(\\d+) nodes in database", Pattern.DOTALL);
        int defined;
        int database;

        do {
            // wait just a bit to give them time to synch
            Thread.sleep(100);
            
            // reload the provisioning groups page
            web.gotoPage("admin/provisioningGroups.htm");
            
            // find the 'nodes define, nodes in database' text for this group
            String synchStatus = web.getElementTextByXPath("//span[preceding-sibling::a[contains(@href, 'editRequisition') and contains(@href, '" + groupName + "')]]");
            
            // pull the numbers out
            Matcher m = re.matcher(synchStatus);
            assertTrue(m.find());
            defined = Integer.valueOf(m.group(1));
            database = Integer.valueOf(m.group(2));
            
            // repeat until the numbers match
        } while (defined != database);
    }

    private void addIPv6LocalhostToGroup(String groupName) throws InterruptedException {
        // go to the provisioning groups page
        web.gotoPage("admin/provisioningGroups.htm");
        
        // click the Edit link for the this provisioning group
        web.clickElementByXPath("//a[contains(@href, 'editRequisition(\"" + groupName + "\")')]");
        
        // add a node
        web.clickButtonWithText("Add Node");

        // set the nodeLabel to 'localNode' and save
        String addedNode = setTreeFieldsAndSave("nodeEditForm", text("nodeLabel", "localNode-" + groupName));
        
        // add an interface
        web.clickElementByXPath("//a[contains(@href, '" + addedNode + "') and text() = '[Add Interface]']");
        
        // set the ipAddr to ::1 and set snmpPrimary to 'P' and save
        setTreeFieldsAndSave("nodeEditForm", text("ipAddr", "::1"), option("snmpPrimary", "P"));
        
        // we are done editting the foreign source
        web.clickButtonWithText("Done");
    }

    private void add8980DetectorToForeignSource(String groupName) throws InterruptedException {
        // go to the provisioning groups page
        web.gotoPage("admin/provisioningGroups.htm");
        
        // the the Eidt link for editing the foreign source for this provision group
        web.clickElementByXPath("//a[contains(@href, 'editForeignSource(\"" + groupName + "\")')]");

        // add a detector
        web.clickButtonWithText("Add Detector");
        
        // set the name to 'HTTP-8980' and use the HttpDectector and save
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", text("name", "HTTP-8980"), option("pluginClass", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));

        // now add a parameter to that dectector by click its Add Parameter link
        web.clickElementByXPath("//a[contains(@href, '" + detectorNode + "') and text() = '[Add Parameter]']");

        // set the port parameter to have the value '8980' and save
        setTreeFieldsAndSave("foreignSourceEditForm", option("key", "port"), text("value", "8980"));

        // click done we are finished adding the detector
        web.clickButtonWithText("Done");
    }
    
    private String setTreeFieldsAndSave(String formName, Setter... setters) throws InterruptedException {
        
        Thread.sleep(1000);

        String currentNode = web.getElementAttributeByXPath("//input[@name='currentNode']", "value");
        String prefix = currentNode.replace(formName+".", "") + ".";

        for(Setter setter : setters) {
            setter.setField(prefix);
        }

        web.clickElementByXPath("//input[contains(@onclick, '" + currentNode + "') and @value='Save']");
        return currentNode;
    }
    
    private Setter text(final String suffix, final String value) {
        return new Setter() {

            public void setField(String prefix) {
                web.setTextField(prefix + suffix, value);
            }
            
        };
    }
    
    private Setter option(final String suffix, final String value) {
        return new Setter() {

            public void setField(String prefix) {
                web.selectOptionByValue(prefix+suffix, value);
            }
            
        };
    }
    
    private void deleteProvisioningGroup(String groupName) {
        // provisioning groups page
        web.gotoPage("admin/provisioningGroups.htm");
        
        // click the Delete Group button for the group
        web.clickElementByXPath("//input[contains(@onclick, '" + groupName + "') and @value='Delete Group']");
        
        // make sure the group is gone
        web.assertTextNotPresent(groupName);
    }

    private void deleteProvisionedNodes(String groupName) throws InterruptedException {
        // provisioning group page
        web.gotoPage("admin/provisioningGroups.htm");

        // when the 'are you sure' dialog pops out.. answer yes
        web.setExpectedJavaScriptConfirm("Are you sure you want to delete all the nodes from group " + groupName + "? This CANNOT be undone.", true);

        // now click the delete nodes button (this pops up the dialog)
        web.clickElementByXPath("//input[contains(@onclick, '" + groupName + "') and @value='Delete Nodes']");

        // now synchronize the group to the database nodes are removed
        synchronizeGroup(groupName);
    }

    private void createProvisiongGroup(String groupName) {
        web.gotoPage("admin/provisioningGroups.htm");
        web.setWorkingForm("takeAction");
        web.setTextField("groupName", groupName);
        web.assertTextFieldEquals("groupName", groupName);
        web.clickButtonWithText("Add New Group");
    }

    private void login() {
        web.beginAt("/");
        web.assertElementPresentByXPath("//input[@name='j_username']");
        web.assertElementPresentByXPath("//input[@name='j_password']");
        web.setTextField("j_username", "admin");
        web.setTextField("j_password", "admin");
        web.submit();
        web.assertTextPresent("Log out");
    }
    
    private void logout() {
        web.gotoPage("index.jsp");
        web.clickLinkWithExactText("Log out");
        web.assertTextPresent("You have been logged out.");
    }
    

}
