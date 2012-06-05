package org.opennms.features.topology.app.internal;

import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;

public class SimpleLayoutAlgorithm implements LayoutAlgorithm {

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.topology.LayoutAlgorithm#updateLayout(org.opennms.features.vaadin.topology.Graph)
     */
    public void updateLayout(GraphContainer graphContainer) {
    	int szl = graphContainer.getSemanticZoomLevel();
    	Graph graph = new Graph(graphContainer);
        int r = 100;
        int cx = 500;
        int cy = 500;
        List<Vertex> vertices = graph.getVertices(szl);
		for(int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
			System.err.println("Laying out Vertex " + vertex);
            if(i == 0) {
                vertex.setX(cx);
                vertex.setY(cy);
            }else {
    	        int n = i - 1;
    	        double a = (2*Math.PI)/(vertices.size() -1);
    	        
    	        int x = (int) (r * Math.cos(n*a) + cx);
    	        int y = (int) (r * Math.sin(n*a) + cy);
    	        
    	        vertex.setX(x);
    	        vertex.setY(y);
            }
        }
    }
    
}
