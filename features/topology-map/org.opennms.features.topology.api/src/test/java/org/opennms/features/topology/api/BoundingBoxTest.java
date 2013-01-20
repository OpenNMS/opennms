package org.opennms.features.topology.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BoundingBoxTest {

    @Test
    public void test() {
        BoundingBox rBBox = new BoundingBox(0,0, 100, 150).computeWithAspectRatio(1.5);
        
        assertEquals(225, rBBox.getWidth());
        assertEquals(150, rBBox.getHeight());
        assertEquals(50, rBBox.getCenter().getX());
        assertEquals(75, rBBox.getCenter().getY());
        
        BoundingBox bBox = new BoundingBox(0, 0, 600, 100).computeWithAspectRatio(1.5);
        assertEquals(600, bBox.getWidth());
        assertEquals(400, bBox.getHeight());
        assertEquals(300, bBox.getCenter().getX());
        assertEquals(50, bBox.getCenter().getY());
        
        
        //custom size
        BoundingBox customBBox = new BoundingBox(60, 53, 2389, 1301).computeWithAspectRatio(1449.0/843.0);
        assertEquals(2389, customBBox.getWidth());
        assertEquals(1390, customBBox.getHeight());
        
    }
    

}
