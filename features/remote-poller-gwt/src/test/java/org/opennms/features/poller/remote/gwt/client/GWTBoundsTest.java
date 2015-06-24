/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class GWTBoundsTest {
    
    @Test
    public void testEasternHemiContains() {
        
        GWTBounds bounds = new GWTBounds(new GWTLatLng(30.0, 70.0), new GWTLatLng(40.0, 80.0));
        
        GWTLatLng coords = new GWTLatLng(35.0, 75.0);
        
        
        assertTrue(bounds.contains(coords));
    }
    
    @Test
    public void testWesternHemiContains() {
        GWTBounds bounds = new GWTBounds(new GWTLatLng(30.0, -80.0), new GWTLatLng(40.0, -70.0));
        
        GWTLatLng coords = new GWTLatLng(35.0, -75.0);
        
        
        assertTrue(bounds.contains(coords));

    }

    @Test
    public void testSouthernHemiContains() {
        GWTBounds bounds = new GWTBounds(new GWTLatLng(-40.0, -80.0), new GWTLatLng(30.0, 70.0));
        
        GWTLatLng coords = new GWTLatLng(-35.0, -75.0);
        
        
        assertTrue(bounds.contains(coords));

    }
    
    
    @Test
    public void testDatelineContains() {
        GWTBounds bounds = new GWTBounds(new GWTLatLng(-40.0, 80.0), new GWTLatLng(30.0, -70.0));
        
        GWTLatLng coords = new GWTLatLng(10.0, -175.0);
        
        
        assertTrue(bounds.contains(coords));

    }
    
    @Test
    public void testEquals() {
        GWTBounds b1 = new GWTBounds(1.0, 2.0, 3.0, 4.0);
        GWTBounds b2 = new GWTBounds(1.0, 2.0, 3.0, 4.0);
        GWTBounds b3 = new GWTBounds(1.0, 2.0, 3.0, 5.0);
        
        assertTrue(b1.equals(b1));
        assertTrue(b1.equals(b2));
        assertTrue(b2.equals(b1));
        assertFalse(b1.equals(b3));
        assertFalse(b3.equals(b1));
        
        
    }
}
