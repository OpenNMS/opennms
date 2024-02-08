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

import java.util.Collection;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.InfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;

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
