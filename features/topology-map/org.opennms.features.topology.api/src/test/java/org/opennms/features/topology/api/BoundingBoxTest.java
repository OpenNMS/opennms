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
package org.opennms.features.topology.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BoundingBoxTest {

    @Test
    public void test() {
        BoundingBox rBBox = new BoundingBox(0,0, 100, 150).computeWithAspectRatio(1.5);
        double delta = 0.001;
        assertEquals(225.0, rBBox.getWidth(), delta);
        assertEquals(150.0, rBBox.getHeight(), delta);
        assertEquals(50, rBBox.getCenter().getX(), delta);
        assertEquals(75, rBBox.getCenter().getY(), delta);
        
        BoundingBox bBox = new BoundingBox(0, 0, 600, 100).computeWithAspectRatio(1.5);
        assertEquals(600.0, bBox.getWidth(), delta);
        assertEquals(400.0, bBox.getHeight(), delta);
        assertEquals(300.0, bBox.getCenter().getX(), delta);
        assertEquals(50.0, bBox.getCenter().getY(), delta);
        
        
        //custom size
        BoundingBox customBBox = new BoundingBox(60, 53, 2389, 1301).computeWithAspectRatio(1449.0/843.0);
        assertEquals(2389.0, customBBox.getWidth(), delta);
        assertEquals(1390.0, customBBox.getHeight(), delta);
        
    }
    

}
