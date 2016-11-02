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
