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
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InventoryExporter {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryExporter.class);

    private final NodeDao nodeDao;
    private final IpInterfaceDao ipInterfaceDao;
    private final SnmpInterfaceDao snmpInterfaceDao;
    private final MonitoredServiceDao onmsMonitoredServiceDao;
    private final RuntimeInfo runtimeInfo;
    private final AlarmInventoryGrpcClient client;
    private final Duration snapshotInterval;
    private final ScheduledExecutorService scheduler;

    public InventoryExporter(final NodeDao nodeDao,
                             final IpInterfaceDao ipInterfaceDao,
                             final SnmpInterfaceDao snmpInterfaceDao,
                             final MonitoredServiceDao onmsMonitoredServiceDao,
                             final RuntimeInfo runtimeInfo,
                             final AlarmInventoryGrpcClient client,
                             final Duration snapshotInterval) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.ipInterfaceDao = ipInterfaceDao;
        this.snmpInterfaceDao = snmpInterfaceDao;
        this.onmsMonitoredServiceDao = onmsMonitoredServiceDao;
        this.runtimeInfo = Objects.requireNonNull(runtimeInfo);
        this.client = Objects.requireNonNull(client);
        this.snapshotInterval = Objects.requireNonNull(snapshotInterval);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory("nms-inventory-service-snapshot-sender"));
    }

    public InventoryExporter(final NodeDao nodeDao,
                             final IpInterfaceDao ipInterfaceDao,
                             final SnmpInterfaceDao snmpInterfaceDao,
                             final MonitoredServiceDao onmsMonitoredServiceDao,
                             final RuntimeInfo runtimeInfo,
                             final AlarmInventoryGrpcClient client,
                             final long snapshotInterval) {
        this(nodeDao, ipInterfaceDao, snmpInterfaceDao, onmsMonitoredServiceDao, runtimeInfo, client, Duration.ofSeconds(snapshotInterval));
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

        loadNodeDetails(List.of(node));
        final var inventory = NmsInventoryMapper.INSTANCE.toInventoryUpdatesList(List.of(node),
                    this.runtimeInfo, SystemInfoUtils.getInstanceId(), false);
        this.client.sendNmsInventoryUpdate(inventory);

    }

    public void sendSnapshot() {
            final var nodes = this.nodeDao.findAll().stream().collect(Collectors.toList());
            loadNodeDetails(nodes);
            final var inventory = NmsInventoryMapper.INSTANCE.toInventoryUpdates(nodes, this.runtimeInfo, SystemInfoUtils.getInstanceId(), true);
            this.client.sendNmsInventoryUpdate(inventory);
    }

    private void loadNodeDetails(List<OnmsNode> listNode) {
        if (listNode == null) return;

        listNode.forEach(node -> {
            List<OnmsIpInterface> listIpInterfaces = ipInterfaceDao.findByNodeId(node.getId());
            List<OnmsMonitoredService> listServices = this.onmsMonitoredServiceDao.findByNode(node.getId());
            List<OnmsSnmpInterface> listSnmpInterfaces = snmpInterfaceDao.findByNodeId(node.getId());

            listIpInterfaces.forEach(ipInterface ->
                    ipInterface.setMonitoredServices(
                            listServices.stream()
                                    .filter(service -> service.getIpInterfaceId().equals(ipInterface.getId()))
                                    .collect(Collectors.toSet())));
            node.setSnmpInterfaces(listSnmpInterfaces.stream().collect(Collectors.toSet()));
            node.setIpInterfaces(listIpInterfaces.stream().collect(Collectors.toSet()));
        });
    }
}
