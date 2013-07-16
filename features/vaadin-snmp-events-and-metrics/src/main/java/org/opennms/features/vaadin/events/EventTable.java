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
package org.opennms.features.vaadin.events;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Mask;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Runo;

/**
 * The Class Event Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class EventTable extends Table {

    /** The Constant COLUMN_NAMES. */
    public static final String[] COLUMN_NAMES = new String[] { "eventLabel", "uei" };

    /** The Constant COLUMN_LABELS. */
    public static final String[] COLUMN_LABELS = new String[] { "Event Label", "Event UEI" };

    /** The Table Container for Events. */
    private final BeanContainer<String, org.opennms.netmgt.xml.eventconf.Event> container;

    /**
     * Instantiates a new event table.
     *
     * @param events the OpenNMS events
     */
    public EventTable(final Events events) {
        container = new BeanContainer<String, org.opennms.netmgt.xml.eventconf.Event>(org.opennms.netmgt.xml.eventconf.Event.class);
        container.setBeanIdProperty("uei");
        container.addAll(events.getEventCollection());
        setContainerDataSource(container);
        setStyleName(Runo.TABLE_SMALL);
        setImmediate(true);
        setSelectable(true);
        setVisibleColumns(COLUMN_NAMES);
        setColumnHeaders(COLUMN_LABELS);
        setWidth("100%");
        setHeight("250px");
        addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (getValue() != null) {
                    updateExternalSource(getEvent(getValue()));
                }
            }
        });
    }

    /**
     * Update external source.
     *
     * @param event the OpenNMS event
     */
    public abstract void updateExternalSource(org.opennms.netmgt.xml.eventconf.Event event);

    /**
     * Gets all the OpenNMS events.
     *
     * @return the OpenNMS events
     */
    public List<org.opennms.netmgt.xml.eventconf.Event> getOnmsEvents() {
        List<org.opennms.netmgt.xml.eventconf.Event> events = new ArrayList<org.opennms.netmgt.xml.eventconf.Event>();
        for (String itemId : container.getItemIds()) {
            org.opennms.netmgt.xml.eventconf.Event e = getEvent(itemId);
            // It doesn't make any sense an alarmData without reductionKey
            AlarmData a = e.getAlarmData();
            if (a != null && (a.getReductionKey() == null || a.getReductionKey().trim().equals("")))
                e.setAlarmData(null);
            // It doesn't make any sense an mask without mask elements.
            Mask m = e.getMask();
            if (m != null && m.getMaskelementCollection().isEmpty())
                e.setMask(null);
            events.add(e);
        }
        return events;
    }

    /**
     * Adds an event.
     *
     * @param event the new event
     */
    public void addEvent(org.opennms.netmgt.xml.eventconf.Event event) {
        container.addBean(event);
    }

    /**
     * Gets the OpenNMS event.
     *
     * @param itemId the internal Item ID (Event's UEI)
     * @return the OpenNMS event
     */
    private org.opennms.netmgt.xml.eventconf.Event getEvent(Object itemId) {
        return container.getItem(itemId).getBean();
    }

}
