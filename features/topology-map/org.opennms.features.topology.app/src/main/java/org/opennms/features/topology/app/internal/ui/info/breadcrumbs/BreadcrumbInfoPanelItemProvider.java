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

package org.opennms.features.topology.app.internal.ui.info.breadcrumbs;

import static org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;

import java.util.Collection;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.InfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.operations.NavigateToOperation;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;


/**
 * Visualizes an existing {@link BreadcrumbCriteria}.
 * If no criteria is found, it does not provide any components.
 *
 * @author mvrueden
 */
public class BreadcrumbInfoPanelItemProvider implements InfoPanelItemProvider {

    @Override
    public Collection<? extends InfoPanelItem> getContributions(GraphContainer container) {
        return InfoPanelItemProvider.contributeIf(
                getBreadcrumbCriteria(container) != null,
                () -> new DefaultInfoPanelItem()
                        .withOrder(-100)
                        .withTitle("Breadcrumbs")
                        .withComponent(createComponent(container)));
    }

    // Component for the breadcrumbs
    private Component createComponent(GraphContainer container) {
        final BreadcrumbCriteria criteria = getBreadcrumbCriteria(container);
        final HorizontalLayout navigationLayout = new HorizontalLayout();
        for (BreadcrumbCriteria.Breadcrumb eachBreadcrumb : criteria.getBreadcrumbs()) {
            if (navigationLayout.getComponentCount() >= 1) {
                navigationLayout.addComponent(new Label(" > "));
            }
            navigationLayout.addComponent(createButton(container, eachBreadcrumb.getSourceVertex(), eachBreadcrumb.getTargetNamespace()));
        }
        navigationLayout.setSpacing(true);
        return navigationLayout;
    }

    private static BreadcrumbCriteria getBreadcrumbCriteria(GraphContainer container) {
        return VertexHopCriteria.getSingleCriteriaForGraphContainer(container, BreadcrumbCriteria.class, false);
    }

    private static Button createButton(GraphContainer container, VertexRef sourceVertex, String targetNamespace) {
        Button button = new Button();
        button.addStyleName(BaseTheme.BUTTON_LINK);
        button.addClickListener((event) -> {
            // only navigate if namespace is different, otherwise we switch to the same target, which does not make any sense
            if (!container.getBaseTopology().getVertexNamespace().equals(targetNamespace)) {
                new NavigateToOperation().navigateTo(container, sourceVertex, targetNamespace);
            }
        });
        button.setCaption(sourceVertex.getLabel());
        return button;
    }
}
