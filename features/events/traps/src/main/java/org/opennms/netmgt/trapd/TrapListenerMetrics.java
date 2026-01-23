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
package org.opennms.netmgt.trapd;

import java.util.function.Supplier;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;

/**
 * Listener-level trap metrics using Dropwizard MetricRegistry.
 * Used on Minion to expose only listener-relevant metrics via JMX.
 *
 * This class exposes metrics for:
 * - Raw traps received at the UDP listener (before any processing)
 * - Traps with errors during dispatch
 * - Queue size metrics (current, max, batch size)
 */
public class TrapListenerMetrics {
    private static final String JMX_DOMAIN = "org.opennms.netmgt.trapd";

    private final MetricRegistry metrics;
    private JmxReporter jmxReporter;

    // Counters (listener level)
    private final Counter rawTrapsReceived;
    private final Counter trapsDispatched;
    private final Counter trapsErrored;

    // Gauges
    private Supplier<Integer> queueSizeSupplier = () -> 0;
    private int maxQueueSize = 0;
    private int batchSize = 0;

    public TrapListenerMetrics(MetricRegistry metrics) {
        this.metrics = metrics;

        // Register listener-level counters only
        this.rawTrapsReceived = metrics.counter(name("rawTrapsReceived"));
        this.trapsDispatched = metrics.counter(name("trapsDispatched"));
        this.trapsErrored = metrics.counter(name("trapsErrored"));

        // Register queue gauges
        metrics.register(name("currentQueueSize"), (Gauge<Integer>) () -> queueSizeSupplier.get());
        metrics.register(name("maxQueueSize"), (Gauge<Integer>) () -> maxQueueSize);
        metrics.register(name("batchSize"), (Gauge<Integer>) () -> batchSize);
    }

    private String name(String metric) {
        return metric;
    }

    public void startJmxReporter() {
        jmxReporter = JmxReporter.forRegistry(metrics)
            .inDomain(JMX_DOMAIN)
            .build();
        jmxReporter.start();
    }

    public void stopJmxReporter() {
        if (jmxReporter != null) {
            jmxReporter.close();
            jmxReporter = null;
        }
    }

    // Counter methods
    public void incRawTrapsReceivedCount() {
        rawTrapsReceived.inc();
    }

    public void incTrapsDispatched() {
        trapsDispatched.inc();
    }

    public void incErrorCount() {
        trapsErrored.inc();
    }

    // Gauge setters
    public void setQueueSizeSupplier(Supplier<Integer> supplier) {
        this.queueSizeSupplier = supplier != null ? supplier : () -> 0;
    }

    public void setMaxQueueSize(int size) {
        this.maxQueueSize = size;
    }

    public void setBatchSize(int size) {
        this.batchSize = size;
    }
}
