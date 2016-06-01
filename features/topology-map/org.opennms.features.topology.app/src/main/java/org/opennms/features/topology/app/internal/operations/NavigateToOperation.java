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

package org.opennms.features.topology.app.internal.operations;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.ui.info.breadcrumbs.BreadcrumbCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigateToOperation implements Constants, Operation {

    private static final Logger LOG = LoggerFactory.getLogger(NavigateToOperation.class);

	@Override
	public void execute(List<VertexRef> targets, final OperationContext operationContext) {
	    final GraphContainer graphContainer = operationContext.getGraphContainer();
	    final MetaTopologyProvider metaTopologyProvider = graphContainer.getMetaTopologyProvider();
        final Optional<VertexRef> vertexRef = targets.stream().findFirst();
        if (!vertexRef.isPresent()) {
            return;
        }

        // Find the vertices in other graphs that this vertex links to
        final Collection<VertexRef> oppositeVertices = metaTopologyProvider.getOppositeVertices(vertexRef.get());

        // We don't currently offer the user to select which graph to jump to, so select the first namespace
        final String targetNamespace = oppositeVertices.stream().findFirst()
                .map(v -> v.getNamespace()).orElse(null);

        navigateTo(graphContainer, vertexRef.get(), targetNamespace);
	}

    public void navigateTo(GraphContainer graphContainer, VertexRef sourceVertex, String targetNamespace) {
        // Find the graph provider for the target namespace
        final GraphProvider targetGraphProvider = graphContainer.getMetaTopologyProvider().getGraphProviders().stream()
                .filter(g -> g.getVertexNamespace().equals(targetNamespace))
                .findFirst().orElse(null);
        if (targetGraphProvider == null) {
            LOG.warn("No graph provider found for namespace '{}'.", targetNamespace);
            return;
        }
        // Get the Breadcrumb (before) navigating, otherwise it is lost
        BreadcrumbCriteria breadcrumbCriteria = VertexHopGraphProvider.VertexHopCriteria.getSingleCriteriaForGraphContainer(graphContainer, BreadcrumbCriteria.class, true);
        graphContainer.selectTopologyProvider(targetGraphProvider, false);

        // TODO: Consolidate that this is configurable and we can define a default SZL and default Focus per layer
        // TODO: Use a default SZL per graph?
        graphContainer.clearCriteria(); // Remove all criteria
        graphContainer.setSemanticZoomLevel(1); // Reset the SZL to 1
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
        breadcrumbCriteria.setNewRoot(sourceVertex, targetNamespace);

        // Render
        graphContainer.redoLayout();
    }

	@Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
	    // Only display the operation, when we have a single vertex selected, and the topology contains multiple graphs
	    final MetaTopologyProvider metaTopologyProvider = operationContext.getGraphContainer().getMetaTopologyProvider();
	    return targets.size() == 1 && metaTopologyProvider.getGraphProviders().size() > 1;
	}

	@Override
	public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        // Only enable the operation the vertex links to other graphs
	    final MetaTopologyProvider metaTopologyProvider = operationContext.getGraphContainer().getMetaTopologyProvider();
        return targets.stream().findFirst()
                .map(v -> metaTopologyProvider.getOppositeVertices(v).size() > 0)
                .orElse(false);
	}

	@Override
	public String getId() {
		return NavigateToOperation.class.getCanonicalName();
	}
}
