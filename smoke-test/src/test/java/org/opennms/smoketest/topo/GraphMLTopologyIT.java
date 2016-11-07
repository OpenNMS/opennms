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

package org.opennms.smoketest.topo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.core.web.HttpClientWrapper;
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
        importGraph();
        topologyUIPage = new TopologyIT.TopologyUIPage(this, getBaseUrl());
        topologyUIPage.open();
        // Select EnLinkd, otherwise the "GraphML Topology Provider (test-graph)" is always pre-selected due to history restoration
        topologyUIPage.selectTopologyProvider(TopologyIT.TopologyProvider.ENLINKD);
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
        assertEquals(TopologyIT.Layout.D3, topologyUIPage.getSelectedLayout());

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

        topologyUIPage.selectLayout(TopologyIT.Layout.MANUAL);
        assertEquals(true, topologyUIPage.getSaveLayoutButton().isEnabled()); // now it should be enabled
        topologyUIPage.getSaveLayoutButton().click();
        assertEquals(false, topologyUIPage.getSaveLayoutButton().isEnabled()); // it should be disabled after save
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
