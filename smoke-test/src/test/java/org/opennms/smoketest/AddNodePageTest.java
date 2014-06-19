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
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

public class AddNodePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        clickAndWait("link=Add Node");
    }

    @Test
    public void testAddNode() throws Exception {
        setupProvisioningGroup();
        addNode();
        deleteProvisioningGroup();
    }

    public void setupProvisioningGroup() throws Exception {
        clickAndWait("link=Admin");
        clickAndWait("link=Manage Provisioning Requisitions");
        selenium.type("css=form[name=takeAction] > input[name=groupName]", "test");
        clickAndWait("css=input[type=submit]");
        clickAndWait("//input[@value='Synchronize']");
    }

    public void addNode() throws Exception {
        frontPage();
        clickAndWait("link=Add Node");
        waitForText("Category:");
        assertEquals("Provision", selenium.getValue("css=input[type=submit]"));
        waitForElement("css=input[type=reset]");
        waitForText("Enable Password:");
        waitForText("Node Quick-Add");
        waitForText("CLI Authentication Parameters (optional)");
        waitForText("SNMP Parameters (optional)");
        waitForText("Surveillance Category Memberships (optional)");
        waitForText("Basic Attributes (required)");
        selenium.type("//input[@name='ipAddress']", "::1");
        selenium.type("//input[@name='nodeLabel']", "AddNodePageTest");
        selenium.click("//input[@type='submit']");
        waitForPageToLoad();
        waitForText("Your node has been added to the test requisition");
    }

    public void deleteProvisioningGroup() throws Exception {
        frontPage();
        clickAndWait("link=Admin");
        clickAndWait("link=Manage Provisioning Requisitions");
        // node has been created in requisition, needs sync to database
        waitForText("1 nodes defined");
        waitForText("0 nodes in database");
        clickAndWait("//input[@value='Synchronize']");

        // refresh until the node has been added to the database
        waitForElementRefresh("//input[@value='Delete Nodes']");
        waitForText("1 nodes in database");

        // then delete the nodes from the requisition
        selenium.chooseOkOnNextConfirmation();
        selenium.click("//input[@value='Delete Nodes']");
        selenium.getConfirmation();
        waitForPageToLoad();

        // now that we have deleted the nodes from the requisition, we should still
        // see it still in the DB, but not in the requisition
        waitForText("0 nodes defined");
        waitForText("1 nodes in database");

        // so then we sync it to get rid of the DB node
        waitForElement("//input[@value='Synchronize']");
        selenium.click("//input[@value='Synchronize']");

        // now we wait for the sync to delete the node; once it has
        // we'll have the 'Delete Requisition' button and we can delete it
        waitForElementRefresh("//input[@value='Delete Requisition']");
        clickAndWait("//input[@value='Delete Requisition']");
    }

}
