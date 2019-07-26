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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.smoketest.selenium.ResponseData;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class NodeDetailPageIT extends OpenNMSSeleniumIT {

    public static class NodeDetailPage {

        private final OpenNMSSeleniumIT testCase;
        private final String url;

        public NodeDetailPage(OpenNMSSeleniumIT testCase, int nodeId) {
            this.testCase = Objects.requireNonNull(testCase);
            this.url = Objects.requireNonNull(testCase.getBaseUrlInternal()) + "opennms/element/node.jsp?node=" + nodeId;
        }

        public NodeDetailPage open() {
            testCase.getDriver().get(url);
            return this;
        }

        public TopologyIT.TopologyUIPage viewInTopology() {
            testCase.getDriver().findElement(By.linkText("View in Topology")).click();
            final TopologyIT.TopologyUIPage topologyUIPage = new TopologyIT.TopologyUIPage(testCase, testCase.getBaseUrlInternal());
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
            final HttpGet httpGet = new HttpGet(getBaseUrlExternal() + "opennms/rest/nodes?label=TestMachine&foreignSource=SeleniumTestGroup");
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
            eventBuilder.setParam(EventConstants.PARAM_TOPOLOGY_NAMESPACE, "all");
            stack.opennms().getRestClient().sendEvent(eventBuilder.getEvent());
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
            
            getDriver().get(getBaseUrlInternal()+"opennms/element/node.jsp?node=test:nodeWith10Interfaces");

            setImplicitWait(1, SECONDS);

            Assert.assertEquals(1, driver.findElements(By.id("availability-box")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.1")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.2")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.3")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.4")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.5")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.6")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.7")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.8")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.9")).size());
            Assert.assertEquals(1, driver.findElements(By.linkText("192.168.1.10")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.11")).size());

            driver.get(getBaseUrlInternal()+"opennms/element/node.jsp?node=test:nodeWith11Interfaces");

            Assert.assertEquals(0, driver.findElements(By.id("availability-box")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.1")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.2")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.3")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.4")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.5")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.6")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.7")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.8")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.9")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.10")).size());
            Assert.assertEquals(0, driver.findElements(By.linkText("192.168.1.11")).size());
        } finally {
            sendDelete("rest/nodes/test:nodeWith10Interfaces", 202);
            sendDelete("rest/nodes/test:nodeWith11Interfaces", 202);
        }
    }

    @Test
    public void verifyTimeline() throws Exception {
        try {
            final String node = "<node type=\"A\" label=\"myNode\" foreignSource=\"smoketests\" foreignId=\"nodeForeignId\">" +
                    "<labelSource>H</labelSource><sysContact>Ulf</sysContact><sysDescription>Test System</sysDescription><sysLocation>Fulda</sysLocation>" +
                    "<sysName>myNode</sysName><sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId><createTime>2019-03-11T07:12:46.421-04:00</createTime>" +
                    "<lastCapsdPoll>2019-03-11T07:12:46.421-04:00</lastCapsdPoll></node>";

            sendPost("rest/nodes", node, 201);

            final String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\"><ipAddress>192.168.10.254</ipAddress><hostName>myNode.local</hostName></ipInterface>";

            sendPost("rest/nodes/smoketests:nodeForeignId/ipinterfaces", ipInterface, 201);

            final String service = "<service status=\"A\"><applications/><serviceType id=\"1\"><name>ICMP</name></serviceType></service>";

            sendPost("rest/nodes/smoketests:nodeForeignId/ipinterfaces/192.168.10.254/services", service, 201);

            driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=smoketests:nodeForeignId");

            await().atMost(5, SECONDS)
                    .pollInterval(1, SECONDS)
                    .until(() -> {
                        final List<WebElement> timelineImages = getDriver().findElements(By.tagName("img")).stream().filter(i -> i.getAttribute("src").contains("timeline")).collect(Collectors.toList());
                        if (timelineImages.size() >= 2) {
                            for (final WebElement timelineImage : timelineImages) {
                                final Object result = ((JavascriptExecutor) driver).executeScript("return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0", timelineImage);
                                if (!(result instanceof Boolean) || !((Boolean) result).booleanValue()) {
                                    return false;
                                }
                            }
                            return true;
                        } else {
                            return false;
                        }
                    });
        } finally {
            sendDelete("rest/nodes/smoketests:nodeForeignId", 202);
        }
    }

    // See NMS-10679
    @Test
    public void verifyNodeNotFoundMessageIsShown() {
        final String NODE_NOT_FOUND = "Node Not Found";
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=12345");
        pageContainsText(NODE_NOT_FOUND);

        final String NODE_ID_NOT_FOUND = "Node ID Not Found";
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=abc");
        pageContainsText(NODE_ID_NOT_FOUND);
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=ab:cd");
        pageContainsText(NODE_ID_NOT_FOUND);
    }
}
