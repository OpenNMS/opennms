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

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.app.internal.ManualLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.LayoutManager;
import org.opennms.netmgt.topology.persistence.api.LayoutEntity;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;


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
