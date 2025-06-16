/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.datacollection;

import java.util.List;

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.datacollection.ResourceType;

import com.vaadin.v7.ui.Table;

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
