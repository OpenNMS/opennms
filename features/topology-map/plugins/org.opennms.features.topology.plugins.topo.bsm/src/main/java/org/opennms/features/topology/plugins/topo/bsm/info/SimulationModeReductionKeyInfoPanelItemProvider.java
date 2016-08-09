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

package org.opennms.features.topology.plugins.topo.bsm.info;

import java.util.Arrays;
import java.util.Collections;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.VertexInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.ReductionKeyVertex;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SetStatusToCriteria;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;
import org.opennms.netmgt.bsm.service.model.Status;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;

public class SimulationModeReductionKeyInfoPanelItemProvider extends VertexInfoPanelItemProvider {

    private Component createComponent(ReductionKeyVertex vertex, GraphContainer container) {
        final FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);

        NativeSelect dropdown = new NativeSelect("Severity");
        dropdown.setMultiSelect(false);
        dropdown.setNewItemsAllowed(false);
        dropdown.setNullSelectionAllowed(true);
        dropdown.setImmediate(true);
        dropdown.setRequired(true);
        dropdown.addItems(Arrays.asList(Status.values()));

        SetStatusToCriteria setStatusTo = findCriteria(container, vertex);
        if (setStatusTo != null) {
            dropdown.setValue(setStatusTo.getStatus());
        } else {
            dropdown.setValue(null);
        }

        dropdown.addValueChangeListener(event -> {
            // The set of criteria may have changed since we last queried it above
            // do we issue try finding it again, instead of using the same existing object
            SetStatusToCriteria currentSetStatusTo = findCriteria(container, vertex);
            Status selectedStatus = (Status) dropdown.getValue();
            if (currentSetStatusTo != null) {
                currentSetStatusTo.setStatus(selectedStatus);
            } else {
                currentSetStatusTo = new SetStatusToCriteria(vertex.getReductionKey(), selectedStatus);
                container.addCriteria(currentSetStatusTo);
            }

            // Remove the current selection before redrawing the layout in order
            // to avoid centering on the current vertex
            container.getSelectionManager().setSelectedVertexRefs(Collections.emptyList());
            container.getSelectionManager().setSelectedEdgeRefs(Collections.emptyList());
            container.redoLayout();
        });
        formLayout.addComponent(dropdown);

        return formLayout;
    }

    @Override
    protected boolean contributeTo(VertexRef ref, GraphContainer container) {
        return ref instanceof ReductionKeyVertex && SimulationAwareStateMachineFactory.isInSimulationMode(container.getCriteria());
    }

    protected InfoPanelItem createInfoPanelItem(VertexRef ref, GraphContainer container) {
        return new DefaultInfoPanelItem()
                .withOrder(0)
                .withTitle("Simulate")
                .withComponent(createComponent((ReductionKeyVertex) ref, container));
    }

    private static SetStatusToCriteria findCriteria(GraphContainer container, ReductionKeyVertex vertex) {
        for (SetStatusToCriteria set : container.findCriteria(SetStatusToCriteria.class)) {
            if (vertex.getReductionKey().equals(set.getReductionKey())) {
                return set;
            }
        }
        return null;
    }
}
