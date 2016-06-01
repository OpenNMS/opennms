/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
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
public class SnmpRequestExecutorLocalImpl implements SnmpRequestExecutor {

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
        for (SnmpWalkRequestDTO walkRequest : request.getWalkRequest()) {
            CompletableFuture<SnmpResponseDTO> future = walk(request, walkRequest);
            combinedFuture = combinedFuture.thenCombine(future, (m,s) -> {
                m.getResponses().add(s);
                return m;
            });
        }
        return combinedFuture;
    }

    public CompletableFuture<SnmpResponseDTO> walk(SnmpRequestDTO request, SnmpWalkRequestDTO walk) {
        final List<SnmpResult> results = new ArrayList<>();
        final CompletableFuture<SnmpResponseDTO> future = new CompletableFuture<SnmpResponseDTO>();
        
        CollectionTracker tracker;
        if (walk.isSingleInstance()) {
            if (walk.getOids().size() != 1) {
                future.completeExceptionally(new IllegalArgumentException("Single instance requests must have a single OID."));
                return future;
            }
            final SnmpObjId oid = walk.getOids().get(0);
            int[] ids = oid.getIds();
            if (ids.length < 1) {
                future.completeExceptionally(new IllegalArgumentException("OID oups."));
                return future;
            }
            int[] baseIds = new int[ids.length - 1];
            for (int i = 0; i < ids.length - 1; i++) {
                baseIds[i] = ids[i];
            }
            SnmpObjId baseOid = new SnmpObjId(baseIds, false);
            tracker = new SingleInstanceTracker(baseOid, oid.getInstance(baseOid));
        } else {
            final Collection<Collectable> columnTrackers = walk.getOids().stream()
                    .map(oid -> SnmpObjId.get(oid))
                    .map(objId -> new ColumnTracker(objId))
                    .collect(Collectors.toList());
            tracker = new AggregateTracker(columnTrackers) {
                @Override
                protected void storeResult(SnmpResult res) {
                    results.add(res);
                }
            };
        }
        if (walk.getMaxRepetitions() != null) {
            tracker.setMaxRepetitions(walk.getMaxRepetitions());
        }

        final SnmpWalker walker = SnmpUtils.createWalker(request.getAgent(), request.getDescription(), tracker);
        walker.setCallback(new SnmpWalkCallback() {
            @Override
            public void complete(SnmpWalker tracker, Throwable t) {
                if (t != null) {
                    future.completeExceptionally(t);
                } else {
                    final SnmpResponseDTO response = new SnmpResponseDTO();
                    response.setCorrelationId(walk.getCorrelationId());
                    response.setResults(results);
                    future.complete(response);
                }
            }
        });
        walker.start();
        return future;
    }

    public CompletableFuture<SnmpResponseDTO> get(SnmpRequestDTO request, SnmpGetRequestDTO get) {
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
}
