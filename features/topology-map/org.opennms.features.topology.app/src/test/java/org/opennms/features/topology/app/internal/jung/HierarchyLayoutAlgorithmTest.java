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
package org.opennms.features.topology.app.internal.jung;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.DefaultLayout;
import org.opennms.features.topology.app.internal.TestGraph;
import org.opennms.features.topology.app.internal.TestVertex;

public class HierarchyLayoutAlgorithmTest {
    private static final double delta = 0.00001d;
    private final List<Vertex> vertices = new ArrayList<>();
    final List<Edge> edges = new ArrayList<>();
    private DefaultLayout layout;

    /**
     * Generates the following graph:
     *
     *     t1-v1
     *    /     \
     *  t1-v2  t1-v3
     *
     * and verifies the resulting X and Y coordinates.
     */
    @Test
    public void basicLayout() {
        // Vertices
        vertices.add(new LevelAwareTestVertex("v1", 0));
        vertices.add(new LevelAwareTestVertex("v2", 1));
        vertices.add(new LevelAwareTestVertex("v3", 1));

        // Edges
        edges.add(new AbstractEdge("test", "v1-v2", vertices.get(0), vertices.get(1)));
        edges.add(new AbstractEdge("test","v1-v3", vertices.get(0), vertices.get(2)));

        buildLayout();

        expectVertexToBeAt(0, 150.0d, 100.0d);
        expectVertexToBeAt(1, 100.0d, 200.0d);
        expectVertexToBeAt(2, 200.0d, 200.0d);
    }

    /**
     * Generates the following graph:
     *
     *     t1-v1       t2-v1
     *    /     \
     *  t1-v2  t1-v3
     *   /
     * t1-v4
     *
     * and verifies the resulting X and Y coordinates.
     */
    @Test
    public void notSoBasicLayout() {
        // Vertices
        vertices.add(new LevelAwareTestVertex("t1-v1", 0));
        vertices.add(new LevelAwareTestVertex("t1-v2", 1));
        vertices.add(new LevelAwareTestVertex("t1-v3", 1));
        vertices.add(new LevelAwareTestVertex("t1-v4", 2));

        vertices.add(new LevelAwareTestVertex("t2-v1", 0));

        // Edges
        edges.add(new AbstractEdge("test", "t1-v1-v2", vertices.get(0), vertices.get(1)));
        edges.add(new AbstractEdge("test","t1-v1-v3", vertices.get(0), vertices.get(2)));
        edges.add(new AbstractEdge("test","t1-v2-v4", vertices.get(1), vertices.get(3)));

        buildLayout();

        expectVertexToBeAt(0, 150.0d, 100.0d);
        expectVertexToBeAt(1, 100.0d, 200.0d);
        expectVertexToBeAt(2, 200.0d, 200.0d);
        expectVertexToBeAt(3, 100.0d, 300.0d);
        expectVertexToBeAt(4, 300.0d, 100.0d);
    }

    // NMS-8703
    @Test
    public void verifyHierarchyLayoutFallBack() {
        // Vertices
        vertices.add(new TestVertex("root"));
        vertices.add(new TestVertex("v1"));
        vertices.add(new TestVertex("v2"));
        vertices.add(new TestVertex("v3"));

        // Edges
        edges.add(new AbstractEdge("test", "rootEdge", vertices.get(0), vertices.get(1)));
        edges.add(new AbstractEdge("test","e1", vertices.get(1), vertices.get(2)));
        edges.add(new AbstractEdge("test","e2", vertices.get(2), vertices.get(3)));

        // Circle
        edges.add(new AbstractEdge("test","e3", vertices.get(3), vertices.get(1)));

        // Update the layouts and ensure no exception is thrown
        buildLayout();
    }

    private void buildLayout() {
        // Mock ALL the things
        final GraphContainer mockGraphContainer = Mockito.mock(GraphContainer.class);
        layout = new DefaultLayout();
        final TestGraph testGraph = new TestGraph(layout, vertices, edges);
        Mockito.when(mockGraphContainer.getGraph()).thenReturn(testGraph);

        // Update layouts and ensure no exception is thrown
        new HierarchyLayoutAlgorithm().updateLayout(testGraph);
    }

    private void expectVertexToBeAt(int idx, double x, double y) {
        assertEquals(x, layout.getLocation(vertices.get(idx)).getX(), delta);
        assertEquals(y, layout.getLocation(vertices.get(idx)).getY(), delta);
    }

    private static class LevelAwareTestVertex extends TestVertex implements LevelAware {
        private final int level;

        public LevelAwareTestVertex(String id, int level) {
            super(id);
            this.level = level;
        }

        @Override
        public int getLevel() {
            return level;
        }
    }
}
