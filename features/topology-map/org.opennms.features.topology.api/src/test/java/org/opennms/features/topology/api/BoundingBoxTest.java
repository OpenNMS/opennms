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
