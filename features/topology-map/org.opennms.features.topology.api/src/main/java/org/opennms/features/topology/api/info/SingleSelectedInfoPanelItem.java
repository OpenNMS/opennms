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

package org.opennms.features.topology.api.info;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.Ref;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Component;

/**
 * Info Panel for selectable items.
 *
 * @param <T> The type of the selectable item, e.g. VertexRef or EdgeRef
 */
public abstract class SingleSelectedInfoPanelItem<T extends Ref> implements InfoPanelItem {

    private static final String NOTHING_SELECTED = "Neither a vertex nor a edge is selected.";

    @Override
    public Component getComponent(GraphContainer container) {
        T ref = findSingleSelectedItem(container);
        Preconditions.checkState(ref != null, NOTHING_SELECTED);
        return getComponent(ref, container);
    }

    @Override
    public boolean contributesTo(GraphContainer container) {
        T ref = findSingleSelectedItem(container);
        if (ref != null) {
            return contributesTo(ref, container);
        }
        return false;
    }

    @Override
    public String getTitle(GraphContainer container) {
        T ref = findSingleSelectedItem(container);
        Preconditions.checkState(ref != null, NOTHING_SELECTED);
        return getTitle(ref);
    }

    protected abstract boolean contributesTo(T ref, GraphContainer graphContainer);

    protected abstract Component getComponent(T ref, GraphContainer graphContainer);

    protected abstract T findSingleSelectedItem(GraphContainer container);

    protected abstract String getTitle(T ref);
}
