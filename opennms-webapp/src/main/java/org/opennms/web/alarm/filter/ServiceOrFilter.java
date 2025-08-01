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
package org.opennms.web.alarm.filter;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.OrFilter;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Map;

public class ServiceOrFilter extends OrFilter {

    private final Integer[] serviceIds;
    private final  Map<String, Integer> serviceNameToIdMap;

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
        return String.format("Service ID: %d",  serviceId);
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
