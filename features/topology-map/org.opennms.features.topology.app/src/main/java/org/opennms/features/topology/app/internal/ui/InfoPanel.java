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
import com.vaadin.ui.HorizontalLayout;

public class InfoPanel extends HorizontalLayout {

    private static String ID = "info-panel-component";
    private static String HIDE_TOOLTIP = "Hide info panel";
    private static String SHOW_TOOLTIP = "Show info panel";

    private final CssLayout contentArea = new CssLayout();
    private final CssLayout infoArea = new CssLayout();
    /** Static Components are always visible, if expanded. */
    private final List<Component> staticComponents = Lists.newArrayList();
    private final List<Component> dynamicComponents = Lists.newArrayList();
    private final CssLayout toggleButton;
    /** Defines if the info panel is expanded or not. If true it is expanded, false otherwise. */
    private boolean expanded = true;

    public InfoPanel(SearchBox searchBox, Component mainComponent) {
        setId(ID);
        addStyleName(ID);
        setSizeFull();

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

        contentArea.addStyleName("v-scrollable");
        contentArea.addComponent(mainComponent);
        contentArea.setSizeFull();

        infoArea.addStyleName("info-panel-area");
        infoArea.addStyleName("v-scrollable");

        staticComponents.add(searchBox);
        staticComponents.add(toggleButton);

        addComponents(infoArea, contentArea);
        setExpandRatio(contentArea, 1);
    }

    private void refreshInfoArea() {
        infoArea.removeAllComponents();
        if (expanded) {
            infoArea.removeAllComponents();
            staticComponents.forEach(sc -> infoArea.addComponent(sc));
            dynamicComponents.forEach(c -> infoArea.addComponent(c));
        } else {
            infoArea.addComponent(toggleButton);
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
