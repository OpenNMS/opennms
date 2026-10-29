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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class GraphMLGraphProviderIT extends OpenNMSSeleniumIT {

    private static final String LABEL = "GraphML Topology Provider (test-graph)";

    private static final Pattern CONTAINER_COUNT = Pattern.compile("(\\d+) registered Graph Container\\(s\\)");

    private static final Pattern GRAPH_COUNT = Pattern.compile("(\\d+) registered Graph\\(s\\)");

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
        // The number of built-in graph providers changes over time, so compare against the count before the import
        final AtomicInteger containers = new AtomicInteger(-1);
        final AtomicInteger graphs = new AtomicInteger(-1);
        karafShell.runCommand("opennms:graph-list -a", output -> {
            containers.set(count(output, CONTAINER_COUNT));
            graphs.set(count(output, GRAPH_COUNT));
            return containers.get() >= 0 && graphs.get() >= 0;
        });

        importGraph();

        karafShell.runCommand("opennms:graph-list -a", output -> count(output, CONTAINER_COUNT) == containers.get() + 1
                && count(output, GRAPH_COUNT) == graphs.get() + 1
                && output.contains(LABEL));
    }

    private static int count(final String output, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(output);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
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
