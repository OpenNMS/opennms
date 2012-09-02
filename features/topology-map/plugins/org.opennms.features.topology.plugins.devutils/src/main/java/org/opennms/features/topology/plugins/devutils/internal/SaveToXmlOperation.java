package org.opennms.features.topology.plugins.devutils.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;


public class SaveToXmlOperation implements Operation {
    
    TopologyProvider m_topologyProvider;
	
    public SaveToXmlOperation(TopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }

    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
    	
    	
    	
		Map<Object, WrappedVertex> idMap = new HashMap<Object, WrappedVertex>();
		
		// first create all the vertices;
		List<WrappedVertex> vertices = new ArrayList<WrappedVertex>();
		for(Object vertexId : m_topologyProvider.getVertexIds()) {
			WrappedVertex wrappedVertex = WrappedVertex.create(m_topologyProvider.getVertexItem(vertexId));
			vertices.add(wrappedVertex);
			idMap.put(vertexId, wrappedVertex);
		}
		
		// then set the parents for each
		for(Object vertexId : m_topologyProvider.getVertexIds()) {
			Object parentId = m_topologyProvider.getVertexContainer().getParent(vertexId);
			WrappedVertex vertex = idMap.get(vertexId);
			WrappedVertex parent = idMap.get(parentId);
			
			vertex.setParent((WrappedGroup)parent);
		}
		
		// then create the edges
		List<WrappedEdge> edges = new ArrayList<WrappedEdge>();
		for(Object edgeId : m_topologyProvider.getEdgeIds()) {
			
			Collection<?> vertexIds = m_topologyProvider.getEndPointIdsForEdge(edgeId);
			
			Iterator<?> it = vertexIds.iterator();
			
			Object sourceId = it.next();
			Object targetId = it.next();
			
			WrappedVertex source = idMap.get(sourceId);
			WrappedVertex target = idMap.get(targetId);
			
			edges.add(new WrappedEdge(m_topologyProvider.getEdgeItem(edgeId), source, target));
			

		}
		
		WrappedGraph graph = new WrappedGraph(vertices, edges);
		
        JAXB.marshal(graph, new File("/tmp/saved-graph.xml"));

		
		return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return "SaveToXML";
    }
}