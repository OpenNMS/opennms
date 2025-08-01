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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.features.topology.api.topo.Vertex;

import com.google.common.collect.ComparisonChain;

public class GridLayoutAlgorithm extends AbstractLayoutAlgorithm {

    /**
     * Updates the current layout by extracting the containers graph and then perform a (x,y) transformation
     * of all vertices.
     *
     * @param graph The container of the current graph. Contains all relevant information to perform the transformation
     *                       of the {@link Graph} by changing its {@link Layout}
     */
    @Override
    public void updateLayout(Graph graph) {
        final Layout graphLayout = graph.getLayout();

        // Sort the vertices
        final List<Vertex> sortedVertices = graph.getDisplayVertices().stream().sorted(new Comparator<Vertex>() {
            @Override
            public int compare(Vertex v1, Vertex v2) {
                return ComparisonChain.start()
                        .compare(getIndex(v1), getIndex(v2))
                        .compare(v1.getLabel(), v2.getLabel())
                        .compare(v1.getId(), v2.getId())
                        .result();
            }
        }).collect(Collectors.toList());

        // Find the smallest rectangle (grid) that will fit all the vertices
        // while attempting to preserve the aspect ration of the view port
        final int numberOfVertices = sortedVertices.size();
        final BoundingBox layoutBounds = graphLayout.computeBoundingBox(new ArrayList<>(sortedVertices));
        final BoundingBox grid = calculateGrid(numberOfVertices, layoutBounds.getWidth(), layoutBounds.getHeight());

        // Layout the (sorted) vertices in the grid
        int k = 0;
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                if (k >= numberOfVertices) {
                    break;
                }
                graphLayout.setLocation(sortedVertices.get(k++), new Point(x * ELBOW_ROOM * 2, y * ELBOW_ROOM * 2));
            }
        }
    }

    /**
     * Calculates a grid of width W and height H such that:
     *     W * H >= N
     *     W/H ~= width/height
     */
    protected static BoundingBox calculateGrid(int N, int width, int height) {
        double ratio = (float)width / height;
        Double H = Math.sqrt(N / ratio);
        Double W = Math.ceil(N / H);
        H = Math.ceil(H); // Only round after the division
        return new BoundingBox(0, 0, W.intValue(), H.intValue());
    }

    private static int getIndex(Vertex v) {
        if (v instanceof LevelAware) {
            // Prioritize vertices that have a lower level
            return ((LevelAware)v).getLevel();
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
