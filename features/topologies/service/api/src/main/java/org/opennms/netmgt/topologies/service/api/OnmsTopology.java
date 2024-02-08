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
package org.opennms.netmgt.topologies.service.api;

import java.util.HashSet;
import java.util.Set;

public class OnmsTopology {

    private Set<OnmsTopologyVertex> m_vertices;
    private Set<OnmsTopologyEdge> m_edges;
    private OnmsTopologyVertex m_defaultVertex;

    public OnmsTopology() {
        m_vertices = new HashSet<OnmsTopologyVertex>();
        m_edges = new HashSet<OnmsTopologyEdge>();
    }

    public OnmsTopologyVertex getVertex(String id) {
        return m_vertices.stream().filter(vertex -> id.equals(vertex.getId())).findAny().orElse(null);
    }

    public OnmsTopologyEdge getEdge(String id) {
        return m_edges.stream().filter(edge -> id.equals(edge.getId())).findAny().orElse(null);
    }

    public Set<OnmsTopologyVertex> getVertices() {
        return m_vertices;
    }

    public void addVertex(OnmsTopologyVertex v) {
        m_vertices.add(v);
    }

    public void setVertices(Set<OnmsTopologyVertex> vertices) {
        m_vertices = vertices;
    }

    public Set<OnmsTopologyEdge> getEdges() {
        return m_edges;
    }

    public void addEdge(OnmsTopologyEdge e) {
        m_edges.add(e);
    }

    public void setEdges(Set<OnmsTopologyEdge> edges) {
        m_edges = edges;
    }    

    public boolean hasVertex(String id) {
        return (getVertex(id) != null);
    }
    
    public boolean hasEdge(String id) {
        return (getEdge(id) != null);
    }
    
    public OnmsTopology clone() {
        OnmsTopology topo = new OnmsTopology();
        topo.setVertices(new HashSet<>(m_vertices));
        topo.setEdges(new HashSet<>(m_edges));
        topo.setDefaultVertex(m_defaultVertex);
        return topo;
    }

    public OnmsTopologyVertex getDefaultVertex() {
        return m_defaultVertex;
    }

    public void setDefaultVertex(OnmsTopologyVertex defaultVertex) {
        m_defaultVertex = defaultVertex;
    }
}

