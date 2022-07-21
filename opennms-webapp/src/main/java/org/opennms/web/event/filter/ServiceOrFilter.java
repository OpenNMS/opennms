/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.event.filter;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.OrFilter;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Map;

public class ServiceOrFilter extends OrFilter {

    private final Integer[] serviceIds;
    private final Map<String, Integer> serviceNameToIdMap;

    public ServiceOrFilter(Integer[] serviceIds, ServletContext servletContext) {
        super(Arrays.stream(serviceIds)
                .map(serviceId -> new ServiceFilter(serviceId, servletContext))
                .toArray(Filter[]::new));
        this.serviceIds = serviceIds;
        this.serviceNameToIdMap = NetworkElementFactory
                .getInstance(servletContext).getServiceNameToIdMap();
    }

    @Override
    public String getTextDescription() {
        String[] serviceNames = new String[serviceIds.length];
        for (int index = 0; index < serviceIds.length; index++) {
            Integer serviceId = serviceIds[index];
            serviceNames[index] = findServiceName(serviceId);
        }
        return ("Service OR Filter: \"" + Arrays.toString(serviceNames) + "\"");
    }

    private String findServiceName(Integer serviceId) {
        if (!CollectionUtils.isEmpty(serviceNameToIdMap)) {
            for (Map.Entry<String, Integer> stringIntegerEntry : serviceNameToIdMap.entrySet()) {
                if (stringIntegerEntry.getValue().equals(serviceId)) {
                    return stringIntegerEntry.getKey();
                }
            }
        }
        return String.format("Service ID: %d", serviceId);
    }

    @Override
    public String toString() {
        return ("<ServiceOrFilter: " + this.getDescription() + ">");
    }

    @Override
    public String getDescription() {
        return TYPE + "=" + Arrays.toString(serviceIds);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ServiceOrFilter)) return false;
        return this.toString().equals(obj.toString());
    }
}
