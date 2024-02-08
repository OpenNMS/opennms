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
package org.opennms.netmgt.telemetry.protocols.openconfig.connector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.api.OpenConfigClientFactory;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.SocketUtils;

public class OpenConfigConnector implements Connector {
    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigConnector.class);

    private final OpenConfigClientFactory clientFactory;
    private final AsyncDispatcher<TelemetryMessage> dispatcher;
    private OpenConfigClient openConfigClient;

    public OpenConfigConnector(AsyncDispatcher<TelemetryMessage> dispatcher, OpenConfigClientFactory clientFactory) {
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.clientFactory = Objects.requireNonNull(clientFactory);
    }

    @Override
    public void stream(int nodeId, InetAddress ipAddress, List<Map<String, String>> paramList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting new OpenConfig client for: {}", InetAddressUtils.str(ipAddress));
        }
        openConfigClient = clientFactory.create(ipAddress, paramList);
        openConfigClient.subscribe(new OpenConfigClient.Handler() {
            @Override
            public void accept(InetAddress host, Integer port, byte[] data) {
                InetSocketAddress remoteAddress = SocketUtils.socketAddress(host.getHostAddress(), port);
                final TelemetryMessage msg = new TelemetryMessage(remoteAddress, ByteBuffer.wrap(data));
                dispatcher.send(msg);
            }

            @Override
            public void onError(String error) {
                LOG.warn("Error while streaming openconfig data, {}", error);
            }
        });

    }

    @Override
    public void close() {
        openConfigClient.shutdown();
    }
}
