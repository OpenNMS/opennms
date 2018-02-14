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

package org.opennms.features.topology.app.internal.ui.breadcrumbs;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.breadcrumbs.Breadcrumb;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbCriteria;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;


/**
 * Component to visualizes breadcrumbs.
 * It requires a {@link BreadcrumbCriteria} registered to the {@link GraphContainer}.
 * If no criteria is found, it does not show anything.
 *
 * @author mvrueden
 */
public class BreadcrumbComponent extends CustomComponent implements GraphContainer.ChangeListener {

    public BreadcrumbComponent() {
        final HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setSpacing(true);
        setCompositionRoot(rootLayout);
        setId("breadcrumbs");
    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        final BreadcrumbCriteria criteria = Criteria.getSingleCriteriaForGraphContainer(graphContainer, BreadcrumbCriteria.class, true);
        final HorizontalLayout breadcrumbLayout = (HorizontalLayout) getCompositionRoot();
        breadcrumbLayout.removeAllComponents();

        // Verify that breadcrumbs are enabled
        if (graphContainer.getTopologyServiceClient().getBreadcrumbStrategy() == BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT) {
            final Collection<Vertex> displayVertices = graphContainer.getGraph().getDisplayVertices();
            if (!displayVertices.isEmpty()) {
                final PathTree pathTree = BreadcrumbPathCalculator.findPath(graphContainer.getTopologyServiceClient(), displayVertices.stream().map(v -> (VertexRef) v).collect(Collectors.toSet()));
                final List<Breadcrumb> breadcrumbs = pathTree.toBreadcrumbs();
                criteria.setBreadcrumbs(breadcrumbs);
            }
            for (Breadcrumb eachBreadcrumb : criteria.getBreadcrumbs()) {
                if (breadcrumbLayout.getComponentCount() >= 1) {
                    breadcrumbLayout.addComponent(new Label(" > "));
                }
                breadcrumbLayout.addComponent(createButton(graphContainer, eachBreadcrumb));
            }
        }
    }

    private static Button createButton(GraphContainer container, Breadcrumb breadcrumb) {
        final Button button = new Button();
        final String layerName = getLayerName(container, breadcrumb.getTargetNamespace());
        if (breadcrumb.getSourceVertices().isEmpty()) {
            button.setCaption(layerName);
        } else {
            String sourceLayerName = getLayerName(container, breadcrumb.getSourceVertices().get(0).getNamespace());
            if (breadcrumb.getSourceVertices().size() > 2) {
                button.setCaption("Multiple " + layerName);
                button.setDescription(String.format("Multiple vertices from %s", sourceLayerName));
            } else {
                button.setCaption(breadcrumb.getSourceVertices().stream().map(VertexRef::getLabel).collect(Collectors.joining(", ")));
                button.setDescription(String.format("%s from %s", button.getCaption(), sourceLayerName));
            }
        }
        button.addStyleName(BaseTheme.BUTTON_LINK);
        button.addClickListener((event) -> breadcrumb.clicked(container));
        return button;
    }

    private static String getLayerName(GraphContainer container, String namespace) {
        Objects.requireNonNull(container);
        Objects.requireNonNull(namespace);
        return container.getTopologyServiceClient().getGraphProviderBy(namespace).getTopologyProviderInfo().getName();
    }
}
