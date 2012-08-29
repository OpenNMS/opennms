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

import java.io.File;
import java.io.FileWriter;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;

import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

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
     * TODO Validations
     * 
     * - Check if there is no DCGroup with the same name
     */
    public void processDataCollection(final DatacollectionGroup dcGroup, final Logger logger) {
        final File configDir = new File(ConfigFileConstants.getHome(), "etc/datacollection/");
        final File file = new File(configDir, getCaption().replaceFirst("\\..*$", ".xml"));
        if (file.exists()) {
            MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                    "Are you sure?",
                    MessageBox.Icon.QUESTION,
                    "Do you really want to override the existig file?<br/>All current information will be lost.",
                    new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                    new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        saveFile(file, dcGroup, logger);
                    }
                }
            });
        } else {
            saveFile(file, dcGroup, logger);
        }
    }

    /**
     * Save file.
     *
     * @param file the file
     * @param dcGroup the datacollection-group
     * @param logger the logger
     */
    private void saveFile(final File file, final DatacollectionGroup dcGroup, final Logger logger) {
        try {
            FileWriter writer = new FileWriter(file);
            JaxbUtils.marshal(dcGroup, writer);
            logger.info("Saving XML data into " + file.getAbsolutePath());
            logger.warn("Remember to update datacollection-config.xml to include the group " + dcGroup.getName() + " and restart OpenNMS.");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        close();
    }

}
