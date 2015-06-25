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

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NodeListPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        createNode("node1");
        createNode("node2");
        nodePage();
    }

    @After
    public void tearDown() throws Exception {
        deleteNode("node1");
        deleteNode("node2");
    }

    private void createNode(final String foreignId) throws Exception {
        final String node = "<node type=\"A\" label=\"TestMachine" + foreignId + "\" foreignSource=\""+ REQUISITION_NAME +"\" foreignId=\"" + foreignId + "\">" +
        "<labelSource>H</labelSource>" +
        "<sysContact>The Owner</sysContact>" +
        "<sysDescription>" +
        "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
        "</sysDescription>" +
        "<sysLocation>DevJam</sysLocation>" +
        "<sysName>TestMachine" + foreignId + "</sysName>" +
        "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
        "</node>";
        sendPost("/rest/nodes", node);
    }

    private void deleteNode(final String foreignId) throws Exception {
        sendDelete("/rest/nodes/" + REQUISITION_NAME + ":" + foreignId);
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//h3//span[text()='Nodes']");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("Show interfaces").click();
        findElementByLink("Hide interfaces");
    }
}
