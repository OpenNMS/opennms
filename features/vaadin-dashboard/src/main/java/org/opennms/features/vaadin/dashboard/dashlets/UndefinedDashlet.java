/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.model.*;

/**
 * This class represents a "undefined" {@link Dashlet} used for error conditions when the
 * required {@link Dashlet} cannot be found.
 *
 * @author Christian Pape
 */
public class UndefinedDashlet extends AbstractDashlet {

    /**
     * Constructor for instantiating this {@link Dashlet}
     *
     * @param dashletSpec the {@link DashletSpec} to use
     */
    public UndefinedDashlet(String name, DashletSpec dashletSpec) {
        super(name, dashletSpec);
    }

    @Override
    public DashletComponent getWallboardComponent() {
        DashletComponent dashletComponent = new AbstractDashletComponent() {
            @Override
            public void refresh() {
            }

            @Override
            public Component getComponent() {
                VerticalLayout verticalLayout = new VerticalLayout();
                Label label = new Label("The specified dashlet could not be found!");
                verticalLayout.addComponent(label);
                verticalLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

                return verticalLayout;
            }
        };
        return dashletComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        return getWallboardComponent();
    }
}
