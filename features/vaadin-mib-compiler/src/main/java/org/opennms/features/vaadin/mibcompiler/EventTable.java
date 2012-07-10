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

import java.util.List;

import org.opennms.features.vaadin.mibcompiler.model.EventDTO;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Table;

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
    public EventTable(List<EventDTO> events) {
        setContainerDataSource(new BeanItemContainer<EventDTO>(EventDTO.class, events));
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
        return item;
    }

    /**
     * Update external source.
     *
     * @param item the item
     */
    public abstract void updateExternalSource(BeanItem<EventDTO> item);
}
