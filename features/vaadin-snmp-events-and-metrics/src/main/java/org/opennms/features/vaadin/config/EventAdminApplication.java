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
import org.opennms.features.vaadin.events.EventPanel;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Runo;

/**
 * The Class Event Administration Application.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class EventAdminApplication extends Application {

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
    public void init() {
        if (eventProxy == null)
            throw new RuntimeException("eventProxy cannot be null.");
        if (eventConfDao == null)
            throw new RuntimeException("eventConfDao cannot be null.");

        setTheme(Runo.THEME_NAME);

        final VerticalLayout layout = new VerticalLayout();

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setMargin(true);

        final File eventsDir = new File(ConfigFileConstants.getFilePathString(), "events");
        final ComboBox eventSource = new ComboBox();
        toolbar.addComponent(eventSource);
        eventSource.setImmediate(true);
        eventSource.setNullSelectionAllowed(false);
        eventSource.setContainerDataSource(new XmlFileContainer(eventsDir));
        eventSource.setItemCaptionPropertyId(FilesystemContainer.PROPERTY_NAME);
        eventSource.addListener(new ComboBox.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                final File file = (File) event.getProperty().getValue();
                LogUtils.infof(this, "Loading events from %s", file);
                Events events = JaxbUtils.unmarshal(Events.class, file);
                layout.removeComponent(layout.getComponent(1));
                layout.addComponent(createEventPanel(file, events));
            }
        });

        final Button add = new Button("Add File");
        toolbar.addComponent(add);
        add.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                PromptWindow w = new PromptWindow("New Events Configuration", "Events File Name") {
                    @Override
                    public void textFieldChanged(String fieldValue) {
                        File file = new File(eventsDir, fieldValue);
                        LogUtils.infof(this, "Adding new events file %s", file);
                        Events events = new Events();
                        layout.removeComponent(layout.getComponent(1));
                        layout.addComponent(createEventPanel(file, events));
                    }
                };
                getMainWindow().addWindow(w);
            }
        });

        final Button remove = new Button("Remove File");
        toolbar.addComponent(remove);
        remove.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                getMainWindow().showNotification("Not impementet yet!");
            }
        });

        layout.addComponent(toolbar);
        layout.addComponent(new Label(""));
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        final Window mainWindow = new Window("Events Administration", layout);
        setMainWindow(mainWindow);
    }

    /**
     * Creates a new Events Panel.
     *
     * @param file the Events File Name
     * @param events the Events Object
     * @return a new Events Panel Object
     */
    private EventPanel createEventPanel(final File file, final Events events) {
        EventPanel eventPanel = new EventPanel(eventConfDao, eventProxy, file.getName(), events, new SimpleLogger()) {
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
        eventPanel.setCaption("Events from " + file.getName());
        return eventPanel;
    }

}
