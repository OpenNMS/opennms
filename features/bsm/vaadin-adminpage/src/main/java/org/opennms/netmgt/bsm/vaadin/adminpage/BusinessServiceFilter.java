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
package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Map;

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;

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