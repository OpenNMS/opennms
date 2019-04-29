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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.persistence.GraphRepository;
import org.opennms.test.JUnitConfigurationEnvironment;
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
        final GenericGraphContainer originalContainer = new GenericGraphContainer();
        originalContainer.setId(CONTAINER_ID);
        originalContainer.setDescription("Container for 'unique-id' graph");
        originalContainer.setLabel("I am soooo unique \\o/");

        // Create first graph
        final GenericGraph graph1 = new GenericGraph(NAMESPACE);
        graph1.setLabel("Dummy Graph");
        graph1.setDescription("I am not so unique, I may be replaced at any time :(");

        final GenericVertex v1 = new GenericVertex(graph1.getNamespace(), "v1");
        v1.setLabel("Vertex 1");
        final GenericVertex v2 = new GenericVertex(graph1.getNamespace(), "v2");
        v2.setLabel("Vertex 2");

        graph1.addVertex(v1);
        graph1.addVertex(v2);
        graph1.addEdge(new GenericEdge(NAMESPACE, v1.getVertexRef(), v2.getVertexRef()));

        // Second graph is a copy of the first
        final GenericGraph graph2 = new GenericGraph(graph1, NAMESPACE + "2");
        graph2.setLabel(graph1.getLabel() + " 2");

        // Persist
        originalContainer.addGraph(graph1);
        originalContainer.addGraph(graph2);
        graphRepository.save(originalContainer);

        // Verify
        verifyEquals(originalContainer, graphRepository.findContainerById(CONTAINER_ID));

        /*
         * Update
         */
        // Add new graph which is a copy of an existing graph
        final GenericGraph graph3 = new GenericGraph(graph1, NAMESPACE + "3");
        graph3.setLabel(graph1.getLabel() + " 3");
        originalContainer.addGraph(graph3);

        // Remove existing graph.
        originalContainer.removeGraph(graph2.getNamespace());

        // Update existing graph
        graph1.setLabel("New Dummy Graph");
        graph1.addVertex(new GenericVertex(NAMESPACE, "v3"));

        // Persist changes
        graphRepository.save(originalContainer);

        // Verify
        verifyEquals(originalContainer, graphRepository.findContainerById(CONTAINER_ID));

        /*
         * Delete
         */
        graphRepository.deleteContainer(CONTAINER_ID);
        Assert.assertNull(graphRepository.findContainerById(CONTAINER_ID));
    }

    @Test
    public void verifySavingCollections() {

        GenericVertex vertex = new GenericVertex(NAMESPACE, "v1");
        vertex.setProperty("collectionProperty", Arrays.asList("E", "F"));

        final GenericGraph graph = new GenericGraph();
        graph.setNamespace(NAMESPACE);
        graph.setProperty("collectionProperty", Arrays.asList("C", "D"));
        graph.addVertex(vertex);

        GenericGraphContainer originalContainer = new GenericGraphContainer();
        originalContainer.setId(CONTAINER_ID);
        originalContainer.setProperty("collectionProperty", Arrays.asList("A", "B"));
        originalContainer.addGraph(graph);

        graphRepository.save(originalContainer);
        GenericGraphContainer loadedContainer = graphRepository.findContainerById(CONTAINER_ID);
        assertEquals(originalContainer.getGraph(NAMESPACE), loadedContainer.getGraph(NAMESPACE));
    }

    private void verifyEquals(GenericGraphContainer originalContainer, GenericGraphContainer persistedContainer) {
        Assert.assertEquals(originalContainer.getId(), persistedContainer.getId());
        Assert.assertEquals(originalContainer.getDescription(), persistedContainer.getDescription());
        Assert.assertEquals(originalContainer.getLabel(), persistedContainer.getLabel());
        Assert.assertEquals(originalContainer.getGraphs().size(), persistedContainer.getGraphs().size());
        Assert.assertEquals(originalContainer, persistedContainer);
    }
}
