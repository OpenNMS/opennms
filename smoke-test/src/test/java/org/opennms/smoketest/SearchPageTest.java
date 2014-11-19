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
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clickAndWait("link=Search");
    }

    @Test
    public void a_testAllTextIsPresent() throws Exception {
        waitForText("Search for Nodes");
        waitForText("Search Asset Information");
        waitForText("Search Options");
        waitForText("MAC Address");
    }

    @Test
    public void b_testAllLinksArePresent() throws InterruptedException {
        waitForElement("link=All nodes");
        waitForElement("link=All nodes and their interfaces");
        waitForElement("link=All nodes with asset info");
    }

    @Test 
    public void c_testAllFormsArePresent() throws InterruptedException {
        waitForElement("css=input[type=submit]");
        assertEquals("Search", selenium.getValue("css=input[type=submit]"));
    }

    @Test
    public void d_testAllLinks() throws InterruptedException {
        clickAndWait("link=All nodes");
        waitForText("Nodes", LOAD_TIMEOUT);
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=All nodes and their interfaces");
        waitForText("Nodes and their interfaces");
        waitForElement("link=Hide interfaces");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=All nodes with asset info");
        waitForText("Assets");
    }

}
