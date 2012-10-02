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
package org.opennms.features.vaadin.config;

import java.io.File;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.datacollection.DataCollectionGroupPanel;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Runo;

@SuppressWarnings("serial")
public class SnmpCollectionAdminApplication extends Application {

    /** The OpenNMS Data Collection Configuration DAO. */
    private DataCollectionConfigDao dataCollectionDao;

    /**
     * Sets the OpenNMS Data Collection Configuration DAO.
     *
     * @param eventConfDao the new OpenNMS Data Collection Configuration DAO
     */
    public void setDataCollectionDao(DataCollectionConfigDao dataCollectionDao) {
        this.dataCollectionDao = dataCollectionDao;
    }

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        if (dataCollectionDao == null)
            throw new RuntimeException("dataCollectionDao cannot be null.");

        setTheme(Runo.THEME_NAME);
        final File eventsDir = new File(ConfigFileConstants.getFilePathString(), "datacollection");

        final VerticalLayout layout = new VerticalLayout();
        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setMargin(true);

        final ComboBox dcGroupSource = new ComboBox();
        toolbar.addComponent(dcGroupSource);
        dcGroupSource.setImmediate(true);
        dcGroupSource.setNullSelectionAllowed(false);
        dcGroupSource.setContainerDataSource(new FilesystemContainer(eventsDir));
        dcGroupSource.setItemCaptionPropertyId(FilesystemContainer.PROPERTY_NAME);
        dcGroupSource.addListener(new ComboBox.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                final File file = (File) event.getProperty().getValue();
                LogUtils.infof(this, "Loading data collection data from %s", file);
                DatacollectionGroup dcGroup = JaxbUtils.unmarshal(DatacollectionGroup.class, file);
                layout.removeComponent(layout.getComponent(1));
                layout.addComponent(createDataCollectionGroupPanel(file, dcGroup));
            }
        });

        final Button add = new Button("+");
        toolbar.addComponent(add);
        add.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                EventFileNameWindow w = new EventFileNameWindow() {
                    @Override
                    public void filenameChangeHandler(String fileName) {
                        File file = new File(eventsDir, fileName);
                        LogUtils.infof(this, "Adding new data collection file %s", file);
                        DatacollectionGroup dcGroup = new DatacollectionGroup();
                        dcGroup.setName(file.getName());
                        layout.removeComponent(layout.getComponent(1));
                        layout.addComponent(createDataCollectionGroupPanel(file, dcGroup));
                    }
                };
                getMainWindow().addWindow(w);
            }
        });

        final Button remove = new Button("-");
        toolbar.addComponent(remove);
        remove.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                getMainWindow().showNotification("Not impementet yet!");
            }
        });

        layout.addComponent(toolbar);
        layout.addComponent(new Label("Please select a data collection group file from the above toolbar"));
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        final Window mainWindow = new Window("SNMP Collection Administration", layout);
        setMainWindow(mainWindow);
    }

    /**
     * Creates a data collection group panel
     * 
     * @param file data collection group file name
     * @param dcGroup the data collection group object
     * 
     * @return a new data collection group panel object
     */
    private DataCollectionGroupPanel createDataCollectionGroupPanel(final File file, final DatacollectionGroup dcGroup) {
        DataCollectionGroupPanel panel = new DataCollectionGroupPanel(dcGroup, new SimpleLogger()) {
            @Override
            public void cancel() {
                this.setVisible(false);
            }
            @Override
            public void success() {
                getMainWindow().showNotification("Event file " + file.getName() + " has been successfuly saved.");
                this.setVisible(false);
            }
            @Override
            public void failure() {
                getMainWindow().showNotification("Event file " + file.getName() + " cannot be saved.", Notification.TYPE_ERROR_MESSAGE);
            }
        };
        panel.setCaption("Data Collection from " + file.getName());
        return panel;
    }

}
