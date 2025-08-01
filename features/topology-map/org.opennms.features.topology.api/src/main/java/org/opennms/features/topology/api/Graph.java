/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.api;

import java.util.Collection;
import java.util.Map;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public interface Graph {

	/**
	 * @return The layout which defines x and y positions of the graphs vertices.
     */
	Layout getLayout();

	void setLayout(Layout layout);

	Collection<Vertex> getDisplayVertices();
	
	Collection<Edge> getDisplayEdges();

	Edge getEdgeByKey(String edgeKey);
	
	Vertex getVertexByKey(String vertexKey);

	Map<? extends VertexRef, ? extends Status> getVertexStatus();

	Map<? extends EdgeRef, ? extends Status> getEdgeStatus();

	void visit(GraphVisitor visitor) throws Exception;
}
