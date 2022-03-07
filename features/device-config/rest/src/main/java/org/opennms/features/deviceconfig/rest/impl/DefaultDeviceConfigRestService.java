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

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.rest.BackupRequestDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDeviceConfigRestService implements DeviceConfigRestService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDeviceConfigRestService.class);
    private static final String REQUISITION_CONTEXT = "requisition";
    private static final String SCHEDULE_METADATA_KEY = "schedule";

    private static final Map<String,String> ORDERBY_QUERY_PROPERTY_MAP = Map.of(
        "lastupdated", "lastUpdated",
        "devicename", "ipInterface.node.label",
        "createdtime", "createdTime",
        "ipaddress", "ipInterface.ipAddr"
    );

    private final DeviceConfigDao deviceConfigDao;
    private final MonitoredServiceDao monitoredServiceDao;
    private final DeviceConfigService deviceConfigService;

    private static class ScheduleInfo {
        private final Date nextScheduledBackup;
        private final String scheduleInterval;

        public Date getNextScheduledBackup() { return this.nextScheduledBackup; }

        public String getScheduleInterval() { return this.scheduleInterval; }

        public ScheduleInfo(Date nextScheduledBackup, String scheduleInterval) {
            this.nextScheduledBackup = nextScheduledBackup;
            this.scheduleInterval = scheduleInterval;
        }
    }

    public DefaultDeviceConfigRestService(DeviceConfigDao deviceConfigDao, MonitoredServiceDao monitoredServiceDao, DeviceConfigService deviceConfigService) {
        this.deviceConfigDao = deviceConfigDao;
        this.monitoredServiceDao = monitoredServiceDao;
        this.deviceConfigService = deviceConfigService;
    }

    @Override
    public Response getDeviceConfigs(
            Integer limit,
            Integer offset,
            String orderBy,
            String order,
            String deviceName,
            String ipAddress,
            Integer ipInterfaceId,
            String configType,
            Long createdAfter,
            Long createdBefore
    ) {
        var criteria = getCriteria(limit, offset, orderBy, order, deviceName, ipAddress, ipInterfaceId, configType, createdAfter, createdBefore);

        // find DeviceConfig items in device_config database as per filter/sort criteria
        List<DeviceConfig> deviceConfigs = deviceConfigDao.findMatching(criteria);

        // do initial conversion to DTO with DeviceConfig data
        List<DeviceConfigDTO> dtos = deviceConfigs.stream().map(DefaultDeviceConfigRestService::createDeviceConfigDto).collect(Collectors.toList());

        // Get ipInterface Ids for current set of device configs
        List<Integer> ipInterfaceIds = dtos.stream().map(d -> d.getIpInterfaceId()).distinct().collect(Collectors.toList());

        // Get the services matching the above device config records
        List<OnmsMonitoredService> services = getDeviceConfigServices(ipInterfaceIds);

        // Get a list of services per ipInterfaceId so we can correlate with DeviceConfig data
        Map<Integer, List<OnmsMonitoredService>> serviceMap =
            services.stream().collect(Collectors.groupingBy(OnmsMonitoredService::getIpInterfaceId));

        populateScheduleInfo(dtos, serviceMap);

        if (limit != null || offset != null) {
            criteria.setLimit(null);
            criteria.setOffset(null);
            final int total = deviceConfigDao.countMatching(criteria);

            return ResponseUtils.createResponse(dtos, offset, total);
        } else {
            if (dtos.isEmpty()) {
                return Response.noContent().build();
            } else {
                return Response.ok(dtos).build();
            }
        }
    }

    /** Get DeviceConfig-related OnmsMonitoredServices for the given ip interface ids. */
    private List<OnmsMonitoredService> getDeviceConfigServices(List<Integer> ipInterfaceIds) {
        List<OnmsMonitoredService> services = monitoredServiceDao.findByServiceTypeAndIpInterfaceId(DEVICE_CONFIG_SERVICE_PREFIX, ipInterfaceIds);

        return services;
    }

    /**
     * Find corresponding service for each DTO (based on IP address and config type) and
     * update DTO schedule information based on service metadata.
     */
    private void populateScheduleInfo(List<DeviceConfigDTO> dtos, Map<Integer, List<OnmsMonitoredService>> serviceMap) {
        final Date currentDate = new Date();

        for (DeviceConfigDTO dto : dtos) {
            if (serviceMap.containsKey((Integer) dto.getIpInterfaceId())) {
                final List<OnmsMonitoredService> serviceList = serviceMap.get((Integer) dto.getIpInterfaceId());

                final String configType = Strings.isNullOrEmpty(dto.getConfigType()) ? ConfigType.Default : dto.getConfigType();
                final String serviceName = DEVICE_CONFIG_SERVICE_PREFIX + "-" + configType;

                final Optional<String> scheduleValue = getScheduleValue(serviceList, serviceName);

                if (scheduleValue.isPresent()) {
                    final ScheduleInfo scheduleInfo = getScheduleInfo(currentDate, scheduleValue.get());

                    dto.setNextScheduledBackupDate(scheduleInfo.getNextScheduledBackup());
                    dto.setScheduledInterval(scheduleInfo.getScheduleInterval());
                }
            }
        }
    }

    public static Optional<String> getScheduleValue(final List<OnmsMonitoredService> serviceList, final String serviceName) {
        final var matchingService =
            serviceList.stream()
            .filter(x -> x.getServiceName().equalsIgnoreCase(serviceName))
            .findFirst();

        if (matchingService.isPresent()) {
            return getScheduleMetaDataValue(matchingService.get().getMetaData());
        }

        return Optional.empty();
    }

    public static Optional<String> getScheduleMetaDataValue(final List<OnmsMetaData> metaData) {
        final var entry = metaData.stream()
            .filter(m -> m.getContext().equals(REQUISITION_CONTEXT) && m.getKey().equals((SCHEDULE_METADATA_KEY)))
            .map(m -> m.getValue())
            .findFirst();

        return entry;
    }

    private ScheduleInfo getScheduleInfo(Date current, String schedulePattern) {
        // TODO: parse out cron-style schedulePattern and determine values

        final Date nextScheduledBackup = Date.from(current.toInstant().plus(Duration.ofHours(25)));
        final String scheduleInterval = schedulePattern;

        return new ScheduleInfo(nextScheduledBackup, scheduleInterval);
    }

    @Override
    public DeviceConfigDTO getDeviceConfig(long id) {
        var dc = deviceConfigDao.get(id);
        return createDeviceConfigDto(dc);
    }

    @Override
    public void deleteDeviceConfig(long id) {
        deviceConfigDao.delete(id);
    }

    @Override
    public Response downloadDeviceConfig(@PathParam("id") long id) {
        throw new UnsupportedOperationException("downloadDeviceConfig not yet implemented.");
    }

    @Override
    public Response downloadDeviceConfigs() {
        throw new UnsupportedOperationException("downloadDeviceConfigs not yet implemented.");
    }

    @Override
    public Response triggerDeviceConfigBackup(BackupRequestDTO backupRequestDTO) {
        try {
            deviceConfigService.triggerConfigBackup(backupRequestDTO.getIpAddress(),
                    backupRequestDTO.getLocation(), backupRequestDTO.getConfigType());
        } catch (Exception e) {
            LOG.error("Unable to trigger config backup for {} at location {} with configType {}",
                    backupRequestDTO.getIpAddress(), backupRequestDTO.getLocation(), backupRequestDTO.getConfigType());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.accepted().build();
    }

    private Criteria getCriteria(
        Integer limit,
        Integer offset,
        String orderBy,
        String order,
        String deviceName,
        String ipAddress,
        Integer ipInterfaceId,
        String configType,
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

        // Sorting
        orderBy = orderBy != null ? orderBy.toLowerCase(Locale.ROOT) : null;

        if (!Strings.isNullOrEmpty(orderBy) && ORDERBY_QUERY_PROPERTY_MAP.containsKey(orderBy)) {
            String orderByToUse = ORDERBY_QUERY_PROPERTY_MAP.get(orderBy);

            boolean isOrderAscending = Strings.isNullOrEmpty(order) || !"desc".equals(order);
            criteriaBuilder.orderBy(orderByToUse, isOrderAscending);
        } else {
            // default sort order
            criteriaBuilder.orderBy("lastUpdated", false);
        }

        // Filtering
        if (!Strings.isNullOrEmpty(deviceName)) {
            criteriaBuilder.ilike("ipInterface.node.label", deviceName);
        }

        if (!Strings.isNullOrEmpty(ipAddress)) {
            criteriaBuilder.ilike("ipInterface.ipAddr", ipAddress);
        }

        if (ipInterfaceId != null) {
            criteriaBuilder.eq("ipInterface.id", ipInterfaceId);
        }

        if (StringUtils.isNoneBlank(configType)) {
            criteriaBuilder.ilike("configType", configType);
        }

        if (createdAfter != null) {
            criteriaBuilder.ge("createdTime", new Date(createdAfter));
        }

        if (createdBefore != null) {
            criteriaBuilder.le("createdTime", new Date(createdBefore));
        }

        return criteriaBuilder.toCriteria();
    }

    private static DeviceConfigDTO createDeviceConfigDto(DeviceConfig deviceConfig) {
        var dto = new DeviceConfigDTO(
            deviceConfig.getId(),
            deviceConfig.getIpInterface().getId(),
            InetAddressUtils.str(deviceConfig.getIpInterface().getIpAddress()),
            deviceConfig.getCreatedTime(),
            deviceConfig.getLastUpdated(),
            deviceConfig.getLastSucceeded(),
            deviceConfig.getLastFailed(),
            deviceConfig.getEncoding(),
            deviceConfig.getConfigType(),
            deviceConfig.getFileName(),
            deviceConfig.getFailureReason()
        );

        OnmsIpInterface ipInterface = deviceConfig.getIpInterface();
        OnmsNode node = ipInterface.getNode();

        dto.setNodeId(node.getId());
        dto.setNodeLabel(node.getLabel());
        dto.setDeviceName(node.getLabel());
        dto.setLocation(node.getLocation().getLocationName());
        dto.setOperatingSystem(node.getOperatingSystem());

        return dto;
    }
}
