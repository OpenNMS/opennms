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

import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.opennms.features.vaadin.components.graph.InlineGraphContainer;
import com.vaadin.v7.ui.Label;

public class InfoPanel extends CssLayout {

    private static String ID = "info-panel-component";
    private static String HIDE_TOOLTIP = "Hide info panel";
    private static String SHOW_TOOLTIP = "Show info panel";

    /** Static Components are always visible, if expanded. */
    private final List<Component> staticComponents = Lists.newArrayList();
    private final List<Component> dynamicComponents = Lists.newArrayList();
    private final CssLayout toggleButton;
    /** Defines if the info panel is expanded or not. If true it is expanded, false otherwise. */
    private boolean expanded = true;

    public InfoPanel(SearchBox searchBox) {
        setId(ID);
        addStyleName(ID);
        addStyleName("v-scrollable");

        // A CssLayout is a simple "div"-element. This makes it much easier to custom style it
        toggleButton = new CssLayout();
        toggleButton.setStyleName("toggle-button");
        toggleButton.setDescription(HIDE_TOOLTIP);
        toggleButton.addLayoutClickListener((event) -> {
            expanded = !expanded;
            if (expanded) {
                toggleButton.removeStyleName("info-panel-collapsed");
                toggleButton.setDescription(HIDE_TOOLTIP);
            } else {
                toggleButton.addStyleName("info-panel-collapsed");
                toggleButton.setDescription(SHOW_TOOLTIP);
            }
            refreshInfoArea();
        });

        staticComponents.add(searchBox);
        staticComponents.add(toggleButton);
    }

    private void refreshInfoArea() {
        removeAllComponents();
        if (expanded) {
            staticComponents.forEach(this::addComponent);
            dynamicComponents.forEach(this::addComponent);

            // Add an empty component with width = 350px to always force the max length
            // This is required as otherwise the left area of the info panel would be empty, even if the info panel
            // is not shown.
            Label label = new Label();
            label.setWidth(350, Unit.PIXELS);
            addComponent(label);

            // Add a graph container to trigger backshift graph renderings on each update
            addComponent(new InlineGraphContainer());
            
        } else {
            addComponent(toggleButton);
        }
    }

    /**
     * These components change according to the selection in the Topology.
     *
     * @param dynamicComponents
     */
    public void setDynamicComponents(List<Component> dynamicComponents) {
        this.dynamicComponents.clear();
        this.dynamicComponents.addAll(dynamicComponents);
        refreshInfoArea();
    }
}
