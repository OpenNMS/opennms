package org.opennms.features.vaadin.topology;

public class SimpleLayoutAlgorithm implements LayoutAlgorithm {

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.topology.LayoutAlgorithm#updateLayout(org.opennms.features.vaadin.topology.Graph)
     */
    public void updateLayout(Graph graph) {
        int r = 100;
        int cx = 500;
        int cy = 500;
        for(int i = 0; i < graph.getVertices().size(); i++) {
            Vertex vertex = graph.getVertices().get(i);
            if(i == 0) {
                vertex.setX(cx);
                vertex.setY(cy);
            }else {
    	        int n = i - 1;
    	        double a = (2*Math.PI)/(graph.getVertices().size() -1);
    	        
    	        int x = (int) (r * Math.cos(n*a) + cx);
    	        int y = (int) (r * Math.sin(n*a) + cy);
    	        
    	        vertex.setX(x);
    	        vertex.setY(y);
            }
        }
    }
    
}
