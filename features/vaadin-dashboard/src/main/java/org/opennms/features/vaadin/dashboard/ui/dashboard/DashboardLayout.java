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
package org.opennms.features.vaadin.dashboard.ui.dashboard;

import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.vaadin.addon.portallayout.container.PortalColumns;
import org.vaadin.addon.portallayout.portal.StackPortalLayout;

import java.util.List;

/**
 * This class implements a portal-like dashboard.
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class DashboardLayout extends PortalColumns {
    /**
     * The columns to be used
     */
    private StackPortalLayout[] m_columns = new StackPortalLayout[3];

    /**
     * Default constructor.
     */
    public DashboardLayout() {
        setSizeFull();

        for (int i = 0; i < m_columns.length; i++) {
            m_columns[i] = new StackPortalLayout();
            m_columns[i].setSizeFull();
            m_columns[i].setMargin(true);
            m_columns[i].setSpacing(true);
            appendPortal(m_columns[i]);
        }
    }

    /**
     * Method for retrieving {@link Dashlet} instances for a given {@link DashletSpec}.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     * @return the new {@link Dashlet} instance
     */
    private Dashlet getDashletInstance(DashletSpec dashletSpec) {
        DashletSelector dashletSelector = ((DashletSelectorAccess) getUI()).getDashletSelector();
        return dashletSelector.getDashletFactoryForName(dashletSpec.getDashletName()).newDashletInstance(dashletSpec);
    }

    /**
     * This method sets the {@link List} of {@link DashletSpec} instances.
     *
     * @param dashletSpecs the list of {@link DashletSpec} instances
     */
    public void setDashletSpecs(List<DashletSpec> dashletSpecs) {

        int c = 0;
        int i = 0;

        for (DashletSpec dashletSpec : dashletSpecs) {
            Dashlet dashlet = getDashletInstance(dashletSpec);

            dashlet.update();

            boolean boosted = dashlet.isBoosted();

            m_columns[i].portletFor(dashlet);

            i++;

            if (i % m_columns.length == 0) {
                c++;
                i = 0;
            }
        }
    }
}
