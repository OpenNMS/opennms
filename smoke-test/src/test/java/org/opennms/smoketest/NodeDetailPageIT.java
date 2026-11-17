/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.smoketest.selenium.ResponseData;
import org.opennms.smoketest.ui.NodeDetailsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The Vue Node Details page. The legacy element/node.jsp is now a redirect to it (NMS-20441).
 */
public class NodeDetailPageIT extends OpenNMSSeleniumIT {

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

            // Navigate to the node details page, and view the node in the topology
            new NodeDetailsPage(this).open(nodeId).clickLink("Graphs", "Topology Map");
            final TopologyIT.TopologyUIPage topologyUIPage = new TopologyIT.TopologyUIPage(this, getBaseUrlInternal());
            topologyUIPage.open();

            // Verify it works
            Assert.assertEquals(1, topologyUIPage.getFocusedVertices().size());
            Assert.assertEquals("TestMachine", topologyUIPage.getFocusedVertices().get(0).getLabel());
        } finally {
            deleteTestRequisition();
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

            final String service = "<service status=\"A\"><serviceType id=\"1\"><name>ICMP</name></serviceType></service>";

            sendPost("rest/nodes/smoketests:nodeForeignId/ipinterfaces/192.168.10.254/services", service, 201);

            new NodeDetailsPage(this).open("smoketests:nodeForeignId");

            // The Availability panel draws its timeline in the browser: one row per service,
            // under its interface.
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[@data-test='availability-timeline']//tr[@data-test='timeline-service-row']//a[text()='ICMP']")));
            Assert.assertEquals("The availability panel reported a load failure.",
                    0, getDriver().findElements(By.cssSelector("[data-test='availability-error']")).size());
        } finally {
            sendDelete("rest/nodes/smoketests:nodeForeignId", 202);
        }
    }

    // See NMS-10679. Goes through the legacy URL, which stored links still use.
    @Test
    public void verifyNodeNotFoundMessageIsShown() {
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=12345");
        wait.until(pageContainsText("Node Not Found"));

        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=abc");
        wait.until(pageContainsText("Invalid Node ID"));

        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=ab:cd");
        wait.until(pageContainsText("Node Not Found"));
    }
}
