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

package org.opennms.features.grpc.exporter.inventory;

import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.grpc.exporter.NamedThreadFactory;
import org.opennms.features.grpc.exporter.mapper.NmsInventoryMapper;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InventoryService {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryService.class);

    private final NodeDao nodeDao;
    private final RuntimeInfo runtimeInfo;
    private final NmsInventoryGrpcClient client;
    private final Duration snapshotInterval;
    private final ScheduledExecutorService scheduler;
    private final SessionUtils sessionUtils;

    public InventoryService(final NodeDao nodeDao,
                            final RuntimeInfo runtimeInfo,
                            final NmsInventoryGrpcClient client,
                            final SessionUtils sessionUtils,
                            final long snapshotInterval) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.runtimeInfo = Objects.requireNonNull(runtimeInfo);
        this.client = Objects.requireNonNull(client);
        this.snapshotInterval = Duration.ofSeconds(snapshotInterval);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory("nms-inventory-service-snapshot-sender"));
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

    public void sendAddNmsInventory(OnmsNode node) {
        if (!client.isEnabled()) {
            LOG.info("NMS Inventory service disabled, not sending inventory updates");
            return;
        }
        sessionUtils.withReadOnlyTransaction(() -> {
            final var inventory = NmsInventoryMapper.INSTANCE.toInventoryUpdatesList(List.of(node),
                    this.runtimeInfo, SystemInfoUtils.getInstanceId(), false);
            this.client.sendNmsInventoryUpdate(inventory);
        });

    }

    public void sendSnapshot() {
        if (!client.isEnabled()) {
            LOG.info("NMS Inventory service disabled, not sending inventory snapshot");
            return;
        }

        sessionUtils.withReadOnlyTransaction(() -> {
            final var nodes = this.nodeDao.findAll();
            final var inventory = NmsInventoryMapper.INSTANCE.toInventoryUpdates(nodes, this.runtimeInfo, SystemInfoUtils.getInstanceId(), true);
            this.client.sendNmsInventoryUpdate(inventory);
        });

    }


}
