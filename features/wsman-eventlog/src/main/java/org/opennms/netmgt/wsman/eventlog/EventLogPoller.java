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
package org.opennms.netmgt.wsman.eventlog;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.netmgt.config.wsman.eventlog.Log;
import org.opennms.netmgt.config.wsman.eventlog.Package;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.provision.detector.wsman.WsmanEndpointUtils;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogBatchDTO;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogQueryDTO;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogRecordDTO;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogRequestDTO;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogResponseDTO;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogWql;
import org.opennms.netmgt.wsman.eventlog.rpc.LocationAwareWsManEventLogClient;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads one log of one node: builds the request from the cursor, forwards an event per
 * record, and advances the cursor. Nodes that keep failing are polled less often.
 */
public class EventLogPoller {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogPoller.class);

    /** After this many consecutive failures a node is skipped for MAX_BACKOFF polls. */
    private static final int MAX_BACKOFF = 16;

    /** Wait on the RPC a little longer than the agent's own timeouts before giving up. */
    private static final long RPC_GRACE_MS = 10_000L;
    private static final long DEFAULT_TIMEOUT_MS = 30_000L;

    private final WSManConfigDao wsManConfigDao;
    private final LocationAwareWsManEventLogClient client;
    private final EventLogCursorStore cursorStore;
    private final EventLogEventMapper mapper;
    private final EventForwarder eventForwarder;
    private final WsManEventLogdMetrics metrics;
    private final int retries;

    private final Map<String, Backoff> backoffs = new ConcurrentHashMap<>();

    public EventLogPoller(WSManConfigDao wsManConfigDao, LocationAwareWsManEventLogClient client, EventLogCursorStore cursorStore,
            EventLogEventMapper mapper, EventForwarder eventForwarder, WsManEventLogdMetrics metrics, int retries) {
        this.wsManConfigDao = Objects.requireNonNull(wsManConfigDao);
        this.client = Objects.requireNonNull(client);
        this.cursorStore = Objects.requireNonNull(cursorStore);
        this.mapper = Objects.requireNonNull(mapper);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        this.metrics = Objects.requireNonNull(metrics);
        this.retries = retries;
    }

    public void poll(Package pkg, Log log, EventLogTarget target) {
        final String key = target.getNodeId() + "/" + log.getName();
        final Backoff backoff = backoffs.computeIfAbsent(key, k -> new Backoff());
        if (backoff.skip()) {
            LOG.debug("Skipping {} {} while backing off", target, log.getName());
            return;
        }
        try {
            final EventLogRequestDTO request = buildRequest(target, log);
            final EventLogResponseDTO response = client.read(request)
                    .get(request.getTimeToLiveMs() + RPC_GRACE_MS, TimeUnit.MILLISECONDS);
            if (response.getErrorMessage() != null) {
                throw new IllegalStateException(response.getErrorMessage());
            }
            for (EventLogBatchDTO batch : response.getBatches()) {
                if (batch.getError() != null) {
                    throw new IllegalStateException(batch.getError());
                }
                publish(pkg, log, target, batch);
            }
            backoff.succeeded();
            metrics.pollCompleted();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            final Throwable cause = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
            backoff.failed();
            metrics.pollFailed();
            LOG.warn("Reading the {} log on {} failed ({} consecutive): {}", log.getName(), target, backoff.failures, cause.getMessage());
        }
    }

    EventLogRequestDTO buildRequest(EventLogTarget target, Log log) {
        final WSManEndpoint endpoint = wsManConfigDao.getEndpoint(target.getAddress());
        final EventLogRequestDTO request = new EventLogRequestDTO();
        request.setLocation(target.getLocation());
        request.setRetries(retries);
        request.setEndpointAttributes(WsmanEndpointUtils.toMap(endpoint));
        final long timeout = endpoint.getReceiveTimeout() != null ? endpoint.getReceiveTimeout() : DEFAULT_TIMEOUT_MS;
        request.setTimeToLiveMs((timeout + RPC_GRACE_MS) * (retries + 1));
        request.addTracingInfo(EventLogRequestDTO.TAG_NODE_ID, Integer.toString(target.getNodeId()));
        request.addTracingInfo(EventLogRequestDTO.TAG_IP_ADDRESS, target.getAddress().getHostAddress());

        final EventLogQueryDTO query = new EventLogQueryDTO(log.getName());
        query.setMaxRecords(log.getMaxRecords());
        query.setMode(log.getMode());
        query.setEventTypes(EventLogLevel.parseEventTypes(log.getLevels()));
        final Long cursor = cursorStore.get(target.getNodeId(), log.getName());
        if (cursor != null) {
            query.setAfterRecordNumber(cursor);
        } else {
            final Duration lookback = Durations.parse(log.getLookback());
            query.setSinceTime(EventLogWql.toDmtf(Instant.now().minus(lookback)));
        }
        request.addQuery(query);
        return request;
    }

    private void publish(Package pkg, Log log, EventLogTarget target, EventLogBatchDTO batch) {
        final Set<Integer> include = parseIds(log.getIncludeEventIds());
        final Set<Integer> exclude = parseIds(log.getExcludeEventIds());
        final List<Event> events = new ArrayList<>();
        long highest = -1;
        for (EventLogRecordDTO record : batch.getRecords()) {
            highest = Math.max(highest, record.getRecordNumber());
            if (record.getEventCode() != null) {
                if (!include.isEmpty() && !include.contains(record.getEventCode())) {
                    continue;
                }
                if (exclude.contains(record.getEventCode())) {
                    continue;
                }
            }
            events.add(mapper.toEvent(target, record, pkg.getEventMappings()));
        }
        metrics.recordsRead(batch.getRecords().size());
        for (Event event : events) {
            eventForwarder.sendNow(event);
        }
        metrics.eventsPublished(events.size());
        if (highest >= 0) {
            cursorStore.put(target.getNodeId(), log.getName(), highest);
        }
        if (batch.isTruncated()) {
            metrics.pollTruncated();
            eventForwarder.sendNow(mapper.truncatedEvent(target, log.getName(), log.getMaxRecords()));
            LOG.info("The {} log on {} had more than {} new records; the rest are read on the next poll", log.getName(), target, log.getMaxRecords());
        }
        LOG.debug("{} {}: {} record(s), {} event(s), cursor {}", target, log.getName(), batch.getRecords().size(), events.size(), highest);
    }

    static Set<Integer> parseIds(String csv) {
        final Set<Integer> ids = new HashSet<>();
        if (csv == null || csv.trim().isEmpty()) {
            return ids;
        }
        Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(s -> ids.add(Integer.valueOf(s)));
        return ids;
    }

    /** Doubles the number of polls skipped after each consecutive failure, up to MAX_BACKOFF. */
    static final class Backoff {
        int failures;
        int skipsLeft;

        synchronized boolean skip() {
            if (skipsLeft > 0) {
                skipsLeft--;
                return true;
            }
            return false;
        }

        synchronized void failed() {
            failures++;
            skipsLeft = Math.min(MAX_BACKOFF, 1 << Math.min(failures - 1, 4));
        }

        synchronized void succeeded() {
            failures = 0;
            skipsLeft = 0;
        }
    }
}
