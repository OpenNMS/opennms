/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import org.vaadin.dialogs.ConfirmDialog;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.events.EventPanel;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * The Class Event Administration Application.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@Theme("opennms")
@Title("Events Administration")
@SuppressWarnings("serial")
public class EventAdminApplication extends UI {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EventAdminApplication.class);


    /** The OpenNMS Event Proxy. */
    private EventProxy eventProxy;

    /** The OpenNMS Event Configuration DAO. */
    private EventConfDao eventConfDao;

    /**
     * Sets the OpenNMS Event configuration DAO.
     *
     * @param eventConfDao the new OpenNMS Event configuration DAO
     */
    public void setEventConfDao(EventConfDao eventConfDao) {
        this.eventConfDao = eventConfDao;
    }

    /**
     * Sets the OpenNMS Event Proxy.
     *
     * @param eventProxy the new event proxy
     */
    public void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;
    }

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init(VaadinRequest request) {
        if (eventProxy == null)
            throw new RuntimeException("eventProxy cannot be null.");
        if (eventConfDao == null)
            throw new RuntimeException("eventConfDao cannot be null.");

        final VerticalLayout layout = new VerticalLayout();

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setMargin(true);

        final Label comboLabel = new Label("Select Events Configuration File");
        toolbar.addComponent(comboLabel);
        toolbar.setComponentAlignment(comboLabel, Alignment.MIDDLE_LEFT);

        final File eventsDir = new File(ConfigFileConstants.getFilePathString(), "events");
        final XmlFileContainer container = new XmlFileContainer(eventsDir, true);
        container.addExcludeFile("default.events.xml"); // This is a protected file, should not be updated.
        final ComboBox eventSource = new ComboBox();
        toolbar.addComponent(eventSource);
        eventSource.setImmediate(true);
        eventSource.setNullSelectionAllowed(false);
        eventSource.setContainerDataSource(container);
        eventSource.setItemCaptionPropertyId(FilesystemContainer.PROPERTY_NAME);
        eventSource.addValueChangeListener(new ComboBox.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                final File file = (File) event.getProperty().getValue();
                if (file == null)
                    return;
                try {
                    LOG.info("Loading events from {}", file);
                    final Events events = JaxbUtils.unmarshal(Events.class, file);
                    addEventPanel(layout, file, events);
                } catch (Exception e) {
                    LOG.error("an error ocurred while saving the event configuration {}: {}", file, e.getMessage(), e);
                    Notification.show("Can't parse file " + file + " because " + e.getMessage());
                }
            }
        });

        final Button add = new Button("Add New Events File");
        toolbar.addComponent(add);
        add.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                PromptWindow w = new PromptWindow("New Events Configuration", "Events File Name") {
                    @Override
                    public void textFieldChanged(String fieldValue) {
                        final File file = new File(eventsDir, normalizeFilename(fieldValue));
                        LOG.info("Adding new events file {}", file);
                        final Events events = new Events();
                        addEventPanel(layout, file, events);
                    }
                };
                addWindow(w);
            }
        });

        final Button remove = new Button("Remove Selected Events File");
        toolbar.addComponent(remove);
        remove.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (eventSource.getValue() == null) {
                    Notification.show("Please select an event configuration file.");
                    return;
                }
                final File file = (File) eventSource.getValue();
                ConfirmDialog.show(getUI(),
                                   "Are you sure?",
                                   "Do you really want to remove the file " + file.getName() + "?\nThis cannot be undone and OpenNMS won't be able to handle the events configured on this file.",
                                   "Yes",
                                   "No",
                                   new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            LOG.info("deleting file {}", file);
                            if (file.delete()) {
                                try {
                                    // Updating eventconf.xml
                                    boolean modified = false;
                                    File configFile = ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
                                    Events config = JaxbUtils.unmarshal(Events.class, configFile);
                                    for (Iterator<String> it = config.getEventFiles().iterator(); it.hasNext();) {
                                        String fileName = it.next();
                                        if (file.getAbsolutePath().contains(fileName)) {
                                            it.remove();
                                            modified = true;
                                        }
                                    }
                                    if (modified) {
                                        JaxbUtils.marshal(config, new FileWriter(configFile));
                                        EventBuilder eb = new EventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI, "WebUI");
                                        eventProxy.send(eb.getEvent());
                                    }
                                    // Updating UI Components
                                    eventSource.select(null);
                                    if (layout.getComponentCount() > 1)
                                        layout.removeComponent(layout.getComponent(1));
                                } catch (Exception e) {
                                    LOG.error("an error ocurred while saving the event configuration: {}", e.getMessage(), e);
                                    Notification.show("Can't save event configuration. " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
                                }
                            } else {
                                Notification.show("Cannot delete file " + file, Notification.Type.WARNING_MESSAGE);
                            }
                        }
                    }
                });
            }
        });

        layout.addComponent(toolbar);
        layout.addComponent(new Label(""));
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        setContent(layout);
    }

    /**
     * Normalize filename.
     *
     * @param currentFileName the current file name
     * @return the string
     */
    protected String normalizeFilename(String currentFileName) {
        String fileName = currentFileName.replaceFirst("\\.$", "");
        if (fileName.toLowerCase().endsWith(".xml")) {
            if (fileName.toLowerCase().endsWith(".events.xml")) {
                fileName = fileName.replaceFirst("\\.[Xx][Mm][Ll]", ".xml");
            } else {
                fileName = fileName.replaceFirst("\\.[Xx][Mm][Ll]", ".events.xml");
            }
        } else {
            if (fileName.toLowerCase().endsWith(".events")) {
                fileName += ".xml";
            } else {
                fileName += ".events.xml";
            }
        }
        return fileName;
    }

    /**
     * Adds a new Events Panel.
     *
     * @param layout the layout
     * @param file the Events File Name
     * @param events the Events Object
     * @return a new Events Panel Object
     */
    private void addEventPanel(final VerticalLayout layout, final File file, final Events events) {
        EventPanel eventPanel = new EventPanel(eventConfDao, eventProxy, file, events, new SimpleLogger()) {
            @Override
            public void cancel() {
                this.setVisible(false);
            }
            @Override
            public void success() {
                Notification.show("Event file " + file + " has been successfuly saved.");
                this.setVisible(false);
            }
            @Override
            public void failure(String reason) {
                Notification.show("Event file " + file + " cannot be saved" + (reason == null ? "." : ", because: " + reason), Notification.Type.ERROR_MESSAGE);
            }
        };
        eventPanel.setCaption("Events from " + file);
        removeEventPanel(layout);
        layout.addComponent(eventPanel);
    }

    /**
     * Removes the event panel.
     *
     * @param layout the layout
     */
    private void removeEventPanel(final VerticalLayout layout) {
        if (layout.getComponentCount() > 1)
            layout.removeComponent(layout.getComponent(1));
    }

}
