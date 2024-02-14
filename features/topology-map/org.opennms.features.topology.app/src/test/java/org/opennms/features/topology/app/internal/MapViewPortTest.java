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
package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Point;

public class MapViewPortTest {
    private double m_delta = 0.001;
    @Test
    public void testGetBounds() {
        DefaultMapViewManager viewManager = new DefaultMapViewManager();
        viewManager.setMapBounds(new BoundingBox(0,0, 8000,4000));
        viewManager.setViewPort(400, 300);
        
        BoundingBox boundingBox = viewManager.getCurrentBoundingBox();
        assertNotNull(boundingBox);
        assertEquals(4000.0, boundingBox.getCenter().getX(), 0.001);
        assertEquals(2000.0, boundingBox.getCenter().getY(), 0.001);
        assertEquals(8000, boundingBox.getWidth());
        assertEquals(6000, boundingBox.getHeight());
        assertEquals(0, boundingBox.getX());
        assertEquals(-1000, boundingBox.getY());
        
        assertEquals(0.0, viewManager.getScale(), 0.0);
        
        viewManager.setScale(1.0);
        boundingBox = viewManager.getCurrentBoundingBox();
        assertEquals(4000, boundingBox.getCenter().getX(), m_delta);
        assertEquals(2000, boundingBox.getCenter().getY(), m_delta);
        assertEquals(200, boundingBox.getWidth());
        assertEquals(150, boundingBox.getHeight());
        assertEquals(3900, boundingBox.getX());
        assertEquals(1925, boundingBox.getY());
        
        viewManager.setBoundingBox(new BoundingBox(0,0, 1265, 600));
        
        assertEquals(0.5, viewManager.getScale(), 0.0001);
        
    }
    
    @Test
    public void testPanMap() {
        DefaultMapViewManager viewManager = new DefaultMapViewManager();
        viewManager.setMapBounds(new BoundingBox(0,0, 8000,4000));
        viewManager.setViewPort(400, 300);
        
        
        BoundingBox box = viewManager.getCurrentBoundingBox();
        assertNotNull(box);
        assertEquals(4000, box.getCenter().getX(), m_delta);
        assertEquals(2000, box.getCenter().getY(), m_delta);
        assertEquals(8000, box.getWidth());
        assertEquals(6000, box.getHeight());
        assertEquals(0, box.getX());
        assertEquals(-1000, box.getY());
        
        viewManager.setCenter(new Point(3900, 1900));
        
        box = viewManager.getCurrentBoundingBox();
        assertNotNull(box);
        assertEquals(3900, box.getCenter().getX(), m_delta);
        assertEquals(1900, box.getCenter().getY(), m_delta);
        assertEquals(8000, box.getWidth());
        assertEquals(6000, box.getHeight());
        assertEquals(-100, box.getX());
        assertEquals(-1100, box.getY());
    }
    


}
