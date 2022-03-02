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

package org.opennms.features.deviceconfig.sink.dispatcher;

import java.net.InetAddress;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.distributed.core.api.Identity;
import org.opennms.features.deviceconfig.sink.module.DeviceConfigSinkDTO;
import org.opennms.features.deviceconfig.sink.module.DeviceConfigSinkModule;
import org.opennms.features.deviceconfig.tftp.TftpFileReceiver;
import org.opennms.features.deviceconfig.tftp.TftpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigDispatcher implements TftpFileReceiver, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigDispatcher.class);

    private final Identity identity;
    private final TftpServer tftpServer;
    private final DeviceConfigSinkModule sinkModule;
    private final MessageDispatcherFactory messageDispatcherFactory;
    private final AsyncDispatcher<DeviceConfigSinkDTO> asyncDispatcher;

    public DeviceConfigDispatcher(
            Identity identity,
            TftpServer tftpServer,
            DeviceConfigSinkModule sinkModule,
            MessageDispatcherFactory messageDispatcherFactory
    ) throws Exception {
        this.identity = identity;
        this.tftpServer = tftpServer;
        this.sinkModule = sinkModule;
        this.messageDispatcherFactory = messageDispatcherFactory;
        asyncDispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
        tftpServer.register(this);
    }

    @Override
    public void onFileReceived(InetAddress address, String fileName, byte[] content) {
        LOG.debug("received - address: " + address.getHostAddress() + "; fileName: " + fileName + "; contentLength: " + content.length);
        // No need to handle files that are retrieved by monitor.
        if (fileName.contains("monitor")) {
            return;
        }
        var dto = new DeviceConfigSinkDTO(identity.getLocation(), address.getAddress(), fileName, content);
        asyncDispatcher.send(dto).whenComplete((status, throwable) -> {
            if (status != null) {
                LOG.debug("sent - address: " + address.getHostAddress() + "; fileName: " + fileName + "; dispatchStatus: " + status.name());
            }
        });
    }

    public void close() throws Exception {
        asyncDispatcher.close();
        tftpServer.unregister(this);
    }
}
