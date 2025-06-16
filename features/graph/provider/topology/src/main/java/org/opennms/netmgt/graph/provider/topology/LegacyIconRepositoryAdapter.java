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
package org.opennms.netmgt.graph.provider.topology;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.IconRepository;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;

import com.google.common.collect.Sets;

public class LegacyIconRepositoryAdapter implements IconRepository {

    private final GraphProvider delegate;

    public LegacyIconRepositoryAdapter(final GraphProvider delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public boolean contains(String iconKey) {
        return getUsedIconIds().contains(iconKey);
    }

    @Override
    public String getSVGIconId(String iconKey) {
        return iconKey;
    }

    private Set<String> getUsedIconIds() {
        if (delegate.getCurrentGraph() != null) {
            final Set<String> iconIds = delegate.getCurrentGraph().getVertices().stream()
                    .map(Vertex::getIconKey)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return iconIds;
        }
        return Sets.newHashSet();
    }
}
