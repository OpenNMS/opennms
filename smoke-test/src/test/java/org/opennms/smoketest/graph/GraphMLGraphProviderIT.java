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
