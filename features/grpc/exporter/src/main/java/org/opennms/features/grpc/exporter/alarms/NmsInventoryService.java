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

package org.opennms.features.grpc.exporter.alarms;

import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.grpc.exporter.NamedThreadFactory;
import org.opennms.features.grpc.exporter.mapper.NmsInventoryMapper;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.model.IpInterface;
import org.opennms.integration.api.v1.model.MetaData;
import org.opennms.integration.api.v1.model.MonitoredService;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.SnmpInterface;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NmsInventoryService {
    private static final Logger LOG = LoggerFactory.getLogger(NmsInventoryService.class);

    private final NodeDao nodeDao;

    private final RuntimeInfo runtimeInfo;

    private final AlarmInventoryGrpcClient client;

    private final Duration snapshotInterval;

    private final ScheduledExecutorService scheduler;

    public NmsInventoryService(final NodeDao nodeDao,
                               final RuntimeInfo runtimeInfo,
                               final AlarmInventoryGrpcClient client,
                               final Duration snapshotInterval) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.runtimeInfo = Objects.requireNonNull(runtimeInfo);
        this.client = Objects.requireNonNull(client);
        this.snapshotInterval = Objects.requireNonNull(snapshotInterval);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory("nms-inventory-service-snapshot-sender"));
    }

    public NmsInventoryService(final NodeDao nodeDao,
                               final RuntimeInfo runtimeInfo,
                               final AlarmInventoryGrpcClient client,
                               final long snapshotInterval) {
        this(nodeDao, runtimeInfo, client, Duration.ofSeconds(snapshotInterval));
    }

    public void start() {
        // Start timer to send snapshots
        this.scheduler.scheduleAtFixedRate(this::sendSnapshot,
                this.snapshotInterval.getSeconds(),
                this.snapshotInterval.getSeconds(),
                TimeUnit.SECONDS);
        // Set this callback to send snapshot for initial server connect and reconnects
        this.client.setInventoryCallback(this::sendSnapshot);

    }

    public void stop() {
        this.scheduler.shutdown();
    }

    public void sendAddNmsInventory(Node node) {
        final var inventory = NmsInventoryMapper.INSTANCE.toInventoryUpdates(List.of(node), this.runtimeInfo, SystemInfoUtils.getInstanceId(), true);
        this.client.sendNmsInventoryUpdate(inventory);
    }

    public void sendSnapshot() {
        final var nodes = this.nodeDao.getNodes().stream().collect(Collectors.toList());
        final var inventory = NmsInventoryMapper.INSTANCE.toInventoryUpdates(nodes, this.runtimeInfo, SystemInfoUtils.getInstanceId(), true);
        this.client.sendNmsInventoryUpdate(inventory);
    }
}
