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

import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents a Alert Dashlet with minimum details.
 *
 * @author Christian Pape
 */
public class BSMDashlet extends AbstractDashlet {
    /**
     * The {@link BusinessServiceManager} used
     */
    private BusinessServiceManager m_businessServiceManager;
    /**
     * boosted value
     */
    private boolean boosted = false;
    /**
     * wallboard layout
     */
    private DashletComponent m_wallboardComponent = null;
    /**
     * dashboard layout
     */
    private DashletComponent m_dashboardComponent = null;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     * @param businessServiceManager    the {@link BusinessServiceManager} to be used
     */
    public BSMDashlet(String name, DashletSpec dashletSpec, BusinessServiceManager businessServiceManager) {
        super(name, dashletSpec);
        /**
         * Setting the member fields
         */
        m_businessServiceManager = businessServiceManager;
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_wallboardComponent == null) {
            m_wallboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setWidth("100%");
                    refresh();
                }

                @Override
                public void refresh() {
                    m_verticalLayout.removeAllComponents();

                    boosted = false;

                    m_verticalLayout.addComponent(new Label("Wallboard"));
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }
        return m_wallboardComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        if (m_dashboardComponent == null) {
            m_dashboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setWidth("100%");
                    refresh();
                }

                @Override
                public void refresh() {
                    m_verticalLayout.removeAllComponents();

                    boosted = false;

                    m_verticalLayout.addComponent(new Label("Dashboard"));
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }
        return m_dashboardComponent;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }
}
