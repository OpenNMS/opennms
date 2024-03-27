/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.dashboard.dashlets;

import org.opennms.features.timeformat.api.TimeformatService;
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

    private final TimeformatService m_timeformatService;

    /**
     * Constructor used for instantiating a new factory.
     *
     * @param alarmDao the {@link AlarmDao} to be used
     * @param nodeDao  the {@link NodeDao} to be used
     */
    public AlarmDetailsDashletFactory(AlarmDao alarmDao, NodeDao nodeDao, AlarmRepository alarmRepository,
                                      TransactionOperations transactionTemplate, TimeformatService timeformatService) {
        m_alarmDao = alarmDao;
        m_nodeDao = nodeDao;
        m_alarmRepository = alarmRepository;
        m_transactionTemplate = transactionTemplate;
        m_timeformatService = timeformatService;
    }

    /**
     * Method for instantiating a new {@link Dashlet} instance.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return a new {@link Dashlet} instance
     */
    public Dashlet newDashletInstance(DashletSpec dashletSpec) {
        return new AlarmDetailsDashlet(getName(), dashletSpec, m_alarmDao, m_nodeDao, m_alarmRepository, m_transactionTemplate
        , m_timeformatService);
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
