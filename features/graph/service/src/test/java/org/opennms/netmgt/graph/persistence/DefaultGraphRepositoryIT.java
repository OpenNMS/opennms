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

package org.opennms.netmgt.graph.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.graph.AbstractGraphEntity;
import org.opennms.netmgt.graph.EdgeEntity;
import org.opennms.netmgt.graph.FocusEntity;
import org.opennms.netmgt.graph.GraphContainerEntity;
import org.opennms.netmgt.graph.PropertyEntity;
import org.opennms.netmgt.graph.VertexEntity;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer.GenericGraphContainerBuilder;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.persistence.GraphRepository;
import org.opennms.netmgt.graph.domain.AbstractDomainGraphContainer;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainGraph;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainGraphContainer;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainVertex;
import org.opennms.netmgt.graph.provider.application.ApplicationGraph;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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

    @Autowired
    private GenericPersistenceAccessor persistenceAccessor;

    private static final String NAMESPACE = "dummy";
    private static final String CONTAINER_ID = "unique-id";

    @Before
    public void setUp() {
        // Clean up properly after each test
        persistenceAccessor.findAll(GraphContainerEntity.class).forEach(e -> graphRepository.deleteContainer(e.getNamespace()));
        assertThat("AbstractGraphEntity", persistenceAccessor.findAll(AbstractGraphEntity.class), Matchers.hasSize(0));
        assertThat("FocusEntity", persistenceAccessor.findAll(FocusEntity.class), Matchers.hasSize(0));
        assertThat("PropertyEntity", persistenceAccessor.findAll(PropertyEntity.class), Matchers.hasSize(0));
    }

    @Test
    public void verifyCRUD() {
        /*
         * Create/Persist
         */
        final GenericGraphContainerBuilder originalContainerBuilder = GenericGraphContainer.builder()
            .id(CONTAINER_ID)
            .description("Container for 'unique-id' graph")
            .label("I am soooo unique \\o/");

        // Create first graph
        final GenericGraphBuilder graph1Builder = GenericGraph.builder()
                .namespace(NAMESPACE)
                .label("Dummy Graph")
                .description("I am not so unique, I may be replaced at any time :(");

        final GenericVertex v1 = GenericVertex.builder()
        		.namespace(NAMESPACE)
        		.id("v1")
        		.label("Vertex 1")
        		.build();
        final GenericVertex v2 = GenericVertex.builder()
        		.namespace(NAMESPACE)
        		.id("v2")
        		.label("Vertex 2")
        		.build();

        graph1Builder.addVertex(v1);
        graph1Builder.addVertex(v2);
        graph1Builder.addEdge(GenericEdge.builder()
                .namespace(NAMESPACE)
                .source(v1.getVertexRef())
                .target(v2.getVertexRef()).build());
        final GenericGraph graph1 = graph1Builder.build();

        // Second graph is a copy of the first
        final GenericGraph graph2 = GenericGraph.builder()
                .properties(graph1.getProperties())
                .namespace(NAMESPACE + "2")
                .label(graph1.getLabel() + " 2").build();

        // Persist
        originalContainerBuilder.addGraph(graph1);
        originalContainerBuilder.addGraph(graph2);
        graphRepository.save(originalContainerBuilder.build());

        // Verify
        verifyEquals(originalContainerBuilder.build(), graphRepository.findContainerById(CONTAINER_ID));

        /*
         * Update
         */
        // Add new graph which is a copy of an existing graph
        final GenericGraph graph3 = GenericGraph.builder()
                .properties(graph1.getProperties())
                .namespace(NAMESPACE + "3")
                .label(graph1.getLabel() + " 3")
                .build();
        originalContainerBuilder.addGraph(graph3);

        // Remove existing graph.
        // originalContainer.removeGraph(graph2.getNamespace());

        // Update existing graph 
        GenericGraph graph1Updated = GenericGraph.builder()
                .graph(graph1)
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v3").build())
                .build();
        originalContainerBuilder.addGraph(graph1Updated);
        
        // Persist changes
        graphRepository.save(originalContainerBuilder.build());

        // Verify
        verifyEquals(originalContainerBuilder.build(), graphRepository.findContainerById(CONTAINER_ID));

        /*
         * Delete
         */
        graphRepository.deleteContainer(CONTAINER_ID);
        Assert.assertNull(graphRepository.findContainerById(CONTAINER_ID));
    }

    @Test
    public void verifySavingCollections() {
        final GenericVertex vertex = GenericVertex.builder()
                .namespace(NAMESPACE)
                .id("v1")
                .property("collectionProperty", ImmutableList.of("E", "F")).build();

        final GenericGraph graph = GenericGraph.builder()
                .namespace(NAMESPACE)
                .property("collectionProperty", ImmutableList.of("C", "D"))
                .addVertex(vertex)
                .build();

        final GenericGraphContainer originalContainer = GenericGraphContainer.builder()
            .id(CONTAINER_ID)
            .property("collectionProperty", ImmutableList.of("A", "B"))
            .addGraph(graph).build();

        graphRepository.save(originalContainer);
        GenericGraphContainer loadedContainer = graphRepository.findContainerById(CONTAINER_ID);
        assertEquals(originalContainer.getGraph(NAMESPACE), loadedContainer.getGraph(NAMESPACE));
    }

    @Test
    public void verifyFocusPersistence() {
        final GenericGraphContainerBuilder containerBuilder = GenericGraphContainer.builder().id(CONTAINER_ID);
        final GenericGraphBuilder graphBuilder = GenericGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v1").label("Vertex 1").build())
                .addVertex((GenericVertex.builder().namespace(NAMESPACE).id("v2").label("Vertex 2").build()));
        GenericGraphContainer container = containerBuilder.addGraph(graphBuilder.build()).build();
        graphRepository.save(container);

        // By default the focus should be EMPTY
        final Focus emptyFocus = graphRepository.findContainerById(container.getId()).getGraph(NAMESPACE).getDefaultFocus();
        assertThat(emptyFocus.getVertexRefs(), Matchers.hasSize(0));

        // Set to FIRST
        container = containerBuilder.addGraph(graphBuilder.focus().first().apply().build()).build();
        graphRepository.save(container);
        final Focus firstFocus = graphRepository.findContainerById(container.getId()).getGraph(NAMESPACE).getDefaultFocus();
        assertThat(firstFocus.getVertexRefs(), Matchers.hasSize(1));

        // Set to ALL
        container = containerBuilder.addGraph(graphBuilder.focus().all().apply().build()).build();
        graphRepository.save(container);
        final Focus allFocus = graphRepository.findContainerById(container.getId()).getGraph(NAMESPACE).getDefaultFocus();
        assertThat(allFocus.getVertexRefs(), Matchers.hasSize(2));

        // Set to Specific
        container = containerBuilder.addGraph(graphBuilder.focus().selection(Lists.newArrayList(new VertexRef(NAMESPACE, "v2"))).apply().build()).build();
        graphRepository.save(container);
        final Focus selectiveFocus = graphRepository.findContainerById(container.getId()).getGraph(NAMESPACE).getDefaultFocus();
        assertThat(selectiveFocus.getVertexRefs(), Matchers.hasSize(1));

        // Ensure all (obsolete) focus entities have been removed from the underlying database
        assertThat(persistenceAccessor.findAll(FocusEntity.class), Matchers.hasSize(1)); // only one focus entity should be present
    }

    @Test
    // Verifies that when deleting the graphcontainer all other entities are also deleted (properties, focus, etc)
    public void verifyDeleteCascades() {
        final GenericGraphContainerBuilder containerBuilder = GenericGraphContainer.builder().id(CONTAINER_ID);
        final GenericGraphBuilder graphBuilder = GenericGraph.builder()
                .namespace(NAMESPACE)
                .addVertex(GenericVertex.builder().namespace(NAMESPACE).id("v1").label("Vertex 1").build())
                .addVertex((GenericVertex.builder().namespace(NAMESPACE).id("v2").label("Vertex 2").build()));
        final GenericGraphContainer container = containerBuilder.addGraph(graphBuilder.build()).build();
        graphRepository.save(container);

        // Verify creation of objects
        // 1 container, 1 graph, 2 vertices
        assertThat(persistenceAccessor.findAll(AbstractGraphEntity.class), Matchers.hasSize(4));
        // 1 Focus
        assertThat(persistenceAccessor.findAll(FocusEntity.class), Matchers.hasSize(1));
        // 1 container id, 1 graph namespace, 2 vertex namespace, 2 vertex ids, 2 vertex labels
        assertThat(persistenceAccessor.findAll(PropertyEntity.class), Matchers.hasSize(8));

        // Delete the container
        graphRepository.deleteContainer(container.getId());

        // Verify deletion of objects
        assertThat(persistenceAccessor.findAll(AbstractGraphEntity.class), Matchers.hasSize(0));
        assertThat(persistenceAccessor.findAll(FocusEntity.class), Matchers.hasSize(0));
        assertThat(persistenceAccessor.findAll(PropertyEntity.class), Matchers.hasSize(0));
    }

    @Test
    public void verifyPersistingEdgesOfDifferentNamespaces() {
        final String namespace = "test-namespace";
        final String containerId = "test-container-id";
        final SimpleDomainGraph graph = SimpleDomainGraph.builder()
                .namespace(namespace)
                .addVertex(SimpleDomainVertex.builder().namespace(namespace).id("v1").build())
                .addEdge(SimpleDomainEdge.builder()
                        .namespace(namespace)
                        .id("e1")
                        .source(namespace, "v1")
                        .target("other", "o1").build())
                .build();
        final SimpleDomainGraphContainer container = SimpleDomainGraphContainer.builder()
                .id(containerId)
                .addGraph(graph)
                .build();
        graphRepository.save(container);

        // Verify that only one of each was persisted
        assertThat(persistenceAccessor.findAll(GraphContainerEntity.class), Matchers.hasSize(1));
        assertThat(persistenceAccessor.findAll(VertexEntity.class), Matchers.hasSize(1));
        assertThat(persistenceAccessor.findAll(EdgeEntity.class), Matchers.hasSize(1));

        // Verify persisted properly
        final GenericGraphContainer genericGraphContainer = graphRepository.findContainerById(containerId);
        assertEquals(container, SimpleDomainGraphContainer.from(genericGraphContainer));
    }

    private void verifyEquals(GenericGraphContainer originalContainer, GenericGraphContainer persistedContainer) {
        Assert.assertEquals(originalContainer.getId(), persistedContainer.getId());
        Assert.assertEquals(originalContainer.getDescription(), persistedContainer.getDescription());
        Assert.assertEquals(originalContainer.getLabel(), persistedContainer.getLabel());
        Assert.assertEquals(originalContainer.getGraphs().size(), persistedContainer.getGraphs().size());
        Assert.assertEquals(originalContainer, persistedContainer);
    }

    /**
     * Helper container for test purposes.
     */
    private static class ApplicationGraphContainer extends AbstractDomainGraphContainer<ApplicationGraph> {

        private ApplicationGraphContainer(GenericGraphContainer genericGraphContainer){
            super(genericGraphContainer);
        }

        @Override
        protected ApplicationGraph convert(GenericGraph graph) {
            return new ApplicationGraph(graph);
        }

        public static ApplicationGraphContainer.ApplicationGraphContainerBuilder builder() {
            return new ApplicationGraphContainer.ApplicationGraphContainerBuilder();
        }

        public static ApplicationGraphContainer from(GenericGraphContainer genericGraphContainer) {
            return new ApplicationGraphContainer(genericGraphContainer);
        }

        public final static class ApplicationGraphContainerBuilder extends AbstractDomainGraphContainerBuilder<ApplicationGraphContainerBuilder, ApplicationGraph> {

            private ApplicationGraphContainerBuilder() {}

            public ApplicationGraphContainer build() {
                return new ApplicationGraphContainer(this.builder.build());
            }
        }
    }
}
