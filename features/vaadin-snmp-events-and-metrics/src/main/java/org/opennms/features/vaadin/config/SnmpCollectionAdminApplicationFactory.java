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
package org.opennms.features.vaadin.config;

import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.vaadin.extender.AbstractApplicationFactory;

import com.vaadin.ui.UI;

/**
 * A factory for creating SNMP Collection Administration Application objects.
 */
public class SnmpCollectionAdminApplicationFactory extends AbstractApplicationFactory {

    /** The OpenNMS Data Collection Configuration DAO. */
    private DataCollectionConfigDao dataCollectionDao;

    /**
     * Sets the OpenNMS Data Collection Configuration DAO.
     *
     * @param dataCollectionDao the new data collection DAO
     */
    public void setDataCollectionDao(DataCollectionConfigDao dataCollectionDao) {
        this.dataCollectionDao = dataCollectionDao;
    }

    /* (non-Javadoc)
     * @see org.opennms.vaadin.extender.AbstractApplicationFactory#getUI()
     */
    @Override
    public UI createUI() {
        SnmpCollectionAdminApplication app = new SnmpCollectionAdminApplication();
        app.setDataCollectionDao(dataCollectionDao);
        return app;
    }

    /* (non-Javadoc)
     * @see org.opennms.vaadin.extender.AbstractApplicationFactory#getUIClass()
     */
    @Override
    public Class<? extends UI> getUIClass() {
        return SnmpCollectionAdminApplication.class;
    }

}
