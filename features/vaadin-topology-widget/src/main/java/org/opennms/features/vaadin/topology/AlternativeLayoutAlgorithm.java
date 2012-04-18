package org.opennms.features.vaadin.topology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlternativeLayoutAlgorithm implements LayoutAlgorithm {

    public void updateLayout(Graph graph) {
        Vertex vertex = graph.getVertices().get(0);
        //Set<Vertex> neighbors = getNeighbors(graph, vertex);
        List<Vertex> vertices = graph.getVertices();
        int i = 0;
        for(Vertex v : vertices) {
            
            v.setX(50 + (50*i));
            v.setY(50);
            i++;
        }
    }
    
    public Set<Vertex> getNeighbors(Graph graph, Vertex vertex){
        Set<Vertex> neighbors = new HashSet<Vertex>();
        List<Edge> edges = graph.getEdgesForVertex(vertex);
        for(Edge edge : edges) {
            if(edge.getSource() != vertex ) {
                neighbors.add(edge.getSource());
            }else if(edge.getTarget() != vertex) {
                neighbors.add(edge.getTarget());
            }
        }
        
        return neighbors;
    }

}
