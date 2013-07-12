/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.dashboard.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;
import org.opennms.features.vaadin.dashboard.ui.dashboard.DashboardView;
import org.opennms.features.vaadin.dashboard.ui.wallboard.WallboardView;

/**
 * The wallboard application's "main" class
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
@SuppressWarnings("serial")
@Theme("dashboard")
@Title("OpenNMS Dashboard")
public class WallboardUI extends UI implements DashletSelectorAccess {
    /**
     * The {@link DashletSelector} for querying configuration data
     */
    DashletSelector m_dashletSelector;

    /**
     * Assigns the associated {@link DashletSelector}.
     *
     * @param dashletSelector the dashlet selector to be used
     */
    public void setDashletSelector(DashletSelector dashletSelector) {
        this.m_dashletSelector = dashletSelector;
    }

    /**
     * Returns the associated {@link DashletSelector}.
     *
     * @return the dashlet selector
     */
    public DashletSelector getDashletSelector() {
        return m_dashletSelector;
    }

    /**
     * Entry point for a VAADIN application.
     *
     * @param request the {@link VaadinRequest} instance
     */
    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        HeaderLayout headerLayout = new HeaderLayout();
        rootLayout.addComponent(headerLayout);

        VerticalLayout portalWrapper = new VerticalLayout();
        portalWrapper.setSizeFull();
        portalWrapper.setMargin(true);

        rootLayout.addComponent(portalWrapper);
        rootLayout.setExpandRatio(portalWrapper, 1);
        setContent(rootLayout);

        Navigator navigator = new Navigator(this, portalWrapper);

        navigator.addView("dashboard", DashboardView.class);
        navigator.addView("wallboard", WallboardView.class);

        navigator.navigateTo("wallboard");
    }
}
