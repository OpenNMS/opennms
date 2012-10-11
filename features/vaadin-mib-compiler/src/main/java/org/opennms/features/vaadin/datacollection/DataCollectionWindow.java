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
package org.opennms.features.vaadin.datacollection;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;

import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

/**
 * The Class Data Collection Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class DataCollectionWindow extends Window {

    /**
     * Instantiates a new data collection window.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param fileName the MIB's file name
     * @param dcGroup the OpenNMS data collection group
     * @param logger the logger object
     * 
     * @throws Exception the exception
     */
    public DataCollectionWindow(final DataCollectionConfigDao dataCollectionConfigDao, final String fileName, final DatacollectionGroup dcGroup, final Logger logger) throws Exception {
        super(fileName); // Using fileName for as the window's name.
        setScrollable(true);
        setModal(false);
        setClosable(false);
        setDraggable(false);
        setResizable(false);
        addStyleName(Runo.WINDOW_DIALOG);
        setSizeFull();
        setContent(new DataCollectionGroupPanel(dataCollectionConfigDao, dcGroup, logger) {
            @Override
            public void cancel() {
                close();
            }
            @Override
            public void success() {
                close();
            }
            @Override
            public void failure() {
                close();
            }
        });
    }

}
