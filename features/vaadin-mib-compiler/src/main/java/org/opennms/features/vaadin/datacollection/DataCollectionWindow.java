/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.io.StringWriter;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
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
     * Instantiates a new events window.
     *
     * @param caption the caption
     * @param dcGroup the data collection group
     * @param logger the logger
     */
    public DataCollectionWindow(final String caption, final DatacollectionGroup dcGroup, final Logger logger) {
        super(caption);
        setScrollable(true);
        setModal(false);
        setClosable(false);
        setDraggable(false);
        setResizable(false);
        addStyleName(Runo.WINDOW_DIALOG);
        setSizeFull();
        setContent(new DataCollectionGroupPanel(dcGroup, logger) {
            @Override
            public void cancelProcessing() {
                close();
            }
            @Override
            public void generateDataCollectionFile(DatacollectionGroup group) {
                close();
                processDataCollection(group, logger);
            }
        });
    }

    /**
     * Process data collection.
     *
     * @param dcGroup the OpenNMS Data Collection Group
     * @param logger the logger
     */
    /*
     * FIXME This is just for testing purposes
     */
    public void processDataCollection(final DatacollectionGroup dcGroup, final Logger logger) {
        StringWriter writer = new StringWriter();
        try {
            JaxbUtils.marshal(dcGroup, writer);
            logger.debug("The XML file:<pre>" + writer.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</pre>");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
