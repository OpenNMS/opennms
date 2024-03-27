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

import org.junit.Test;
import org.opennms.features.topology.api.BoundingBox;

import static org.junit.Assert.assertEquals;

public class GridLayoutAlgorithmTest {

    @Test
    public void canCalculateGrid() {
        // No vertices should generate an empty grid
        calculateGrid(0, 0, 0, 0, 0);
        calculateGrid(0, 600, 400, 0, 0);

        // A single vertex
        calculateGrid(1, 400, 400, 1, 1);
        calculateGrid(1, 600, 400, 2, 1);
        calculateGrid(1, 1200, 400, 2, 1);

        // More vertices
        calculateGrid(4, 400, 400, 2, 2);
        calculateGrid(4, 600, 400, 3, 2);
        calculateGrid(15, 400, 400, 4, 4);
        calculateGrid(15, 600, 400, 5, 4);

        // These should fit perfectly
        calculateGrid(2400, 600, 400, 60, 40);
    }

    void calculateGrid(int N, int width, int height, int expectedWidth, int expectedHeight) {
        BoundingBox grid = GridLayoutAlgorithm.calculateGrid(N, width, height);
        System.out.printf("Generated a grid of width: %d and height: %d for N: %d, w: %d and h: %d\n",
                grid.getWidth(), grid.getHeight(), N, width, height);
        assertEquals(expectedWidth, grid.getWidth());
        assertEquals(expectedHeight, grid.getHeight());
    }
}
