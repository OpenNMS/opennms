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
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.TopologyIT;

import com.google.common.base.Charsets;

/**
 * Tests the 'GraphML' Topology Provider
 *
 * @author mvrueden
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GraphMLTopologyIT extends OpenNMSSeleniumTestCase {

    private static final String URL = BASE_URL + "opennms/rest/graphml/test-graph";

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
        topologyUIPage = new TopologyIT.TopologyUIPage(this);
        topologyUIPage.open();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        deleteGraph();
    }

    @Test
    public void canShowVertices() throws IOException {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.clearFocus();

        // Search for and select the first business service in our list
        final String regionName = "South";
        TopologyIT.TopologyUISearchResults searchResult = topologyUIPage.search(regionName);
        assertEquals(5, searchResult.countItemsThatContain(regionName));
        searchResult.selectItemThatContains(regionName);

        // We should have a single vertex in focus
        assertEquals(1, topologyUIPage.getFocusedVertices().size());

        // We should have one vertex visible
        List<TopologyIT.VisibleVertex> visibleVertices = topologyUIPage.getVisibleVertices();
        assertEquals(1, visibleVertices.size());

        // Verify that the layout is the D3 Layout as this layer does not provide a preferredLayout
        assertEquals(TopologyIT.Layout.D3, topologyUIPage.getSelectedLayout());
    }

    @Test
    public void canSwitchLayers() {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.clearFocus();
        topologyUIPage.selectLayer("Markets");
        assertEquals(1, topologyUIPage.getFocusedVertices().size());
        assertEquals("North 1", topologyUIPage.getFocusedVertices().get(0).getLabel());
    }

    private static boolean existsGraph() throws IOException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpGet httpGet = new HttpGet(URL);
            httpGet.addHeader("Accept", "application/xml");
            CloseableHttpResponse response = client.execute(httpGet);
            return response.getStatusLine().getStatusCode() == 200;
        }
    }

    private static void importGraph() throws IOException, InterruptedException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setHeader("Accept", "application/xml");
            httpPost.setHeader("Content-Type", "application/xml");
            httpPost.setEntity(new StringEntity(IOUtils.toString(GraphMLTopologyIT.class.getResourceAsStream("/topology/graphml/test-topology.xml"), Charsets.UTF_8)));
            CloseableHttpResponse response = client.execute(httpPost);
            Assert.assertEquals(201, response.getStatusLine().getStatusCode());
        }
        // We wait to give the GraphMLMetaTopologyFactory the chance to initialize the new Topology
        Thread.sleep(20000);
    }

    private static void deleteGraph() throws IOException, InterruptedException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpDelete httpDelete = new HttpDelete(URL);
            CloseableHttpResponse response = client.execute(httpDelete);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        }
        // We wait to give teh GraphMLMetaTopologyFactory the chance to clean up afterwards
        Thread.sleep(20000);
    }

    private static HttpClientWrapper createClientWrapper() {
        HttpClientWrapper wrapper = HttpClientWrapper.create();
        wrapper.addBasicCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
        return wrapper;
    }

}
