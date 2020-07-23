/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.enrichment;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.OnmsAssert;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

public class EnrichmentGraphBuilderTest {

    private static final String NAMESPACE = "dummy";

    private GenericGraph graph;

    @Before
    public void setUp() {
        graph = GenericGraph.builder()
                .namespace(NAMESPACE)
                .label("Dummy Graph")
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v1").build())
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v2").build())
                .addEdge(GenericEdge.builder().namespace(NAMESPACE).id("e1").target(NAMESPACE, "v1").source(NAMESPACE, "v2").build())
                .build();
    }

    @Test
    public void verifyEnrichment() {
        final EnrichmentGraphBuilder enrichmentGraphBuilder = new EnrichmentGraphBuilder(graph);
        enrichmentGraphBuilder.property("enriched", "graphValue");
        enrichmentGraphBuilder.property(graph.getVertex("v1"), "enriched", "vertexValue");
        enrichmentGraphBuilder.property(graph.getEdge("e1"), "enriched", "edgeValue");
        final GenericGraph enrichedGraph = enrichmentGraphBuilder.build();
        assertThat(enrichedGraph.getProperty("enriched"), is("graphValue"));
        assertThat(enrichedGraph.getVertex("v1").getProperty("enriched"), is("vertexValue"));
        assertThat(enrichedGraph.getEdge("e1").getProperty("enriched"), is("edgeValue"));
    }

    @Test
    public void verifyCannotModifyExistingProperties() {
        final EnrichmentGraphBuilder enrichmentGraphBuilder = new EnrichmentGraphBuilder(graph);
        OnmsAssert.assertThrowsException(IllegalArgumentException.class, () -> enrichmentGraphBuilder.property(GenericProperties.LABEL, "someValue"));
        OnmsAssert.assertThrowsException(IllegalArgumentException.class, () -> enrichmentGraphBuilder.property(graph.getVertex("v1"), GenericProperties.NAMESPACE, "someValue"));
        OnmsAssert.assertThrowsException(IllegalArgumentException.class, () -> enrichmentGraphBuilder.property(graph.getEdge("e1"), GenericProperties.NAMESPACE, "someValue"));
    }

}