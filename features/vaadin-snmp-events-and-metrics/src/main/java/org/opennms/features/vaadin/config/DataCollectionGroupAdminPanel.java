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
import java.io.FileWriter;
import java.util.Iterator;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.datacollection.DataCollectionGroupPanel;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

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
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Class Data Collection Group Administration Panel.
 */
// TODO When deleting a group, all the SNMP collections UI components must be updated.
@SuppressWarnings("serial")
public class DataCollectionGroupAdminPanel extends VerticalLayout {
    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionGroupAdminPanel.class);

    private String m_selectedGroup;

    /**
     * Instantiates a new data collection group administration panel.
     *
     * @param dataCollectionDao the OpenNMS data collection configuration DAO
     */
    public DataCollectionGroupAdminPanel(final DataCollectionConfigDao dataCollectionDao) {
        setCaption("Data Collection Groups");

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setMargin(true);

        final Label comboLabel = new Label("Select Data Collection Group File");
        toolbar.addComponent(comboLabel);
        toolbar.setComponentAlignment(comboLabel, Alignment.MIDDLE_LEFT);

        final File datacollectionDir = new File(ConfigFileConstants.getFilePathString(), "datacollection");
        final ComboBox dcGroupSource = new ComboBox();
        toolbar.addComponent(dcGroupSource);
        dcGroupSource.setImmediate(true);
        dcGroupSource.setNullSelectionAllowed(false);
        dcGroupSource.setContainerDataSource(new XmlFileContainer(datacollectionDir, false));
        dcGroupSource.setItemCaptionPropertyId(FilesystemContainer.PROPERTY_NAME);
        dcGroupSource.addListener(new ComboBox.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                final File file = (File) event.getProperty().getValue();
                if (file == null)
                    return;
                try {
                    LOG.info("Loading data collection data from {}", file);
                    DatacollectionGroup dcGroup = JaxbUtils.unmarshal(DatacollectionGroup.class, file);
                    m_selectedGroup = dcGroup.getName();
                    addDataCollectionGroupPanel(dataCollectionDao, file, dcGroup);
                } catch (Exception e) {
                    LOG.error("an error ocurred while parsing the data collection configuration {}: {}", file, e.getMessage(), e);
                    getApplication().getMainWindow().showNotification("Can't parse file " + file + " because " + e.getMessage());
                }
            }
        });

        final Button add = new Button("Add New Data Collection File");
        toolbar.addComponent(add);
        add.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                PromptWindow w = new PromptWindow("New Data Collection Group", "Group Name") {
                    @Override
                    public void textFieldChanged(String fieldValue) {
                        File file = new File(datacollectionDir, fieldValue.replaceAll(" ", "_") + ".xml");
                        LOG.info("Adding new data collection file {}", file);
                        DatacollectionGroup dcGroup = new DatacollectionGroup();
                        dcGroup.setName(fieldValue);
                        addDataCollectionGroupPanel(dataCollectionDao, file, dcGroup);
                    }
                };
                getApplication().getMainWindow().addWindow(w);
            }
        });

        final Button remove = new Button("Remove Selected Data Collection File");
        toolbar.addComponent(remove);
        remove.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (dcGroupSource.getValue() == null) {
                    getApplication().getMainWindow().showNotification("Please select a data collection group configuration file.");
                    return;
                }
                final File file = (File) dcGroupSource.getValue();
                MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                               "Are you sure?",
                                               MessageBox.Icon.QUESTION,
                                               "Do you really want to remove the file " + file.getName() + "?<br/>This cannot be undone and OpenNMS won't be able to collect the metrics defined on this file.",
                                               new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                               new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
                mb.addStyleName(Runo.WINDOW_DIALOG);
                mb.show(new EventListener() {
                    @Override
                    public void buttonClicked(ButtonType buttonType) {
                        if (buttonType == MessageBox.ButtonType.YES) {
                            LOG.info("deleting file {}", file);
                            if (file.delete()) {
                                try {
                                    // Updating datacollection-config.xml
                                    File configFile = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
                                    DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, configFile);
                                    boolean modified = false;
                                    for (SnmpCollection collection : config.getSnmpCollectionCollection()) {
                                        for (Iterator<IncludeCollection> it = collection.getIncludeCollectionCollection().iterator(); it.hasNext();) {
                                            IncludeCollection ic = it.next();
                                            if (m_selectedGroup != null && m_selectedGroup.equals(ic.getDataCollectionGroup())) {
                                                it.remove();
                                                modified = true;
                                            }
                                        }
                                    }
                                    if (modified) {
                                        LOG.info("updating data colleciton configuration on {}.", configFile);
                                        JaxbUtils.marshal(config, new FileWriter(configFile));
                                    }
                                    // Updating UI Components
                                    dcGroupSource.select(null);
                                    removeDataCollectionGroupPanel();
                                } catch (Exception e) {
                                    LOG.error("an error ocurred while saving the data collection configuration: {}", e.getMessage(), e);
                                    getApplication().getMainWindow().showNotification("Can't save data collection configuration. " + e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                                }
                            } else {
                                getApplication().getMainWindow().showNotification("Cannot delete file " + file, Notification.TYPE_WARNING_MESSAGE);
                            }
                        }
                    }
                });
            }
        });

        addComponent(toolbar);
        addComponent(new Label(""));
        setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Adds a data collection group panel.
     *
     * @param dataCollectionDao the OpenNMS data collection configuration DAO
     * @param file data collection group file name
     * @param dcGroup the data collection group object
     */
    private void addDataCollectionGroupPanel(final DataCollectionConfigDao dataCollectionDao, final File file, final DatacollectionGroup dcGroup) {
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
        removeDataCollectionGroupPanel();
        addComponent(panel);
    }

    /**
     * Removes the data collection group panel.
     */
    private void removeDataCollectionGroupPanel() {
        if (this.getComponentCount() > 1)
            this.removeComponent(this.getComponent(1));
    }

}
