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

package org.opennms.features.vaadin.datacollection;

import java.util.List;

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.datacollection.ResourceType;

import com.vaadin.ui.Table;

/**
 * The Class Resource Type Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ResourceTypeTable extends Table {

    /** The Resource Type Container. */
    private OnmsBeanContainer<ResourceType> container = new OnmsBeanContainer<ResourceType>(ResourceType.class);

    /**
     * Instantiates a new resource type table.
     *
     * @param resourceTypes the resource types
     */
    public ResourceTypeTable(final List<ResourceType> resourceTypes) {
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
     * @param resourceTypeId the resourceType ID (the Item ID associated with the container)
     * @return the resource type
     */
    public ResourceType getResourceType(Object resourceTypeId) {
        return container.getItem(resourceTypeId).getBean();
    }

    /**
     * Adds the resource type.
     *
     * @param resourceType the new resource type
     * @return the resourceTypeId
     */
    public Object addResourceType(ResourceType resourceType) {
        Object resourceTypeId = container.addOnmsBean(resourceType);
        select(resourceTypeId);
        return resourceTypeId;

    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public List<ResourceType> getResourceTypes() {
        return container.getOnmsBeans();
    }

}
