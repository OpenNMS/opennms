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

import static org.opennms.features.topology.api.topo.Criteria.getSingleCriteriaForGraphContainer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.features.topology.api.Callbacks;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexRefAdapter;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * Element to describe a breadcrumb.
 *
 * @author mvrueden
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="breadcrumb")
public class Breadcrumb implements ClickListener {

    @XmlElement(name="label")
    private String label;

    @XmlElement(name="target-namespace")
    private String targetNamespace;

    @XmlElement(name="source-vertex")
    @XmlJavaTypeAdapter(value= VertexRefAdapter.class)
    private VertexRef sourceVertex;

    // JAXB-Constructor
    protected Breadcrumb() {

    }

    public Breadcrumb(String label, String targetNamespace, VertexRef sourceVertex) {
        this.label = Objects.requireNonNull(label);
        this.targetNamespace = Objects.requireNonNull(targetNamespace);
        this.sourceVertex = sourceVertex;
    }

    public String getLabel() {
        return label;
    }

    public VertexRef getSourceVertex() {
        return sourceVertex;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Breadcrumb) {
            Breadcrumb other = (Breadcrumb) obj;
            boolean equals = Objects.equals(label, other.label)
                    && Objects.equals(targetNamespace, other.targetNamespace)
                    && Objects.equals(sourceVertex, other.sourceVertex);
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, targetNamespace, sourceVertex);
    }

    @Override
    public void clicked(GraphContainer graphContainer) {
        // Only navigate if namespace is different, otherwise we would switch to the current layer
        if (!graphContainer.getBaseTopology().getVertexNamespace().equals(targetNamespace)) {
            navigateTo(graphContainer);
        }
    }

    public void navigateTo(GraphContainer graphContainer) {
        Objects.requireNonNull(graphContainer);
        GraphProvider targetGraphProvider = graphContainer.getMetaTopologyProvider().getGraphProviders()
                .stream()
                .filter(eachGraphProvider -> eachGraphProvider.getVertexNamespace().equals(getTargetNamespace()))
                .findFirst()
                .get();
        navigateTo(graphContainer, this, targetGraphProvider);
    }

    private static void navigateTo(GraphContainer graphContainer, Breadcrumb breadcrumb, GraphProvider targetGraphProvider) {
        // Get the Breadcrumb (before) navigating, otherwise it is lost
        BreadcrumbCriteria breadcrumbCriteria = getSingleCriteriaForGraphContainer(graphContainer, BreadcrumbCriteria.class, true);

        // If no breadcrumb is defined yet, add the starting point (<Layer Name> > <Source Vertex Name>)
        if (breadcrumbCriteria.isEmpty()) {
            final GraphProvider graphProvider = graphContainer.getBaseTopology();
            breadcrumbCriteria.setNewRoot(new Breadcrumb(graphProvider.getTopologyProviderInfo().getName(), graphProvider.getVertexNamespace(), null));
        }

        // Only navigate if namespace is different, otherwise we would switch to the same target,
        // which does not make any sense
        if (!graphContainer.getBaseTopology().getVertexNamespace().equals(targetGraphProvider.getVertexNamespace())) {
            graphContainer.selectTopologyProvider(targetGraphProvider,
                    Callbacks.clearCriteria(),
                    Callbacks.applyDefaultSemanticZoomLevel(),
                    (theGraphContainer, theGraphProvider) -> theGraphContainer.addCriteria(breadcrumbCriteria));
        }

        // Update Criteria for Breadcrumbs
        breadcrumbCriteria.setNewRoot(breadcrumb);
        VertexRef sourceVertex = breadcrumb.getSourceVertex();
        // If we have a source, add the opposite vertices to focus
        if (sourceVertex != null) {
            // Find the vertices in other graphs that this vertex links to
            final Collection<VertexRef> oppositeVertices = graphContainer.getMetaTopologyProvider().getOppositeVertices(sourceVertex);

            // Filter the vertices for those matching the target namespace
            final String targetNamespace = targetGraphProvider.getVertexNamespace();
            final List<VertexRef> targetVertices = oppositeVertices.stream()
                    .filter(v -> v.getNamespace().matches(targetNamespace))
                    .collect(Collectors.toList());

            // Add the target vertices to focus
            targetVertices.stream().forEach(v -> graphContainer.addCriteria(new VertexHopGraphProvider.DefaultVertexHopCriteria(v)));

            // If target vertices are empty, apply default focus
            if (targetVertices.isEmpty()) {
                Callbacks.applyDefaultCriteria().callback(graphContainer, targetGraphProvider);
            }
        } else {
            Callbacks.applyDefaultCriteria().callback(graphContainer, targetGraphProvider);
        }

        // Render
        graphContainer.redoLayout();
    }
}
