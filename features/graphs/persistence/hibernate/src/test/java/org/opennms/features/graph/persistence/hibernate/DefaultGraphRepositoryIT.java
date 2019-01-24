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

package org.opennms.features.graph.persistence.hibernate;

import java.util.Objects;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.graph.api.generic.GenericGraph;
import org.opennms.features.graph.api.generic.GenericGraphContainer;
import org.opennms.features.graph.api.generic.GenericProperties;
import org.opennms.features.graph.api.info.NodeInfo;
import org.opennms.features.graph.persistence.api.GraphRepository;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennsm.features.graphs.simple.SimpleEdge;
import org.opennsm.features.graphs.simple.SimpleGraph;
import org.opennsm.features.graphs.simple.SimpleGraphContainer;
import org.opennsm.features.graphs.simple.SimpleVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultGraphRepositoryIT {

    @Autowired
    private GraphRepository graphRepository;

    private static final String NAMESPACE = "dummy";
    private static final String CONTAINER_ID = "unique-id";

    @Test
    public void verifyCRUD() {
        /*
         * Create/Persist
         */
        final SimpleGraphContainer originalContainer = new SimpleGraphContainer(CONTAINER_ID);
        originalContainer.setDescription("Container for 'unique-id' graph");
        originalContainer.setLabel("I am soooo unique \\o/");

        // Create first graph
        final SimpleGraph graph1 = new SimpleGraph(NAMESPACE);
        graph1.setLabel("Dummy Graph");
        graph1.setDescription("I am not so unique, I may be replaced at any time :(");

        final SimpleVertex v1 = graph1.createVertex("v1");
        v1.setLabel("Vertex 1");
        final SimpleVertex v2 = graph1.createVertex("v2");
        v2.setLabel("Vertex 2");

        graph1.createEdge(v1, v2);

        // Second graph is a copy of the first
        final SimpleGraph graph2 = new SimpleGraph(graph1);
        graph2.setNamespace(NAMESPACE + "2");
        graph2.setLabel(graph1.getLabel() + " 2");

        // Persist
        originalContainer.addGraph(graph1);
        originalContainer.addGraph(graph2);
        graphRepository.save(originalContainer);

        // Verify
        verifyEquals(originalContainer, graphRepository.findContainerById(CONTAINER_ID, new Y()));

        /*
         * Update
         */
        // Add new graph which is a copy of an existing graph
        final SimpleGraph graph3 = new SimpleGraph(graph1);
        graph3.setNamespace(NAMESPACE + "3");
        graph3.setLabel(graph1.getLabel() + " 3");
        originalContainer.addGraph(graph3);

        // Remove existing graph.
        originalContainer.removeGraph(graph2.getNamespace());

        // Update existing graph
        graph1.setLabel("New Dummy Graph");
        graph1.addVertex(new SimpleVertex(NAMESPACE, "v3"));

        // Persist changes
        graphRepository.save(originalContainer);

        // Verify
        verifyEquals(originalContainer, graphRepository.findContainerById(CONTAINER_ID, new Y()));

        /*
         * Delete
         */
        graphRepository.deleteContainer(CONTAINER_ID);
        Assert.assertNull(graphRepository.findContainerById(CONTAINER_ID, new Y()));
    }

    private void verifyEquals(SimpleGraphContainer originalContainer, SimpleGraphContainer persistedContainer) {
        Assert.assertEquals(originalContainer.getId(), persistedContainer.getId());
        Assert.assertEquals(originalContainer.getDescription(), persistedContainer.getDescription());
        Assert.assertEquals(originalContainer.getLabel(), persistedContainer.getLabel());
        Assert.assertEquals(originalContainer.getGraphs().size(), persistedContainer.getGraphs().size());
        Assert.assertEquals(originalContainer, persistedContainer);
    }

    // TODO MVR name me
    private static class Y implements Function<GenericGraphContainer, SimpleGraphContainer> {
        @Override
        public SimpleGraphContainer apply(GenericGraphContainer genericGraphContainer) {
            Objects.requireNonNull(genericGraphContainer);
            final SimpleGraphContainer simpleGraphContainer = new SimpleGraphContainer(genericGraphContainer.getId());
            simpleGraphContainer.setLabel(genericGraphContainer.getLabel());
            simpleGraphContainer.setDescription(genericGraphContainer.getDescription());
            genericGraphContainer.getGraphs().forEach(genericGrah -> {
                final SimpleGraph simpleGraph = new X().apply((GenericGraph) genericGrah); // TODO MVR this should not be necessary
                simpleGraphContainer.addGraph(simpleGraph);
            });
            return simpleGraphContainer;
        }
    }

    // TODO MVR name me and fully implement
    private static class X implements Function<GenericGraph, SimpleGraph> {

        @Override
        public SimpleGraph apply(GenericGraph genericGraph) {
                final SimpleGraph simpleGraph = new SimpleGraph(genericGraph.getNamespace(), SimpleVertex.class);
                simpleGraph.setLabel(genericGraph.getLabel());
                simpleGraph.setDescription(genericGraph.getDescription());

                genericGraph.getVertices().forEach(genericVertex -> {
                    try {
                        // TODO MVR we should use the factory instead
//                        final SimpleVertex eachSimpleVertex = vertexFactory.createVertex(SimpleVertex.class, simpleGraph.getNamespace(), genericVertex.getId());
                        final SimpleVertex eachSimpleVertex = new SimpleVertex(simpleGraph.getNamespace(), genericVertex.getId());
                        eachSimpleVertex.setLabel(genericVertex.getProperty(GenericProperties.LABEL));
                        eachSimpleVertex.setIconKey(genericVertex.getProperty(GenericProperties.ICON_KEY));
                        eachSimpleVertex.setTooltip(genericVertex.getProperty(GenericProperties.TOOLTIP));
                        eachSimpleVertex.setNodeRefString(genericVertex.getProperty(GenericProperties.NODE_REF));
                        eachSimpleVertex.setNodeInfo((NodeInfo) genericVertex.getComputedProperties().get("node"));
                        simpleGraph.addVertex(eachSimpleVertex);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                genericGraph.getEdges().forEach(genericEdge -> {
                    final SimpleVertex sourceVertex = simpleGraph.getVertex(genericEdge.getSource().getId());
                    final SimpleVertex targetVertex = simpleGraph.getVertex(genericEdge.getTarget().getId());
                    final SimpleEdge eachSimpleEdge = new SimpleEdge(sourceVertex, targetVertex);
                    simpleGraph.addEdge(eachSimpleEdge);
                });
                return simpleGraph;
            }
    }
}
