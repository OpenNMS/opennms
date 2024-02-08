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
package org.opennms.netmgt.telemetry.protocols.flows;

import static com.codahale.metrics.MetricRegistry.name;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.core.mate.api.ContextKey;
import org.opennms.netmgt.flows.api.DetailedFlowException;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.UnrecoverableFlowException;
import org.opennms.netmgt.flows.processing.Pipeline;
import org.opennms.netmgt.flows.processing.ProcessingOptions;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;

public abstract class AbstractFlowAdapter<P> implements Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFlowAdapter.class);

    private final Pipeline pipeline;

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

    private boolean applicationThresholding;
    private boolean applicationDataCollection;

    private final List<? extends PackageDefinition> packages;

    public AbstractFlowAdapter(final AdapterDefinition adapterConfig,
                               final MetricRegistry metricRegistry,
                               final Pipeline pipeline) {
        Objects.requireNonNull(adapterConfig);
        Objects.requireNonNull(metricRegistry);

        this.pipeline = Objects.requireNonNull(pipeline);

        this.logParsingTimer = metricRegistry.timer(name("adapters", adapterConfig.getFullName(), "logParsing"));
        this.packetsPerLogHistogram = metricRegistry.histogram(name("adapters", adapterConfig.getFullName(), "packetsPerLog"));
        this.entriesReceived = metricRegistry.meter(name("adapters", adapterConfig.getFullName(), "entriesReceived"));
        this.entriesParsed = metricRegistry.meter(name("adapters", adapterConfig.getFullName(), "entriesParsed"));
        this.entriesConverted = metricRegistry.meter(name("adapters", adapterConfig.getFullName(), "entriesConverted"));

        this.packages = Objects.requireNonNull(adapterConfig.getPackages());
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

                    final List<Flow> converted = this.convert(flowPacket, Instant.ofEpochMilli(eachMessage.getTimestamp()));
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
            this.pipeline.process(flows, source, ProcessingOptions.builder()
                                                                  .setApplicationThresholding(this.applicationThresholding)
                                                                  .setApplicationDataCollection(this.applicationDataCollection)
                                                                  .setPackages(this.packages)
                                                                  .build());
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

    protected abstract List<Flow> convert(final P packet, final Instant receivedAt);

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

    public boolean isApplicationThresholding() {
        return this.applicationThresholding;
    }

    public void setApplicationThresholding(final boolean applicationThresholding) {
        this.applicationThresholding = applicationThresholding;
    }

    public boolean isApplicationDataCollection() {
        return this.applicationDataCollection;
    }

    public void setApplicationDataCollection(final boolean applicationDataCollection) {
        this.applicationDataCollection = applicationDataCollection;
    }
}
