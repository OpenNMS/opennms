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

package org.opennms.features.topology.api.topo;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;

import com.google.common.collect.Lists;

/**
 * A {@link MetaTopologyProvider} that supports a single {@link GraphProvider}.
 *
 * @author jwhite
 */
public class SimpleMetaTopologyProvider implements MetaTopologyProvider {

    private final GraphProvider graphProvider;
    private String preferredLayout;

    public SimpleMetaTopologyProvider(GraphProvider graphProvider) {
        this.graphProvider = Objects.requireNonNull(graphProvider);
    }

    @Override
    public String getId() {
        return getGraphProviders().stream()
                .sorted(Comparator.comparing(GraphProvider::getNamespace))
                .map(g -> g.getNamespace())
                .collect(Collectors.joining(":"));
    }

    @Override
    public GraphProvider getDefaultGraphProvider() {
        return graphProvider;
    }

    @Override
    public Collection<VertexRef> getOppositeVertices(VertexRef vertexRef) {
        return Collections.emptySet();
    }

    @Override
    public List<GraphProvider> getGraphProviders() {
        return Lists.newArrayList(graphProvider);
    }

    @Override
    public GraphProvider getGraphProviderBy(String namespace) {
        return getGraphProviders()
                .stream()
                .filter(provider -> provider.getNamespace().equals(namespace))
                .findFirst().orElse(null);
    }

    @Override
    public BreadcrumbStrategy getBreadcrumbStrategy() {
        return BreadcrumbStrategy.NONE;
    }

    public void setPreferredLayout(String preferredLayout) {
        this.preferredLayout = preferredLayout;
    }

    public String getPreferredLayout() {
        return preferredLayout;
    }
}
