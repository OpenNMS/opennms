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
import java.util.Optional;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public abstract class VertexInfoPanelItemProvider extends SingleSelectedInfoPanelItemProvider<VertexRef> {

    @Override
    protected Optional<VertexRef> findSingleSelectedItem(GraphContainer container) {
        Collection<VertexRef> selectedVertexRefs = container.getSelectionManager().getSelectedVertexRefs();
        if (selectedVertexRefs.size() == 1) {
            final VertexRef vertexRef = selectedVertexRefs.iterator().next();
            Vertex vertex = container.getTopologyServiceClient().getVertex(vertexRef);
            return Optional.ofNullable(vertex);
        }

        return Optional.empty();
    }
}
