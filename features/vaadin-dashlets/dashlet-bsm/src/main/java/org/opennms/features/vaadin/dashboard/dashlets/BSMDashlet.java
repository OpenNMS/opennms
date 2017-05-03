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

package org.opennms.features.vaadin.dashboard.dashlets;

import java.util.List;

import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteria;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * This class represents a Alert Dashlet with minimum details.
 *
 * @author Christian Pape
 */
public class BSMDashlet extends AbstractDashlet {

    private class BSMDashletComponent extends AbstractDashletComponent {

        private final GridLayout m_gridLayout;

        private BSMDashletComponent(int rowCount, int columnCount) {
            m_gridLayout = new GridLayout(columnCount, rowCount);
            m_gridLayout.setCaption(getName());
            m_gridLayout.setWidth("100%");
            refresh();
        }

        @Override
        public void refresh() {
            m_gridLayout.removeAllComponents();
            final List<BusinessService> services = m_businessServiceManager.search(m_businessServiceSearchCriteria);
            if (services.isEmpty()) {
                m_gridLayout.addComponent(new Label("There are no Business Services with matching criterias found."));
            } else {
                for (BusinessService eachService : services) {
                    m_gridLayout.addComponent(createRow(eachService));
                }
            }
            boosted = false;
        }

        @Override
        public Component getComponent() {
            return m_gridLayout;
        }
    };
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
     * the search criteria
     */
    private BusinessServiceSearchCriteria m_businessServiceSearchCriteria;
    /**
     * the column count for Ops board
     */
    private int m_columnCountBoard;
    /**
     * the column count for Ops panel
     */
    private int m_columnCountPanel;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec            the {@link DashletSpec} to be used
     * @param businessServiceManager the {@link BusinessServiceManager} to be used
     */
    public BSMDashlet(String name, DashletSpec dashletSpec, BusinessServiceManager businessServiceManager, TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        super(name, dashletSpec);
        m_businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
        m_businessServiceSearchCriteria = BSMConfigHelper.fromMap(getDashletSpec().getParameters());

        m_columnCountBoard = BSMConfigHelper.getIntForKey(getDashletSpec().getParameters(), "columnCountBoard", 10);

        m_columnCountPanel = BSMConfigHelper.getIntForKey(getDashletSpec().getParameters(), "columnCountPanel", 5);
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_wallboardComponent == null) {
            m_wallboardComponent = new BSMDashletComponent(1, m_columnCountBoard);
        }
        return m_wallboardComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        if (m_dashboardComponent == null) {
            m_dashboardComponent = new BSMDashletComponent(1, m_columnCountPanel);
        }
        return m_dashboardComponent;
    }

    private HorizontalLayout createRow(BusinessService service) {
        HorizontalLayout rowLayout = new HorizontalLayout();
        rowLayout.setSizeFull();
        rowLayout.setSpacing(true);

        final Status severity = m_businessServiceManager.getOperationalStatus(service);
        Label nameLabel = new Label(service.getName());
        nameLabel.setSizeFull();
        nameLabel.setStyleName("h3");
        nameLabel.addStyleName("bright");
        nameLabel.addStyleName("severity");
        nameLabel.addStyleName(severity.getLabel());

        rowLayout.addComponent(nameLabel);
        return rowLayout;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }
}
