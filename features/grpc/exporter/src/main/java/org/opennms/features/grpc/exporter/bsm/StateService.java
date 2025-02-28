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

package org.opennms.features.grpc.exporter.bsm;

import org.opennms.features.grpc.exporter.common.MonitoredServiceWithMetadata;
import org.opennms.features.grpc.exporter.mapper.MonitoredServiceMapper;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StateService {
    private static final Logger LOG = LoggerFactory.getLogger(StateService.class);
    private final NodeDao nodeDao;
    private final RuntimeInfo runtimeInfo;
    private final BsmGrpcClient client;

    public StateService(final NodeDao nodeDao,
                        final RuntimeInfo runtimeInfo,
                        final BsmGrpcClient client) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.runtimeInfo = Objects.requireNonNull(runtimeInfo);
        this.client = Objects.requireNonNull(client);
    }

    public void sendState(final List<MonitoredServiceWithMetadata> services) {
        final var updates = MonitoredServiceMapper.INSTANCE.toStateUpdates(services, this.runtimeInfo);
        this.client.sendMonitoredServicesStatusUpdate(updates);
    }

    public void sendAllState() {
        final var services = this.nodeDao.getNodes().stream()
                .flatMap(node -> node.getIpInterfaces().stream()
                        .flatMap(iface -> iface.getMonitoredServices().stream()
                                .map(service -> new MonitoredServiceWithMetadata(node, iface, service))))
                .collect(Collectors.toList());

        this.sendState(services);
    }
}
