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
