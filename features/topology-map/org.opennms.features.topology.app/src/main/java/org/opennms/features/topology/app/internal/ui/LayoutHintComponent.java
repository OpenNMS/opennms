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
package org.opennms.features.topology.app.internal.ui;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.app.internal.ManualLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.LayoutManager;
import org.opennms.netmgt.topology.persistence.api.LayoutEntity;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;


/**
 * Component to indicate the for the current visible vertex a manual layout is persisted and can be applied.
 * The indicator is only shown if the Manual Layout is not selected and the vertices do not match the coordinates of
 * the persisted layout.
 *
 * @author mvrueden
 */
public class LayoutHintComponent extends CustomComponent implements GraphContainer.ChangeListener {

    private final LayoutManager layoutManager;

    public LayoutHintComponent(LayoutManager layoutManager, GraphContainer graphContainer) {
        this.layoutManager = layoutManager;

        final Label icon = new Label();
        icon.setIcon(FontAwesome.INFO_CIRCLE);
        final Label text = new Label("A manual layout exists for the current selection.");

        final HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(icon);
        layout.addComponent(text);
        layout.setDescription("Click to apply the manual layout");
        layout.addLayoutClickListener((event) -> {
            graphContainer.setLayoutAlgorithm(new ManualLayoutAlgorithm(layoutManager));
            graphContainer.redoLayout();
        });
        layout.setSpacing(true);
        setCompositionRoot(layout);
    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        if (!(graphContainer.getLayoutAlgorithm() instanceof ManualLayoutAlgorithm)) {
            LayoutEntity layoutEntity = layoutManager.loadLayout(graphContainer.getGraph());
            if (layoutEntity != null) {
                boolean isEqualLayout = layoutManager.isPersistedLayoutEqualToCurrentLayout(graphContainer.getGraph());
                getCompositionRoot().setVisible(!isEqualLayout);
            } else {
                getCompositionRoot().setVisible(false);
            }
        } else {
            getCompositionRoot().setVisible(false);
        }
    }
}
