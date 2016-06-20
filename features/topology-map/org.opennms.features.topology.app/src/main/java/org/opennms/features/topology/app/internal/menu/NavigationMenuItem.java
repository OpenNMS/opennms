/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.menu;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.ui.breadcrumbs.BreadcrumbCriteria;
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
        return new MenuCommand() {
            @Override
            public void execute(List<VertexRef> targets, OperationContext operationContext) {
                final GraphContainer graphContainer = operationContext.getGraphContainer();
                navigateTo(graphContainer, sourceVertex, targetGraphProvider);
            }

            private void navigateTo(GraphContainer graphContainer, VertexRef sourceVertex, GraphProvider targetGraphProvider) {
                final String targetNamespace = targetGraphProvider.getVertexNamespace();

                // Get the Breadcrumb (before) navigating, otherwise it is lost
                BreadcrumbCriteria breadcrumbCriteria = VertexHopGraphProvider.VertexHopCriteria.getSingleCriteriaForGraphContainer(graphContainer, BreadcrumbCriteria.class, true);

                // If no breadcrumb is defined yet, add source before target.
                if (breadcrumbCriteria.isEmpty()) {
                    final GraphProvider graphProvider = graphContainer.getBaseTopology();
                    breadcrumbCriteria.setNewRoot(new BreadcrumbCriteria.Breadcrumb(
                            graphProvider.getTopologyProviderInfo().getName(),
                            (theGraphContainer) -> theGraphContainer.selectTopologyProvider(graphProvider, true)));
                }
                graphContainer.selectTopologyProvider(targetGraphProvider, false);

                // TODO: Consolidate that this is configurable and we can define a default SZL and default Focus per layer
                // TODO: Use a default SZL per graph?
                graphContainer.clearCriteria(); // Remove all criteria
                graphContainer.setSemanticZoomLevel(targetGraphProvider.getDefaultSzl());  // Use the default SZL
                graphContainer.addCriteria(breadcrumbCriteria); // add it again, it was cleared

                // Find the vertices in other graphs that this vertex links to
                final Collection<VertexRef> oppositeVertices = graphContainer.getMetaTopologyProvider().getOppositeVertices(sourceVertex);

                // Filter the vertices for those matching the target namespace
                final List<VertexRef> targetVertices = oppositeVertices.stream()
                        .filter(v -> v.getNamespace().matches(targetNamespace))
                        .collect(Collectors.toList());

                // Add the target vertices to focus
                targetVertices.stream().forEach(v -> graphContainer.addCriteria(new VertexHopGraphProvider.DefaultVertexHopCriteria(v)));

                // Update Criteria for Breadcrumbs
                breadcrumbCriteria.setNewRoot(new BreadcrumbCriteria.Breadcrumb(
                        sourceVertex.getLabel(),
                        (theGraphContainer) -> {
                            // only navigate if namespace is different, otherwise we switch to the same target, which does not make any sense
                            if (!theGraphContainer.getBaseTopology().getVertexNamespace().equals(targetNamespace)) {
                                navigateTo(theGraphContainer, sourceVertex, targetGraphProvider);
                            }
                        }));

                // Render
                graphContainer.redoLayout();
            }
        };
    }

    @Override
    public boolean isChecked(List<VertexRef> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean isVisible(List<VertexRef> targets, OperationContext operationContext) {
        // Only display the operation, when we have a single vertex selected, and the topology contains multiple graphs
        final MetaTopologyProvider metaTopologyProvider = operationContext.getGraphContainer().getMetaTopologyProvider();
        return targets.size() == 1 && metaTopologyProvider.getGraphProviders().size() > 1;
    }

    @Override
    public boolean isEnabled(List<VertexRef> targets, OperationContext operationContext) {
        // Only enable the operation the vertex links to other graphs
        final MetaTopologyProvider metaTopologyProvider = operationContext.getGraphContainer().getMetaTopologyProvider();
        return targets.stream().findFirst()
                .map(v -> metaTopologyProvider.getOppositeVertices(v).size() > 0)
                .orElse(false);
    }
}
