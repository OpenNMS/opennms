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

import org.opennms.features.vaadin.dashboard.model.AbstractDashletFactory;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

/**
 * This class implements a factory used for instantiating new dashlet instances.
 *
 * @author Christian Pape
 */
public class BSMDashletFactory extends AbstractDashletFactory {
    /**
     * The {@link BusinessServiceManager} used
     */
    private BusinessServiceManager m_businessServiceManager;

    /**
     * The {@link TransactionAwareBeanProxyFactory} used
     */
    private TransactionAwareBeanProxyFactory m_transactionAwareBeanProxyFactory;

    /**
     * Constructor used for instantiating a new factory.
     *
     * @param businessServiceManager the {@link BusinessServiceManager} to be used
     */
    public BSMDashletFactory(BusinessServiceManager businessServiceManager, TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        m_businessServiceManager = businessServiceManager;
        m_transactionAwareBeanProxyFactory = transactionAwareBeanProxyFactory;
    }

    /**
     * Method for instatiating a new {@link Dashlet} instance.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return a new {@link Dashlet} instance
     */
    public Dashlet newDashletInstance(DashletSpec dashletSpec) {
        return new BSMDashlet(getName(), dashletSpec, m_businessServiceManager, m_transactionAwareBeanProxyFactory);
    }

    /**
     * Returns the help content {@link String}
     *
     * @return the help content
     */
    @Override
    public String getHelpContentHTML() {
        return "This Dashlet displays the status of selected Business Services.";
    }

    /**
     * Returns a custom configuration window.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return the configuration window
     */
    @Override
    public DashletConfigurationWindow configurationWindow(DashletSpec dashletSpec) {
        return new BSMConfigurationWindow(dashletSpec);
    }
}
