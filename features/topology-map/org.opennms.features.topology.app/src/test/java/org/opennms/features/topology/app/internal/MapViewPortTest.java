/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
