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

package org.opennms.netmgt.graph.dao.hibernate;

import java.util.NoSuchElementException;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.graph.dao.api.EntityProperties;
import org.opennms.netmgt.graph.dao.api.GraphContainerDao;
import org.opennms.netmgt.graph.EdgeEntity;
import org.opennms.netmgt.graph.FocusEntity;
import org.opennms.netmgt.graph.GraphContainerEntity;
import org.opennms.netmgt.graph.GraphEntity;
import org.opennms.netmgt.graph.VertexEntity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

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
public class GraphContainerDaoIT {

    private static final String CONTAINER_ID = "unique-container-id";
    private static final String CONTAINER_DESCRIPTION = "Container for '" + CONTAINER_ID + "' graph";
    private static final String CONTAINER_LABEL = "I am soooo unique \\o/";

    private static final String GRAPH_NAMESPACE = "dummy";
    private static final String GRAPH_LABEL = "Dummy Graph";
    private static final String GRAPH_DESCRIPTION = "I am not so unique, I may be replaced at any time :(";

    @Autowired
    private GenericPersistenceAccessor persistenceAccessor;

    @Autowired
    private GraphContainerDao graphContainerDao;

    @Autowired
    private TransactionOperations transactionOperations;

    @Test
    public void verifyCRUD() {
        /*
         * Create
         */
        final GraphContainerEntity originalContainerEntity = new GraphContainerEntity();
        originalContainerEntity.setNamespace(CONTAINER_ID);
        originalContainerEntity.setProperty(EntityProperties.DESCRIPTION, String.class, CONTAINER_DESCRIPTION);
        originalContainerEntity.setProperty(EntityProperties.LABEL, String.class, CONTAINER_LABEL);

        final GraphEntity originalGraphEntity1 = createExampleGraph(GRAPH_NAMESPACE);
        final GraphEntity originalGraphEntity2 = createExampleGraph(GRAPH_NAMESPACE + "2");

        // Persist
        originalContainerEntity.getGraphs().add(originalGraphEntity1);
        originalContainerEntity.getGraphs().add(originalGraphEntity2);
        transactionOperations.execute(status -> {
            graphContainerDao.save(originalContainerEntity);
            status.flush();
            return null;
        });

        // Verify Container
        transactionOperations.execute(status -> {
            final GraphContainerEntity persistedGraphContainerEntity = graphContainerDao.findContainerById(originalContainerEntity.getNamespace());
            Assert.assertEquals(CONTAINER_LABEL, persistedGraphContainerEntity.getLabel());
            Assert.assertEquals(CONTAINER_DESCRIPTION, persistedGraphContainerEntity.getDescription());
            Assert.assertEquals(CONTAINER_ID, persistedGraphContainerEntity.getNamespace());
            Assert.assertEquals(2, persistedGraphContainerEntity.getGraphs().size());

            // Verify Graph
            for (String eachNamespace : Lists.newArrayList(GRAPH_NAMESPACE, GRAPH_NAMESPACE + "2")) {
                final GraphEntity originalGraphEntity = originalContainerEntity.getGraph(eachNamespace);
                final GraphEntity persistedGraphEntity = persistedGraphContainerEntity.getGraph(eachNamespace);
                Assert.assertNotNull(persistedGraphEntity);
                Assert.assertEquals(eachNamespace, persistedGraphEntity.getNamespace());
                Assert.assertEquals(originalGraphEntity.getLabel(), persistedGraphEntity.getLabel());
                Assert.assertEquals(originalGraphEntity.getDescription(), persistedGraphEntity.getDescription());
                Assert.assertEquals(3, persistedGraphEntity.getVertices().size());
                Assert.assertEquals(1, persistedGraphEntity.getEdges().size());

                // Verify focus has been persisted
                Assert.assertNotNull(persistedGraphEntity.getDefaultFocus());
                Assert.assertThat(persistedGraphEntity.getDefaultFocus().getType(), Matchers.is("FIRST"));
            }
            return null;
        });

        /*
         * Update
         */
        transactionOperations.execute(status -> {
            final GraphContainerEntity persistedGraphContainerEntity = graphContainerDao.findContainerById(originalContainerEntity.getNamespace());

            // Add new graph to the container
            final GraphEntity graphEntity3 = createExampleGraph(GRAPH_NAMESPACE + "3");
            graphEntity3.setProperty(EntityProperties.LABEL, String.class, "Ultimate " + GRAPH_LABEL);
            persistedGraphContainerEntity.getGraphs().add(graphEntity3);

            // Remove existing graph.
            persistedGraphContainerEntity.removeGraph(GRAPH_NAMESPACE + "2");

            // Update existing graph
            final GraphEntity graph = persistedGraphContainerEntity.getGraph(GRAPH_NAMESPACE);
            graph.setProperty(EntityProperties.LABEL, String.class,"New " + GRAPH_LABEL);

            // Adding new relations cause edges to be empty, so we test this by adding a new vertex
            final VertexEntity vertex = new VertexEntity();
            vertex.setNamespace(GRAPH_NAMESPACE);
            vertex.setProperty(EntityProperties.ID, String.class, "v4");
            vertex.setProperty(EntityProperties.LABEL, String.class, "Vertex 4");
            graph.addVertex(vertex);

            // The same is for adding an edge. It is simply removed
            final EdgeEntity edge = new EdgeEntity();
            edge.setNamespace(GRAPH_NAMESPACE);
            edge.setSource(GRAPH_NAMESPACE, graph.getVertices().get(1).getId());
            edge.setTarget(GRAPH_NAMESPACE, graph.getVertices().get(2).getId());
            graph.addEdge(edge);

            graphContainerDao.update(persistedGraphContainerEntity);
            return null;
        });

        // Verify Container
        transactionOperations.execute(status -> {
            final GraphContainerEntity persistedGraphContainerEntity = graphContainerDao.findContainerById(originalContainerEntity.getNamespace());
            Assert.assertEquals(CONTAINER_LABEL, persistedGraphContainerEntity.getLabel());
            Assert.assertEquals(CONTAINER_DESCRIPTION, persistedGraphContainerEntity.getDescription());
            Assert.assertEquals(CONTAINER_ID, persistedGraphContainerEntity.getNamespace());
            Assert.assertEquals(2, persistedGraphContainerEntity.getGraphs().size());

            // Verify Graphs
            try {
                persistedGraphContainerEntity.getGraph(GRAPH_NAMESPACE + "2");
                Assert.fail("Expected element to not be present");
            } catch (NoSuchElementException ex) {
                // expected
            }

            // Verify <GRAPH_NAMESPACE>
            GraphEntity persistedGraphEntity = persistedGraphContainerEntity.getGraph(GRAPH_NAMESPACE);
            Assert.assertNotNull(persistedGraphEntity);
            Assert.assertEquals(GRAPH_NAMESPACE, persistedGraphEntity.getNamespace());
            Assert.assertEquals("New " + GRAPH_LABEL, persistedGraphEntity.getLabel());
            Assert.assertEquals(GRAPH_DESCRIPTION, persistedGraphEntity.getDescription());
            Assert.assertEquals(4, persistedGraphEntity.getVertices().size());
            Assert.assertEquals(2, persistedGraphEntity.getEdges().size());

            // Verify <GRAPH_NAMESPACE>3
            persistedGraphEntity = persistedGraphContainerEntity.getGraph(GRAPH_NAMESPACE + "3");
            Assert.assertNotNull(persistedGraphEntity);
            Assert.assertEquals(GRAPH_NAMESPACE + "3", persistedGraphEntity.getNamespace());
            Assert.assertEquals("Ultimate " + GRAPH_LABEL, persistedGraphEntity.getLabel());
            Assert.assertEquals(GRAPH_DESCRIPTION, persistedGraphEntity.getDescription());
            Assert.assertEquals(3, persistedGraphEntity.getVertices().size());
            Assert.assertEquals(1, persistedGraphEntity.getEdges().size());

            return null;
        });

        /*
         * Delete
         */
        transactionOperations.execute(status -> {
            graphContainerDao.delete(originalContainerEntity.getNamespace());
            return null;
        });
        transactionOperations.execute(status -> {
            Assert.assertEquals(0, persistenceAccessor.find("Select gc from GraphContainerEntity gc").size());
            Assert.assertEquals(0, persistenceAccessor.find("Select g from GraphEntity g").size());
            Assert.assertEquals(0, persistenceAccessor.find("Select v from VertexEntity v").size());
            Assert.assertEquals(0, persistenceAccessor.find("Select e from EdgeEntity e").size());
            Assert.assertEquals(0, persistenceAccessor.find("Select p from PropertyEntity p").size());
            return null;
        });
    }

    private GraphEntity createExampleGraph(final String namespace) {
        final GraphEntity graph = new GraphEntity();
        graph.setNamespace(namespace);
        graph.setProperty(EntityProperties.LABEL, String.class, GRAPH_LABEL);
        graph.setProperty(EntityProperties.DESCRIPTION, String.class, GRAPH_DESCRIPTION);
        graph.setDefaultFocus(new FocusEntity("FIRST"));

        final VertexEntity v1 = new VertexEntity();
        v1.setNamespace(namespace);
        v1.setProperty(EntityProperties.ID, String.class, "v1");
        v1.setProperty(EntityProperties.LABEL, String.class, "Vertex 1");

        final VertexEntity v2 = new VertexEntity();
        v2.setNamespace(namespace);
        v2.setProperty(EntityProperties.ID, String.class, "v2");
        v2.setProperty(EntityProperties.LABEL, String.class, "Vertex 2");

        final VertexEntity v3 = new VertexEntity();
        v3.setNamespace(GRAPH_NAMESPACE);
        v3.setProperty(EntityProperties.ID, String.class, "v3");
        v3.setProperty(EntityProperties.LABEL, String.class, "Vertex 3");

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);

        final EdgeEntity edge = new EdgeEntity();
        edge.setNamespace(namespace);
        edge.setSource(namespace, v1.getId());
        edge.setTarget(namespace, v2.getId());
        graph.addEdge(edge);

        return graph;
    }
}
