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

import java.nio.charset.Charset;
import java.util.Map;

import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

import static org.opennms.core.utils.InetAddressUtils.addr;

public class DeviceConfigMonitorAdaptor implements ServiceMonitorAdaptor {

    private static final String DEVICE_CONFIG_MONITOR_PREFIX = "DeviceConfig";

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private EventForwarder eventForwarder;

    @Override
    public PollStatus handlePollResult(MonitoredService svc, Map<String, Object> parameters, PollStatus status) {

        if (!svc.getSvcName().startsWith(DEVICE_CONFIG_MONITOR_PREFIX)) {
            return status;
        }
        // Retrieve interface
        final OnmsIpInterface ipInterface = ipInterfaceDao.findByNodeIdAndIpAddress(svc.getNodeId(), svc.getIpAddr());
        String encodingAttribute = getObjectAsString(parameters.get("encoding"));
        String configTypeAttribute = getObjectAsString(parameters.get("config-type"));
        String encoding = !Strings.isNullOrEmpty(encodingAttribute) ? encodingAttribute : Charset.defaultCharset().name();
        String configType = !Strings.isNullOrEmpty(configTypeAttribute) ? configTypeAttribute : ConfigType.Default;
        var deviceConfig = status.getDeviceConfig();

        // unknown means that retrieval was skipped, so nothing to persist
        if (!status.isUnknown()) {
            if (deviceConfig == null) {
                // Config retrieval failed
                deviceConfigDao.updateDeviceConfigFailure(
                        ipInterface,
                        configType,
                        encoding,
                        status.getReason()
                );
                sendConfigRetrievalFailedEvent(ipInterface, svc.getSvcName());
            } else {
                // Config retrieval succeeded
                deviceConfigDao.updateDeviceConfigContent(
                        ipInterface,
                        configType,
                        encoding,
                        deviceConfig.content,
                        deviceConfig.filename
                );
                sendConfigRetrievalSucceededEvent(ipInterface, svc.getSvcName());
            }
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

    private void sendConfigRetrievalFailedEvent(OnmsIpInterface ipInterface, String serviceName) {
        EventBuilder bldr = new EventBuilder(EventConstants.DEVICE_CONFIG_RETRIEVAL_FAILED_UEI, "poller");
        bldr.setIpInterface(ipInterface);
        bldr.setService(serviceName);
        eventForwarder.sendNow(bldr.getEvent());
    }

    private void sendConfigRetrievalSucceededEvent(OnmsIpInterface ipInterface, String serviceName) {
        EventBuilder bldr = new EventBuilder(EventConstants.DEVICE_CONFIG_RETRIEVAL_SUCCEEDED_UEI, "poller");
        bldr.setIpInterface(ipInterface);
        bldr.setService(serviceName);
        eventForwarder.sendNow(bldr.getEvent());
    }
}
