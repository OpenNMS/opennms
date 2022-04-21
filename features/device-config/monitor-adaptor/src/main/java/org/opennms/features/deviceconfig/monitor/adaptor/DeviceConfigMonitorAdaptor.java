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
import java.util.Map;

import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.service.DeviceConfigUtil;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.DeviceConfig;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;


public class DeviceConfigMonitorAdaptor implements ServiceMonitorAdaptor {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigMonitorAdaptor.class);
    private static final String DEVICE_CONFIG_SVC_NAME = "DeviceConfig";

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private EventForwarder eventForwarder;

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
        String configTypeAttribute = getObjectAsString(parameters.get("config-type"));
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
            deviceConfigDao.updateDeviceConfigContent(
                    ipInterface,
                    svc.getSvcName(),
                    configType,
                    encoding,
                    content,
                    deviceConfig.getFilename());
            sendEvent(ipInterface, svc.getSvcName(), EventConstants.DEVICE_CONFIG_RETRIEVAL_SUCCEEDED_UEI);
        }

        return status;
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

    private void sendEvent(OnmsIpInterface ipInterface, String serviceName, String uei) {
        EventBuilder bldr = new EventBuilder(uei, "poller");
        bldr.setIpInterface(ipInterface);
        bldr.setService(serviceName);
        eventForwarder.sendNow(bldr.getEvent());
    }

}
