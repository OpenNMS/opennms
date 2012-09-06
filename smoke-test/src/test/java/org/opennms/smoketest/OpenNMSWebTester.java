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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import net.sourceforge.jwebunit.api.IElement;
import net.sourceforge.jwebunit.junit.WebTester;

public class OpenNMSWebTester extends WebTester {

    interface Setter {
        public void setField(String prefix);
    }

    public Double getVersionNumber() throws InterruptedException {
        final String version = getVersion();
        final String[] versionData = version.split("\\.");
        if (versionData != null && versionData.length >= 2) {
            return Double.valueOf(versionData[0] + "." + versionData[1]);
        }
        return 0.0;
    }

    public String getVersion() throws InterruptedException {
        gotoPage("support/about.jsp");
        final List<IElement> elements = getElementsByXPath("//td[@class=\"standard\"]");
        return elements.get(0).getTextContent();
    }

    public void addUserToGroup(String groupName, String userName) throws InterruptedException {
        gotoPage("admin/userGroupView/groups/list.htm");
        clickElementByXPath("//a[@href=\"javascript:modifyGroup('" + groupName + "')\"]");

        assertFormElementPresent("modifyGroup");
        selectOption("availableUsers", userName);
        clickElementByXPath("//input[@onclick=\"addUsers()\"]");
        Thread.sleep(1000);

        clickButtonWithText("Finish");

        clickLinkWithExactText(groupName);

        assertTextPresent(userName);

    }

    void createUser(String userName, String password) throws InterruptedException {
        gotoPage("admin/userGroupView/users/list.jsp");
        clickLinkWithExactText("Add New User");
        assertFormPresent("newUserForm");
        assertFormElementPresent("userID");
        assertFormElementPresent("pass1");
        assertFormElementPresent("pass2");

        setTextField("userID", userName);
        setTextField("pass1", password);
        setTextField("pass2", password);
        clickButtonWithText("OK");

        Thread.sleep(3000);

        clickButton("saveUserButton");

        assertLinkPresent("users(" + userName + ").doDetails");

    }

    void createGroup(String groupName) throws InterruptedException {
        //Add group
        gotoPage("admin/userGroupView/groups/list.htm");
        clickElementByXPath("//a[@href=\"javascript:addNewGroup()\"]");

        assertFormPresent("newGroupForm");
        assertFormElementPresent("groupName");
        assertFormElementPresent("groupComment");
        setTextField("groupName", groupName);
        setTextField("groupComment", "JWebUnit Group");
        clickButtonWithText("OK");

        clickButtonWithText("Finish");
    }

    void createProvisiongGroup(String groupName) {
        gotoPage("admin/provisioningGroups.htm");
        setWorkingForm("takeAction");
        setTextField("groupName", groupName);
        assertTextFieldEquals("groupName", groupName);
        clickButtonWithText("Add New Group");
    }

    void login() {
        beginAt("/");
        assertElementPresentByXPath("//input[@name='j_username']");
        assertElementPresentByXPath("//input[@name='j_password']");
        setTextField("j_username", "admin");
        setTextField("j_password", "admin");
        submit();
        assertTextPresent("Log out");
    }

    void logout() {
        gotoPage("index.jsp");
        clickLinkWithExactText("Log out");
        assertTextPresent("You have been logged out.");
    }

    void waitForGroupToSynchronize(String groupName) throws InterruptedException {

        Pattern re = Pattern.compile("(\\d+) nodes defined,\\s+(\\d+) nodes in database", Pattern.DOTALL);
        int defined;
        int database;

        do {
            // wait just a bit to give them time to synch
            Thread.sleep(100);

            // reload the provisioning groups page
            gotoPage("admin/provisioningGroups.htm");

            // find the 'nodes define, nodes in database' text for this group
            String synchStatus = getElementTextByXPath("//span[preceding-sibling::a[contains(@href, 'editRequisition') and contains(@href, '" + groupName + "')]]");

            // pull the numbers out
            Matcher m = re.matcher(synchStatus);
            Assert.assertTrue(m.find());
            defined = Integer.valueOf(m.group(1));
            database = Integer.valueOf(m.group(2));

            // repeat until the numbers match
        } while (defined != database);
    }

    void add8980DetectorToForeignSource(String groupName) throws InterruptedException {
        // go to the provisioning groups page
        gotoPage("admin/provisioningGroups.htm");

        // the the Eidt link for editing the foreign source for this provision group
        clickElementByXPath("//a[contains(@href, 'editForeignSource(\"" + groupName + "\")')]");

        // add a detector
        clickButtonWithText("Add Detector");

        // set the name to 'HTTP-8980' and use the HttpDectector and save
        String detectorNode = setTreeFieldsAndSave("foreignSourceEditForm", text("name", "HTTP-8980"), option("pluginClass", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));

        // now add a parameter to that dectector by click its Add Parameter link
        clickElementByXPath("//a[contains(@href, '" + detectorNode + "') and text() = '[Add Parameter]']");

        // set the port parameter to have the value '8980' and save
        setTreeFieldsAndSave("foreignSourceEditForm", option("key", "port"), text("value", "8980"));

        // click done we are finished adding the detector
        clickButtonWithText("Done");
    }

    void deleteProvisioningGroup(String groupName) {
        // provisioning groups page
        gotoPage("admin/provisioningGroups.htm");

        // click the Delete Group button for the group
        clickElementByXPath("//input[contains(@onclick, '" + groupName + "') and @value='Delete Group']");

        // make sure the group is gone
        assertTextNotPresent(groupName);
    }

    void synchronizeGroup(String groupName) throws InterruptedException {
        // go the managing provisiong group page
        gotoPage("admin/provisioningGroups.htm");

        // click the synchronize button for the group
        clickElementByXPath("//input[contains(@onclick, '" + groupName + "') and @value='Synchronize']");

        // wait until nodes define and nodes in database match
        waitForGroupToSynchronize(groupName);

    }

    void deleteProvisionedNodes(String groupName) throws InterruptedException {
        // provisioning group page
        gotoPage("admin/provisioningGroups.htm");

        // when the 'are you sure' dialog pops out.. answer yes
        setExpectedJavaScriptConfirm("Are you sure you want to delete all the nodes from group " + groupName + "? This CANNOT be undone.", true);

        // now click the delete nodes button (this pops up the dialog)
        clickElementByXPath("//input[contains(@onclick, '" + groupName + "') and @value='Delete Nodes']");

        // now synchronize the group to the database nodes are removed
        synchronizeGroup(groupName);
    }

    Setter text(final String suffix, final String value) {
        return new OpenNMSWebTester.Setter() {

            public void setField(String prefix) {
                setTextField(prefix + suffix, value);
            }

        };
    }

    Setter option(final String suffix, final String value) {
        return new OpenNMSWebTester.Setter() {

            public void setField(String prefix) {
                selectOptionByValue(prefix+suffix, value);
            }

        };
    }

    String setTreeFieldsAndSave(String formName, OpenNMSWebTester.Setter... setters) throws InterruptedException {

        Thread.sleep(1000);

        String currentNode = getElementAttributeByXPath("//input[@name='currentNode']", "value");
        String prefix = currentNode.replace(formName+".", "") + ".";

        for(OpenNMSWebTester.Setter setter : setters) {
            setter.setField(prefix);
        }

        clickElementByXPath("//input[contains(@onclick, '" + currentNode + "') and @value='Save']");
        return currentNode;
    }

    void addIPv6LocalhostToGroup(String groupName, String nodeLabel) throws InterruptedException {
        // go to the provisioning groups page
        gotoPage("admin/provisioningGroups.htm");

        // click the Edit link for the this provisioning group
        clickElementByXPath("//a[contains(@href, 'editRequisition(\"" + groupName + "\")')]");

        // add a node
        clickButtonWithText("Add Node");

        // set the nodeLabel to 'localNode' and save
        String addedNode = setTreeFieldsAndSave("nodeEditForm", text("nodeLabel", nodeLabel));

        // add an interface
        clickElementByXPath("//a[contains(@href, '" + addedNode + "') and text() = '[Add Interface for Scanning]']");

        // set the ipAddr to ::1 and set snmpPrimary to 'P' and save
        setTreeFieldsAndSave("nodeEditForm", text("ipAddr", "::1"), option("snmpPrimary", "P"));

        // we are done editting the foreign source
        clickButtonWithText("Done");
    }

}
