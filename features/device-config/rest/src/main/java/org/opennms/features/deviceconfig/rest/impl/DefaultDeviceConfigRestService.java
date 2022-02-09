/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.rest.impl;

import java.util.Date;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.web.utils.ResponseUtils;

public class DefaultDeviceConfigRestService implements DeviceConfigRestService {

    private final DeviceConfigDao deviceConfigDao;

    public DefaultDeviceConfigRestService(DeviceConfigDao deviceConfigDao) {
        this.deviceConfigDao = deviceConfigDao;
    }

    @Override
    public Response getDeviceConfigs(
            Integer limit,
            Integer offset,
            String orderBy,
            String order,
            Integer ipInterfaceId,
            Long createdAfter,
            Long createdBefore
    ) {
        var criteriaBuilder = new CriteriaBuilder(DeviceConfig.class);

        if (limit != null) {
            criteriaBuilder.limit(limit);
        }
        if (offset != null) {
            criteriaBuilder.offset(offset);
        }
        if (orderBy != null) {
            criteriaBuilder.orderBy(orderBy, "asc".equals(order));
        }

        if (ipInterfaceId != null) {
            criteriaBuilder.eq("ipInterface.id", ipInterfaceId);
        }

        if (createdAfter != null) {
            criteriaBuilder.ge("createdTime", new Date(createdAfter));
        }

        if (createdBefore != null) {
            criteriaBuilder.le("createdTime", new Date(createdBefore));
        }

        var criteria = criteriaBuilder.toCriteria();

        var deviceConfigs = deviceConfigDao.findMatching(criteria);
        var dtos = deviceConfigs.stream().map(DefaultDeviceConfigRestService::deviceConfigDto).collect(Collectors.toList());

        if (limit != null || offset != null) {
            criteria.setLimit(null);
            criteria.setOffset(null);
            var total = deviceConfigDao.countMatching(criteria);
            return ResponseUtils.createResponse(dtos, offset, total);
        } else {
            if (dtos.isEmpty()) {
                return Response.noContent().build();
            } else {
                return Response.ok(dtos).build();
            }
        }
    }

    @Override
    public DeviceConfigDTO getDeviceConfig(long id) {
        var dc = deviceConfigDao.get(id);
        return deviceConfigDto(dc);
    }

    @Override
    public void deleteDeviceConfig(long id) {
        deviceConfigDao.delete(id);
    }

    private static DeviceConfigDTO deviceConfigDto(DeviceConfig deviceConfig) {
        return new DeviceConfigDTO(
                deviceConfig.getId(),
                deviceConfig.getIpInterface().getId(),
                deviceConfig.getConfig(),
                deviceConfig.getEncoding(),
                deviceConfig.getCreatedTime()
        );
    }
}
