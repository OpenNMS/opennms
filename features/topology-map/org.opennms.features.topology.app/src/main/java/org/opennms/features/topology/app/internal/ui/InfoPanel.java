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

package org.opennms.features.topology.app.internal.ui;

import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.opennms.features.vaadin.components.graph.InlineGraphContainer;
import com.vaadin.ui.Label;

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
