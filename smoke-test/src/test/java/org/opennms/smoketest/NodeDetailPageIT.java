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
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
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

            //force loading topology
            final EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_TOPOLOGY_UEI, getClass().getSimpleName());
            eventBuilder.setTime(new Date());
            eventBuilder.setParam(EventConstants.PARAM_TOPOLOGY_NAMESPACE, "all");
            sendPost("/rest/events", JaxbUtils.marshal(eventBuilder.getEvent()), 202);
            Thread.sleep(5000); // Wait to allow the event to be processed

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

    private void createNodeWithInterfaces(final String nodeLabel, final int numberOfInterfaces) throws Exception {
        final String foreignSource = "test";
        final String foreignId = nodeLabel;
        final String foreignSourceAndForeignId = foreignSource + ":" + foreignId;

        final String node = "<node type=\"A\" label=\""+nodeLabel+"\" foreignSource=\""+foreignSource+"\" foreignId=\""+foreignId+"\">" +
                "<labelSource>H</labelSource><sysContact>Ulf</sysContact><sysDescription>Test System</sysDescription><sysLocation>Fulda</sysLocation>"+
                "<sysName>"+nodeLabel+"</sysName><sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId><createTime>2019-03-11T07:12:46.421-04:00</createTime>" +
                "<lastCapsdPoll>2019-03-11T07:12:46.421-04:00</lastCapsdPoll></node>";

        sendPost("rest/nodes", node, 201);

        for (int i = 0; i < numberOfInterfaces; i++) {
            final String ipAddress = "192.168.1." + (i + 1);
            final String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                    "<ipAddress>"+ipAddress+"</ipAddress><hostName>"+nodeLabel+".local</hostName>" +
                    "</ipInterface>";

            sendPost("rest/nodes/"+foreignSourceAndForeignId+"/ipinterfaces", ipInterface, 201);
        }
    }

    @Test
    public void checkMaximumNumberOfInterfaces() throws Exception {
        try {
            createNodeWithInterfaces("nodeWith10Interfaces", 10);
            createNodeWithInterfaces("nodeWith11Interfaces", 11);
            
            m_driver.get(getBaseUrl()+"opennms/element/node.jsp?node=test:nodeWith10Interfaces");

            setImplicitWait(1, TimeUnit.SECONDS);

            Assert.assertEquals(1, m_driver.findElements(By.id("availability-box")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.1")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.2")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.3")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.4")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.5")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.6")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.7")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.8")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.9")).size());
            Assert.assertEquals(1, m_driver.findElements(By.linkText("192.168.1.10")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.11")).size());

            m_driver.get(getBaseUrl()+"opennms/element/node.jsp?node=test:nodeWith11Interfaces");

            Assert.assertEquals(0, m_driver.findElements(By.id("availability-box")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.1")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.2")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.3")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.4")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.5")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.6")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.7")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.8")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.9")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.10")).size());
            Assert.assertEquals(0, m_driver.findElements(By.linkText("192.168.1.11")).size());
        } finally {
            sendDelete("rest/nodes/test:nodeWith10Interfaces", 202);
            sendDelete("rest/nodes/test:nodeWith11Interfaces", 202);
        }
    }
}
