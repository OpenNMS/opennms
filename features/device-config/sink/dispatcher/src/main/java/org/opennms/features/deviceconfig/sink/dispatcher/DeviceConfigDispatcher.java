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
