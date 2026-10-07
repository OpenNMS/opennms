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
package org.opennms.features.openconfig.telemetry;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.api.OpenConfigClientFactory;

public class OpenConfigClientFactoryImpl implements OpenConfigClientFactory {
    // Shared by every client. Tasks only build channels and schedule timeouts and retries;
    // connections themselves proceed on the gRPC event loops.
    private final ScheduledExecutorService executor =
            OpenConfigClientImpl.newExecutor(Math.max(2, Runtime.getRuntime().availableProcessors()));
    private final Set<OpenConfigClientImpl> clients = ConcurrentHashMap.newKeySet();
    private boolean closed;

    @Override
    public synchronized OpenConfigClient create(InetAddress ipAddress, List<Map<String, String>> paramList) {
        if (closed) {
            throw new IllegalStateException("OpenConfig client factory is closed");
        }
        OpenConfigClientImpl client = new OpenConfigClientImpl(ipAddress, paramList, executor, clients::remove);
        clients.add(client);
        return client;
    }

    int activeClients() {
        return clients.size();
    }

    public void shutdown() {
        List<OpenConfigClientImpl> activeClients;
        synchronized (this) {
            closed = true;
            activeClients = new ArrayList<>(clients);
        }
        activeClients.forEach(OpenConfigClientImpl::shutdown);
        executor.shutdownNow();
    }
}
