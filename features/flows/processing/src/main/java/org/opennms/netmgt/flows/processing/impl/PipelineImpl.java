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
package org.opennms.netmgt.flows.processing.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.processing.Pipeline;
import org.opennms.netmgt.flows.processing.ProcessingOptions;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;

public class PipelineImpl implements Pipeline {

    public static final String REPOSITORY_ID = "flows.repository.id";

    private static final Logger LOG = LoggerFactory.getLogger(PipelineImpl.class);

    /**
     * Time taken to enrich the flows in a log
     */
    private final Timer logEnrichementTimer;

    /**
     * Time taken to apply thresholding to a log
     */
    private final Timer logThresholdingTimer;

    /**
     * Time taken to mark the flows in a log
     */
    private final Timer logMarkingTimer;

    /**
     * Number of flows in a log
     */
    private final Histogram flowsPerLog;

    /**
     * Number of logs without a flow
     */
    private final Counter emptyFlows;

    private final MetricRegistry metricRegistry;

    private final DocumentEnricherImpl documentEnricher;

    private final InterfaceMarkerImpl interfaceMarker;

    private final FlowThresholdingImpl thresholding;

    private final Map<String, Persister> persisters = Maps.newConcurrentMap();

    public PipelineImpl(final MetricRegistry metricRegistry,
                        final DocumentEnricherImpl documentEnricher,
                        final InterfaceMarkerImpl interfaceMarker,
                        final FlowThresholdingImpl thresholding) {
        this.documentEnricher = Objects.requireNonNull(documentEnricher);
        this.interfaceMarker = Objects.requireNonNull(interfaceMarker);
        this.thresholding = Objects.requireNonNull(thresholding);

        this.emptyFlows = metricRegistry.counter("emptyFlows");
        this.flowsPerLog = metricRegistry.histogram("flowsPerLog");

        this.logEnrichementTimer = metricRegistry.timer("logEnrichment");
        this.logMarkingTimer = metricRegistry.timer("logMarking");
        this.logThresholdingTimer = metricRegistry.timer("logThresholding");

        this.metricRegistry = Objects.requireNonNull(metricRegistry);
    }

    public void process(final List<Flow> flows, final FlowSource source, final ProcessingOptions processingOptions) throws FlowException {
        // Track the number of flows per call
        this.flowsPerLog.update(flows.size());
        
        // Filter empty logs
        if (flows.isEmpty()) {
            this.emptyFlows.inc();
            LOG.info("Received empty flows from {} @ {}. Nothing to do.", source.getSourceAddress(), source.getLocation());
            return;
        }
        
        // Enrich with model data
        LOG.debug("Enriching {} flow documents.", flows.size());
        final List<EnrichedFlow> enrichedFlows;
        try (final Timer.Context ctx = this.logEnrichementTimer.time()) {
            enrichedFlows = documentEnricher.enrich(flows, source);
        } catch (Exception e) {
            throw new FlowException("Failed to enrich one or more flows.", e);
        }

        // Mark nodes and interfaces as having associated flows
        try (final Timer.Context ctx = this.logMarkingTimer.time()) {
            this.interfaceMarker.mark(enrichedFlows);
        }

        // Apply thresholding to flows
        try (final Timer.Context ctx = this.logThresholdingTimer.time()) {
            this.thresholding.threshold(enrichedFlows, processingOptions);
        } catch (ThresholdInitializationException | ExecutionException e) {
            throw new FlowException("Failed to threshold one or more flows.", e);
        }

        // Push flows to persistence
        for (final var persister : this.persisters.entrySet()) {
            persister.getValue().persist(enrichedFlows);
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onBind(final FlowRepository repository, final Map properties) {
        if (properties.get(REPOSITORY_ID) == null) {
            LOG.error("Flow repository has no repository ID defined. Ignoring...");
            return;
        }

        final String pid = Objects.toString(properties.get(REPOSITORY_ID));
        this.persisters.put(pid, new Persister(repository,
                                               this.metricRegistry.timer(MetricRegistry.name("logPersisting", pid))));
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onUnbind(final FlowRepository repository, final Map properties) {
        if (properties.get(REPOSITORY_ID) == null) {
            LOG.error("Flow repository has no repository ID defined. Ignoring...");
            return;
        }

        final String pid = Objects.toString(properties.get(REPOSITORY_ID));
        this.persisters.remove(pid);
    }

    private static class Persister {
        public final FlowRepository repository;
        public final Timer logTimer;

        public Persister(final FlowRepository repository, final Timer logTimer) {
            this.repository = Objects.requireNonNull(repository);
            this.logTimer = Objects.requireNonNull(logTimer);
        }

        public void persist(final Collection<EnrichedFlow> flows) throws FlowException {
            try (final var ctx = this.logTimer.time()) {
                this.repository.persist(flows);
            }
        }
    }
}
