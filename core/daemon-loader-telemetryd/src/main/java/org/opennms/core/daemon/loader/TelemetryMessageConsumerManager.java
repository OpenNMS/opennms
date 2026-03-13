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
package org.opennms.core.daemon.loader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Multi-module MessageConsumerManager for Telemetryd.
 *
 * <p>Unlike the single-module LocalMessageConsumerManager (used by Trapd/Syslogd),
 * this manager spawns a separate KafkaSinkBridge per registered SinkModule.
 * Each bridge consumes from its own Kafka topic (e.g., OpenNMS.Sink.Telemetry-Netflow-5).</p>
 *
 * <p>When {@code Telemetryd.start()} runs, it creates {@code TelemetrySinkModule} instances
 * per queue and registers consumers with this manager. The {@code startConsumingForModule()}
 * callback fires once per registered module, spawning a {@code KafkaSinkBridge} thread
 * for each.</p>
 */
public class TelemetryMessageConsumerManager extends AbstractMessageConsumerManager
        implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryMessageConsumerManager.class);

    private final Map<String, KafkaSinkBridge> bridges = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    protected void startConsumingForModule(SinkModule<?, Message> module) {
        String moduleId = module.getId();
        LOG.info("Telemetry sink consumer registered for module: {}", moduleId);

        if (bridges.containsKey(moduleId)) {
            LOG.warn("Bridge already exists for module: {}", moduleId);
            return;
        }

        KafkaSinkBridge bridge = new KafkaSinkBridge(this);
        bridge.setModule(module);
        bridges.put(moduleId, bridge);

        try {
            bridge.afterPropertiesSet();
            LOG.info("KafkaSinkBridge started for telemetry module: {}", moduleId);
        } catch (Exception e) {
            LOG.error("Failed to start KafkaSinkBridge for module {}: {}", moduleId, e.getMessage(), e);
            bridges.remove(moduleId);
        }
    }

    @Override
    protected void stopConsumingForModule(SinkModule<?, Message> module) {
        String moduleId = module.getId();
        KafkaSinkBridge bridge = bridges.remove(moduleId);
        if (bridge != null) {
            bridge.destroy();
            LOG.info("KafkaSinkBridge stopped for telemetry module: {}", moduleId);
        }
    }

    @Override
    public void destroy() {
        for (Map.Entry<String, KafkaSinkBridge> entry : bridges.entrySet()) {
            entry.getValue().destroy();
            LOG.info("KafkaSinkBridge destroyed for module: {}", entry.getKey());
        }
        bridges.clear();
    }
}
