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
import java.util.Collection;
import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.opennms.features.vaadin.mibcompiler.model.EventDTO;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Runo;

/**
 * The Class EventTable.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class EventTable extends Table {

    /** The Constant COLUMN_NAMES. */
    public static final String[] COLUMN_NAMES = new String[] { "eventLabel" };

    /** The Constant COLUMN_LABELS. */
    public static final String[] COLUMN_LABELS = new String[] { "Generated Events" };
    
    /**
     * Instantiates a new event table.
     *
     * @param events the events
     */
    public EventTable(final Events events) {
        setContainerDataSource(new BeanItemContainer<EventDTO>(EventDTO.class, getDtoEvents(events)));
        setStyleName(Runo.TABLE_SMALL);
        setImmediate(true);
        setSelectable(true);
        setVisibleColumns(COLUMN_NAMES);
        setColumnHeaders(COLUMN_LABELS);
        setWidth("100%");
        setHeight("250px");
        addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                if (getValue() != null) {
                    BeanItem<EventDTO> item = createEventItem((EventDTO)getValue());
                    updateExternalSource(item);
                }
            }
        });
    }

    /**
     * Creates the event item.
     *
     * @param event the event
     * @return the bean item
     */
    public BeanItem<EventDTO> createEventItem(EventDTO event) {
        BeanItem<EventDTO> item = new BeanItem<EventDTO>(event);
        item.addItemProperty("logmsgContent", new NestedMethodProperty(item.getBean(), "logmsg.content"));
        item.addItemProperty("logmsgDest", new NestedMethodProperty(item.getBean(), "logmsg.dest"));
        item.addItemProperty("maskElements", new NestedMethodProperty(item.getBean(), "mask.maskelementCollection"));
        item.addItemProperty("maskVarbinds", new NestedMethodProperty(item.getBean(), "mask.varbindCollection"));
        item.addItemProperty("alarmReductionKey", new NestedMethodProperty(item.getBean(), "alarmData.reductionKey"));
        item.addItemProperty("alarmClearKey", new NestedMethodProperty(item.getBean(), "alarmData.clearKey"));
        item.addItemProperty("alarmType", new NestedMethodProperty(item.getBean(), "alarmData.alarmType"));
        item.addItemProperty("alarmAutoClean", new NestedMethodProperty(item.getBean(), "alarmData.autoClean"));
        return item;
    }
    
    /**
     * Update external source.
     *
     * @param item the item
     */
    public abstract void updateExternalSource(BeanItem<EventDTO> item);
    
    /**
     * Gets the OpenNMS events.
     *
     * @param eventDtoList the Event DTO list
     * @return the OpenNMS events
     */
    @SuppressWarnings("unchecked")
    public List<org.opennms.netmgt.xml.eventconf.Event> getOnmsEvents() {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();
        List<org.opennms.netmgt.xml.eventconf.Event> events = new ArrayList<org.opennms.netmgt.xml.eventconf.Event>();
        Collection<EventDTO> eventDtoList = ((BeanItemContainer<EventDTO>)getContainerDataSource()).getItemIds();
        for (EventDTO dto : eventDtoList) {
            org.opennms.netmgt.xml.eventconf.Event e = mapper.map(dto, org.opennms.netmgt.xml.eventconf.Event.class);
            e.setDescr(encodeHtml(e.getDescr()));
            e.getLogmsg().setContent(encodeHtml(e.getLogmsg().getContent()));
            events.add(e);
        }
        return events;
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
     * Encode HTML.
     *
     * @param html the HTML
     * @return the encoded string
     */
    private String encodeHtml(String html) {
        return html.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
