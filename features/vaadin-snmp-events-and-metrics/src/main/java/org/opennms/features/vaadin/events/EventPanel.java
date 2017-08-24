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

package org.opennms.features.vaadin.events;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.config.EditorToolbar;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class Event Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class EventPanel extends Panel {

    /** The isNew flag. True, if the group is new. */
    private boolean isNew = false;

    /** The Events Configuration DAO. */
    private EventConfDao eventConfDao;

    /** The Events Proxy. */
    private EventProxy eventProxy;

    /** The Events File. */
    private File eventFile;

    /** The selected Event ID. */
    private Object selectedEventId;

    /** The event table. */
    final EventTable eventTable;

    /** The base event object. */
    final Events baseEventsObject = new Events();

    /**
     * Instantiates a new event panel.
     *
     * @param eventConfDao the OpenNMS Events Configuration DAO
     * @param eventProxy the OpenNMS Events Proxy
     * @param eventFile the events file
     * @param events the OpenNMS events object
     * @param logger the logger object
     */
    public EventPanel(final EventConfDao eventConfDao, final EventProxy eventProxy, final File eventFile, final Events events, final Logger logger) {

        if (eventProxy == null) {
            throw new RuntimeException("eventProxy cannot be null.");
        }

        if (eventConfDao == null) {
            throw new RuntimeException("eventConfDao cannot be null.");
        }

        this.eventConfDao = eventConfDao;
        this.eventProxy = eventProxy;
        this.eventFile = eventFile;

        setCaption("Events");
        addStyleName("light");

        baseEventsObject.setGlobal(events.getGlobal());
        baseEventsObject.setEventFiles(events.getEventFiles());

        final HorizontalLayout topToolbar = new HorizontalLayout();
        topToolbar.addComponent(new Button("Save Events File", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                processEvents(logger);
            }
        }));
        topToolbar.addComponent(new Button("Cancel", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                logger.info("Event processing has been canceled");
                cancel();
            }
        }));

        eventTable = new EventTable(events.getEvents());

        final EventForm eventForm = new EventForm();
        eventForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public boolean save() {
                org.opennms.netmgt.xml.eventconf.Event event = eventForm.getEvent();
                logger.info("Event " + event.getUei() + " has been " + (isNew ? "created." : "updated."));
                try {
                    eventForm.commit();
                    eventForm.setReadOnly(true);
                    eventTable.refreshRowCache();
                } catch (CommitException e) {
                    String msg = "Can't save the changes: " + e.getMessage();
                    logger.error(msg);
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                    return false;
                }
                return true;
            }
            @Override
            public boolean delete() {
                Object eventId = eventTable.getValue();
                if (eventId != null) {
                    org.opennms.netmgt.xml.eventconf.Event event = eventTable.getEvent(eventId);
                    logger.info("Event " + event.getUei() + " has been removed.");
                    eventTable.select(null);
                    eventTable.removeItem(eventId);
                    eventTable.refreshRowCache();
                }
                return true;
            }
            @Override
            public boolean edit() {
                eventForm.setReadOnly(false);
                return true;
            }
            @Override
            public boolean cancel() {
                eventForm.discard();
                eventForm.setReadOnly(true);
                return true;
            }
        };
        bottomToolbar.setVisible(false);

        eventTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (eventForm.isVisible() && !eventForm.isReadOnly()) {
                    eventTable.select(selectedEventId);
                    Notification.show("An event seems to be being edited.\nPlease save or cancel your current changes.", Notification.Type.WARNING_MESSAGE);
                } else {
                    Object eventId = eventTable.getValue();
                    if (eventId != null) {
                        selectedEventId = eventId;
                        eventForm.setEvent(eventTable.getEvent(eventId));
                    }
                    eventForm.setReadOnly(true);
                    eventForm.setVisible(eventId != null);
                    bottomToolbar.setReadOnly(true);
                    bottomToolbar.setVisible(eventId != null);
                }
            }
        });   

        final Button add = new Button("Add Event", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                eventTable.addEvent(eventForm.createBasicEvent());
                eventForm.setReadOnly(false);
                bottomToolbar.setReadOnly(false);
                setIsNew(true);
            }
        });

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(topToolbar);
        mainLayout.addComponent(eventTable);
        mainLayout.addComponent(add);
        mainLayout.addComponent(eventForm);
        mainLayout.addComponent(bottomToolbar);
        mainLayout.setComponentAlignment(topToolbar, Alignment.MIDDLE_RIGHT);
        mainLayout.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);

        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the group is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Cancel.
     */
    public abstract void cancel();

    /**
     * Success.
     */
    public abstract void success();

    /**
     * Failure.
     *
     * @param reason the reason
     */
    public abstract void failure(String reason);

    /**
     * Process events.
     *
     * @param logger the logger
     */
    public void processEvents(final Logger logger) {
        if (eventFile.exists()) {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to override the existig file?\nAll current information will be lost.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        validateFile(eventFile, logger);
                    }
                }
            });
        } else {
            validateFile(eventFile, logger);
        }
    }

    /**
     * Validate file.
     *
     * @param file the file
     * @param logger the logger
     */
    private void validateFile(final File file, final Logger logger) {
        int eventCount = 0;
        for (org.opennms.netmgt.xml.eventconf.Event e : eventTable.getOnmsEvents()) {
            if (eventConfDao.findByUei(e.getUei()) != null)
                eventCount++;
        }
        if (eventCount == 0) {
            saveFile(file, logger);
        } else {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               eventCount + " of the new events are already on the configuration files.\nIf you click 'Yes', the existing definitions are going to be ignored.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        saveFile(file, logger);
                    }
                }
            });
        }
    }

    /**
     * Save file.
     *
     * @param file the file
     * @param logger the logger
     */
    private void saveFile(final File file, final Logger logger) {
        try {
            // Updating the base events object with the new events set.
            baseEventsObject.setEvents(eventTable.getOnmsEvents());
            // Normalize the Event Content (required to avoid marshaling problems)
            // TODO Are other normalizations required ?
            for (org.opennms.netmgt.xml.eventconf.Event event : baseEventsObject.getEvents()) {
                logger.debug("Normalizing event " + event.getUei());
                final AlarmData ad = event.getAlarmData();
                if (ad != null && (ad.getReductionKey() == null || ad.getReductionKey().trim().isEmpty() || ad.getAlarmType() == null || ad.getAlarmType() == 0)) {
                    event.setAlarmData(null);
                }
                final Mask m = event.getMask();
                if (m != null && m.getMaskelements().isEmpty()) {
                    event.setMask(null);
                }
            }
            // Save the XML of the new events
            saveEvents(baseEventsObject, file, logger);
            // Add a reference to the new file into eventconf.xml if there are events
            String fileName = file.getAbsolutePath().replaceFirst(".*\\" + File.separatorChar + "events\\" + File.separatorChar + "(.*)", "events" + File.separatorChar + "$1");
            final Events rootEvents = eventConfDao.getRootEvents();
            final File rootFile = ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
            if (baseEventsObject.getEvents().size() > 0) {
                if (!rootEvents.getEventFiles().contains(fileName)) {
                    logger.info("Adding a reference to " + fileName + " inside eventconf.xml.");
                    rootEvents.getEventFiles().add(0, fileName);
                    saveEvents(rootEvents, rootFile, logger);
                }
            } else {
                // If a reference to an empty events file exist, it should be removed.
                if (rootEvents.getEventFiles().contains(fileName)) {
                    logger.info("Removing a reference to " + fileName + " inside eventconf.xml because there are no events.");
                    rootEvents.getEventFiles().remove(fileName);
                    saveEvents(rootEvents, rootFile, logger);
                }
            }
            EventBuilder eb = new EventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI, "WebUI");
            eventProxy.send(eb.getEvent());
            logger.info("The event's configuration reload operation is being performed.");
            success();
        } catch (Exception e) {
            logger.error(e.getClass() + ": " + (e.getMessage() == null ? "[No Details]" : e.getMessage()));
            if (e.getMessage() == null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(sw.toString());
            }
            failure(e.getMessage());
        }
    }

    /**
     * Save events.
     *
     * @param events the events
     * @param eventFile the events file
     * @param logger the logger
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void saveEvents(final Events events, final File eventFile, final Logger logger) throws IOException {
        logger.info("Saving XML data into " + eventFile);
        FileWriter writer = new FileWriter(eventFile);
        JaxbUtils.marshal(events, writer);
        writer.close();
    }
}
