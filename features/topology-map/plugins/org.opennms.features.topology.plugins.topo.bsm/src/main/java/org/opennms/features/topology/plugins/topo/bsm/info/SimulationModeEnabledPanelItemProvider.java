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

import java.util.Collection;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.InfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class SimulationModeEnabledPanelItemProvider implements InfoPanelItemProvider {

    private Component createComponent() {
        Label label = new Label("Simulation Mode Enabled");
        label.setDescription("Simulation Mode is enabled");
        label.setIcon(FontAwesome.EXCLAMATION_TRIANGLE);
        label.addStyleName("warning");

        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(label);
        layout.addStyleName("simulation");
        return layout;
    }

    @Override
    public Collection<InfoPanelItem> getContributions(GraphContainer container) {
        return InfoPanelItemProvider.contributeIf(
                SimulationAwareStateMachineFactory.isInSimulationMode(container.getCriteria()),
                () -> new DefaultInfoPanelItem()
                        .withOrder(Integer.MIN_VALUE)
                        .withComponent(createComponent()));
    }
}
