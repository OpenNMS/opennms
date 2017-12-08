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

package org.opennms.features.topology.api.support.breadcrumbs;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Callbacks;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

/**
 * Criteria to store breadcrumbs in order allow Navigation backwards.
 *
 * @author mvrueden
 */
public class BreadcrumbCriteria extends Criteria {

    private List<Breadcrumb> breadcrumbs = Lists.newArrayList();

    public BreadcrumbCriteria() {

    }

    public void setNewRoot(final Breadcrumb breadcrumb) {
        if (breadcrumbs.contains(breadcrumb)) {
            int index = breadcrumbs.indexOf(breadcrumb);
            breadcrumbs = breadcrumbs.subList(0, index + 1);
        } else {
            breadcrumbs.add(breadcrumb);
        }
    }

    public void clear() {
        breadcrumbs.clear();
    }

    public List<Breadcrumb> getBreadcrumbs() {
        return Collections.unmodifiableList(breadcrumbs);
    }

    public boolean isEmpty() {
        return breadcrumbs.isEmpty();
    }

    @Override
    public ElementType getType() {
        return ElementType.GRAPH;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(breadcrumbs);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof BreadcrumbCriteria) {
            BreadcrumbCriteria other = (BreadcrumbCriteria) obj;
            boolean equals = Objects.equals(breadcrumbs, other.breadcrumbs);
            return equals;
        }
        return false;
    }

    public void setBreadcrumbs(List<Breadcrumb> breadcrumbs) {
        this.breadcrumbs = Lists.newArrayList(Objects.requireNonNull(breadcrumbs));
    }

    private Breadcrumb getNext(Breadcrumb breadcrumb) {
        int index = breadcrumbs.indexOf(breadcrumb);
        if (index == breadcrumbs.size() - 1) {
            return null;
        }
        return breadcrumbs.get(index + 1);
    }

    private Breadcrumb getPrevious(Breadcrumb breadcrumb) {
        int index = breadcrumbs.indexOf(breadcrumb);
        if (index == 0) {
            return null;
        }
        return breadcrumbs.get(index - 1);

    }

    private boolean isLeaf(Breadcrumb breadcrumb) {
        return breadcrumbs.indexOf(breadcrumb) == breadcrumbs.size() - 1;
    }

    public void handleClick(Breadcrumb breadcrumb, GraphContainer graphContainer) {
        final GraphProvider targetGraphProvider = graphContainer.getTopologyServiceClient().getGraphProviderBy(breadcrumb.getTargetNamespace());
        if (isLeaf(breadcrumb)) {
            if (breadcrumb.getSourceVertices().isEmpty()) {
                final List<VertexRef> defaultFocus = targetGraphProvider.getDefaults().getCriteria()
                        .stream()
                        .filter(c -> c instanceof VertexHopGraphProvider.VertexHopCriteria)
                        .map(c -> ((VertexHopGraphProvider.VertexHopCriteria) c).getVertices())
                        .flatMap(Set::stream)
                        .collect(Collectors.toList());
                handleClick(graphContainer, targetGraphProvider, defaultFocus, breadcrumb);
            } else {
                List<VertexRef> oppositeVertices = breadcrumb.getSourceVertices().stream().flatMap(sourceVertex -> getOppositeVertices(graphContainer, breadcrumb.getTargetNamespace(), sourceVertex).stream()).collect(Collectors.toList());
                handleClick(graphContainer, targetGraphProvider, oppositeVertices, breadcrumb);
            }
        } else {
            Breadcrumb next = getNext(breadcrumb);
            handleClick(graphContainer, targetGraphProvider, next.getSourceVertices(), breadcrumb);
        }
    }

    private static List<VertexRef> getOppositeVertices(GraphContainer graphContainer, String targetNamespace, VertexRef sourceVertex) {
        // Find the vertices in other graphs that this vertex links to
        final Collection<VertexRef> oppositeVertices = graphContainer.getTopologyServiceClient().getOppositeVertices(sourceVertex);

        // Filter the vertices for those matching the target namespace
        final List<VertexRef> targetVertices = oppositeVertices.stream()
                .filter(v -> v.getNamespace().matches(targetNamespace))
                .collect(Collectors.toList());

        return targetVertices;
    }

    private static void handleClick(GraphContainer graphContainer, GraphProvider targetGraphProvider, List<VertexRef> verticesToFocus, Breadcrumb breadcrumb) {
        final String targetNamespace = targetGraphProvider.getNamespace();
        final String currentNamespace = graphContainer.getTopologyServiceClient().getNamespace();

        // Only Change the layer if namespace is different, otherwise we would switch to the current layer
        if (!currentNamespace.equals(targetNamespace)) {
            BreadcrumbCriteria breadcrumbCriteria = getBreadcrumbCriteria(graphContainer);
            graphContainer.selectTopologyProvider(targetGraphProvider, Callbacks.applyDefaults(), (graphContainer1, graphProvider) -> graphContainer1.addCriteria(breadcrumbCriteria));
            breadcrumbCriteria.setNewRoot(breadcrumb);
        }

        // Reset focus
        getCriteriaForGraphContainer(graphContainer, VertexHopGraphProvider.VertexHopCriteria.class).forEach(graphContainer::removeCriteria);

        // Set elements to focus
        verticesToFocus.forEach(v -> graphContainer.addCriteria(new VertexHopGraphProvider.DefaultVertexHopCriteria(v)));

        // Render
        graphContainer.setDirty(true);
        graphContainer.redoLayout();
    }


    private static BreadcrumbCriteria getBreadcrumbCriteria(GraphContainer graphContainer) {
        return Criteria.getSingleCriteriaForGraphContainer(graphContainer, BreadcrumbCriteria.class, true);
    }
}
