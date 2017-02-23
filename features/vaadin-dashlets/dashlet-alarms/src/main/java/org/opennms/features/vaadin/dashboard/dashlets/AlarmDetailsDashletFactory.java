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
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.dao.api.NodeDao;
import org.springframework.transaction.support.TransactionOperations;

/**
 * This class implements a factory used for instantiating new dashlet instances.
 *
 * @author Christian Pape
 */
public class AlarmDetailsDashletFactory extends AbstractDashletFactory {
    /**
     * The {@link AlarmDao} used
     */
    private final AlarmDao m_alarmDao;
    /**
     * The {@link NodeDao} used
     */
    private final NodeDao m_nodeDao;

    private final AlarmRepository m_alarmRepository;

    private final TransactionOperations m_transactionTemplate;

    /**
     * Constructor used for instantiating a new factory.
     *
     * @param alarmDao the {@link AlarmDao} to be used
     * @param nodeDao  the {@link NodeDao} to be used
     */
    public AlarmDetailsDashletFactory(AlarmDao alarmDao, NodeDao nodeDao, AlarmRepository alarmRepository, TransactionOperations transactionTemplate) {
        m_alarmDao = alarmDao;
        m_nodeDao = nodeDao;
        m_alarmRepository = alarmRepository;
        m_transactionTemplate = transactionTemplate;
    }

    /**
     * Method for instantiating a new {@link Dashlet} instance.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return a new {@link Dashlet} instance
     */
    public Dashlet newDashletInstance(DashletSpec dashletSpec) {
        return new AlarmDetailsDashlet(getName(), dashletSpec, m_alarmDao, m_nodeDao, m_alarmRepository, m_transactionTemplate);
    }

    /**
     * Returns the help content {@link String}
     *
     * @return the help content
     */
    @Override
    public String getHelpContentHTML() {
        return "This Dashlet displays a detailed alarm list.";
    }

    /**
     * Returns a custom configuration window.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return the configuration window
     */
    @Override
    public DashletConfigurationWindow configurationWindow(DashletSpec dashletSpec) {
        return new AlarmConfigurationWindow(dashletSpec);
    }
}
