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

import static org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.breadcrumbs.Breadcrumb;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbCriteria;

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
        final BreadcrumbCriteria criteria = getBreadcrumbCriteria(graphContainer);
        final HorizontalLayout breadcrumbLayout = (HorizontalLayout) getCompositionRoot();
        breadcrumbLayout.removeAllComponents();

        if (criteria != null) {
            for (Breadcrumb eachBreadcrumb : criteria.getBreadcrumbs()) {
                if (breadcrumbLayout.getComponentCount() >= 1) {
                    breadcrumbLayout.addComponent(new Label(" > "));
                }
                breadcrumbLayout.addComponent(createButton(graphContainer, eachBreadcrumb));
            }
        }
    }

    private static BreadcrumbCriteria getBreadcrumbCriteria(GraphContainer container) {
        return VertexHopCriteria.getSingleCriteriaForGraphContainer(container, BreadcrumbCriteria.class, false);
    }

    private static Button createButton(GraphContainer container, Breadcrumb breadcrumb) {
        Button button = new Button();
        button.addStyleName(BaseTheme.BUTTON_LINK);
        button.setCaption(breadcrumb.getLabel());
        button.addClickListener((event) -> breadcrumb.clicked(container));
        return button;
    }
}
