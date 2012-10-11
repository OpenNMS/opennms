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

import java.io.File;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.datacollection.DataCollectionGroupPanel;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * The Class Data Collection Group Administration Panel.
 */
//TODO When deleting a group, all the SNMP collections must be updated.
@SuppressWarnings("serial")
public class DataCollectionGroupAdminPanel extends VerticalLayout {

    /**
     * Instantiates a new data collection group administration panel.
     *
     * @param dataCollectionDao the OpenNMS data collection configuration DAO
     * @param logger the logger
     */
    public DataCollectionGroupAdminPanel(final DataCollectionConfigDao dataCollectionDao, final Logger logger) {
        setCaption("Data Collection Groups");
        final File datacollectionDir = new File(ConfigFileConstants.getFilePathString(), "datacollection");

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setMargin(true);

        final VerticalLayout self = this;

        final ComboBox dcGroupSource = new ComboBox();
        toolbar.addComponent(dcGroupSource);
        dcGroupSource.setImmediate(true);
        dcGroupSource.setNullSelectionAllowed(false);
        dcGroupSource.setContainerDataSource(new FilesystemContainer(datacollectionDir));
        dcGroupSource.setItemCaptionPropertyId(FilesystemContainer.PROPERTY_NAME);
        dcGroupSource.addListener(new ComboBox.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                final File file = (File) event.getProperty().getValue();
                LogUtils.infof(this, "Loading data collection data from %s", file);
                DatacollectionGroup dcGroup = JaxbUtils.unmarshal(DatacollectionGroup.class, file);
                self.removeComponent(getComponent(1));
                self.addComponent(createDataCollectionGroupPanel(dataCollectionDao, file, dcGroup));
            }
        });

        final Button add = new Button("Add File");
        toolbar.addComponent(add);
        add.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                PromptWindow w = new PromptWindow("New Data Collection Group", "Group Name") {
                    @Override
                    public void textFieldChanged(String fieldValue) {
                        File file = new File(datacollectionDir, fieldValue.replaceAll(" ", "_") + ".xml");
                        LogUtils.infof(this, "Adding new data collection file %s", file);
                        DatacollectionGroup dcGroup = new DatacollectionGroup();
                        dcGroup.setName(fieldValue);
                        self.removeComponent(getComponent(1));
                        self.addComponent(createDataCollectionGroupPanel(dataCollectionDao, file, dcGroup));
                    }
                };
                getApplication().getMainWindow().addWindow(w);
            }
        });

        final Button remove = new Button("Remove File");
        toolbar.addComponent(remove);
        remove.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                getApplication().getMainWindow().showNotification("Not impementet yet!");
            }
        });

        addComponent(toolbar);
        addComponent(new Label(""));
        setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Creates a data collection group panel.
     *
     * @param dataCollectionDao the OpenNMS data collection configuration DAO
     * @param file data collection group file name
     * @param dcGroup the data collection group object
     * @return a new data collection group panel object
     */
    private DataCollectionGroupPanel createDataCollectionGroupPanel(final DataCollectionConfigDao dataCollectionDao, final File file, final DatacollectionGroup dcGroup) {
        DataCollectionGroupPanel panel = new DataCollectionGroupPanel(dataCollectionDao, dcGroup, new SimpleLogger()) {
            @Override
            public void cancel() {
                this.setVisible(false);
            }
            @Override
            public void success() {
                getApplication().getMainWindow().showNotification("Data collection group file " + file.getName() + " has been successfuly saved.");
                this.setVisible(false);
            }
            @Override
            public void failure() {
                getApplication().getMainWindow().showNotification("Data collection group file " + file.getName() + " cannot be saved.", Notification.TYPE_ERROR_MESSAGE);
            }
        };
        panel.setCaption("Data Collection from " + file.getName());
        return panel;
    }

}
