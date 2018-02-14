/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class BusinessServiceFilter implements Container.Filter {
    public static final String NAME_PROPERTY = "name";

    private BusinessServiceContainer businessServiceContainer;
    private String filter;

    public BusinessServiceFilter(BusinessServiceContainer businessServiceContainer, String filter) {
        this.businessServiceContainer = businessServiceContainer;
        this.filter = filter.toLowerCase();
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) {
        final Property<?> property = item.getItemProperty(NAME_PROPERTY);
        return checkSuccessor(itemId, property) || checkPredecessor(itemId, property);
    }

    private boolean checkPredecessor(Object itemId, Property<?> property) {
        if (property != null) {
            if (property.getValue() != null) {
                if (((String) property.getValue()).toLowerCase().contains(filter)) {
                    return true;
                }
            }
        }

        for (Map.Entry<Long, Long> entry : businessServiceContainer.getRowIdToParentRowIdMapping().entrySet()) {
            if (itemId.equals(entry.getValue())) {
                if (checkPredecessor(entry.getKey(), businessServiceContainer.getContainerProperty(entry.getKey(), NAME_PROPERTY))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSuccessor(Object itemId, Property<?> property) {
        if (property != null) {
            if (property.getValue() != null) {
                if (((String) property.getValue()).toLowerCase().contains(filter)) {
                    return true;
                }
            }
        }

        for (Map.Entry<Long, Long> entry : businessServiceContainer.getRowIdToParentRowIdMapping().entrySet()) {
            if (itemId.equals(entry.getKey())) {
                if (checkSuccessor(entry.getValue(), businessServiceContainer.getContainerProperty(entry.getValue(), NAME_PROPERTY))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean appliesToProperty(Object propertyId) {
        return NAME_PROPERTY.equals(propertyId);
    }
}