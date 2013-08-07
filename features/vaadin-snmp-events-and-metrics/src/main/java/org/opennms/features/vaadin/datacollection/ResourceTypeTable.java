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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.datacollection.ResourceType;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;

/**
 * The Class Resource Type Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ResourceTypeTable extends Table {

    /**
     * Instantiates a new resource type table.
     *
     * @param resourceTypes the resource types
     */
    public ResourceTypeTable(final List<ResourceType> resourceTypes) {
        BeanContainer<String,ResourceType> container = new BeanContainer<String,ResourceType>(ResourceType.class);
        container.setBeanIdProperty("name");
        container.addAll(resourceTypes);
        setContainerDataSource(container);
        addStyleName("light");
        setImmediate(true);
        setSelectable(true);
        setVisibleColumns(new Object[] { "label", "name" });
        setColumnHeaders(new String[] { "Resource Type Label", "Resource Type Name" });
        setWidth("100%");
        setHeight("250px");
    }

    /**
     * Gets the resource type.
     *
     * @param resourceTypeId the resourceType ID (the Item ID associated with the container, in this case, the ResourceType's name)
     * @return the resource type
     */
    @SuppressWarnings("unchecked")
    public ResourceType getResourceType(Object resourceTypeId) {
        return ((BeanItem<ResourceType>)getItem(resourceTypeId)).getBean();
    }

    /**
     * Gets the container.
     *
     * @return the container
     */
    @SuppressWarnings("unchecked")
    public BeanContainer<String, ResourceType> getContainer() {
        return (BeanContainer<String, ResourceType>) getContainerDataSource();
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public List<ResourceType> getResourceTypes() {
        List<ResourceType> resourceTypes = new ArrayList<ResourceType>();
        for (String itemId : getContainer().getItemIds()) {
            resourceTypes.add(getContainer().getItem(itemId).getBean());
        }
        return resourceTypes;
    }

}
