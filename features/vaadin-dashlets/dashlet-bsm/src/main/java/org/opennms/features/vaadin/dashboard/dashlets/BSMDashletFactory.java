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
