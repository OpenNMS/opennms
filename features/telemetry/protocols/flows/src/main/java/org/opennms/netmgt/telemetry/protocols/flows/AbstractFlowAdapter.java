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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public abstract class AbstractFlowAdapter<P> implements Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFlowAdapter.class);

    private final FlowRepository flowRepository;

    private final Converter<P> converter;

    /**
     * Time taken to parse a log
     */
    private final Timer logParsingTimer;

    /**
     * Number of packets per log
     */
    private final Histogram packetsPerLogHistogram;

    public AbstractFlowAdapter(final MetricRegistry metricRegistry,
                           final FlowRepository flowRepository,
                           final Converter<P> converter) {
        this.flowRepository = Objects.requireNonNull(flowRepository);
        this.converter = Objects.requireNonNull(converter);

        logParsingTimer = metricRegistry.timer("logParsing");
        packetsPerLogHistogram = metricRegistry.histogram("packetsPerLog");
    }

    @Override
    public void setConfig(AdapterDefinition protocol) {
        // we do not need the protocol
    }

    @Override
    public void handleMessageLog(TelemetryMessageLog messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessageList().size());

        final List<P> flowPackets = new LinkedList<>();
        final List<Flow> flows = new LinkedList<>();
        try (Timer.Context ctx = logParsingTimer.time()) {
            for (TelemetryMessageLogEntry eachMessage : messageLog.getMessageList()) {
                LOG.trace("Parsing packet: {}", eachMessage);
                final P flowPacket = parse(eachMessage);
                if (flowPacket != null) {
                    flowPackets.add(flowPacket);
                    flows.addAll(converter.convert(flowPacket));
                }
            }
            packetsPerLogHistogram.update(flowPackets.size());
        }

        try {
            LOG.debug("Persisting {} packets, {} flows.", flowPackets.size(), flows.size());
            final FlowSource source = new FlowSource(messageLog.getLocation(), messageLog.getSourceAddress());
            flowRepository.persist(flows, source);
        } catch (FlowException ex) {
            LOG.error("Failed to persist one or more packets: {}", ex.getMessage());
        }

        LOG.debug("Completed processing {} telemetry messages.",
                messageLog.getMessageList().size());
    }

    protected abstract P parse(TelemetryMessageLogEntry message);

    public void destroy() {
        // not needed
    }
}
