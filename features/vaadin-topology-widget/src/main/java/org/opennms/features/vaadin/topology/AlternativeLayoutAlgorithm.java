package org.opennms.features.vaadin.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AlternativeLayoutAlgorithm implements LayoutAlgorithm {
	
	private static class Layer extends LinkedHashSet<Vertex> {}
	
	private static class Layout {
		Graph m_graph;
		int m_xSpacing;
		int m_ySpacing;
		List<Layer> m_layers = new ArrayList<Layer>();
		
		
		public Layout(GraphContainer graphContainer, int xSpacing, int ySpacing) {
			m_graph = new Graph(graphContainer);
			m_xSpacing = xSpacing;
			m_ySpacing = ySpacing;
		}

		public void doLayout() {
			
			int szl = m_graph.getSemanticZoomLevel();
			
			Set<Vertex> included = new LinkedHashSet<Vertex>();
			
			List<Vertex> vertices = m_graph.getVertices(szl);
			if (vertices.size() < 1) return;

			Vertex initialVertex = vertices.get(0);
			Layer layer = new Layer();
			layer.add(initialVertex);
			
			while(layer != null) {
				included.addAll(layer);
				m_layers.add(layer);
				System.err.printf("Added layer %d which contains %d vertices totaling  %d\n", m_layers.size(), layer.size(), included.size());
				layer = nextLayer(layer, included);
			}
			

			int maxLayer = -1;
			for(Layer l : m_layers) {
				maxLayer = Math.max(maxLayer, l.size());
			}
			
			int width = m_xSpacing*(maxLayer+1);
			

			for(int i = 0; i < m_layers.size(); i++) {
				Layer l = m_layers.get(i);
				int j = 0;
				int lineLength = m_xSpacing*(l.size()-1);
				int xOffSet = (width-lineLength)/2;
				for(Vertex v : l) {
					v.setX(xOffSet + m_xSpacing*j);
					v.setY(m_ySpacing*(i+1));
					j++;
				}
			}
			
			
		}
		
	    private Layer nextLayer(Layer layer, Set<Vertex> included) {
	    	Layer nextLayer = new Layer();
	    	for(Vertex v : layer) {
	    		Set<Vertex> vertices = new LinkedHashSet<Vertex>(getNeighbors(m_graph, v));
	    		vertices.removeAll(included);
	    		nextLayer.addAll(vertices);
	    	}
	    	
	    	// no more vertices adjacent to existing entries look for disconnected ones
	    	if (nextLayer.isEmpty()) {
	    		Set<Vertex> remaining = new LinkedHashSet<Vertex>(m_graph.getVertices());
	    		remaining.removeAll(included);
	    		if (remaining.isEmpty()) {
	    			nextLayer = null;
	    		} else {
	    			nextLayer.add(remaining.iterator().next());
	    		}
	    	}
	    	
	    	return nextLayer;

		}

		public Set<Vertex> getNeighbors(Graph graph, Vertex vertex){
	        Set<Vertex> neighbors = new HashSet<Vertex>();
	        List<Edge> edges = graph.getEdgesForVertex(vertex, graph.getSemanticZoomLevel());
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
	

    public void updateLayout(GraphContainer graphContainer) {
    	
    	new Layout(graphContainer, 50, 50).doLayout();
    	
    }
    

}
