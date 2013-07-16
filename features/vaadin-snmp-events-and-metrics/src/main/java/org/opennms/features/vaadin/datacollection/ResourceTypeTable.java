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
package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.ResourceType;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Runo;

/**
 * The Class Resource Type Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class ResourceTypeTable extends Table {

    /** The Constant COLUMN_NAMES. */
    public static final String[] COLUMN_NAMES = new String[] { "label", "name" };

    /** The Constant COLUMN_LABELS. */
    public static final String[] COLUMN_LABELS = new String[] { "Resource Type Label", "Resource Type Name" };

    /**
     * Instantiates a new resource type table.
     *
     * @param group the OpenNMS Data Collection Group
     */
    public ResourceTypeTable(final DatacollectionGroup group) {
        BeanContainer<String,ResourceType> container = new BeanContainer<String,ResourceType>(ResourceType.class);
        container.setBeanIdProperty("name");
        container.addAll(group.getResourceTypeCollection());
        setContainerDataSource(container);
        setStyleName(Runo.TABLE_SMALL);
        setImmediate(true);
        setSelectable(true);
        setVisibleColumns(COLUMN_NAMES);
        setColumnHeaders(COLUMN_LABELS);
        setWidth("100%");
        setHeight("250px");
        addValueChangeListener(new Property.ValueChangeListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (getValue() != null) {
                    BeanItem<ResourceType> item = (BeanItem<ResourceType>) getContainerDataSource().getItem(getValue());
                    updateExternalSource(item);
                }
            }
        });
    }

    /**
     * Update external source.
     *
     * @param item the item
     */
    public abstract void updateExternalSource(BeanItem<ResourceType> item);

    /**
     * Adds a resource type.
     *
     * @param resourceType the resource type
     */
    @SuppressWarnings("unchecked")
    public void addResourceType(ResourceType resourceType) {
        ((BeanContainer<String,ResourceType>) getContainerDataSource()).addBean(resourceType);
    }

}
