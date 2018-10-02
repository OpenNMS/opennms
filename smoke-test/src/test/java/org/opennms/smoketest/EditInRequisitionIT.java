/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditInRequisitionIT extends OpenNMSSeleniumTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(EditInRequisitionIT.class);

    @Before
    public void before() throws Exception {
        createRequisition();
        createNode();
        m_driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        LOG.debug("Timeout for element lookup decreased to two seconds");
    }

    @After
    public void after() throws Exception {
        deleteRequisition();
        deleteNode();
    }

    @Test
    public void testIfNotRequisition() throws Exception {
        LOG.debug("Check whether the 'Edit in Requisition' link appear for nodes in database without requisition...");

        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node=my-foreign-source:my-foreign-id");

        final List<WebElement> webElement = m_driver.findElements(By.linkText("Edit in Requisition"));
        Assert.assertTrue("Link 'Edit in Requisition' was found in page!", webElement.isEmpty());
    }

    @Test
    public void testIfDeployed() throws Exception {
        LOG.debug("Check whether the 'Edit in Requisition' link appear for nodes in database and requisition...");

        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node=" + OpenNMSSeleniumTestCase.REQUISITION_NAME + ":my-foreign-id-1");
        m_driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

        final WebElement webElement = m_driver.findElement(By.linkText("Edit in Requisition"));
        Assert.assertNotNull("Link 'Edit in Requisition' not found in page!", webElement);
        webElement.click();

        Assert.assertEquals("Node my-node-1 at " + OpenNMSSeleniumTestCase.REQUISITION_NAME,
                m_driver.findElement(By.cssSelector("#content > div.ng-scope > div > form > div:nth-child(1) > div:nth-child(1) > h4")).getText());
    }

    @Test
    public void testIfPending() throws Exception {
        LOG.debug("Check whether the 'Edit in Requisition' link appear for nodes in database that are not in a requisition anymore...");

        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node=" + OpenNMSSeleniumTestCase.REQUISITION_NAME + ":my-foreign-id-2");

        final List<WebElement> webElement = m_driver.findElements(By.linkText("Edit in Requisition"));
        Assert.assertTrue("Link 'Edit in Requisition' was found in page!", webElement.isEmpty());
    }

    private void deleteRequisition() throws Exception {
        deleteTestRequisition();
        LOG.debug("Deleted requisition '" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "'");
    }

    private void createNode() throws Exception {
        final String node = "<node type=\"A\" label=\"my-node\" foreignSource=\"my-foreign-source\" foreignId=\"my-foreign-id\">" +
                "<labelSource>H</labelSource>" +
                "<sysContact>Me</sysContact>" +
                "<sysDescription>WOPR</sysDescription>" +
                "<sysLocation>Fulda</sysLocation>" +
                "<sysName>my-node</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "<createTime>2018-09-25T15:24:46.421-04:00</createTime>" +
                "<lastCapsdPoll>2018-09-25T15:24:46.421-04:00</lastCapsdPoll>" +
                "</node>";

        sendPost("rest/nodes", node, 201);
        LOG.debug("Created node 'my-foreign-source/my-foreign-id'");
    }

    private void createRequisition() throws Exception {
        // Create foreign source.
        final String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        createForeignSource(OpenNMSSeleniumTestCase.REQUISITION_NAME, foreignSourceXML);

        // Create two nodes...
        final String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">" +
                "   <node foreign-id=\"my-foreign-id-1\" node-label=\"my-node-1\">" +
                "       <interface ip-addr=\"::2\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"AAA\"/>" +
                "       </interface>" +
                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"BBB\"/>" +
                "       </interface>" +
                "   </node>" +
                "   <node foreign-id=\"my-foreign-id-2\" node-label=\"my-node-2\">" +
                "       <interface ip-addr=\"::1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"CCC\"/>" +
                "       </interface>" +
                "       <interface ip-addr=\"127.0.0.2\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"DDD\"/>" +
                "       </interface>" +
                "   </node>" +
                "</model-import>";

        // ...and add them to the requisition.
        createRequisition(OpenNMSSeleniumTestCase.REQUISITION_NAME, requisitionXML, 2);

        // Now, delete one node from requisition...
        sendDelete("rest/requisitions/" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "/nodes/my-foreign-id-2");

        // ...and assure that 'my-foreign-id-2' is in database but not in requisition anymore.
        with().pollInterval(1, SECONDS).await().atMost(30, SECONDS).until(() -> (getNodesInRequisition(OpenNMSSeleniumTestCase.REQUISITION_NAME) == 1));
        LOG.debug("Created requisition '" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "'");
    }

    private void deleteNode() throws Exception {
        sendDelete("rest/nodes/my-foreign-source:my-foreign-id", 202);
        LOG.debug("Deleted node 'my-foreign-source/my-foreign-id'");
    }
}
