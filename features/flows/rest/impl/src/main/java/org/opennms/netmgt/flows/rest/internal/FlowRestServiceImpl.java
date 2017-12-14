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

package org.opennms.netmgt.flows.rest.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.rest.FlowRestService;
import org.opennms.netmgt.flows.rest.model.FlowSeriesResponse;
import org.opennms.netmgt.flows.rest.model.FlowSummaryResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class FlowRestServiceImpl implements FlowRestService {

    private final FlowRepository flowRepository;

    public FlowRestServiceImpl(FlowRepository flowRepository) {
        this.flowRepository = Objects.requireNonNull(flowRepository);
    }

    @Override
    public Long getFlowCount(long start, long end) {
        final long effectiveEnd = getEffectiveEnd(end);
        final long effectiveStart = getEffectiveStart(start, effectiveEnd);
        return waitForFuture(flowRepository.getFlowCount(effectiveStart, effectiveEnd));
    }

    @Override
    public FlowSeriesResponse getTopNApplicationSeries(long start, long end, long step, int N) {
        final long effectiveEnd = getEffectiveEnd(end);
        final long effectiveStart = getEffectiveStart(start, effectiveEnd);

        final CompletableFuture<FlowSeriesResponse> future = flowRepository.getTopNApplicationsSeries(N, effectiveStart,
                effectiveEnd, step).thenApply(res -> {
            final FlowSeriesResponse response = new FlowSeriesResponse();
            response.setStart(effectiveStart);
            response.setEnd(effectiveEnd);
            response.setLabels(res.rowKeySet().stream()
                    .map((d) -> String.format("%s (%s)", d.getValue(), d.isSource() ? "In" : "Out"))
                    .collect(Collectors.toList()));
            populateResponseFromTable(res, response);
            return response;
        });
        return waitForFuture(future);
    }

    @Override
    public FlowSummaryResponse getTopNApplications(long start, long end, int N) {
        final long effectiveEnd = getEffectiveEnd(end);
        final long effectiveStart = getEffectiveStart(start, effectiveEnd);

        final CompletableFuture<FlowSummaryResponse> future = flowRepository.getTopNApplications(N, effectiveStart, effectiveEnd).thenApply(res -> {
            final FlowSummaryResponse response = new FlowSummaryResponse();
            response.setStart(effectiveStart);
            response.setEnd(effectiveEnd);
            response.setHeaders(Lists.newArrayList("Application", "Bytes In", "Bytes Out"));
            response.setRows(res.stream()
                    .map(sum -> Arrays.asList((Object)sum.getEntity(), sum.getBytesIn(), sum.getBytesOut()))
                    .collect(Collectors.toList()));
            return response;
        });
        return waitForFuture(future);
    }

    @Override
    public FlowSummaryResponse getTopNConversations(long start, long end, int N) {
        final long effectiveEnd = getEffectiveEnd(end);
        final long effectiveStart = getEffectiveStart(start, effectiveEnd);

        final CompletableFuture<FlowSummaryResponse> future = flowRepository.getTopNConversations(N, effectiveStart, effectiveEnd).thenApply(res -> {
            final FlowSummaryResponse response = new FlowSummaryResponse();
            response.setStart(effectiveStart);
            response.setEnd(effectiveEnd);
            response.setHeaders(Lists.newArrayList("Location", "Protocol", "Source IP", "Source Port", "Dest. IP", "Dest. Port", "Bytes In", "Bytes Out"));
            response.setRows(res.stream()
                    .map(sum -> {
                        final ConversationKey key = sum.getEntity();
                        return Lists.newArrayList((Object)key.getLocation(), key.getProtocol(),
                                key.getSrcIp(), key.getSrcPort(), key.getDstIp(), key.getDstPort(),
                                sum.getBytesIn(), sum.getBytesOut());
                    })
                    .collect(Collectors.toList()));
            return response;
        });
        return waitForFuture(future);
    }

    @Override
    public FlowSeriesResponse getTopNConversationsSeries(long start, long end, long step, int N) {
        final long effectiveEnd = getEffectiveEnd(end);
        final long effectiveStart = getEffectiveStart(start, effectiveEnd);

        final CompletableFuture<FlowSeriesResponse> future = flowRepository.getTopNConversationsSeries(N, effectiveStart, effectiveEnd, step).thenApply(res -> {
            final FlowSeriesResponse response = new FlowSeriesResponse();
            response.setStart(effectiveEnd);
            response.setEnd(effectiveEnd);
            response.setLabels(res.rowKeySet().stream()
                    .map((d) -> {
                        final ConversationKey key = d.getValue();
                        return String.format("%s:%d <-> %s:%d (%s)", key.getSrcIp(), key.getSrcPort(),
                                key.getDstIp(), key.getDstPort(), d.isSource() ? "In" : "Out");
                    })
                    .collect(Collectors.toList()));
            populateResponseFromTable(res, response);
            return response;
        });
        return waitForFuture(future);
    }

    private static long getEffectiveStart(long start, long effectiveEnd) {
        // If start is negative, subtract it from the end
        long effectiveStart = start >= 0 ? start : effectiveEnd + start;
        // Make sure the resulting start time is not negative
        effectiveStart = Math.max(effectiveStart, 0);
        return effectiveStart;
    }

    public static long getEffectiveEnd(long end) {
        // If end is not strictly positive, use the current timestamp
        return end > 0 ? end : new Date().getTime();
    }

    private static void populateResponseFromTable(Table<?, Long, Double> table, FlowSeriesResponse response) {
        final List<Long> timestamps = table.columnKeySet().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        final List<List<Double>> columns = new LinkedList<>();
        for (Object rowKey : table.rowKeySet()) {
            final List<Double> column = new ArrayList<>(timestamps.size());
            for (Long ts : timestamps) {
                Double val = table.get(rowKey, ts);
                if (val == null) {
                    val = Double.NaN;
                }
                column.add(val);
            }
            columns.add(column);
        }

        response.setTimestamps(timestamps);
        response.setColumns(columns);
    }

    private static <T> T waitForFuture(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException|ExecutionException e) {
            throw new WebApplicationException("Failed to execute query: " + e.getMessage(), e);
        }
    }
}
