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

package org.opennms.features.deviceconfig.sink.consumer;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.sink.module.DeviceConfigDTO;
import org.opennms.features.deviceconfig.sink.module.DeviceConfigSinkModule;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigConsumer implements MessageConsumer<DeviceConfigDTO, DeviceConfigDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigConsumer.class);

    private final DeviceConfigSinkModule module;
    private final IpInterfaceDao ipInterfaceDao;
    private final DeviceConfigDao deviceConfigDao;

    public DeviceConfigConsumer(DeviceConfigSinkModule module, IpInterfaceDao ipInterfaceDao, DeviceConfigDao deviceConfigDao) {
        this.module = module;
        this.ipInterfaceDao = ipInterfaceDao;
        this.deviceConfigDao = deviceConfigDao;
    }

    @Override
    public SinkModule<DeviceConfigDTO, DeviceConfigDTO> getModule() {
        return module;
    }

    @Override
    public void handleMessage(DeviceConfigDTO message) {
        try {
            var address = InetAddress.getByAddress(message.address);
            LOG.debug("handle message - location: " + message.location + "; " + address.getHostAddress() + "; fileName: " + message.fileName);
            OnmsIpInterface itf = ipInterfaceDao.findByIpAddressAndLocation(address.getHostAddress(), message.location);
            if (itf != null) {
                // TODO consider config type
                var latest = deviceConfigDao.getLatestConfigForInterface(itf);
                if (latest.isPresent()) {
                    var dc = latest.get();
                    dc.setConfig(message.config);
                    // TODO set updated time & reset failure
                    deviceConfigDao.update(dc);
                } else {
                    // TODO set config type, updated time
                    var dc = new DeviceConfig();
                    dc.setIpInterface(itf);
                    dc.setConfig(message.config);
                    dc.setCreatedTime(new Date());
                    deviceConfigDao.save(dc);
                }
            } else {
                LOG.warn("can not persist device config; did not find interface - location: "+ message.location + "; " + address.getHostAddress());
            }
        } catch (Exception e) {
            LOG.error("could not handle device config backup message", e);
        }
    }
}
