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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Strings;
import net.redhogs.cronparser.CronExpressionDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigQueryResult;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.rest.BackupRequestDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.utils.ResponseUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class DefaultDeviceConfigRestService implements DeviceConfigRestService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDeviceConfigRestService.class);
    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();
    public static final String BINARY_ENCODING = "binary";

    private static final Map<String,String> ORDERBY_QUERY_PROPERTY_MAP = Map.of(
        "lastupdated", "lastUpdated",
        "devicename", "ipInterface.node.label",
        "lastbackup", "createdTime",
        "ipaddress", "ipInterface.ipAddr"
    );

    private final DeviceConfigDao deviceConfigDao;
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

    public DefaultDeviceConfigRestService(DeviceConfigDao deviceConfigDao, DeviceConfigService deviceConfigService) {
        this.deviceConfigDao = deviceConfigDao;
        this.deviceConfigService = deviceConfigService;
    }

    /** {@inheritDoc} */
    @Override
    public Response getDeviceConfig(long id) {
        final var dc = deviceConfigDao.get(id);

        if (dc == null) {
            return Response.noContent().build();
        }

        return Response.ok(createDeviceConfigDto(dc)).build();
    }

    /** {@inheritDoc} */
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
            Set<DeviceConfigStatus> statuses,
            Long createdAfter,
            Long createdBefore
    ) {
        var criteria = getCriteria(
            limit,
            offset,
            orderBy,
            order,
            deviceName,
            ipAddress,
            ipInterfaceId,
            configType,
            statuses,
            createdAfter,
            createdBefore);

        List<DeviceConfigDTO> dtos = this.deviceConfigDao.findMatching(criteria)
            .stream()
            .map(this::createDeviceConfigDto)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        final long offsetToUse = offset != null ? offset.longValue() : 0L;
        int totalCount = dtos.size();

        if (limit != null || offset != null) {
            criteria.setLimit(null);
            criteria.setOffset(null);
            totalCount = deviceConfigDao.countMatching(criteria);
        }

        return ResponseUtils.createResponse(dtos, offsetToUse, totalCount);
    }

    /** {@inheritDoc} */
    @Override
    public Response getLatestDeviceConfigsForDeviceAndConfigType(
        Integer limit,
        Integer offset,
        String orderBy,
        String order,
        String searchTerm,
        Set<DeviceConfigStatus> statuses
    ) {
        List<DeviceConfigDTO> dtos =
            this.deviceConfigDao.getLatestConfigForEachInterface(limit, offset, orderBy, order, searchTerm, statuses)
                .stream()
                .map(this::createDeviceConfigDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        final int totalCount =
            (limit != null || offset != null)
            ? deviceConfigDao.getLatestConfigCountForEachInterface(searchTerm, statuses)
            : dtos.size();

        final long offsetForResponse = offset != null ? offset.longValue() : 0L;

        return ResponseUtils.createResponse(dtos, offsetForResponse, totalCount);
    }

    /** {@inheritDoc} */
    @Override
    public Response getDeviceConfigsByInterface(Integer ipInterfaceId) {
        var criteria = getCriteria(
            null,
            null,
            "lastUpdated",
            "desc",
            null,
            null,
            ipInterfaceId,
            null,
            null,
            null,
            null);

        List<DeviceConfigDTO> dtos = this.deviceConfigDao.findMatching(criteria)
            .stream()
            .map(this::createDeviceConfigDto)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        final long offsetToUse = 0;
        final int totalCount = dtos.size();

        return ResponseUtils.createResponse(dtos, offsetToUse, totalCount);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteDeviceConfig(long id) {
        throw new UnsupportedOperationException("Delete device config not yet supported.");
        // deviceConfigDao.delete(id);
    }

    /** {@inheritDoc} */
    @Override
    public Response downloadDeviceConfig(String id) {
        if (Strings.isNullOrEmpty(id)) {
            LOG.debug("downloadDeviceConfig: empty or null id supplied by request");
            return Response.noContent().build();
        }

        final String idParamPattern = "\\d+(, ?\\d+)*";
        var pattern = Pattern.compile(idParamPattern);
        var matcher = pattern.matcher(id);

        if (!matcher.matches()) {
            LOG.debug("downloadDeviceConfig: invalid id param supplied by request");
            return Response.status(Status.BAD_REQUEST).entity("Invalid 'id' parameter").build();
        }

        List<Long> ids = Arrays.stream(id.split(","))
            .filter(s -> !Strings.isNullOrEmpty(s))
            .map(Long::parseLong)
            .collect(Collectors.toList());

        if (ids.isEmpty()) {
            LOG.debug("downloadDeviceConfig: no ids supplied by request");
            return Response.noContent().build();
        }

        if (ids.size() == 1) {
            return downloadSingleDeviceConfig(ids.get(0));
        } else {
            return downloadMultipleDeviceConfigs(ids);
        }
    }

    private Response downloadSingleDeviceConfig(Long id) {
        final DeviceConfig dc = deviceConfigDao.get(id);

        if (dc == null || dc.getConfig() == null || dc.getConfig().length == 0) {
            LOG.debug("could not find device config data for id: {}", id);
            return Response.noContent().build();
        }

        final var outputStream = new ByteArrayOutputStream();
        outputStream.write(dc.getConfig(), 0, dc.getConfig().length);

        final String fileName = createDownloadFileName(dc);
        final Charset charset = getEncodingCharset(dc.getEncoding());

        return Response.ok().type("text/plain;charset=" + charset.name())
            .header("Content-Disposition", "attachment; filename=" + fileName)
            .header("Pragma", "public")
            .header("Cache-Control", "cache")
            .header("Cache-Control", "must-revalidate")
            .entity(outputStream.toByteArray()).build();
    }

    private Response downloadMultipleDeviceConfigs(List<Long> ids) {
        final Map<String, byte[]> fileNameToDataMap = ids.stream()
            .map(deviceConfigDao::get)
            .filter(dc -> dc != null && dc.getConfig() != null && dc.getConfig().length > 0)
            .collect(Collectors.toMap(dc -> createDownloadFileName(dc), dc -> dc.getConfig()));

        if (fileNameToDataMap.isEmpty()) {
            LOG.debug("no config data found for request");
            return Response.noContent().build();
        }

        byte[] gzipBytes = null;

        try {
            gzipBytes = CompressionUtils.tarGzipMultipleFiles(fileNameToDataMap);
        } catch (IOException e) {
            var message = "Error compressing multiple files for download.";
            LOG.error(message, e);
            throw getException(Status.INTERNAL_SERVER_ERROR, message);
        }

        final var currentTime = new Date();
        final var formatter = new SimpleDateFormat("YYYYMMdd-HHmmss");
        final String timestamp = formatter.format(currentTime);
        final String fileName = "device-configs-" + timestamp + ".tar.gz";

        return Response.ok().type("application/gzip")
            .header("Content-Disposition", "attachment; filename=" + fileName)
            .header("Pragma", "public")
            .header("Cache-Control", "cache")
            .header("Cache-Control", "must-revalidate")
            .entity(gzipBytes).build();
    }

    public static String createDownloadFileName(
        final String deviceName, String ipAddress, String configType, Date createdTime) {
        final var formatter = new SimpleDateFormat("YYYYMMdd-HHmmss");

        final var items = List.of(
            !Strings.isNullOrEmpty(deviceName) ? deviceName : "device",
            !Strings.isNullOrEmpty(ipAddress) ? ipAddress.replace('.', '_') : "ipaddress",
            !Strings.isNullOrEmpty(configType) ? configType : "default",
            formatter.format(createdTime)
        );

        final String fileName = String.join("-", items) + ".cfg";

        return fileName;
    }

    private static ScheduleInfo getScheduleInfo(Date current, String schedulePattern) {
        final String cronDescription;
        try {
            cronDescription = CronExpressionDescriptor.getDescription(schedulePattern);
        } catch (ParseException pe) {
            return new ScheduleInfo(null, "unknown");
        }

        final Date nextScheduledBackup = getNextRunDate(schedulePattern, current);

        return new ScheduleInfo(nextScheduledBackup, cronDescription);
    }

    @Override
    public Response triggerDeviceConfigBackup(List<BackupRequestDTO> backupRequestDtoList) {
        if (backupRequestDtoList == null || backupRequestDtoList.isEmpty()) {
            final var message = "Cannot trigger config backup on empty request list";
            LOG.error(message);
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }

        for (var requestDto : backupRequestDtoList) {
            try {
                deviceConfigService.triggerConfigBackup(requestDto.getIpAddress(),
                    requestDto.getLocation(), requestDto.getServiceName());
            } catch (Exception e) {
                LOG.error("Unable to trigger config backup for {} at location {} with configType {}",
                    requestDto.getIpAddress(), requestDto.getLocation(), requestDto.getServiceName());
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
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
        Set<DeviceConfigStatus> statuses,
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

        if (statuses != null && !statuses.isEmpty()) {
            List<Restriction> restrictions = statuses.stream()
                .map(status -> Restrictions.ilike("status", status.name())).collect(Collectors.toList());

            criteriaBuilder.or(restrictions.stream().toArray(Restriction[]::new));
        }

        if (createdAfter != null) {
            criteriaBuilder.ge("createdTime", new Date(createdAfter));
        }

        if (createdBefore != null) {
            criteriaBuilder.le("createdTime", new Date(createdBefore));
        }

        return criteriaBuilder.toCriteria();
    }

    private WebApplicationException getException(final Status status, final String message) {
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    private DeviceConfigDTO createDeviceConfigDto(DeviceConfigQueryResult queryResult) {
        Pair<String,String> pair = configToText(queryResult.getEncoding(), queryResult.getConfig());
        final String encoding = pair.getLeft();
        final String config = pair.getRight();

        var dto = new DeviceConfigDTO();
        dto.setId(queryResult.getId());
        dto.setServiceName(queryResult.getServiceName());
        dto.setIpAddress(queryResult.getIpAddr());
        dto.setLastBackupDate(queryResult.getCreatedTime());
        dto.setLastUpdatedDate(queryResult.getLastUpdated());
        dto.setLastSucceededDate(queryResult.getLastSucceeded());
        dto.setLastFailedDate(queryResult.getLastFailed());
        dto.setEncoding(encoding);
        dto.setConfigType(queryResult.getConfigType());
        dto.setFileName(queryResult.getFilename());
        dto.setConfig(config);
        dto.setFailureReason(queryResult.getFailureReason());

        DeviceConfigStatus backupStatus = DeviceConfig.determineBackupStatus(queryResult.getLastUpdated(), queryResult.getLastSucceeded());
        dto.setIsSuccessfulBackup(backupStatus.equals(DeviceConfigStatus.SUCCESS));
        dto.setBackupStatus(backupStatus.name().toLowerCase(Locale.ROOT));

        dto.setIpInterfaceId(queryResult.getIpInterfaceId());
        dto.setNodeId(queryResult.getNodeId());
        dto.setNodeLabel(queryResult.getNodeLabel());
        dto.setDeviceName(queryResult.getNodeLabel());
        dto.setLocation(queryResult.getLocation());
        dto.setOperatingSystem(queryResult.getOperatingSystem());

        populateScheduleInfo(dto);

        return dto;
    }

    private DeviceConfigDTO createDeviceConfigDto(DeviceConfig deviceConfig) {
        Pair<String,String> pair = configToText(deviceConfig.getEncoding(), deviceConfig.getConfig());
        final String encoding = pair.getLeft();
        final String config = pair.getRight();

        var dto = new DeviceConfigDTO();
        dto.setId(deviceConfig.getId());
        dto.setServiceName(deviceConfig.getServiceName());
        dto.setIpAddress(InetAddressUtils.str(deviceConfig.getIpInterface().getIpAddress()));
        dto.setLastBackupDate(deviceConfig.getCreatedTime());
        dto.setLastUpdatedDate(deviceConfig.getLastUpdated());
        dto.setLastSucceededDate(deviceConfig.getLastSucceeded());
        dto.setLastFailedDate(deviceConfig.getLastFailed());
        dto.setEncoding(encoding);
        dto.setConfigType(deviceConfig.getConfigType());
        dto.setFileName(deviceConfig.getFileName());
        dto.setConfig(config);
        dto.setFailureReason(deviceConfig.getFailureReason());

        DeviceConfigStatus backupStatus = DeviceConfig.determineBackupStatus(deviceConfig);
        dto.setIsSuccessfulBackup(backupStatus.equals(DeviceConfigStatus.SUCCESS));
        dto.setBackupStatus(backupStatus.name().toLowerCase(Locale.ROOT));

        final OnmsIpInterface ipInterface = deviceConfig.getIpInterface();
        final OnmsNode node = ipInterface.getNode();

        dto.setIpInterfaceId(ipInterface.getId());
        dto.setNodeId(node.getId());
        dto.setNodeLabel(node.getLabel());
        dto.setDeviceName(node.getLabel());
        dto.setLocation(node.getLocation().getLocationName());
        dto.setOperatingSystem(node.getOperatingSystem());

        populateScheduleInfo(dto);

        return dto;
    }

    private void populateScheduleInfo(DeviceConfigDTO dto) {
        // Figure out the schedules for service defined to do backups for this device
        Date currentDate = new Date();
        final var schedules = this.deviceConfigService.getRetrievalDefinitions(dto.getIpAddress(), dto.getLocation()).stream()
            .filter(ret -> Objects.equals(ret.getConfigType(), dto.getConfigType()))
            .collect(Collectors.toMap(DeviceConfigService.RetrievalDefinition::getServiceName,
                ret -> getScheduleInfo(currentDate, ret.getSchedule())));

        dto.setScheduledInterval(Maps.transformValues(schedules, ScheduleInfo::getScheduleInterval));

        // Calculate next scheduled date over all services
        schedules.values().stream()
            .map(ScheduleInfo::getNextScheduledBackup)
            .min(Date::compareTo)
            .ifPresent(dto::setNextScheduledBackupDate);
    }

    // This method's implementation should be the same as in DeviceConfigMonitor
    private static Date getNextRunDate(String cronSchedule, Date lastRun) {
        final Trigger trigger = TriggerBuilder.newTrigger()
            .withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))
            .startAt(lastRun)
            .build();

        return trigger.getFireTimeAfter(lastRun);
    }

    /**
     * Given an encoding and byte array from a {@link DeviceConfig } object, determine the proper encoding
     * and convert the config to a text representation. Handles various text encodings as well as
     * binary encoding.
     * @param encoding
     * @param configBytes
     * @return a {@link org.apache.commons.lang3.tuple.Pair} object with the actual encoding used
     * and the text representation of the config.
     */
    private static Pair<String,String> configToText(String encoding, byte[] configBytes) {
        final boolean isBinaryEncoding = !Strings.isNullOrEmpty(encoding) && encoding.equals(BINARY_ENCODING);

        if (isBinaryEncoding) {
            String config = configBytes != null ? DatatypeConverter.printHexBinary(configBytes) : "";
            return Pair.of(BINARY_ENCODING, config);
        } else {
            Charset charset = getEncodingCharset(encoding);
            String config = configBytes != null ? new String(configBytes, charset) : "";

            return Pair.of(charset.name(), config);
        }
    }

    private static Charset getEncodingCharset(String encoding) {
        return
            !Strings.isNullOrEmpty(encoding) && Charset.isSupported(encoding)
            ? Charset.forName(encoding) : Charset.defaultCharset();
    }

    private static String createDownloadFileName(DeviceConfig dc) {
        OnmsIpInterface ipInterface = dc.getIpInterface();
        String ipAddress = InetAddressUtils.str(ipInterface.getIpAddress());
        String deviceName = ipInterface.getNode().getLabel();

        return createDownloadFileName(deviceName, ipAddress, dc.getConfigType(), dc.getCreatedTime());
    }
}
