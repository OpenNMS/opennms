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

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.datacollection.DataCollectionGroupPanel;
import org.opennms.features.vaadin.utils.FileValidationUtils;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.util.FilesystemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The Class Data Collection Group Administration Panel.
 */
// TODO When deleting a group, all the SNMP collections UI components must be updated.
@SuppressWarnings("serial")
public class DataCollectionGroupAdminPanel extends VerticalLayout {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionGroupAdminPanel.class);

    /** The m_selected group. */
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
        dcGroupSource.addValueChangeListener(new ComboBox.ValueChangeListener() {
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
                    Notification.show("Can't parse file " + file + " because " + e.getMessage());
                }
            }
        });

        final Button add = new Button("Add New Data Collection File");
        toolbar.addComponent(add);
        add.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                PromptWindow w = new PromptWindow("New Data Collection Group", "Group Name") {
                    @Override
                    public void textFieldChanged(String fieldValue) {
                        if (!isValidFileName(fieldValue)) {
                            Notification.show("Invalid File name " + fieldValue +", File name must begin with a letter or number and contain only: letters, numbers, ., -, _", Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                        if (FileValidationUtils.isFileNameTooLong(fieldValue)) {
                            Notification.show("Filename too long", Notification.Type.ERROR_MESSAGE);
                            return;
                        }

                        File file = new File(datacollectionDir, fieldValue.replaceAll(" ", "_") + ".xml");
                        LOG.info("Adding new data collection file {}", file);
                        DatacollectionGroup dcGroup = new DatacollectionGroup();
                        dcGroup.setName(fieldValue);
                        addDataCollectionGroupPanel(dataCollectionDao, file, dcGroup);
                    }
                };
                getUI().addWindow(w);
            }
        });

        final Button remove = new Button("Remove Selected Data Collection File");
        toolbar.addComponent(remove);
        remove.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (dcGroupSource.getValue() == null) {
                    Notification.show("Please select a data collection group configuration file.");
                    return;
                }
                final File file = (File) dcGroupSource.getValue();
                ConfirmDialog.show(getUI(),
                                   "Are you sure?",
                                   "Do you really want to remove the file " + file.getName() + "?\nThis cannot be undone and OpenNMS won't be able to collect the metrics defined on this file.",
                                   "Yes",
                                   "No",
                                   new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            LOG.info("deleting file {}", file);
                            if (file.delete()) {
                                try {
                                    // Updating datacollection-config.xml
                                    File configFile = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
                                    DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, configFile);
                                    boolean modified = false;
                                    for (SnmpCollection collection : config.getSnmpCollections()) {
                                        for (Iterator<IncludeCollection> it = collection.getIncludeCollections().iterator(); it.hasNext();) {
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
                                    Notification.show("Can't save data collection configuration. " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                                }
                            } else {
                                Notification.show("Cannot delete file " + file, Notification.Type.WARNING_MESSAGE);
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

    public boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // Block path traversal sequences
        if (fileName.contains("..") ||
                fileName.contains("/") ||
                fileName.contains("\\") ||
                fileName.contains("\0") || // null byte
                fileName.contains("%00") || // encoded null byte
                fileName.contains("%2e") || // encoded dot
                fileName.contains("%2f") || // encoded slash
                fileName.contains("%5c")) { // encoded backslash
            return false;
        }

        // Block other dangerous patterns
        if (fileName.matches(".*[<>:\"|?*].*")) {
            return false; // Windows reserved characters
        }

        // Extract just the filename part (in case any path components slipped through)
        String nameOnly = new File(fileName).getName();
        if (!nameOnly.equals(fileName)) {
            return false; // Path components detected
        }

        // Allow only safe characters for filenames
        if (!nameOnly.matches("^[a-zA-Z0-9][a-zA-Z0-9._\\-]+$")) {
            return false;
        }

        // Prevent reserved filenames (Windows)
        String nameWithoutExt = nameOnly.replaceFirst("\\.[^.]+$", "").toUpperCase();
        if (nameWithoutExt.matches("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$")) {
            return false;
        }
        return true;
    }

    /**
     * Adds a data collection group panel.
     *
     * @param dataCollectionDao the OpenNMS data collection configuration DAO
     * @param file data collection group file name
     * @param dcGroup the data collection group object
     */
    private void addDataCollectionGroupPanel(final DataCollectionConfigDao dataCollectionDao, final File file, final DatacollectionGroup dcGroup) {
        DataCollectionGroupPanel panel = new DataCollectionGroupPanel(dataCollectionDao, dcGroup, new SimpleLogger(), file) {
            @Override
            public void cancel() {
                this.setVisible(false);
            }
            @Override
            public void success() {
                Notification.show("Data collection group file " + file.getName() + " has been successfuly saved.");
                this.setVisible(false);
            }
            @Override
            public void failure(String reason) {
                String msg = reason == null ? "." : ", because" + reason;
                Notification.show("Data collection group file " + file.getName() + " cannot be saved" + msg, Notification.Type.ERROR_MESSAGE);
            }
        };
        panel.setCaption("Data Collection Group from " + file.getName());
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
