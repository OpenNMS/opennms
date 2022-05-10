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

package org.opennms.features.deviceconfig.monitor.adaptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Strings;

import org.opennms.core.utils.StringUtils;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.features.deviceconfig.service.DeviceConfigUtil;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;

import static org.opennms.netmgt.events.api.EventConstants.PARM_IPINTERFACE_ID;

@EventListener(name = "OpenNMS.DeviceConfig", logPrefix = "poller")
public class DeviceConfigMonitorAdaptor implements ServiceMonitorAdaptor {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigMonitorAdaptor.class);
    private static final String DEVICE_CONFIG_SVC_NAME = "DeviceConfig";
    private static final Period DEFAULT_RETENTION_PERIOD = Period.of(1, 0, 0);

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SessionUtils sessionUtils;

    @Override
    public PollStatus handlePollResult(MonitoredService svc, Map<String, Object> parameters, PollStatus status) {
        final var deviceConfig = status.getDeviceConfig();
        if (deviceConfig == null && !svc.getSvcName().startsWith(DEVICE_CONFIG_SVC_NAME)) {
            return status;
        }
        // Retrieve interface
        final OnmsIpInterface ipInterface = ipInterfaceDao.findByNodeIdAndIpAddress(svc.getNodeId(), svc.getIpAddr());

        var latestConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, svc.getSvcName());

        String encodingAttribute = getObjectAsString(parameters.get("encoding"));
        String configTypeAttribute = getObjectAsString(parameters.get(DeviceConfigConstants.CONFIG_TYPE));
        String encoding = !Strings.isNullOrEmpty(encodingAttribute) ? encodingAttribute : Charset.defaultCharset().name();
        String configType = !Strings.isNullOrEmpty(configTypeAttribute) ? configTypeAttribute : ConfigType.Default;

        if (deviceConfig == null && latestConfig.isEmpty()) {
            // Create empty DeviceConfig for devices that are not scheduled to be backed up yet.
            deviceConfigDao.createEmptyDeviceConfig(ipInterface, svc.getSvcName(), configType);
            return status;
        } else if (deviceConfig == null) {
            // Returning previous state when no retrieval was attempted.
            var deviceStatus = latestConfig.get().getStatus();
            if (deviceStatus.equals(DeviceConfigStatus.FAILED)) {
                var failureReason = latestConfig.get().getFailureReason();
                return PollStatus.down(failureReason);
            } else if (deviceStatus.equals(DeviceConfigStatus.SUCCESS)) {
                return PollStatus.up();
            } else {
                return status;
            }
        }

        if (deviceConfig.getContent() == null) {
            // Config retrieval failed
            deviceConfigDao.updateDeviceConfigFailure(
                    ipInterface,
                    svc.getSvcName(),
                    configType,
                    encoding,
                    status.getReason());
            sendEvent(ipInterface, svc.getSvcName(), EventConstants.DEVICE_CONFIG_RETRIEVAL_FAILED_UEI);
        } else {
            // Config retrieval succeeded
            // De-compress if content is compressed.
            byte[] content = deviceConfig.getContent();
            if (DeviceConfigUtil.isGzipFile(deviceConfig.getFilename())) {
                try {
                    content = DeviceConfigUtil.decompressGzipToBytes(content);
                } catch (IOException e) {
                    LOG.warn("Failed to decompress content from file {}", deviceConfig.getFilename());
                }
            }

            Optional<Long> updatedId = deviceConfigDao.updateDeviceConfigContent(
                ipInterface,
                svc.getSvcName(),
                configType,
                encoding,
                content,
                deviceConfig.getFilename());
            sendEvent(ipInterface, svc.getSvcName(), EventConstants.DEVICE_CONFIG_RETRIEVAL_SUCCEEDED_UEI);

            // if no record previously existed, don't need to call cleanup since
            // we just added the first record
            if (latestConfig.isPresent()) {
                cleanupStaleConfigs(parameters, ipInterface, svc.getSvcName(), updatedId);
            }
        }

        return status;
    }

    @EventHandler(uei = EventConstants.INTERFACE_DELETED_EVENT_UEI)
    public void handleInterfaceDeletedEvent(IEvent event) {
        LOG.debug("Received event: {}", event.getUei());
        Long nodeId = event.getNodeid();
        Integer ipInterfaceId = -1;
        if (nodeId == null) {
            LOG.error(EventConstants.INTERFACE_DELETED_EVENT_UEI + ": Event with no node ID: " + event);
            return;
        }

        InetAddress ipAddress = event.getInterfaceAddress();
        if (ipAddress == null) {
            LOG.error(EventConstants.INTERFACE_DELETED_EVENT_UEI + ": Event with no Interface Address : " + event);
            return;
        }
        IParm iParm = event.getParm(PARM_IPINTERFACE_ID);
        if (iParm.isValid()) {
            String value = iParm.getValue().getContent();
            ipInterfaceId = StringUtils.parseInt(value, ipInterfaceId);
        }
        if(ipInterfaceId < 0) {
            LOG.error(EventConstants.INTERFACE_DELETED_EVENT_UEI + ": Event with no Interface Id : " + event);
            return;
        }
        final  Integer interfaceId = ipInterfaceId;
        sessionUtils.withTransaction(() -> {

            List<DeviceConfig> deviceConfigList = deviceConfigDao.getAllDeviceConfigsWithAnInterfaceId(interfaceId);
            if (!deviceConfigList.isEmpty()) {
                deviceConfigList.forEach(dc -> deviceConfigDao.delete(dc));
                LOG.info("Deleted all device configs on Interface {}", interfaceId);
            }
            return null;
        });
    }

    public void setDeviceConfigDao(DeviceConfigDao deviceConfigDao) {
        this.deviceConfigDao = deviceConfigDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    private String getObjectAsString(Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        return null;
    }

    /**
     * Clean up any "stale" DeviceConfig records from the database. These are DeviceConfig records
     * containing configuration data that are beyond the configured retention date.
     * However, the latest configuration data is always retained.
     * @param excludedId If present, do not delete this record even if it is beyond the retention date,
     *      it represents the most recent "active" configuration.
     */
    private void cleanupStaleConfigs(Map<String, Object> parameters, OnmsIpInterface ipInterface, String serviceName,
                                     Optional<Long> excludedId) {
        final Date currentDate = new Date();
        final String retentionPeriodPattern = getObjectAsString(parameters.get(DeviceConfigConstants.RETENTION_PERIOD));
        final Period retentionPeriod = getRetentionPeriodOrDefault(retentionPeriodPattern);
        final Date retentionDate = Date.from(currentDate.toInstant().atZone(ZoneId.systemDefault()).minus(retentionPeriod).toInstant());

        List<DeviceConfig> staleConfigs = deviceConfigDao.findStaleConfigs(ipInterface, serviceName, retentionDate, excludedId);

        if (!staleConfigs.isEmpty()) {
            LOG.debug("DCB: Found {} stale device config records to delete", staleConfigs.size());
            staleConfigs.stream().map(DeviceConfig::getId).forEach(deviceConfigDao::delete);
        }
    }

    private static Period getRetentionPeriodOrDefault(String pattern) {
        if (!Strings.isNullOrEmpty(pattern)) {
            try {
                return Period.parse(pattern);
            } catch (DateTimeParseException ignored) {
            }
        }

        return DEFAULT_RETENTION_PERIOD;
    }

    private void sendEvent(OnmsIpInterface ipInterface, String serviceName, String uei) {
        EventBuilder bldr = new EventBuilder(uei, "poller");
        bldr.setIpInterface(ipInterface);
        bldr.setService(serviceName);
        eventForwarder.sendNow(bldr.getEvent());
    }
}
