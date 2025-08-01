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
package org.opennms.features.topology.api.info;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.Ref;

public abstract class SingleSelectedInfoPanelItemProvider<T extends Ref> implements InfoPanelItemProvider {

    @Override
    public Collection<? extends InfoPanelItem> getContributions(GraphContainer container) {
        return findSingleSelectedItem(container)
                .filter(ref -> contributeTo(ref, container))
                .map(ref -> Collections.singleton(createInfoPanelItem(ref, container)))
                .orElseGet(Collections::emptySet);
    }

    protected abstract boolean contributeTo(T ref, GraphContainer graphContainer);

    protected abstract InfoPanelItem createInfoPanelItem(T ref, GraphContainer graphContainer);

    protected abstract Optional<T> findSingleSelectedItem(GraphContainer container);
}
