/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.proxy.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalkCallback;
import org.opennms.netmgt.snmp.SnmpWalker;

/**
 * Executes SNMP requests locally using the current {@link org.opennms.netmgt.snmp.SnmpStrategy}.
 *
 * @author jwhite
 */
public class SnmpProxyRpcModule extends AbstractXmlRpcModule<SnmpRequestDTO, SnmpMultiResponseDTO> {

    public static final SnmpProxyRpcModule INSTANCE = new SnmpProxyRpcModule();

    public static final String RPC_MODULE_ID = "SNMP";

    private static final ExecutorService REAPER_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SNMP-Proxy-RPC-Session-Reaper");
        }
    });

    public SnmpProxyRpcModule() {
        super(SnmpRequestDTO.class, SnmpMultiResponseDTO.class);
    }

    @Override
    public CompletableFuture<SnmpMultiResponseDTO> execute(SnmpRequestDTO request) {
        CompletableFuture<SnmpMultiResponseDTO> combinedFuture = CompletableFuture
                .completedFuture(new SnmpMultiResponseDTO());
        for (SnmpGetRequestDTO getRequest : request.getGetRequests()) {
            CompletableFuture<SnmpResponseDTO> future = get(request, getRequest);
            combinedFuture = combinedFuture.thenCombine(future, (m,s) -> {
                m.getResponses().add(s);
                return m;
            });
        }
        if (request.getWalkRequest().size() > 0) {
            CompletableFuture<Collection<SnmpResponseDTO>> future = walk(request, request.getWalkRequest());
            combinedFuture = combinedFuture.thenCombine(future, (m,s) -> {
                m.getResponses().addAll(s);
                return m;
            });
        }
        return combinedFuture;
    }

    private CompletableFuture<Collection<SnmpResponseDTO>> walk(SnmpRequestDTO request, List<SnmpWalkRequestDTO> walks) {
        final CompletableFuture<Collection<SnmpResponseDTO>> future = new CompletableFuture<>();
        final Map<String, SnmpResponseDTO> responsesByCorrelationId = new LinkedHashMap<>();

        final List<Collectable> trackers = new ArrayList<>(walks.size());
        for (final SnmpWalkRequestDTO walk : walks) {
            CollectionTracker tracker;
            if (walk.getInstance() != null) {
                if (walk.getOids().size() != 1) {
                    future.completeExceptionally(new IllegalArgumentException("Single instance requests must have a single OID."));
                    return future;
                }
                final SnmpObjId oid = walk.getOids().get(0);
                tracker = new SingleInstanceTracker(oid, new SnmpInstId(walk.getInstance())) {
                    @Override
                    protected void storeResult(SnmpResult res) {
                        addResult(res, walk.getCorrelationId(), responsesByCorrelationId);
                    }
                };
            } else {
                final Collection<Collectable> columnTrackers = walk.getOids().stream()
                        .map(SnmpObjId::get)
                        .map(ColumnTracker::new)
                        .collect(Collectors.toList());
                tracker = new AggregateTracker(columnTrackers) {
                    @Override
                    protected void storeResult(SnmpResult res) {
                        addResult(res, walk.getCorrelationId(), responsesByCorrelationId);
                    }
                };
            }
            if (walk.getMaxRepetitions() != null) {
                tracker.setMaxRepetitions(walk.getMaxRepetitions());
            }
            trackers.add(tracker);
        }

        AggregateTracker aggregate = new AggregateTracker(trackers);
        final SnmpWalker walker = SnmpUtils.createWalker(request.getAgent(), request.getDescription(), aggregate);
        walker.setCallback(new SnmpWalkCallback() {
            @Override
            public void complete(SnmpWalker tracker, Throwable t) {
                try {
                    if (t != null) {
                        future.completeExceptionally(t);
                    } else {
                        future.complete(responsesByCorrelationId.values());
                    }
                } finally {
                    // Close the tracker using a separate thread
                    // This allows the SnmpWalker to clean up properly instead
                    // of interrupting execution as it's executing the callback
                    REAPER_EXECUTOR.submit(new Runnable() {
                        @Override
                        public void run() {
                            tracker.close();
                        }
                    });
                }
            }
        });
        walker.start();
        return future;
    }

    private static final void addResult(SnmpResult result, String correlationId, Map<String, SnmpResponseDTO> responsesByCorrelationId) {
        SnmpResponseDTO response = responsesByCorrelationId.get(correlationId);
        if (response == null) {
            response = new SnmpResponseDTO();
            response.setCorrelationId(correlationId);
            responsesByCorrelationId.put(correlationId, response);
        }
        response.getResults().add(result);
    }

    private CompletableFuture<SnmpResponseDTO> get(SnmpRequestDTO request, SnmpGetRequestDTO get) {
        final SnmpObjId[] oids = get.getOids().toArray(new SnmpObjId[get.getOids().size()]);
        final CompletableFuture<SnmpValue[]> future = SnmpUtils.getAsync(request.getAgent(), oids);
        return future.thenApply(values -> {
            final List<SnmpResult> results = new ArrayList<>(oids.length);
            for (int i = 0; i < oids.length; i++) {
                final SnmpResult result = new SnmpResult(oids[i], null, values[i]);
                results.add(result);
            }
            final SnmpResponseDTO responseDTO = new SnmpResponseDTO();
            responseDTO.setCorrelationId(get.getCorrelationId());
            responseDTO.setResults(results);
            return responseDTO;
        });
    }

    @Override
    public SnmpMultiResponseDTO createResponseWithException(Throwable ex) {
        return new SnmpMultiResponseDTO(ex);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }
}
