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
package org.opennms.features.topology.app.internal.operations.icons;

import java.util.List;

import org.opennms.features.topology.api.IconManager;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * An operation to allow the user to reset all icon mappings and restore the defaults.
 */
public class IconResetOperation implements Operation {

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        IconManager iconManager = operationContext.getGraphContainer().getIconManager();
        final boolean[] updated = {false};
        targets.forEach(vertex -> {
            updated[0] |= iconManager.removeIconMapping((Vertex) vertex);
        });

        // Redo the layout to apply new icon
        if (updated[0]) {
            // HACK! We have no concept of "get the default icon for a vertex" at the moment.
            // In order to populate the icon, we have to redo the layout
            operationContext.getGraphContainer().setDirty(true);
            operationContext.getGraphContainer().redoLayout();
        }
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        if (operationContext.getDisplayLocation() != OperationContext.DisplayLocation.CONTEXTMENU) {
            return false;
        }
        return !targets.isEmpty();
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        // only enabled, if all elements are a Vertex
        return targets.stream()
                      .allMatch(v -> v instanceof Vertex
                                  && operationContext.getGraphContainer().getIconManager().findRepositoryByIconKey(((Vertex) v).getIconKey()) != null);
    }

    @Override
    public String getId() {
        return "contextIconReset";
    }
}
