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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;

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

    private List<EventDTO> eventDtoList;

    /**
     * Instantiates a new event panel.
     *
     * @param events the OpenNMS events
     */
    public EventPanel(Events events) {
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
                generateEventFile();
            }
        }));
        mainLayout.addComponent(toolbar);
        mainLayout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        eventDtoList = getDtoEvents(events);
        eventTable = new EventTable(eventDtoList) {
            public void updateExternalSource(BeanItem<EventDTO> item) {
                eventForm.setItemDataSource(item, Arrays.asList(EventForm.FORM_ITEMS));
                eventForm.setVisible(true);
                eventForm.setReadOnly(true);
            }
        };
        mainLayout.addComponent(eventTable);

        eventForm = new EventForm() {
            public void customCommit() {
                eventTable.refreshRowCache();
            }
        };
        mainLayout.addComponent(eventForm);

        setContent(mainLayout);
    }

    /**
     * Gets the DTO events.
     *
     * @param events the OpenNMS events
     * @return the list
     */
    private List<EventDTO> getDtoEvents(Events events) {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();
        List<EventDTO> dtoEvents = new ArrayList<EventDTO>();
        for (org.opennms.netmgt.xml.eventconf.Event e : events.getEventCollection()) {
            EventDTO dto = mapper.map(e, EventDTO.class);
            dtoEvents.add(dto);
        }
        return dtoEvents;
    }

    /**
     * Gets the OpenNMS events.
     *
     * @return the OpenNMS events
     */
    public List<org.opennms.netmgt.xml.eventconf.Event> getOnmsEvents() {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();
        List<org.opennms.netmgt.xml.eventconf.Event> events = new ArrayList<org.opennms.netmgt.xml.eventconf.Event>();
        for (EventDTO dto : eventDtoList) {
            org.opennms.netmgt.xml.eventconf.Event e = mapper.map(dto, org.opennms.netmgt.xml.eventconf.Event.class);
            e.setDescr(encodeHtml(e.getDescr()));
            e.getLogmsg().setContent(encodeHtml(e.getLogmsg().getContent()));
            events.add(e);
        }
        return events;
    }

    /**
     * Cancel processing.
     */
    abstract void cancelProcessing();

    /**
     * Generate event file.
     */
    abstract void generateEventFile();
    
    /**
     * Encode HTML.
     *
     * @param html the HTML
     * @return the encoded string
     */
    private String encodeHtml(String html) {
        return html.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

}
