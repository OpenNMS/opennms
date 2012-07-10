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
package org.opennms.features.vaadin.mibcompiler;

import java.util.Arrays;

import org.opennms.features.vaadin.mibcompiler.model.EventDTO;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class EventPanel.
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
     */
    public EventPanel(final Events events) {
        setCaption("Events");
        addStyleName(Runo.PANEL_LIGHT);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(new Button("Cancel Processing", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                cancelProcessing();
            }
        }));
        toolbar.addComponent(new Button("Generate Evenst File", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                events.setEvent(eventTable.getOnmsEvents());
                generateEventFile();
            }
        }));
        mainLayout.addComponent(toolbar);
        mainLayout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        eventTable = new EventTable(events) {
            public void updateExternalSource(BeanItem<EventDTO> item) {
                eventForm.setItemDataSource(item, Arrays.asList(EventForm.FORM_ITEMS));
                eventForm.setVisible(true);
                eventForm.setReadOnly(true);
            }
        };
        mainLayout.addComponent(eventTable);

        eventForm = new EventForm() {
            public void saveEvent(EventDTO event) {
                eventTable.refreshRowCache();
            }
            public void deleteEvent(EventDTO event) {
                getApplication().getMainWindow().showNotification("Delete? " + eventTable.removeItem(event));
                eventTable.refreshRowCache();
            }
        };
        mainLayout.addComponent(eventForm);

        setContent(mainLayout);
    }

    /**
     * Cancel processing.
     */
    abstract void cancelProcessing();

    /**
     * Generate event file.
     */
    abstract void generateEventFile();

}
