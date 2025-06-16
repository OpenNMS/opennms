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