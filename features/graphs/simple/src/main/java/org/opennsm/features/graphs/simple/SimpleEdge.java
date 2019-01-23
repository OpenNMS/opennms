/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennsm.features.graphs.simple;

import java.util.Objects;

import org.opennms.features.graph.api.Edge;
import org.opennms.features.graph.api.Graph;
import org.opennms.features.graph.api.generic.GenericEdge;

public class SimpleEdge<V extends SimpleVertex> implements Edge<V> {

    private final V source;
    private final V target;
    private String namespace;
    private String id;
    private String label;
    private String tooltip;

    // TODO MVR set namespace and id
    public SimpleEdge(V source, V target) {
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.namespace = source.getNamespace();
        this.id = source.getId() + "->" + target.getId();
    }

    // Convenient method to add an adge from the given graph. Source and Target Vertices are looked up using the given sourceId/targetId
    public SimpleEdge(Graph<V, SimpleEdge<V>> graph, String sourceId, String targetId) {
        Objects.requireNonNull(graph);
        this.source = Objects.requireNonNull(graph.getVertex(sourceId));
        this.target = Objects.requireNonNull(graph.getVertex(targetId));
        this.namespace = graph.getNamespace();
        this.id = source.getId() + "->" + target.getId();
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getId() {
        return id;
    }

    // TODO MVR this should be handled automatically in some sort?!
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public V getSource() {
        return source;
    }

    @Override
    public V getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public GenericEdge asGenericEdge() {
//        // TODO MVR set namespace and id
//        final GenericEdge genericEdge = new GenericEdge(source, target);
//        if (getLabel() != null) {
//            genericEdge.setProperty(GenericProperties.LABEL, getLabel());
//        } else {
//            genericEdge.setProperty(GenericProperties.LABEL, String.format("connection:%s:%s", getSource().getId(), getTarget().getId()));
//        }
//        if (getTooltip() != null) {
//            genericEdge.setProperty(GenericProperties.TOOLTIP, getTooltip());
//        }
//        return genericEdge;
        return null;
    }

    @Override
    public String toString() {
        return asGenericEdge().toString();
    }
}
