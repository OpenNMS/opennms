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
package org.opennms.features.deviceconfig.sink.consumer;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.service.DeviceConfigUtil;
import org.opennms.features.deviceconfig.sink.module.DeviceConfigSinkDTO;
import org.opennms.features.deviceconfig.sink.module.DeviceConfigSinkModule;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigConsumer implements MessageConsumer<DeviceConfigSinkDTO, DeviceConfigSinkDTO>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigConsumer.class);

    private static final String DEVICE_CONFIG_PREFIX = "DeviceConfig";

    private final MessageConsumerManager consumerManager;
    private final DeviceConfigSinkModule module;
    private final IpInterfaceDao ipInterfaceDao;
    private final DeviceConfigDao deviceConfigDao;

    public DeviceConfigConsumer(
            MessageConsumerManager consumerManager,
            DeviceConfigSinkModule module,
            IpInterfaceDao ipInterfaceDao,
            DeviceConfigDao deviceConfigDao
    ) throws Exception {
        this.consumerManager = consumerManager;
        this.module = module;
        this.ipInterfaceDao = ipInterfaceDao;
        this.deviceConfigDao = deviceConfigDao;
        this.consumerManager.registerConsumer(this);
    }

    @Override
    public SinkModule<DeviceConfigSinkDTO, DeviceConfigSinkDTO> getModule() {
        return module;
    }

    @Override
    public void handleMessage(DeviceConfigSinkDTO message) {
        try {
            var address = InetAddress.getByAddress(message.address);
            LOG.debug("handle message - location: " + message.location + "; address: " + address.getHostAddress() + "; fileName: " + message.fileName);
            OnmsIpInterface ipInterface = findMatchingInterface(address.getHostAddress(), message.location);
            if (ipInterface != null) {
                byte[] content = message.config;
                if (DeviceConfigUtil.isGzipFile(message.fileName)) {
                    try {
                        content = DeviceConfigUtil.decompressGzipToBytes(content);
                    } catch (IOException e) {
                        LOG.warn("Failed to decompress content from file {}", message.fileName);
                    }
                }
                deviceConfigDao.updateDeviceConfigContent(
                        ipInterface,
                        null,
                        // use config type sink for configs that are pushed from Device.
                        ConfigType.Sink,
                        null,
                        content,
                        message.fileName
                );
            } else {
                LOG.warn("can not persist device config; did not find interface - location: "+ message.location + "; " + address.getHostAddress());
            }
        } catch (Exception e) {
            LOG.error("could not handle device config backup message", e);
        }
    }

    private OnmsIpInterface findMatchingInterface(final String ipAddress, final String location) {
        var ipInterfaces = this.ipInterfaceDao.findByIpAddressAndLocation(ipAddress, location);
        OnmsIpInterface iface = ipInterfaces.size() > 0 ? ipInterfaces.get(0) : null;
        if (ipInterfaces.size() > 1) {
            var optionalInterface = ipInterfaces
                    .stream().filter(ipInterface ->
                            ipInterface.getMonitoredServices().stream().anyMatch(monitoredService ->
                                    monitoredService.getServiceName().startsWith(DEVICE_CONFIG_PREFIX))).findFirst();
            iface = optionalInterface.orElseGet(() -> ipInterfaces.stream().findFirst().orElse(null));
        }
        return iface;
    }

    @Override
    public void close() throws Exception {
        consumerManager.unregisterConsumer(this);
    }
}
