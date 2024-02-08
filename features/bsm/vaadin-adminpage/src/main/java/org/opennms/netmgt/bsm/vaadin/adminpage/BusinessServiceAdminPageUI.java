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
package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.springframework.transaction.support.TransactionOperations;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;

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
public class BusinessServiceAdminPageUI extends TransactionAwareUI {

    /**
     * the business service used for querying the Business Service data
     */
    private BusinessServiceManager m_businessServiceManager;

    public BusinessServiceAdminPageUI(final TransactionOperations transactionOperations) {
        super(transactionOperations);
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
        m_businessServiceManager = businessServiceManager;
    }
}
