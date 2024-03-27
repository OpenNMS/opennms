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
package org.opennms.features.deviceconfig.rest.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import net.redhogs.cronparser.CronExpressionDescriptor;
import org.apache.commons.collections.CollectionUtils;
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
import org.opennms.features.deviceconfig.rest.BackupResponseDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.features.usageanalytics.api.UsageAnalyticDao;
import org.opennms.features.usageanalytics.api.UsageAnalyticMetricName;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.utils.ResponseUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;
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
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.opennms.features.deviceconfig.service.DeviceConfigService.DEVICE_CONFIG_PREFIX;

public class DefaultDeviceConfigRestService implements DeviceConfigRestService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDeviceConfigRestService.class);
    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();
    public static final String BINARY_ENCODING = "binary";

    private final TransactionOperations operations;

    private UsageAnalyticDao usageAnalyticDao;

    private static final Map<String,String> ORDERBY_QUERY_PROPERTY_MAP = Map.of(
        "lastupdated", "lastUpdated",
        "devicename", "ipInterface.node.label",
        "lastbackup", "createdTime",
        "ipaddress", "ipInterface.ipAddr",
        "location", "ipInterface.node.location.locationName",
        "status", "status"
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

    public DefaultDeviceConfigRestService(DeviceConfigDao deviceConfigDao, DeviceConfigService deviceConfigService, TransactionOperations operations) {
        this.deviceConfigDao = deviceConfigDao;
        this.deviceConfigService = deviceConfigService;
        this.operations = Objects.requireNonNull(operations);
    }

    public void setUsageAnalyticDao(UsageAnalyticDao usageAnalyticDao) {
        this.usageAnalyticDao = usageAnalyticDao;
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
        Set<DeviceConfigStatus> statuses,
        boolean pageEnter
    ) {
        if (pageEnter) {
            operations.execute(status -> {
                usageAnalyticDao.incrementCounterByMetricName(UsageAnalyticMetricName.DCB_WEBUI_ENTRY.toString());
                return null;
            });
        }

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
    public Response getDeviceConfigsByInterface(Integer ipInterfaceId, String configType) {
        var criteria = getCriteria(
            null,
            null,
            "lastUpdated",
            "desc",
            null,
            null,
            ipInterfaceId,
            configType,
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
    public Response deleteDeviceConfigs(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            LOG.debug("Bad request : empty or null DeviceConfig Id");
            return Response.status(Status.BAD_REQUEST).entity("Invalid 'id' parameter").build();
        }

        return operations.execute(status -> {
            try {
                final List<DeviceConfig> deviceConfigList = new ArrayList<>();
                for (Long id : ids) {
                    final DeviceConfig dc = deviceConfigDao.get(id);
                    if (dc == null) {
                        LOG.debug("could not find device config data for id: {}", id);
                        return Response.status(Status.NOT_FOUND).entity("Invalid 'id' parameter").build();
                    }
                    deviceConfigList.add(dc);
                }
                deviceConfigDao.deleteDeviceConfigs(deviceConfigList);
                return Response.noContent().build();
            } catch (Exception e) {
                LOG.error("Exception while deleting device configs, one or more ids not valid {}", e);
                return Response.noContent().build();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Response deleteDeviceConfig(long id) {
        return operations.execute(status -> {
            try {
                final DeviceConfig dc = deviceConfigDao.get(id);
                if (dc == null) {
                    LOG.debug("could not find device config data for id: {}", id);
                    return Response.status(Status.NOT_FOUND).entity("Invalid 'id' parameter").build();
                } else {
                    deviceConfigDao.delete(dc);
                    return Response.noContent().build();
                }
            } catch (Exception e) {
                LOG.error("Exception while deleting device config, provided id is not valid {}", e);
                return Response.noContent().build();
            }
        });
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
        if (Strings.isNullOrEmpty(schedulePattern) || DeviceConfigConstants.NEVER.equalsIgnoreCase(schedulePattern)) {
            return new ScheduleInfo(null, "Never");
        }

        final String cronDescription;
        try {
            cronDescription = CronExpressionDescriptor.getDescription(schedulePattern);
        } catch (ParseException pe) {
            LOG.error("Invalid cron expression {}", schedulePattern, pe);
            return new ScheduleInfo(null, "Invalid Schedule");
        }

        try {
            final Date nextScheduledBackup = getNextRunDate(schedulePattern, current);
            return new ScheduleInfo(nextScheduledBackup, cronDescription);
        } catch (Exception e) {
            LOG.error("Invalid cron expression {}", schedulePattern, e);
            return new ScheduleInfo(null, "Invalid Schedule");
        }
    }

    @Override
    public Response triggerDeviceConfigBackup(List<BackupRequestDTO> backupRequestDtoList) {
        if (backupRequestDtoList == null || backupRequestDtoList.isEmpty()) {
            final var message = "Cannot trigger config backup on empty request list";
            LOG.error(message);
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        }
        List<BackupResponseDTO> backupResponses = new ArrayList<>();

        for (var requestDto : backupRequestDtoList) {
            try {
                DeviceConfigService.DeviceConfigBackupResponse response = null;
                if (requestDto.getBlocking()) {
                    response = deviceConfigService.triggerConfigBackup(requestDto.getIpAddress(), requestDto.getLocation(), requestDto.getServiceName(), true).get();
                } else {
                    deviceConfigService.triggerConfigBackup(requestDto.getIpAddress(), requestDto.getLocation(), requestDto.getServiceName(), true);
                }
                // if the blocking request returned a failure, handle it
                if (response != null && !Strings.isNullOrEmpty(response.getErrorMessage())) {
                    LOG.error("Unable to trigger config backup for {} at location {} with configType {}",
                            requestDto.getIpAddress(), requestDto.getLocation(), requestDto.getServiceName());
                    backupResponses.add(new BackupResponseDTO(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getErrorMessage(), requestDto));
                }
            } catch (IllegalArgumentException e) {
                LOG.error("Unable to trigger config backup for {} at location {} with configType {}",
                        requestDto.getIpAddress(), requestDto.getLocation(), requestDto.getServiceName());
                backupResponses.add(new BackupResponseDTO(Status.BAD_REQUEST.getStatusCode(), e.getMessage(), requestDto));
            } catch (IOException | ExecutionException | InterruptedException e) {
                LOG.error("Unable to trigger config backup for {} at location {} with configType {}",
                        requestDto.getIpAddress(), requestDto.getLocation(), requestDto.getServiceName());
                backupResponses.add(new BackupResponseDTO(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage(), requestDto));
            }
        }

        if (backupResponses.isEmpty()) {
            return Response.accepted().build();
        } else if (backupRequestDtoList.size() == 1 && backupResponses.size() == 1) {
            BackupResponseDTO singleResponse = backupResponses.get(0);
            return Response.status(singleResponse.getStatus()).entity(singleResponse.getFailureMessage()).build();
        }
        // multi response with some failures.
        return Response.status(207).entity(backupResponses).build();
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
        dto.setConfigName(createConfigName(queryResult.getServiceName()));
        dto.setFileName(queryResult.getFilename());
        dto.setConfig(config);
        dto.setFailureReason(queryResult.getFailureReason());

        DeviceConfigStatus backupStatus = queryResult.getStatusOrDefault();
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
        dto.setConfigName(createConfigName(deviceConfig.getServiceName()));
        dto.setFileName(deviceConfig.getFileName());
        dto.setConfig(config);
        dto.setFailureReason(deviceConfig.getFailureReason());

        DeviceConfigStatus backupStatus = deviceConfig.getStatusOrDefault();
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
            .filter(v -> v != null)
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
     * @return a {@link Pair} object with the actual encoding used
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

    /** Convert a service name to a "friendly" configuration name. */
    private static String createConfigName(String serviceName) {
        if (Strings.isNullOrEmpty(serviceName) || !serviceName.startsWith(DEVICE_CONFIG_PREFIX)) {
            return serviceName;
        }

        int index = serviceName.indexOf('-');
        String suffix = index >= 0 ? serviceName.substring(index + 1) : "";

        if (index < 0 || suffix.toLowerCase(Locale.ROOT).equals("default")) {
            // "DeviceConfig" or "DeviceConfig-default"
            return "Startup Configuration";
        } else {
            String name = Arrays.stream(suffix.split("-"))
                .map(StringUtils::capitalize).collect(Collectors.joining(" "));

            return name + " Configuration";
        }
    }
}
