/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.wmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.datacollection.ResourceType;

@XmlRootElement
public class WmiResourceTypeListWrapper {
    private List<ResourceType> resourceTypes = new ArrayList<>();

    public WmiResourceTypeListWrapper() {
    }

    public WmiResourceTypeListWrapper(final Map<String, ResourceType> resourceTypesMap) {
        this.resourceTypes = new ArrayList<>(resourceTypesMap.values());
    }

    public Map<String, ResourceType> getMap() {
        return resourceTypes.stream()
                .collect(Collectors.toMap(r -> r.getName(), Function.identity()));
    }

    public List<ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(final List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }
}
