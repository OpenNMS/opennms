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

import org.opennms.features.vaadin.datacollection.SnmpCollectionPanel;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;

/**
 * The Class SNMP Collection Administration Application.
 */
@Theme("opennms")
@Title("SNMP Collection Administration")
@SuppressWarnings("serial")
public class SnmpCollectionAdminApplication extends UI {

    /** The OpenNMS Data Collection Configuration DAO. */
    private DataCollectionConfigDao dataCollectionDao;

    /**
     * Sets the OpenNMS Data Collection Configuration DAO.
     *
     * @param dataCollectionDao the new OpenNMS data collection DAO
     */
    public void setDataCollectionDao(DataCollectionConfigDao dataCollectionDao) {
        this.dataCollectionDao = dataCollectionDao;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
     */
    @Override
    public void init(VaadinRequest request) {
        if (dataCollectionDao == null)
            throw new RuntimeException("dataCollectionDao cannot be null.");

        TabSheet tabs = new TabSheet();
        tabs.addStyleName("light");
        tabs.setSizeFull();
        tabs.addTab(new SnmpCollectionPanel(dataCollectionDao, new SimpleLogger()));
        tabs.addTab(new DataCollectionGroupAdminPanel(dataCollectionDao));

        setContent(tabs);
    }
}
