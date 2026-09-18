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
package org.opennms.netmgt.wsman.eventlog.rpc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManConstants;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.utils.CachingWSManClientFactory;
import org.opennms.core.wsman.utils.ResponseHandlingUtils;
import org.opennms.core.wsman.utils.RetryNTimesLoop;
import org.opennms.netmgt.provision.detector.wsman.WsmanEndpointUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.collect.ListMultimap;

/**
 * Runs the event log enumeration where the node lives. Records are pulled a page at a
 * time so a large backlog stops at the cap instead of being read to the end.
 */
public class WsManEventLogRpcModule extends AbstractXmlRpcModule<EventLogRequestDTO, EventLogResponseDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(WsManEventLogRpcModule.class);

    public static final String RPC_MODULE_ID = "WsManEventLog";

    private final WSManClientFactory clientFactory;
    private final ExecutorService executor;

    public WsManEventLogRpcModule() {
        this(new CachingWSManClientFactory(), 4);
    }

    public WsManEventLogRpcModule(int threads) {
        this(new CachingWSManClientFactory(), threads);
    }

    public WsManEventLogRpcModule(WSManClientFactory clientFactory, int threads) {
        super(EventLogRequestDTO.class, EventLogResponseDTO.class);
        this.clientFactory = Objects.requireNonNull(clientFactory);
        this.executor = Executors.newFixedThreadPool(Math.max(1, threads));
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public EventLogResponseDTO createResponseWithException(Throwable ex) {
        return new EventLogResponseDTO(ex);
    }

    @Override
    public CompletableFuture<EventLogResponseDTO> execute(EventLogRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> read(request), executor);
    }

    public EventLogResponseDTO read(EventLogRequestDTO request) {
        final EventLogResponseDTO response = new EventLogResponseDTO();
        final WSManEndpoint endpoint;
        try {
            endpoint = WsmanEndpointUtils.fromMap(request.getEndpointAttributes());
        } catch (Exception e) {
            return new EventLogResponseDTO(e);
        }
        try (WSManClient client = clientFactory.getClient(endpoint)) {
            for (EventLogQueryDTO query : request.getQueries()) {
                response.addBatch(readLog(client, request, query));
            }
        }
        return response;
    }

    private EventLogBatchDTO readLog(WSManClient client, EventLogRequestDTO request, EventLogQueryDTO query) {
        final EventLogBatchDTO batch = new EventLogBatchDTO(query.getLogfile());
        final String wql = EventLogWql.forQuery(query);
        final int max = Math.max(1, query.getMaxRecords());
        final RetryNTimesLoop retryLoop = new RetryNTimesLoop(Math.max(0, request.getRetries()));
        while (retryLoop.shouldContinue()) {
            try {
                final List<Node> nodes = new ArrayList<>();
                LOG.debug("Enumerating {} on {} with '{}'", query.getLogfile(), client, wql);
                String context = client.enumerateWithFilter(request.getResourceUri(), WSManConstants.XML_NS_WQL_DIALECT, wql);
                boolean exhausted = context == null;
                while (!exhausted && nodes.size() < max) {
                    context = client.pull(context, request.getResourceUri(), nodes, false);
                    exhausted = context == null;
                }
                final List<EventLogRecordDTO> records = new ArrayList<>(nodes.size());
                for (Node node : nodes) {
                    final EventLogRecordDTO record = toRecord(node, query.getLogfile());
                    if (record != null) {
                        records.add(record);
                    }
                }
                records.sort(Comparator.comparingLong(EventLogRecordDTO::getRecordNumber));
                if (records.size() > max) {
                    records.subList(max, records.size()).clear();
                    exhausted = false;
                }
                batch.setRecords(records);
                batch.setTruncated(!exhausted);
                batch.setError(null);
                return batch;
            } catch (WSManException e) {
                batch.setError(e.getMessage());
                try {
                    retryLoop.takeException(e);
                } catch (WSManException exhausted) {
                    LOG.warn("Reading the {} log failed after {} retries: {}", query.getLogfile(), request.getRetries(), exhausted.getMessage());
                    return batch;
                }
            }
        }
        return batch;
    }

    static EventLogRecordDTO toRecord(Node node, String logfile) {
        final ListMultimap<String, String> values = ResponseHandlingUtils.toMultiMap(node);
        final String recordNumber = first(values, "RecordNumber");
        if (recordNumber == null) {
            LOG.debug("Skipping an item without a RecordNumber: {}", values);
            return null;
        }
        final EventLogRecordDTO record = new EventLogRecordDTO();
        record.setLogfile(firstOr(values, "Logfile", logfile));
        record.setRecordNumber(Long.parseLong(recordNumber.trim()));
        record.setEventCode(toInteger(first(values, "EventCode")));
        record.setEventType(toInteger(first(values, "EventType")));
        record.setSourceName(first(values, "SourceName"));
        record.setTimeGenerated(first(values, "TimeGenerated"));
        record.setComputerName(first(values, "ComputerName"));
        record.setMessage(first(values, "Message"));
        record.setInsertionStrings(new ArrayList<>(values.get("InsertionStrings")));
        return record;
    }

    private static String first(ListMultimap<String, String> values, String name) {
        final List<String> list = values.get(name);
        return list.isEmpty() ? null : list.get(0);
    }

    private static String firstOr(ListMultimap<String, String> values, String name, String fallback) {
        final String value = first(values, name);
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static Integer toInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void destroy() {
        executor.shutdownNow();
        if (clientFactory instanceof AutoCloseable) {
            try {
                ((AutoCloseable) clientFactory).close();
            } catch (Exception e) {
                LOG.debug("Closing the client factory failed", e);
            }
        }
    }
}
