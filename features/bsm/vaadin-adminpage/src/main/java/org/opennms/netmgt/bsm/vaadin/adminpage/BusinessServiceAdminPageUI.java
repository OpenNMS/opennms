/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

/**
 * Business Service Admin Vaadin UI: this class is the main entry point for the Vaadin application
 * responsible for configuring the Business Service definitions.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
@Theme("opennms")
@Title("Business Service Admin Page")
@SuppressWarnings("serial")
public class BusinessServiceAdminPageUI extends UI {

    /**
     * wrapper for transaction-based service instances
     */
    private final TransactionAwareBeanProxyFactory m_transactionAwareBeanProxyFactory;

    /**
     * the business service used for querying the Business Service data
     */
    private BusinessServiceManager m_businessServiceManager;

    /**
     * Constructor
     *
     * @param transactionAwareBeanProxyFactory the instance of the transaction wrapper
     */
    public BusinessServiceAdminPageUI(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.m_transactionAwareBeanProxyFactory = Objects.requireNonNull(transactionAwareBeanProxyFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(VaadinRequest request) {
        setContent(new BusinessServiceMainLayout(m_businessServiceManager));
    }

    /**
     * Sets the associated Business Service Manager instance.
     *
     * @param businessServiceManager the instance to be used
     */
    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        Objects.requireNonNull(businessServiceManager);
        this.m_businessServiceManager = m_transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }
}
