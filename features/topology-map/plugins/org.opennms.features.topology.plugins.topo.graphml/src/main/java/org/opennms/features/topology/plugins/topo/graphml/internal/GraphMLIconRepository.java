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
package org.opennms.features.topology.plugins.topo.graphml.internal;

import java.util.Set;

import org.opennms.features.topology.api.IconRepository;

import com.google.common.collect.Sets;

/**
 * Simple {@link IconRepository} for the {@link org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider}.
 * It should contain the Set of iconIds defined by all {@link org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider}s.
 * This enables users to define custom icons (or using already existing icons) by simply defining them in the GraphML file itself.
 *
 * Please note that multiple {@link org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider}
 * could define identical {@link GraphMLIconRepository}s. For now this is not relevant. However if the icons
 * should be configurable from the Icon Selection Dialog in the UI, this issue must be addressed.
 *
 * @author mvrueden
 */
public class GraphMLIconRepository implements IconRepository {

    private Set<String> knownIconKeys = Sets.newHashSet();

    public GraphMLIconRepository(Set<String> knownIconKeys) {
        this.knownIconKeys = knownIconKeys;
    }

    @Override
    public boolean contains(String iconKey) {
        return knownIconKeys.contains(iconKey);
    }

    @Override
    public String getSVGIconId(String iconKey) {
        return iconKey;
    }
}
