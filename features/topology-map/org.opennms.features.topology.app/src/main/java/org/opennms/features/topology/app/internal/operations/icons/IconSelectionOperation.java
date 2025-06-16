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
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.ui.icons.IconSelectionDialog;

/**
 * Operation to open a {@link IconSelectionDialog} to allow the user to change the icon for the currently selected
 * Vertex.
 */
public class IconSelectionOperation implements Operation {

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        final AbstractVertex vertex = (AbstractVertex) targets.get(0);
        final String preSelectedIconId = operationContext.getGraphContainer().getIconManager().getSVGIconId(vertex);

        new IconSelectionDialog(preSelectedIconId)
                .withOkAction(iconWindow -> {
                    final IconManager iconManager = operationContext.getGraphContainer().getIconManager();
                    final String newIconId = iconWindow.getSelectedIcon();

                    String newIconKey = iconManager.setIconMapping(vertex, newIconId);
                    if (newIconKey != null) {
                        // We have to temporary update the icon key, otherwise the icon is not updated (redoLayout has no effect)
                        vertex.setIconKey(newIconKey);

                        // Redo the layout to apply new icon
                        operationContext.getGraphContainer().setDirty(true);
                        operationContext.getGraphContainer().redoLayout();
                    }
                })
                .open();
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
        if (targets.size() != 1 || !(targets.get(0) instanceof AbstractVertex)) {
            return false;
        }
        return operationContext.getGraphContainer().getIconManager().findRepositoryByIconKey(((AbstractVertex) targets.get(0)).getIconKey()) != null;
    }

    @Override
    public String getId() {
        return "contextIconSelection";
    }
}
