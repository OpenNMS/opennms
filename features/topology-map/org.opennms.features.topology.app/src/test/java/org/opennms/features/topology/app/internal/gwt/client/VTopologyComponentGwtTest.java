package org.opennms.features.topology.app.internal.gwt.client;

import com.google.gwt.junit.client.GWTTestCase;

public class VTopologyComponentGwtTest extends GWTTestCase {

    public void testCreateVertex() {
        GWTVertex vertex = GWTVertex.create("1", 100, 100);
        assertEquals("1", vertex.getId());
        assertEquals(100, vertex.getX());
        assertEquals(100, vertex.getY());
        
    }

    @Override
    public String getModuleName() {
        return "org.opennms.features.topology.app.internal.gwt.TopologyAppWidgetSet";
    }
    
    

}
