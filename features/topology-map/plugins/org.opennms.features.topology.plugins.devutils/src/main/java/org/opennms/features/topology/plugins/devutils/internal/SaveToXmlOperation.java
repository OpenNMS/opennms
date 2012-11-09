/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
    	
    	TopologyProvider topologyProvider = operationContext.getGraphContainer().getDataSource();
    	
		Map<Object, WrappedVertex> idMap = new HashMap<Object, WrappedVertex>();
		
		// first create all the vertices;
		List<WrappedVertex> vertices = new ArrayList<WrappedVertex>();
		for(Object vertexId : topologyProvider.getVertexIds()) {
			WrappedVertex wrappedVertex = WrappedVertex.create(topologyProvider.getVertexItem(vertexId));
			vertices.add(wrappedVertex);
			idMap.put(vertexId, wrappedVertex);
		}
		
		// then set the parents for each
		for(Object vertexId : topologyProvider.getVertexIds()) {
			Object parentId = topologyProvider.getVertexContainer().getParent(vertexId);
			WrappedVertex vertex = idMap.get(vertexId);
			WrappedVertex parent = idMap.get(parentId);
			
			vertex.setParent((WrappedGroup)parent);
		}
		
		// then create the edges
		List<WrappedEdge> edges = new ArrayList<WrappedEdge>();
		for(Object edgeId : topologyProvider.getEdgeIds()) {
			
			Collection<?> vertexIds = topologyProvider.getEndPointIdsForEdge(edgeId);
			
			Iterator<?> it = vertexIds.iterator();
			
			Object sourceId = it.next();
			Object targetId = it.next();
			
			WrappedVertex source = idMap.get(sourceId);
			WrappedVertex target = idMap.get(targetId);
			
			edges.add(new WrappedEdge(topologyProvider.getEdgeItem(edgeId), source, target));
			

		}
		
		WrappedGraph graph = new WrappedGraph(topologyProvider.getNamespace(), vertices, edges);
		
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