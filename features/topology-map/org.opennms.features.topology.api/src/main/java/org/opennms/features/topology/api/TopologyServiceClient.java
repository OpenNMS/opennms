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

import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public interface TopologyServiceClient extends SelectionAware {
    Vertex getVertex(VertexRef target, Criteria... criteria);

    String getNamespace();

    Vertex getVertex(String namespace, String vertexId);

    int getVertexTotalCount();

    int getEdgeTotalCount();

    TopologyProviderInfo getInfo();

    Defaults getDefaults();

//    List<Vertex> getChildren(VertexRef vertexId, Criteria[] criteria);

    Collection<GraphProvider> getGraphProviders();

    Collection<VertexRef> getOppositeVertices(VertexRef vertexRef);

    GraphProvider getGraphProviderBy(String namespace);

    GraphProvider getDefaultGraphProvider();

    LayoutAlgorithm getPreferredLayoutAlgorithm();

    BreadcrumbStrategy getBreadcrumbStrategy();

    void setMetaTopologyId(String metaTopologyId);

    String getMetaTopologyId();

    void setNamespace(String namespace);

    Graph getGraph(Criteria[] criteria, int semanticZoomLevel);
}
