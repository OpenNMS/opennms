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

package org.opennms.smoketest.graph;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

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
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.topo.GraphMLTopologyIT;
import org.opennms.smoketest.utils.KarafShell;

import com.google.common.base.Charsets;

/**
 * Tests the 'GraphML' Topology Provider
 *
 * @author mvrueden
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GraphMLGraphProviderIT extends OpenNMSSeleniumIT {

    private static final String LABEL = "GraphML Topology Provider (test-graph)";

    private final String URL = stack.opennms().getBaseUrlExternal().toString() + "opennms/rest/graphml/test-graph";

    private KarafShell karafShell;

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Install features
        karafShell = new KarafShell(stack.opennms().getSshAddress());
        karafShell.runCommand("feature:install opennms-graphs");
        karafShell.runCommand("feature:install opennms-graph-provider-graphml");
        karafShell.runCommand("feature:list -i", output -> output.contains("opennms-graphs") && output.contains("opennms-graph-provider-graphml"));

        // Sometimes a previous run did not clean up properly, so we do that before we import a graph
        if (existsGraph()) {
            deleteGraph();
        }
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        if (existsGraph()) {
            deleteGraph();
        }
    }

    @Test
    public void canExposeGraphML() throws IOException, InterruptedException {
        karafShell.runCommand("graph:list", output -> output.contains("1 registered Graph Container(s)"));

        importGraph();

        karafShell.runCommand("graph:list", output -> output.contains("2 registered Graph Container(s)")
                && output.contains("3 registered Graph(s)")
                && output.contains(LABEL));
    }

    // TODO MVR same code as in GraphMLTopologyIT
    private boolean existsGraph() throws IOException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpGet httpGet = new HttpGet(URL);
            httpGet.addHeader("Accept", "application/xml");
            CloseableHttpResponse response = client.execute(httpGet);
            return response.getStatusLine().getStatusCode() == 200;
        }
    }

    // TODO MVR same code as in GraphMLTopologyIT
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

    // TODO MVR same code as in GraphMLTopologyIT
    private void deleteGraph() throws IOException, InterruptedException {
        try (HttpClientWrapper client = createClientWrapper()) {
            HttpDelete httpDelete = new HttpDelete(URL);
            CloseableHttpResponse response = client.execute(httpDelete);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
        // We wait to give the GraphMLMetaTopologyFactory the chance to clean up afterwards
        Thread.sleep(20000);
    }

    // TODO MVR same code as in GraphMLTopologyIT
    private static HttpClientWrapper createClientWrapper() {
        HttpClientWrapper wrapper = HttpClientWrapper.create();
        wrapper.addBasicCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
        return wrapper;
    }

}
