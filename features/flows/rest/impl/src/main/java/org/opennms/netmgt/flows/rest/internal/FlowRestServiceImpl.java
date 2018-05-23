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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.NodeCriteria;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.flows.rest.FlowRestService;
import org.opennms.netmgt.flows.rest.model.FlowGraphUrlInfo;
import org.opennms.netmgt.flows.rest.model.FlowNodeDetails;
import org.opennms.netmgt.flows.rest.model.FlowNodeSummary;
import org.opennms.netmgt.flows.rest.model.FlowSeriesColumn;
import org.opennms.netmgt.flows.rest.model.FlowSeriesResponse;
import org.opennms.netmgt.flows.rest.model.FlowSnmpInterface;
import org.opennms.netmgt.flows.rest.model.FlowSummaryResponse;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class FlowRestServiceImpl implements FlowRestService {
    private final FlowRepository flowRepository;
    private final NodeDao nodeDao;
    private final SnmpInterfaceDao snmpInterfaceDao;
    private final TransactionOperations transactionOperations;
    private String flowGraphUrl;

    public FlowRestServiceImpl(FlowRepository flowRepository, NodeDao nodeDao,
                               SnmpInterfaceDao snmpInterfaceDao, TransactionOperations transactionOperations) {
        this.flowRepository = Objects.requireNonNull(flowRepository);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    @Override
    public Long getFlowCount(UriInfo uriInfo) {
        return waitForFuture(flowRepository.getFlowCount(getFiltersFromQueryString(uriInfo.getQueryParameters())));
    }

    @Override
    public List<FlowNodeSummary> getFlowExporters() {
        return transactionOperations.execute(status -> this.nodeDao.findAllHavingFlows().stream())
                .map(n -> new FlowNodeSummary(n.getId(),
                        n.getForeignId(), n.getForeignSource(), n.getLabel(),
                        n.getCategories().stream().map(OnmsCategory::getName).collect(Collectors.toList())))
                .sorted(Comparator.comparingInt(FlowNodeSummary::getId))
                .collect(Collectors.toList());
    }

    @Override
    public FlowNodeDetails getFlowExporter(Integer nodeId) {
        final List<FlowSnmpInterface> ifaces = transactionOperations.execute(status -> this.snmpInterfaceDao.findAllHavingFlows(nodeId)).stream()
                .map(iface -> new FlowSnmpInterface(iface.getIfIndex(),
                            iface.getIfName(),
                            iface.getIfAlias(),
                            iface.getIfDescr()))
                .collect(Collectors.toList());

        return new FlowNodeDetails(nodeId, ifaces);
    }

    @Override
    public FlowSummaryResponse getTopNApplications(int N, boolean includeOther, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);

        final List<TrafficSummary<String>> summary =
                waitForFuture(flowRepository.getTopNApplications(N, includeOther, filters));

        final FlowSummaryResponse response = new FlowSummaryResponse();
        response.setStart(timeRangeFilter.getStart());
        response.setEnd(timeRangeFilter.getEnd());
        response.setHeaders(Lists.newArrayList("Application", "Bytes In", "Bytes Out"));
        response.setRows(summary.stream()
                .map(sum -> Arrays.asList((Object)sum.getEntity(), sum.getBytesIn(), sum.getBytesOut()))
                .collect(Collectors.toList()));
        return response;
    }

    @Override
    public FlowSeriesResponse getTopNApplicationSeries(long step, int N, boolean includeOther, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);
        final Table<Directional<String>, Long, Double> series =
                waitForFuture(flowRepository.getTopNApplicationsSeries(N, step, includeOther, filters));

        final FlowSeriesResponse response = new FlowSeriesResponse();
        response.setStart(timeRangeFilter.getStart());
        response.setEnd(timeRangeFilter.getEnd());
        response.setColumns(series.rowKeySet().stream()
                .map(d -> {
                    final FlowSeriesColumn column = new FlowSeriesColumn();
                    column.setLabel(d.getValue());
                    column.setIngress(d.isIngress());
                    return column;
                })
                .collect(Collectors.toList()));
        populateResponseFromTable(series, response);
        return response;
    }

    @Override
    public FlowSummaryResponse getTopNConversations(int N, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);

        final List<TrafficSummary<Conversation>> summary =
                waitForFuture(flowRepository.getTopNConversations(N, filters));

        final FlowSummaryResponse response = new FlowSummaryResponse();
        response.setStart(timeRangeFilter.getStart());
        response.setEnd(timeRangeFilter.getEnd());
        response.setHeaders(Lists.newArrayList("Location", "Protocol", "Source IP", "Source Port",
                "Dest. IP", "Dest. Port", "Application", "Bytes In", "Bytes Out"));
        response.setRows(summary.stream()
                .map(sum -> {
                    final Conversation convo = sum.getEntity();
                    final ConversationKey key = convo.getKey();
                    return Lists.newArrayList((Object)key.getLocation(), key.getProtocol(),
                            key.getSrcIp(), key.getSrcPort(), key.getDstIp(), key.getDstPort(),
                            convo.getApplication(), sum.getBytesIn(), sum.getBytesOut());
                })
                .collect(Collectors.toList()));
        return response;
    }

    @Override
    public FlowSeriesResponse getTopNConversationsSeries(long step, int N, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);
        final Table<Directional<Conversation>, Long, Double> series =
                waitForFuture(flowRepository.getTopNConversationsSeries(N, step, filters));

        final FlowSeriesResponse response = new FlowSeriesResponse();
        response.setStart(timeRangeFilter.getStart());
        response.setEnd(timeRangeFilter.getEnd());
        response.setColumns(series.rowKeySet().stream()
                .map(d -> {
                    final Conversation convo = d.getValue();
                    final ConversationKey key = convo.getKey();
                    final String applicationTag = convo.getApplication() != null ? String.format(" [%s]", convo.getApplication()) : "";
                    final FlowSeriesColumn column = new FlowSeriesColumn();
                    column.setLabel(String.format("%s:%d <-> %s:%d%s", key.getSrcIp(), key.getSrcPort(),
                            key.getDstIp(), key.getDstPort(), applicationTag));
                    column.setIngress(d.isIngress());
                    return column;
                })
                .collect(Collectors.toList()));
        populateResponseFromTable(series, response);
        return response;
    }

    protected static List<Filter> getFiltersFromQueryString(MultivaluedMap<String, String> queryParams) {
        final List<Filter> filters = new ArrayList<>();

        final String start = queryParams.getFirst("start");
        long startMs;
        if (start != null) {
            startMs = Long.parseLong(start);
        } else {
            // 4 hours ago
            startMs = -TimeUnit.HOURS.toMillis(4);
        }

        final String end = queryParams.getFirst("end");
        long endMs;
        if (end != null) {
            endMs = Long.parseLong(end);
        } else {
            // Now
            endMs = System.currentTimeMillis();
        }
        endMs = getEffectiveEnd(endMs);
        startMs = getEffectiveStart(startMs, endMs);
        filters.add(new TimeRangeFilter(startMs, endMs));

        final String ifIndexStr = queryParams.getFirst("ifIndex");
        if (ifIndexStr != null) {
            int ifIndex = Integer.parseInt(ifIndexStr);
            filters.add(new SnmpInterfaceIdFilter(ifIndex));
        }

        final String exporterNodeCriteria = queryParams.getFirst("exporterNode");
        if (exporterNodeCriteria != null) {
            try {
                filters.add(new ExporterNodeFilter(new NodeCriteria(exporterNodeCriteria)));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid node criteria: " + exporterNodeCriteria);
            }
        }

        return filters;
    }

    private static TimeRangeFilter getRequiredTimeRangeFilter(Collection<Filter> filters) {
        final Optional<TimeRangeFilter> filter = filters.stream()
                .filter(f -> f instanceof TimeRangeFilter)
                .map(f -> (TimeRangeFilter)f)
                .findFirst();
        if (!filter.isPresent()) {
            throw new BadRequestException("Time range is required.");
        }
        return filter.get();
    }

    private static long getEffectiveStart(long start, long effectiveEnd) {
        // If start is negative, subtract it from the end
        long effectiveStart = start >= 0 ? start : effectiveEnd + start;
        // Make sure the resulting start time is not negative
        effectiveStart = Math.max(effectiveStart, 0);
        return effectiveStart;
    }

    private static long getEffectiveEnd(long end) {
        // If end is not strictly positive, use the current timestamp
        return end > 0 ? end : new Date().getTime();
    }

    private static <T> T waitForFuture(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException|ExecutionException e) {
            throw new WebApplicationException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    private static void populateResponseFromTable(Table<?, Long, Double> table, FlowSeriesResponse response) {
        final List<Long> timestamps = table.columnKeySet().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        final List<List<Double>> values = new LinkedList<>();
        for (Object rowKey : table.rowKeySet()) {
            final List<Double> column = new ArrayList<>(timestamps.size());
            for (Long ts : timestamps) {
                Double val = table.get(rowKey, ts);
                if (val == null) {
                    val = Double.NaN;
                }
                column.add(val);
            }
            values.add(column);
        }

        response.setTimestamps(timestamps);
        response.setValues(values);
    }

    @Override
    public FlowGraphUrlInfo getFlowGraphUrlInfo(UriInfo uriInfo) {

        if (Strings.isNullOrEmpty(flowGraphUrl)) {
            return null;
        }

        long flowCount = waitForFuture(
                flowRepository.getFlowCount(getFiltersFromQueryString(uriInfo.getQueryParameters())));
        FlowGraphUrlInfo graphUrlInfo = new FlowGraphUrlInfo();

        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String flowUrl = getFlowGraphUrl();
        final String formattedGraphUrl = flowUrl.replaceAll("\\$nodeId", queryParams.getFirst("exporterNode"))
                .replaceAll("\\$ifIndex", queryParams.getFirst("ifIndex"))
                .replaceAll("\\$start", queryParams.getFirst("start"))
                .replaceAll("\\$end", queryParams.getFirst("end"));
        graphUrlInfo.setFlowGraphUrl(formattedGraphUrl);
        graphUrlInfo.setFlowCount(flowCount);
        return graphUrlInfo;
    }

    public String getFlowGraphUrl() {
        return flowGraphUrl;
    }

    public void setFlowGraphUrl(String flowGraphUrl) {
        this.flowGraphUrl = flowGraphUrl;
    }

}
