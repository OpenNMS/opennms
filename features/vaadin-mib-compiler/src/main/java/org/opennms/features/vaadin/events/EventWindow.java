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
package org.opennms.features.vaadin.events;

import java.io.File;
import java.io.FileWriter;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.web.api.Util;
import org.springframework.core.io.FileSystemResource;

import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Class Event Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class EventWindow extends Window {

    /** The Events DAO. */
    private DefaultEventConfDao eventsDao; // I need to access the raw event configuration.

    /**
     * Instantiates a new events window.
     *
     * @param caption the caption
     * @param events the OpenNMS Events
     * @param logger the logger
     * 
     * @throws Exception the exception
     */
    public EventWindow(final String caption, final Events events, final Logger logger) throws Exception {
        super(caption);

        // TODO: Is this the best way ? If not, getRootEvents must be exposed through EventConfDao and use BeanUtils or @Autowired
        eventsDao = new DefaultEventConfDao();
        eventsDao.setConfigResource(new FileSystemResource(ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME)));
        eventsDao.afterPropertiesSet();

        setScrollable(true);
        setModal(false);
        setClosable(false);
        setDraggable(false);
        setResizable(false);
        addStyleName(Runo.WINDOW_DIALOG);
        setSizeFull();
        setContent(new EventPanel(events, logger) {
            @Override
            public void cancelProcessing() {
                close();
            }
            @Override
            public void generateEventFile(Events events) {
                processEvents(events, logger);
            }
        });
    }

    /**
     * Process events.
     *
     * @param events the OpenNMS Events
     * @param logger the logger
     */
    public void processEvents(final Events events, final Logger logger) {
        final File configDir = new File(ConfigFileConstants.getHome(), "etc/events/");
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
                        validateFile(file, events, logger);
                    }
                }
            });
        } else {
            validateFile(file, events, logger);
        }
    }

    /**
     * Validate file.
     *
     * @param file the file
     * @param events the events
     * @param logger the logger
     */
    private void validateFile(final File file, final Events events, final Logger logger) {
        int eventCount = 0;
        for (org.opennms.netmgt.xml.eventconf.Event e : events.getEventCollection()) {
            if (eventsDao.findByUei(e.getUei()) != null)
                eventCount++;
        }
        if (eventCount == 0) {
            saveFile(file, events, logger);
        } else {
            MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                    "Are you sure?",
                    MessageBox.Icon.QUESTION,
                    eventCount + " of the new events are already on the configuration files. Do you really want to override those events ?",
                    new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                    new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        saveFile(file, events, logger);
                    }
                }
            });
        }
    }

    /**
     * Save file.
     *
     * @param file the file
     * @param events the events
     * @param logger the logger
     */
    private void saveFile(final File file, final Events events, final Logger logger) {
        try {
            logger.info("Saving XML data into " + file.getAbsolutePath());
            // Save the XML of the new events
            FileWriter writer = new FileWriter(file);
            JaxbUtils.marshal(events, writer);
            writer.close();
            // Add a reference to the new file into eventconf.xml
            logger.info("Adding a reference to " + file.getName() + " inside eventconf.xml.");
            eventsDao.getRootEvents().getEventFileCollection().add(0, "events/" + file.getName());
            eventsDao.saveCurrent();
            // Send eventsConfigChange event
            logger.info("Sending an event to reload configuration.");
            EventBuilder ebldr = new EventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI, "MIB-Compiler");
            Util.createEventProxy().send(ebldr.getEvent());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        close();
    }

}
