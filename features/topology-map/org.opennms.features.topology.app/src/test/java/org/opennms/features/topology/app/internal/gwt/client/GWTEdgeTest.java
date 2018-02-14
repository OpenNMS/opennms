/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GWTEdgeTest {

    private static final double delta = 0.00001;

    @Test
    public void canCalculatePathSign() {
        assertEquals(0, GWTEdge.getPathSign(0, 1), delta);

        assertEquals(1, GWTEdge.getPathSign(0, 2), delta);
        assertEquals(-1, GWTEdge.getPathSign(1, 2), delta);

        assertEquals(0, GWTEdge.getPathSign(0, 3), delta);
        assertEquals(1, GWTEdge.getPathSign(1, 3), delta);
        assertEquals(-1, GWTEdge.getPathSign(2, 3), delta);

        assertEquals(1, GWTEdge.getPathSign(0, 4), delta);
        assertEquals(-1, GWTEdge.getPathSign(1, 4), delta);
        assertEquals(1, GWTEdge.getPathSign(2, 4), delta);
        assertEquals(-1, GWTEdge.getPathSign(3, 4), delta);
    }

    @Test
    public void canCalculatePathMultiplier() {
        assertEquals(0, GWTEdge.getPathMultiplier(0, 1), delta);

        assertEquals(1, GWTEdge.getPathMultiplier(0, 2), delta);
        assertEquals(1, GWTEdge.getPathMultiplier(1, 2), delta);

        assertEquals(0, GWTEdge.getPathMultiplier(0, 3), delta);
        assertEquals(1, GWTEdge.getPathMultiplier(1, 3), delta);
        assertEquals(1, GWTEdge.getPathMultiplier(2, 3), delta);

        assertEquals(1, GWTEdge.getPathMultiplier(0, 4), delta);
        assertEquals(1, GWTEdge.getPathMultiplier(1, 4), delta);
        assertEquals(2, GWTEdge.getPathMultiplier(2, 4), delta);
        assertEquals(2, GWTEdge.getPathMultiplier(3, 4), delta);
        
        assertEquals(0, GWTEdge.getPathMultiplier(0, 5), delta);
        assertEquals(1, GWTEdge.getPathMultiplier(1, 5), delta);
        assertEquals(1, GWTEdge.getPathMultiplier(2, 5), delta);
        assertEquals(2, GWTEdge.getPathMultiplier(3, 5), delta);
        assertEquals(2, GWTEdge.getPathMultiplier(4, 5), delta);

        assertEquals(1, GWTEdge.getPathMultiplier(0, 6), delta);
        assertEquals(1, GWTEdge.getPathMultiplier(1, 6), delta);
        assertEquals(2, GWTEdge.getPathMultiplier(2, 6), delta);
        assertEquals(2, GWTEdge.getPathMultiplier(3, 6), delta);
        assertEquals(3, GWTEdge.getPathMultiplier(4, 6), delta);
        assertEquals(3, GWTEdge.getPathMultiplier(5, 6), delta);
    }
}
