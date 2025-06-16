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
package org.opennms.core.ipc.sink.common;

import java.util.Collection;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * The state and metrics pertaining to a particular dispatches.
 *
 * @author jwhite
 */
public class DispatcherState<W, S extends Message, T extends Message> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherState.class);

    private final SinkModule<S, T> module;

    private final W metadata;

    private final MetricRegistry metrics;

    private final Timer dispatchTimer;

    public DispatcherState(AbstractMessageDispatcherFactory<W> dispatcherFactory, SinkModule<S, T> module) {
        this.module = module;
        metadata = dispatcherFactory.getModuleMetadata(module);
        metrics = dispatcherFactory.getMetrics();

        String metricName = MetricRegistry.name(module.getId(), "dispatch");

        Collection<Timer> existingTimers = metrics.getTimers(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return metricName.equals(name);
            }
        }).values();

        switch(existingTimers.size()) {
        case 0:
            dispatchTimer = metrics.timer(metricName);
            break;
        case 1:
            dispatchTimer = existingTimers.iterator().next();
            break;
        default:
            LOG.warn("Multiple timers registered with name {} somehow", metricName);
            dispatchTimer = existingTimers.iterator().next();
        }
    }

    public SinkModule<S, T> getModule() {
        return module;
    }

    public W getMetaData() {
        return metadata;
    }

    protected MetricRegistry getMetrics() {
        return metrics;
    }

    public Timer getDispatchTimer() {
        return dispatchTimer;
    }

    @Override
    public void close() throws Exception {
        final String prefix = MetricRegistry.name(module.getId());
        metrics.removeMatching(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.startsWith(prefix);
            }
        });
    }
}
