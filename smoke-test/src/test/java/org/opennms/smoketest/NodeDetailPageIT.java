/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.io.StringReader;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class NodeDetailPageIT extends OpenNMSSeleniumTestCase {

    public static class NodeDetailPage {

        private final OpenNMSSeleniumTestCase testCase;
        private final String url;

        public NodeDetailPage(OpenNMSSeleniumTestCase testCase, int nodeId) {
            this.testCase = Objects.requireNonNull(testCase);
            this.url = Objects.requireNonNull(testCase.getBaseUrl()) + "opennms/element/node.jsp?node=" + nodeId;
        }

        public NodeDetailPage open() {
            testCase.m_driver.get(url);
            return this;
        }

        public TopologyIT.TopologyUIPage viewInTopology() {
            testCase.m_driver.findElement(By.linkText("View in Topology")).click();
            final TopologyIT.TopologyUIPage topologyUIPage = new TopologyIT.TopologyUIPage(testCase, testCase.getBaseUrl());
            topologyUIPage.open();
            return topologyUIPage;
        }
    }

    // See NMS-8872
    @Test
    public void canUseViewInTopology() throws Exception {
        try {
            // Create a node, we can actually reference
            deleteTestRequisition();
            final String node = "<node type=\"A\" label=\"TestMachine\" foreignSource=\"" + REQUISITION_NAME + "\" foreignId=\"1\">" +
                    "<labelSource>H</labelSource>" +
                    "<sysContact>The Owner</sysContact>" +
                    "<sysDescription>" +
                    "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                    "</sysDescription>" +
                    "<sysLocation>DevJam</sysLocation>" +
                    "<sysName>TestMachine</sysName>" +
                    "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                    "</node>";
            sendPost("/rest/nodes", node, 201);

            // Get the node Id
            final HttpGet httpGet = new HttpGet(getBaseUrl() + "opennms/rest/nodes?label=TestMachine&foreignSource=SeleniumTestGroup");
            final ResponseData responseData = getRequest(httpGet);
            Assert.assertNotNull(responseData.getResponseText());
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(new InputSource(new StringReader(responseData.getResponseText())));
            final Element rootElement = document.getDocumentElement();
            final NodeList children = rootElement.getChildNodes();
            Assert.assertEquals(1, children.getLength());
            final int nodeId = Integer.valueOf(children.item(0).getAttributes().getNamedItem("id").getNodeValue());

            // Navigate to the node details page
            NodeDetailPage nodeDetailPage = new NodeDetailPage(this, nodeId);
            nodeDetailPage.open();

            // View in Topology and verify it works
            TopologyIT.TopologyUIPage topologyUIPage = nodeDetailPage.viewInTopology();
            Assert.assertEquals(1, topologyUIPage.getFocusedVertices().size());
            Assert.assertEquals("TestMachine", topologyUIPage.getFocusedVertices().get(0).getLabel());
        } finally {
            deleteTestRequisition();
        }
    }
}
