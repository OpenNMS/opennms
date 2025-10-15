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

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.events.EventPanel;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.util.FilesystemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;

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
