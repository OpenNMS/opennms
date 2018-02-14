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
