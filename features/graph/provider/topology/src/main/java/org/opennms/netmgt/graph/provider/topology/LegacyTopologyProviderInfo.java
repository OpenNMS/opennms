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

import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public class LegacyTopologyProviderInfo implements TopologyProviderInfo {

    private final GraphInfo delegate;

    public LegacyTopologyProviderInfo(final GenericGraph genericGraph) {
        this(Objects.requireNonNull(genericGraph).getGraphInfo());
    }

    public LegacyTopologyProviderInfo(final GraphInfo graphInfo) {
        this.delegate = Objects.requireNonNull(graphInfo);
    }

    @Override
    public String getName() {
        return delegate.getLabel();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public boolean isHierarchical() {
        return false;
    }

    @Override
    public boolean isSupportsCategorySearch() {
        return false;
    }
}
