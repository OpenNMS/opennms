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
package org.opennms.netmgt.telemetry.distributed.common;

import java.util.Optional;

import org.opennms.netmgt.telemetry.config.api.QueueDefinition;

public class MapBasedQueueDef implements QueueDefinition {
    private final String name;
    private final Optional<Integer> threads;
    private final Optional<Integer> queueSize;
    private final Optional<Integer> batchSize;
    private final Optional<Integer> batchInterval;
    private final Optional<Boolean> useRoutingKey;

    public MapBasedQueueDef(final PropertyTree definition) {
        this.name = definition.getRequiredString("name");
        this.threads = definition.getOptionalInteger("queue", "threads");
        this.queueSize = definition.getOptionalInteger("queue", "size");
        this.batchSize = definition.getOptionalInteger("batch", "size");
        this.batchInterval = definition.getOptionalInteger("batch", "interval");
        this.useRoutingKey = definition.getOptionalBoolean("queue", "use-routing-key");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Integer> getNumThreads() {
        return threads;
    }

    @Override
    public Optional<Integer> getBatchSize() {
        return batchSize;
    }

    @Override
    public Optional<Integer> getBatchIntervalMs() {
        return batchInterval;
    }

    @Override
    public Optional<Integer> getQueueSize() {
        return queueSize;
    }

    @Override
    public Optional<Boolean> getUseRoutingKey() {
        return useRoutingKey;
    }
}
