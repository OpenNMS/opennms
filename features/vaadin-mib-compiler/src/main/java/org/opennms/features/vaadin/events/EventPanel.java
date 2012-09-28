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

import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class Event Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class EventPanel extends Panel {

    /** The event table. */
    private final EventTable eventTable;

    /** The event form. */
    private final EventForm eventForm;

    /**
     * Instantiates a new event panel.
     *
     * @param events the OpenNMS events
     * @param logger the logger
     */
    public EventPanel(final Events events, final Logger logger) {
        setCaption("Events");
        addStyleName(Runo.PANEL_LIGHT);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(new Button("Cancel Processing", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                cancelProcessing();
                logger.info("Event processing has been canceled");
            }
        }));
        toolbar.addComponent(new Button("Generate Evenst File", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                events.setEvent(eventTable.getOnmsEvents());
                generateEventFile(events);
                logger.info("The events have been saved.");
            }
        }));
        mainLayout.addComponent(toolbar);
        mainLayout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        eventTable = new EventTable(events) {
            public void updateExternalSource(org.opennms.netmgt.xml.eventconf.Event event) {
                eventForm.setEventDataSource(event);
                eventForm.setVisible(true);
                eventForm.setReadOnly(true);
            }
        };
        mainLayout.addComponent(eventTable);

        final Button addEventBtn = new Button("Add Event");
        addEventBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                eventTable.addEvent();
            }
        });
        mainLayout.addComponent(addEventBtn);
        mainLayout.setComponentAlignment(addEventBtn, Alignment.MIDDLE_RIGHT);

        eventForm = new EventForm() {
            public void saveEvent(org.opennms.netmgt.xml.eventconf.Event event) {
                logger.info("Event " + event.getUei() + " has been updated.");
                eventTable.refreshRowCache();
            }
            public void deleteEvent(org.opennms.netmgt.xml.eventconf.Event event) {
                logger.info("Event " + event.getUei() + " has been removed.");
                eventTable.removeItem(event);
                eventTable.refreshRowCache();
            }
        };
        mainLayout.addComponent(eventForm);

        setContent(mainLayout);
    }

    /**
     * Cancel processing.
     */
    public abstract void cancelProcessing();

    /**
     * Generate event file.
     *
     * @param events the OpenNMS Events
     */
    public abstract void generateEventFile(Events events);

}
