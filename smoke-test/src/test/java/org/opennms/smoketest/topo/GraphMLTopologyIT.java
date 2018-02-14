/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.topo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.link.Layout;
import org.opennms.features.topology.link.TopologyProvider;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.TopologyIT;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

/**
 * Tests the 'GraphML' Topology Provider
 *
 * @author mvrueden
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GraphMLTopologyIT extends OpenNMSSeleniumTestCase {

    private final String URL = getBaseUrl() + "opennms/rest/graphml/test-graph";

    private static final String LABEL = "GraphML Topology Provider (test-graph)";

    private TopologyIT.TopologyUIPage topologyUIPage;

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Sometimes a previous run did not clean up properly, so we do that before we
        // import a graph
        if (existsGraph()) {
            deleteGraph();
        }

        // Generating dummy nodes for the verifyCanFilterByCategory test method
        this.createDummyNodes();

        importGraph();
        topologyUIPage = new TopologyIT.TopologyUIPage(this, getBaseUrl());
        topologyUIPage.open();
        // Select EnLinkd, otherwise the "GraphML Topology Provider (test-graph)" is always pre-selected due to history restoration
        topologyUIPage.selectTopologyProvider(TopologyProvider.ENLINKD);
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        deleteGraph();
    }

    @Test
    public void canUseTopology() throws IOException {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.defaultFocus();

        List<TopologyIT.FocusedVertex> focusedVertices = topologyUIPage.getFocusedVertices();
        assertEquals(4, focusedVertices.size());
        assertEquals(4, topologyUIPage.getVisibleVertices().size());
        assertEquals(1, topologyUIPage.getSzl());
        focusedVertices.sort(Comparator.comparing(TopologyIT.FocusedVertex::getNamespace).thenComparing(TopologyIT.FocusedVertex::getLabel));
        assertEquals(
                Lists.newArrayList(
                        focusVertex(topologyUIPage, "Acme:regions:", "East Region"),
                        focusVertex(topologyUIPage, "Acme:regions:", "North Region"),
                        focusVertex(topologyUIPage, "Acme:regions:", "South Region"),
                        focusVertex(topologyUIPage, "Acme:regions:", "West Region")
                ), focusedVertices);

        // Search for and select a region
        final String regionName = "South";
        TopologyIT.TopologyUISearchResults searchResult = topologyUIPage.search(regionName);
        assertEquals(5, searchResult.countItemsThatContain(regionName));
        searchResult.selectItemThatContains("South Region");

        // Focus should not have changed
        assertEquals(4, focusedVertices.size());
        assertEquals(4, topologyUIPage.getVisibleVertices().size());

        // Verify that the layout is the D3 Layout as this layer does not provide a preferredLayout
        assertEquals(Layout.D3, topologyUIPage.getSelectedLayout());

        // Switch Layer
        topologyUIPage.selectLayer("Markets");
        assertEquals(0, topologyUIPage.getSzl());
        assertEquals(1, topologyUIPage.getFocusedVertices().size());
        assertEquals("North 4", topologyUIPage.getFocusedVertices().get(0).getLabel());
    }

    @Test
    public void verifySwitchesLayerOnSearchProperly() {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        TopologyIT.TopologyUISearchResults searchResult = topologyUIPage.search("South");
        assertEquals(5, searchResult.countItemsThatContain("South"));
        searchResult.selectItemThatContains("South 3");
        assertEquals(1, topologyUIPage.getVisibleVertices().size());
        assertEquals(1, topologyUIPage.getFocusedVertices().size());
        assertEquals("South 3", topologyUIPage.getFocusedVertices().get(0).getLabel());
        assertEquals("South 3", topologyUIPage.getVisibleVertices().get(0).getLabel());
    }

    @Test
    public void verifyNavigateToAndBreadcrumbs() {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.findVertex("East Region").contextMenu().click("Navigate To", "Markets (East Region)");

        final ArrayList<TopologyIT.FocusedVertex> marketsVertcies = Lists.newArrayList(
                focusVertex(topologyUIPage, "Acme:markets:", "East 1"),
                focusVertex(topologyUIPage, "Acme:markets:", "East 2"),
                focusVertex(topologyUIPage, "Acme:markets:", "East 3"),
                focusVertex(topologyUIPage, "Acme:markets:", "East 4"));
        assertEquals(marketsVertcies, topologyUIPage.getFocusedVertices());
        assertEquals("Markets", topologyUIPage.getSelectedLayer());
        assertEquals(Lists.newArrayList("regions", "East Region"), topologyUIPage.getBreadcrumbs().getLabels());

        // Click on last element should add all vertices to focus
        topologyUIPage.getFocusedVertices().get(0).removeFromFocus(); // remove an element from focus
        topologyUIPage.getBreadcrumbs().click("East Region");
        assertEquals(marketsVertcies, topologyUIPage.getFocusedVertices());

        // Click on 1st element, should switch layer and add "child" to focus
        topologyUIPage.getBreadcrumbs().click("regions");
        assertEquals(Lists.newArrayList("regions"), topologyUIPage.getBreadcrumbs().getLabels());
        assertEquals(Lists.newArrayList(focusVertex(topologyUIPage, "Acme:regions:", "East Region")), topologyUIPage.getFocusedVertices());

        // Click on last element should add all elements to focus
        topologyUIPage.getBreadcrumbs().click("regions");
        List<TopologyIT.FocusedVertex> focusedVertices = topologyUIPage.getFocusedVertices();
        focusedVertices.sort(Comparator.comparing(TopologyIT.FocusedVertex::getNamespace).thenComparing(TopologyIT.FocusedVertex::getLabel));
        assertEquals(Lists.newArrayList(
                focusVertex(topologyUIPage, "Acme:regions:", "East Region"),
                focusVertex(topologyUIPage, "Acme:regions:", "North Region"),
                focusVertex(topologyUIPage, "Acme:regions:", "South Region"),
                focusVertex(topologyUIPage, "Acme:regions:", "West Region")
        ), focusedVertices); // all elements should be focused
    }

    @Test
    public void verifySaveLayoutButton() {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        assertEquals(false, topologyUIPage.getSaveLayoutButton().isEnabled()); // it should be disabled

        topologyUIPage.selectLayout(Layout.MANUAL);
        assertEquals(true, topologyUIPage.getSaveLayoutButton().isEnabled()); // now it should be enabled
        topologyUIPage.getSaveLayoutButton().click();
        assertEquals(false, topologyUIPage.getSaveLayoutButton().isEnabled()); // it should be disabled after save
    }

    @Test
    /**
     * This method tests whether the GraphMLTopologyProvider can work with categories - searching, collapsing and expanding
     */
    public void verifyCanFilterByCategory() throws IOException, InterruptedException {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.selectLayer("Markets");
        topologyUIPage.setSzl(0);
        topologyUIPage.clearFocus();

        // Search for the first category
        topologyUIPage.search("Routers").selectItemThatContains("Routers");
        Assert.assertNotNull(topologyUIPage.getVisibleVertices());
        Assert.assertEquals(2, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("North 2", topologyUIPage.getVisibleVertices().get(0).getLabel());
        Assert.assertEquals("North 3", topologyUIPage.getVisibleVertices().get(1).getLabel());

        // Collapse and verify that collapsing works and that the category is visible while the vertex - not
        topologyUIPage.getFocusedVertices().get(0).collapse();
        Assert.assertEquals(1, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("Routers", topologyUIPage.getVisibleVertices().get(0).getLabel());

        // Search for the second category
        //TODO Theoretically it should display 2 vertices - one for each category. But it does not due to a bug (see issue NMS-9423)
        topologyUIPage.search("Servers").selectItemThatContains("Servers");
        Assert.assertNotNull(topologyUIPage.getVisibleVertices());
        Assert.assertEquals(1, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("Routers", topologyUIPage.getVisibleVertices().get(0).getLabel());

        // Expand and verify that vertices are visible again (and not duplicated)
        topologyUIPage.getFocusedVertices().get(0).expand();
        Assert.assertEquals(2, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("North 2", topologyUIPage.getVisibleVertices().get(0).getLabel());
        Assert.assertEquals("North 3", topologyUIPage.getVisibleVertices().get(1).getLabel());

        // Collapse all and verify that vertices are not visible and that both categories are visible
        for (TopologyIT.FocusedVertex vertex : topologyUIPage.getFocusedVertices()) {
            vertex.collapse();
        }
        Assert.assertEquals(2, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("Routers", topologyUIPage.getVisibleVertices().get(0).getLabel());
        Assert.assertEquals("Servers", topologyUIPage.getVisibleVertices().get(1).getLabel());
    }

    /**
     * Creates and publishes a requisition with 2 dummy nodes with predefined parameters
     */
    private void createDummyNodes() throws IOException, InterruptedException {

        // First node has foreign ID "node1", label - "North 2" and category "Routers"
        // Second node has foreign ID "node2", label - "North 3" and categories "Routers" and "Servers"

        final String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        createForeignSource(REQUISITION_NAME, foreignSourceXML);

        String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">" +
                                "   <node foreign-id=\"node1\" node-label=\"North 2\">" +
                                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                                "           <monitored-service service-name=\"ICMP\"/>" +
                                "       </interface>" +
                                "       <category name=\"Routers\"/>" +
                                "   </node>" +
                                "   <node foreign-id=\"node2\" node-label=\"North 3\">" +
                                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                                "           <monitored-service service-name=\"ICMP\"/>" +
                                "       </interface>" +
                                "       <category name=\"Routers\"/>" +
                                "       <category name=\"Servers\"/>" +
                                "   </node>" +
                                "</model-import>";
        createRequisition(REQUISITION_NAME, requisitionXML, 2);
        // Send an event to force reload of topology
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_TOPOLOGY_UEI, getClass().getSimpleName());
        builder.setTime(new Date());
        builder.setParam(EventConstants.PARAM_TOPOLOGY_NAMESPACE, "all");
        sendPost("/rest/events", JaxbUtils.marshal(builder.getEvent()), 202);
        Thread.sleep(5000); // Wait to allow the event to be processed
    }

    private boolean existsGraph() throws IOException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpGet httpGet = new HttpGet(URL);
            httpGet.addHeader("Accept", "application/xml");
            CloseableHttpResponse response = client.execute(httpGet);
            return response.getStatusLine().getStatusCode() == 200;
        }
    }

    private void importGraph() throws IOException, InterruptedException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setHeader("Accept", "application/xml");
            httpPost.setHeader("Content-Type", "application/xml");
            httpPost.setEntity(new StringEntity(IOUtils.toString(GraphMLTopologyIT.class.getResourceAsStream("/topology/graphml/test-topology.xml"), Charsets.UTF_8)));
            CloseableHttpResponse response = client.execute(httpPost);
            assertEquals(201, response.getStatusLine().getStatusCode());
        }
        // We wait to give the GraphMLMetaTopologyFactory the chance to initialize the new Topology
        Thread.sleep(20000);
    }

    private void deleteGraph() throws IOException, InterruptedException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpDelete httpDelete = new HttpDelete(URL);
            CloseableHttpResponse response = client.execute(httpDelete);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
        // We wait to give the GraphMLMetaTopologyFactory the chance to clean up afterwards
        Thread.sleep(20000);
    }

    private static HttpClientWrapper createClientWrapper() {
        HttpClientWrapper wrapper = HttpClientWrapper.create();
        wrapper.addBasicCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
        return wrapper;
    }

    private static TopologyIT.FocusedVertex focusVertex(TopologyIT.TopologyUIPage topologyUIPage, String namespace, String label) {
        return  new TopologyIT.FocusedVertex(topologyUIPage, namespace, label);
    }
}
