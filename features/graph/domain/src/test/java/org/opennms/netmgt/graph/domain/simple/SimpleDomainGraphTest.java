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

package org.opennms.netmgt.graph.domain.simple;

import static org.junit.Assert.assertEquals;
import static org.opennms.netmgt.graph.domain.simple.TestObjectCreator.createEdge;
import static org.opennms.netmgt.graph.domain.simple.TestObjectCreator.createVertex;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;

public class SimpleDomainGraphTest {

    /**
     * Convert a SimpleGraph into a GenericGraph and back. All properties should be kept but we should have copies
     * of the elements in the graph - not the same objects. */
    @Test
    public void simpleGraphShouldBeAbleToBeConvertedIntoAGenericGraphAndBack() {

        // set up:
        SimpleDomainVertex vertex1 = createVertex(TestObjectCreator.NAMESPACE, UUID.randomUUID().toString());
        SimpleDomainVertex vertex2 = createVertex(TestObjectCreator.NAMESPACE, UUID.randomUUID().toString());
        SimpleDomainVertex vertex3 = createVertex(TestObjectCreator.NAMESPACE, UUID.randomUUID().toString());
        SimpleDomainEdge edge1 = createEdge(vertex1, vertex2);
        SimpleDomainEdge edge2 = createEdge(vertex1, vertex3);
        
        SimpleDomainGraph originalGraph = SimpleDomainGraph.builder()
            .namespace(TestObjectCreator.NAMESPACE)
            .label("labelGraph")
            .addVertex(vertex1)
            .addVertex(vertex2)
            .addVertex(vertex3)
            .addEdge(edge1)
            .addEdge(edge2).build();

        // convert:
        GenericGraph genericGraph = originalGraph.asGenericGraph();
        SimpleDomainGraph copyGraph = new SimpleDomainGraph(genericGraph); // copy constructor

        // test:
        assertEquals(originalGraph.getLabel(), copyGraph.getLabel());
        assertEquals(originalGraph.getNamespace(), copyGraph.getNamespace());
        equalsButNotSame(originalGraph, copyGraph);
        equalsButNotSame(originalGraph.getVertex(vertex1.getId()), copyGraph.getVertex(vertex1.getId()));
        equalsButNotSame(originalGraph.getVertex(vertex2.getId()), copyGraph.getVertex(vertex2.getId()));
        equalsButNotSame(originalGraph.getVertex(vertex3.getId()), copyGraph.getVertex(vertex3.getId()));
        equalsButNotSame(originalGraph.getEdge(edge1.getId()), copyGraph.getEdge(edge1.getId()));
        equalsButNotSame(originalGraph.getEdge(edge2.getId()), copyGraph.getEdge(edge2.getId()));

    }

    private void equalsButNotSame(Object original, Object copy){
        assertEquals(original, copy);
        Assert.assertNotSame(original, copy);
    }

    @Test
    public void simpleGraphShouldBeConstructedFromGraphInfo() {
        DefaultGraphInfo info = new DefaultGraphInfo(TestObjectCreator.NAMESPACE);
        info.setDescription("description");
        info.setLabel("label");
        SimpleDomainGraph graph = SimpleDomainGraph.builder().graphInfo(info).build();
        assertEquals(info.getNamespace(), graph.getNamespace());
        assertEquals(info.getLabel(), graph.getLabel());
        assertEquals(info.getDescription(), graph.getDescription());
    }

}
