/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
