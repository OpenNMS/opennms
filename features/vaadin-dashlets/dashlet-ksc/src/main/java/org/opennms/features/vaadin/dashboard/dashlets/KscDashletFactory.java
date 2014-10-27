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

import org.opennms.features.vaadin.dashboard.model.AbstractDashletFactory;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 09.10.13
 * Time: 22:29
 * To change this template use File | Settings | File Templates.
 */
public class KscDashletFactory extends AbstractDashletFactory {
    private NodeDao m_nodeDao;
    private ResourceDao m_resourceDao;
    private TransactionOperations m_transactionOperations;

    /**
     * Method for creating a new {@link org.opennms.features.vaadin.dashboard.model.Dashlet} instance.
     *
     * @param dashletSpec the {@link org.opennms.features.vaadin.dashboard.model.DashletSpec} to use
     * @return a new {@link org.opennms.features.vaadin.dashboard.model.Dashlet} instance
     */
    public Dashlet newDashletInstance(DashletSpec dashletSpec) {
        return new KscDashlet(getName(), dashletSpec, m_nodeDao, m_resourceDao, m_transactionOperations);
    }

    /**
     * Returns the help content {@link String}
     *
     * @return the help content
     */
    @Override
    public String getHelpContentHTML() {
        return "This Dashlet provides a view to the OpenNMS Rrd graphs. It is configurable via a custom configuration dialog.";
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        this.m_resourceDao = resourceDao;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.m_transactionOperations = transactionOperations;
    }

    /**
     * Returns a custom configuration window.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return the configuration window
     */
    @Override
    public DashletConfigurationWindow configurationWindow(DashletSpec dashletSpec) {
        return new KscDashletConfigurationWindow(dashletSpec);
    }
}
