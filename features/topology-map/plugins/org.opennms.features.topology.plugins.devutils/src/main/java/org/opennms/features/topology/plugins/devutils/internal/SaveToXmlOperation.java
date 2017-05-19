/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.WrappedEdge;
import org.opennms.features.topology.api.topo.WrappedGraph;
import org.opennms.features.topology.api.topo.WrappedVertex;

public class SaveToXmlOperation implements Operation {

	@Override
	public void execute(List<VertexRef> targets, OperationContext operationContext) {

		GraphProvider graphProvider = operationContext.getGraphContainer().getBaseTopology();

		Map<String, WrappedVertex> idMap = new HashMap<String, WrappedVertex>();

		// first create all the vertices;
		List<WrappedVertex> vertices = new ArrayList<WrappedVertex>();
		for(Vertex vertex : graphProvider.getVertices()) {
			WrappedVertex wrappedVertex = WrappedVertex.create(vertex);
			vertices.add(wrappedVertex);
			idMap.put(vertex.getId(), wrappedVertex);
		}

		// then set the parents for each
		for(Vertex vertex : graphProvider.getVertices()) {
			Vertex parent = graphProvider.getParent(vertex);
			if (parent != null) {
				WrappedVertex wrappedVertex = idMap.get(vertex.getId());
				WrappedVertex wrappedParent = idMap.get(parent.getId());
				wrappedVertex.parent = wrappedParent;
			}
		}

		// then create the edges
		List<WrappedEdge> edges = new ArrayList<WrappedEdge>();
		for(Edge edge : graphProvider.getEdges()) {
			WrappedVertex wrappedSource = idMap.get(edge.getSource().getVertex().getId());
			WrappedVertex wrappedTarget = idMap.get(edge.getTarget().getVertex().getId());
			edges.add(new WrappedEdge(edge, wrappedSource, wrappedTarget));
		}

		WrappedGraph graph = new WrappedGraph(graphProvider.getVertexNamespace(), vertices, edges);

		JAXB.marshal(graph, new File("/tmp/saved-graph.xml"));
	}

	@Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
		return true;
	}

	@Override
	public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
		return true;
	}

	@Override
	public String getId() {
		return "SaveToXML";
	}
}
