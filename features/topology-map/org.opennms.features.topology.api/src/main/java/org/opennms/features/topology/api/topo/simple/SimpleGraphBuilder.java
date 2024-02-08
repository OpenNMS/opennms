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
package org.opennms.features.topology.api.topo.simple;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;

public class SimpleGraphBuilder {

	private final BackendGraph m_simpleGraph;
	private AbstractVertex m_currentVertex;
	private AbstractEdge m_currentEdge;
	
	public SimpleGraphBuilder(String namespace) {
		m_simpleGraph = new SimpleGraph(namespace);
	}
	
	public SimpleGraphBuilder vertex(String id) {
		m_currentVertex = new AbstractVertex(ns(), id);
		m_simpleGraph.addVertices(m_currentVertex);
		return this;
	}
	
	public SimpleGraphBuilder vLabel(String label) {
		m_currentVertex.setLabel(label);
		return this;
	}
	
	public SimpleGraphBuilder vTooltip(String tooltipText) {
		m_currentVertex.setTooltipText(tooltipText);
		return this;
	}
	
	public SimpleGraphBuilder vIconKey(String iconKey) {
		m_currentVertex.setIconKey(iconKey);
		return this;
	}
	
	public SimpleGraphBuilder vStyleName(String styleName) {
		m_currentVertex.setStyleName(styleName);
		return this;
	}

	public SimpleGraphBuilder vX(int x) {
		m_currentVertex.setX(x);
		return this;
	}

	public SimpleGraphBuilder vY(int y) {
		m_currentVertex.setY(y);
		return this;
	}
	
	public SimpleGraphBuilder edge(String id, String srcId, String tgtId) {
		VertexRef srcVertex = m_simpleGraph.getVertex(ns(), srcId);
		if (srcVertex == null) {
			srcVertex = new DefaultVertexRef(ns(), srcId);
		}
		VertexRef tgtVertex = m_simpleGraph.getVertex(ns(), tgtId);
		if (tgtVertex == null) {
			tgtVertex = new DefaultVertexRef(ns(), tgtId);
		}
		SimpleConnector source = new SimpleConnector(ns(), srcId+"-"+id+"-connector", srcVertex);
		SimpleConnector target = new SimpleConnector(ns(), tgtId+"-"+id+"-connector", tgtVertex);

		m_currentEdge = new AbstractEdge(ns(), id, source, target);
		source.setEdge(m_currentEdge);
		target.setEdge(m_currentEdge);

		m_simpleGraph.addEdges(m_currentEdge);
		return this;
	}
	
	public SimpleGraphBuilder eLabel(String label) {
		m_currentEdge.setLabel(label);
		return this;
	}
	
	public SimpleGraphBuilder eTooltip(String tooltipText) {
		m_currentEdge.setTooltipText(tooltipText);
		return this;
	}
	
	public SimpleGraphBuilder eStyleName(String styleName) {
		m_currentEdge.setStyleName(styleName);
		return this;
	}
	
	public BackendGraph get() {
		return m_simpleGraph;
	}

	private String ns() {
		return m_simpleGraph.getNamespace();
	}
	
}
