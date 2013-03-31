/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.config;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.datacollection.SnmpCollectionPanel;
import org.opennms.netmgt.config.DataCollectionConfigDao;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.Reindeer;

/**
 * The Class SNMP Collection Administration Application.
 */
@SuppressWarnings("serial")
@Title("SNMP Collection Administration")
@Theme(Reindeer.THEME_NAME)
public class SnmpCollectionAdminApplication extends UI {

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

    @Override
    public void init(VaadinRequest request) {
        if (dataCollectionDao == null)
            throw new RuntimeException("dataCollectionDao cannot be null.");

        Logger logger = new SimpleLogger();
        SnmpCollectionPanel scAdmin = new SnmpCollectionPanel(dataCollectionDao, logger);
        DataCollectionGroupAdminPanel dcgAdmin = new DataCollectionGroupAdminPanel(dataCollectionDao);

        TabSheet tabs = new TabSheet();
        tabs.setStyleName(Reindeer.TABSHEET_SMALL);
        tabs.setSizeFull();
        tabs.addTab(scAdmin);
        tabs.addTab(dcgAdmin);

        setContent(tabs);
    }
}
