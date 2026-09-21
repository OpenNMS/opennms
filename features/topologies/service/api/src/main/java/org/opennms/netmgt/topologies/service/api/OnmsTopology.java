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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OnmsTopology {

    private Set<OnmsTopologyVertex> m_vertices;
    private Set<OnmsTopologyEdge> m_edges;
    private Map<String, OnmsTopologyVertex> m_vertexIndex;
    private Map<String, OnmsTopologyEdge> m_edgeIndex;
    private OnmsTopologyVertex m_defaultVertex;

    public OnmsTopology() {
        m_vertices = new HashSet<>();
        m_edges = new HashSet<>();
        m_vertexIndex = new HashMap<>();
        m_edgeIndex = new HashMap<>();
    }

    public OnmsTopologyVertex getVertex(String id) {
        return m_vertexIndex.get(id);
    }

    public OnmsTopologyEdge getEdge(String id) {
        return m_edgeIndex.get(id);
    }

    public Set<OnmsTopologyVertex> getVertices() {
        return m_vertices;
    }

    public void addVertex(OnmsTopologyVertex v) {
        m_vertices.add(v);
        m_vertexIndex.put(v.getId(), v);
    }

    public void setVertices(Set<OnmsTopologyVertex> vertices) {
        m_vertices = vertices;
        m_vertexIndex = new HashMap<>();
        for (OnmsTopologyVertex v : vertices) {
            m_vertexIndex.put(v.getId(), v);
        }
    }

    public Set<OnmsTopologyEdge> getEdges() {
        return m_edges;
    }

    public void addEdge(OnmsTopologyEdge e) {
        m_edges.add(e);
        m_edgeIndex.put(e.getId(), e);
    }

    public void setEdges(Set<OnmsTopologyEdge> edges) {
        m_edges = edges;
        m_edgeIndex = new HashMap<>();
        for (OnmsTopologyEdge e : edges) {
            m_edgeIndex.put(e.getId(), e);
        }
    }    

    public boolean hasVertex(String id) {
        return m_vertexIndex.containsKey(id);
    }
    
    public boolean hasEdge(String id) {
        return m_edgeIndex.containsKey(id);
    }
    
    public OnmsTopology clone() {
        OnmsTopology topo = new OnmsTopology();
        topo.m_vertices = new HashSet<>(m_vertices);
        topo.m_edges = new HashSet<>(m_edges);
        topo.m_vertexIndex = new HashMap<>(m_vertexIndex);
        topo.m_edgeIndex = new HashMap<>(m_edgeIndex);
        topo.m_defaultVertex = m_defaultVertex;
        return topo;
    }

    public OnmsTopologyVertex getDefaultVertex() {
        return m_defaultVertex;
    }

    public void setDefaultVertex(OnmsTopologyVertex defaultVertex) {
        m_defaultVertex = defaultVertex;
    }
}

