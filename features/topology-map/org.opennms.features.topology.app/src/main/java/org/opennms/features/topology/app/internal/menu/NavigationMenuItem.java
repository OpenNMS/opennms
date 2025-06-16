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
package org.opennms.features.topology.app.internal.menu;

import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.breadcrumbs.Breadcrumb;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Menu item to allow navigation to target vertices from a specific source vertex.
 */
public class NavigationMenuItem extends AbstractMenuItem {

    private static final Logger LOG = LoggerFactory.getLogger(NavigationMenuItem.class);
    private final GraphProvider targetGraphProvider;
    private final VertexRef sourceVertex;

    public NavigationMenuItem(GraphProvider targetGraphProvider, VertexRef sourceVertex) {
        this.targetGraphProvider = Objects.requireNonNull(targetGraphProvider);
        this.sourceVertex = Objects.requireNonNull(sourceVertex);
        setLabel(String.format("%s (%s)", targetGraphProvider.getTopologyProviderInfo().getName(), sourceVertex.getLabel()));
    }

    @Override
    public MenuCommand getCommand() {
        return (targets, operationContext) -> {
            Breadcrumb breadcrumb = new Breadcrumb(targetGraphProvider.getNamespace(), sourceVertex);
            BreadcrumbCriteria criteria = Criteria.getSingleCriteriaForGraphContainer(operationContext.getGraphContainer(), BreadcrumbCriteria.class, true);
            criteria.setNewRoot(breadcrumb);
            criteria.handleClick(breadcrumb, operationContext.getGraphContainer());
        };
    }

    @Override
    public boolean isChecked(List<VertexRef> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean isVisible(List<VertexRef> targets, OperationContext operationContext) {
        // Only display the operation, when we have a single vertex selected, and the topology contains multiple graphs
        return targets.size() == 1 && operationContext.getGraphContainer().getTopologyServiceClient().getGraphProviders().size() > 1;
    }

    @Override
    public boolean isEnabled(List<VertexRef> targets, OperationContext operationContext) {
        // Only enable the operation the vertex links to other graphs
        return targets.stream().findFirst()
                .map(v -> operationContext.getGraphContainer().getTopologyServiceClient().getOppositeVertices(v).size() > 0)
                .orElse(false);
    }
}
