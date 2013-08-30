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

public class AddNodePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        clickAndWait("link=Add Node");
    }
    @Test
    public void setupProvisioningGroup() throws Exception {
        selenium.open("/opennms/admin/node/add.htm");
        clickAndWait("link=Admin");
        clickAndWait("link=Manage Provisioning Requisitions");
        selenium.type("css=form[name=takeAction] > input[name=groupName]", "test");
        clickAndWait("css=input[type=submit]");
        clickAndWait("//input[@value='Synchronize']");
    }
    @Test
    public void testAddNodePage() throws Exception {

        waitForText("Category:");
        assertEquals("Provision", selenium.getValue("css=input[type=submit]"));
        waitForElement("css=input[type=reset]");
        waitForText("Enable Password:");
        waitForText("Node Quick-Add");
        waitForText("CLI Authentication Parameters (optional)");
        waitForText("SNMP Parameters (optional)");
        waitForText("Surveillance Category Memberships (optional)");
        waitForText("Basic Attributes (required)");
    }

}
