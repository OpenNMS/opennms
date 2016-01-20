/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class BsmTestData {

    private List<BusinessServiceEntity> businessServices = new ArrayList<>();

    public BsmTestData(BusinessServiceEntity... entities) {
        if (entities != null) {
            for (BusinessServiceEntity eachEntity : entities) {
                businessServices.add(eachEntity);
            }
        }
    }

    public int getServiceCount() {
        return businessServices.size();
    }

    public List<BusinessServiceEntity> getServices() {
        return Collections.unmodifiableList(businessServices);
    }

    public BusinessServiceEntity findByName(final String name) {
        List<BusinessServiceEntity> entities = businessServices.stream().filter(s -> name.equals(s.getName())).collect(Collectors.toList());
        if (entities.isEmpty()) {
            throw new NoSuchElementException("No Business Service with name " + name + " found in Test Data");
        }
        if (entities.size() > 1) {
            throw new IllegalArgumentException("More than one Business Service with name " + name + " found in Test Data");
        }
        return entities.get(0);
    }

    public OnmsMonitoredService findIpService(String ipAddress, String serviceName) {
        List<OnmsMonitoredService> entities = businessServices
                .stream()
                .flatMap(eachEntity -> eachEntity.getIpServices().stream())
                .filter(eachService -> InetAddressUtils.addr(ipAddress).equals(eachService.getIpAddress()) && eachService.getServiceName().equals(serviceName))
                .collect(Collectors.toList());
        if (entities.isEmpty()) {
            throw new NoSuchElementException("No IpService with criteria found");
        }
        if (entities.size() > 1) {
            throw new IllegalArgumentException("More than one IpService with criteria found");
        }
        return entities.get(0);
    }
}
