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
package org.opennms.features.topology.api.topo.simple;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;

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
