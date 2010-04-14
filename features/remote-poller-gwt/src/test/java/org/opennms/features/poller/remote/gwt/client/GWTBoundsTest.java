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
