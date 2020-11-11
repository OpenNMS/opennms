/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.flows;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.core.rpc.utils.mate.ContextKey;
import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.DetailedFlowException;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.UnrecoverableFlowException;
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
import com.google.common.base.Strings;

public abstract class AbstractFlowAdapter<P> implements Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFlowAdapter.class);

    private final FlowRepository flowRepository;

    private final Converter<P> converter;

    private String metaDataNodeLookup;
    private ContextKey contextKey;

    /**
     * Time taken to parse a log
     */
    private final Timer logParsingTimer;

    /**
     * Number of packets per log
     */
    private final Histogram packetsPerLogHistogram;

    private final Meter entriesReceived;

    private final Meter entriesParsed;

    private final Meter entriesConverted;

    public AbstractFlowAdapter(final AdapterDefinition adapterConfig,
                               final MetricRegistry metricRegistry,
                               final FlowRepository flowRepository,
                               final Converter<P> converter) {
        Objects.requireNonNull(adapterConfig);
        Objects.requireNonNull(metricRegistry);

        this.flowRepository = Objects.requireNonNull(flowRepository);
        this.converter = Objects.requireNonNull(converter);

        this.logParsingTimer = metricRegistry.timer(name("adapters", adapterConfig.getName(), "logParsing"));
        this.packetsPerLogHistogram = metricRegistry.histogram(name("adapters", adapterConfig.getName(), "packetsPerLog"));
        this.entriesReceived = metricRegistry.meter(name("adapters", adapterConfig.getName(), "entriesReceived"));
        this.entriesParsed = metricRegistry.meter(name("adapters", adapterConfig.getName(), "entriesParsed"));
        this.entriesConverted = metricRegistry.meter(name("adapters", adapterConfig.getName(), "entriesConverted"));
    }

    @Override
    public void handleMessageLog(TelemetryMessageLog messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessageList().size());

        int flowPackets = 0;

        final List<Flow> flows = new LinkedList<>();
        try (Timer.Context ctx = logParsingTimer.time()) {
            for (TelemetryMessageLogEntry eachMessage : messageLog.getMessageList()) {
                this.entriesReceived.mark();

                LOG.trace("Parsing packet: {}", eachMessage);
                final P flowPacket = parse(eachMessage);
                if (flowPacket != null) {
                    this.entriesParsed.mark();

                    flowPackets += 1;

                    final List<Flow> converted = converter.convert(flowPacket);
                    flows.addAll(converted);

                    this.entriesConverted.mark(converted.size());
                }
            }
            packetsPerLogHistogram.update(flowPackets);
        }

        try {
            LOG.debug("Persisting {} packets, {} flows.", flowPackets, flows.size());
            final FlowSource source = new FlowSource(messageLog.getLocation(),
                    messageLog.getSourceAddress(),
                    contextKey);
            flowRepository.persist(flows, source);
        } catch (DetailedFlowException ex) {
            LOG.error("Error while persisting flows: {}", ex.getMessage(), ex);
            for (final String logMessage: ex.getDetailedLogMessages()) {
                LOG.error(logMessage);
            }
        } catch(UnrecoverableFlowException ex) {
            LOG.error("Error while persisting flows. Cannot recover: {}. {} messages are lost.", ex.getMessage(), messageLog.getMessageList().size(), ex);
            return;
        } catch (FlowException ex) {
            LOG.error("Error while persisting flows: {}", ex.getMessage(), ex);
        }

        LOG.debug("Completed processing {} telemetry messages.",
                messageLog.getMessageList().size());
    }

    protected abstract P parse(TelemetryMessageLogEntry message);

    public void destroy() {
        // not needed
    }

    public String getMetaDataNodeLookup() {
        return metaDataNodeLookup;
    }

    public void setMetaDataNodeLookup(String metaDataNodeLookup) {
        this.metaDataNodeLookup = metaDataNodeLookup;

        if (!Strings.isNullOrEmpty(this.metaDataNodeLookup)) {
            this.contextKey = new ContextKey(metaDataNodeLookup);
        } else {
            this.contextKey = null;
        }
    }
}
