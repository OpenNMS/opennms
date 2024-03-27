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
import org.opennms.features.usageanalytics.api.UsageAnalyticDao;
import org.opennms.features.usageanalytics.api.UsageAnalyticMetricName;
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
import static org.opennms.netmgt.events.api.EventConstants.PARM_LOSTSERVICE_REASON;

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
    private UsageAnalyticDao usageAnalyticDao;

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

        // why did backup happen?
        boolean triggered = Boolean.parseBoolean(getKeyedString(parameters, DeviceConfigConstants.TRIGGERED_POLL, "false"));
        String controlProtocol = triggered ? DeviceConfigConstants.REST : DeviceConfigConstants.CRON;
        String dataProtocol = getKeyedString(parameters, DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_DATA_PROTOCOL, "TFTP");
        long timestamp = Long.parseLong(getKeyedString(parameters, DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_START_TIME, "0"));


        var latestConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, svc.getSvcName());

        String encoding = getKeyedString(parameters, "encoding", Charset.defaultCharset().name());
        String configType = getKeyedString(parameters, DeviceConfigConstants.CONFIG_TYPE, ConfigType.Default);

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

        // send 'started' event with adjusted time stamp
        sendEvent(ipInterface, svc.getSvcName(), EventConstants.DEVICE_CONFIG_BACKUP_STARTED_UEI, svc.getNodeId(), timestamp, Map.of(
                DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_CONTROL_PROTOCOL, controlProtocol,
                DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_DATA_PROTOCOL, dataProtocol
        ));


        if (deviceConfig.getContent() == null) {
            // Config retrieval failed
            deviceConfigDao.updateDeviceConfigFailure(
                    ipInterface,
                    svc.getSvcName(),
                    configType,
                    encoding,
                    status.getReason());
            sendEvent(ipInterface, svc.getSvcName(), EventConstants.DEVICE_CONFIG_BACKUP_FAILED_UEI, svc.getNodeId(), Map.of(
                    PARM_LOSTSERVICE_REASON, status.getReason()
            ));
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
            sendEvent(ipInterface, svc.getSvcName(), EventConstants.DEVICE_CONFIG_BACKUP_SUCCEEDED_UEI, svc.getNodeId(), Map.of());

            // if no record previously existed, don't need to call cleanup since
            // we just added the first record
            if (latestConfig.isPresent()) {
                cleanupStaleConfigs(parameters, ipInterface, svc.getSvcName(), updatedId);
            }
        }
        // UsageAnalytics
        if (status.isUp()) {
            usageAnalyticDao.incrementCounterByMetricName(UsageAnalyticMetricName.DCB_SUCCEED.toString());
        } else {
            usageAnalyticDao.incrementCounterByMetricName(UsageAnalyticMetricName.DCB_FAILED.toString());
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
        final String retentionPeriodPattern = getKeyedString(parameters, DeviceConfigConstants.RETENTION_PERIOD, null);
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

    public void setDeviceConfigDao(DeviceConfigDao deviceConfigDao) {
        this.deviceConfigDao = deviceConfigDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    private void sendEvent(OnmsIpInterface ipInterface, String serviceName, String uei, int nodeId, Map<String, String> params) {
        this.sendEvent(ipInterface, serviceName, uei, nodeId, 0L, params);
    }

    private void sendEvent(OnmsIpInterface ipInterface, String serviceName, String uei, int nodeId, long timestamp, Map<String, String> params) {
        EventBuilder bldr = new EventBuilder(uei, "poller");
        bldr.setIpInterface(ipInterface);
        bldr.setService(serviceName);
        bldr.setNodeid(nodeId);
        if (timestamp > 0) {
            bldr.setTime(new Date(timestamp));
        }
        params.forEach(bldr::addParam);
        eventForwarder.sendNowSync(bldr.getEvent());
    }

    private static String getKeyedString(final Map<String, Object> parameterMap, final String key, final String defaultValue) {
        String ret = defaultValue;
        if (key != null) {
            Object value = parameterMap.get(key);
            if (value != null) {
                ret = value instanceof String ? (String)value : value.toString();
            }
        }
        return ret;
    }
}
