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
package org.opennms.smoketest.graph;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.graphml.GraphmlDocument;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.RestClient;

/**
 * Tests the 'GraphML' Topology Provider
 *
 * @author mvrueden
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class GraphMLGraphProviderIT extends OpenNMSSeleniumIT {

    private static final String LABEL = "GraphML Topology Provider (test-graph)";

    private final RestClient restClient = stack.opennms().getRestClient();

    private final GraphmlDocument graphmlDocument = new GraphmlDocument("test-graph", "/topology/graphml/test-topology.xml");

    private KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Sometimes a previous run did not clean up properly,
        // so we do that before we import a graph
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
    public void canExposeGraphML() throws InterruptedException {
        karafShell.runCommand("opennms:graph-list", output -> output.contains("4 registered Graph Container(s)"));

        importGraph();

        karafShell.runCommand("opennms:graph-list -a", output -> output.contains("5 registered Graph Container(s)")
                && output.contains("6 registered Graph(s)")
                && output.contains(LABEL));
    }

    private boolean existsGraph() {
        return graphmlDocument.exists(restClient);
    }

    private void importGraph() throws InterruptedException {
        graphmlDocument.create(restClient);

        // We wait to give the GraphMLMetaTopologyFactory the chance to initialize the new Topology
        Thread.sleep(20000);
    }

    private void deleteGraph() throws InterruptedException {
        graphmlDocument.delete(restClient);

        // We wait to give the GraphMLMetaTopologyFactory the chance to clean up afterwards
        Thread.sleep(20000);
    }

}
