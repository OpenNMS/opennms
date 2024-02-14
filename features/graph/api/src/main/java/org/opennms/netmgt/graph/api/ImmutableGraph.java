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
package org.opennms.netmgt.graph.api;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public interface ImmutableGraph<V extends Vertex, E extends Edge> extends GraphInfo {

    List<V> getVertices();

    List<E> getEdges();

    V getVertex(String id);

    E getEdge(String id);

    List<String> getVertexIds();

    List<String> getEdgeIds();

    ImmutableGraph<V, E> getView(Collection<V> verticesInFocus, int szl);

    List<V> resolveVertices(Collection<String> vertexIds);

    List<V> resolveVertices(NodeRef nodeRef);

    V resolveVertex(VertexRef vertexRef);

    List<E> resolveEdges(Collection<String> edgeIds);

    Collection<V> getNeighbors(V eachVertex);

    Collection<E> getConnectingEdges(V eachVertex);

    Focus getDefaultFocus();

    GenericGraph asGenericGraph();

}
