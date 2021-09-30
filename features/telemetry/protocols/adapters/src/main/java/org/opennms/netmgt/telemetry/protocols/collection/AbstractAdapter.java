/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
