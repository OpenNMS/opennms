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
package org.opennms.netmgt.telemetry.protocols.collection;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.Objects;

import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public abstract class AbstractAdapter implements Adapter {
    protected final Logger LOG = LoggerFactory.getLogger(AbstractAdapter.class);

    /**
     * Time taken to handle a log
     */
    protected final Timer logParsingTimer;

    /**
     * Number of message per log
     */
    protected final Histogram packetsPerLogHistogram;

    private final Meter recordsConsumed;

    /**
     * A single instance of an adapter will only be responsible for this one config
     */
    protected final AdapterDefinition adapterConfig;

    public AbstractAdapter(final AdapterDefinition adapterConfig,
                           final MetricRegistry metricRegistry) {
        this.adapterConfig = Objects.requireNonNull(adapterConfig);

        Objects.requireNonNull(metricRegistry);

        this.logParsingTimer = metricRegistry.timer(name("adapters", adapterConfig.getFullName(), "logParsing"));
        this.packetsPerLogHistogram = metricRegistry.histogram(name("adapters", adapterConfig.getFullName(), "packetsPerLog"));
        this.recordsConsumed = metricRegistry.meter(name("adapters", adapterConfig.getFullName(), "recordsConsumed"));
    }

    public abstract void handleMessage(TelemetryMessageLogEntry message, TelemetryMessageLog messageLog);

    @Override
    public void handleMessageLog(final TelemetryMessageLog messageLog) {
        try (final Timer.Context ctx = logParsingTimer.time()) {
            for (final TelemetryMessageLogEntry message : messageLog.getMessageList()) {
                this.handleMessage(message, messageLog);
            }
            packetsPerLogHistogram.update(messageLog.getMessageList().size());
            recordsConsumed.mark(messageLog.getMessageList().size());
        }
    }

    @Override
    public void destroy() {
    }
}
