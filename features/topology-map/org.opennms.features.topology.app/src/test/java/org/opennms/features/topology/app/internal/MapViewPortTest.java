package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import org.junit.Test;
import org.opennms.features.topology.api.BoundingBox;

public class MapViewPortTest {
    
    @Test
    public void testGetBounds() {
        MapViewManager viewManager = new MapViewManager();
        viewManager.setMapBounds(new BoundingBox(0,0, 8000,4000));
        viewManager.setViewPort(400, 300);
        
        BoundingBox boundingBox = viewManager.getCurrentBoundingBox();
        assertNotNull(boundingBox);
        assertEquals(4000, boundingBox.getCenter().getX());
        assertEquals(2000, boundingBox.getCenter().getY());
        assertEquals(8000, boundingBox.getWidth());
        assertEquals(6000, boundingBox.getHeight());
        assertEquals(0, boundingBox.getX());
        assertEquals(-1000, boundingBox.getY());
        
        assertEquals(0.0, viewManager.getScale(), 0.0);
        
        viewManager.setScale(1.0);
        boundingBox = viewManager.getCurrentBoundingBox();
        assertEquals(4000, boundingBox.getCenter().getX());
        assertEquals(2000, boundingBox.getCenter().getY());
        assertEquals(200, boundingBox.getWidth());
        assertEquals(150, boundingBox.getHeight());
        assertEquals(3900, boundingBox.getX());
        assertEquals(1925, boundingBox.getY());
        
        viewManager.setBoundingBox(new BoundingBox(0,0, 1265, 600));
        
        assertEquals(0.5, viewManager.getScale(), 0.0001);
        
    }
    


}
