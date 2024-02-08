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
