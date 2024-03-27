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
package org.opennms.features.topology.plugins.topo.bsm.info;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

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
import com.vaadin.v7.ui.NativeSelect;

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
            final SetStatusToCriteria currentSetStatusTo = findCriteria(container, vertex);
            final Status selectedStatus = (Status) dropdown.getValue();
            final SetStatusToCriteria newSetStatusTo = new SetStatusToCriteria(vertex.getReductionKey(), selectedStatus);
            if (currentSetStatusTo == null || !Objects.equals(selectedStatus, currentSetStatusTo.getStatus())) {
                if (currentSetStatusTo != null) {
                    container.removeCriteria(currentSetStatusTo);
                }
                container.addCriteria(newSetStatusTo);
            }
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
