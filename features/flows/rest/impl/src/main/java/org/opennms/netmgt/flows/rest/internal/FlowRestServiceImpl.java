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
package org.opennms.netmgt.flows.rest.internal;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.DscpFilter;
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class FlowRestServiceImpl implements FlowRestService {
    private final FlowQueryService flowQueryService;
    private final NodeDao nodeDao;
    private final SnmpInterfaceDao snmpInterfaceDao;
    private final SessionUtils sessionUtils;
    private String flowGraphUrl;

    public FlowRestServiceImpl(FlowQueryService flowQueryService, NodeDao nodeDao,
                               SnmpInterfaceDao snmpInterfaceDao, SessionUtils sessionUtils) {
        this.flowQueryService = Objects.requireNonNull(flowQueryService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public Long getFlowCount(UriInfo uriInfo) {
        return waitForFuture(flowQueryService.getFlowCount(getFiltersFromQueryString(uriInfo.getQueryParameters())));
    }

    @Override
    public List<FlowNodeSummary> getFlowExporters() {
        return sessionUtils.withReadOnlyTransaction(() -> this.nodeDao.findAllHavingFlows().stream())
                .map(n -> new FlowNodeSummary(n.getId(),
                        n.getForeignId(), n.getForeignSource(), n.getLabel(),
                        n.getCategories().stream().map(OnmsCategory::getName).collect(Collectors.toList())))
                .sorted(Comparator.comparingInt(FlowNodeSummary::getId))
                .collect(Collectors.toList());
    }

    @Override
    public FlowNodeDetails getFlowExporter(Integer nodeId) {
        final List<FlowSnmpInterface> ifaces = sessionUtils.withReadOnlyTransaction(() -> this.snmpInterfaceDao.findAllHavingFlows(nodeId)).stream()
                .map(iface -> new FlowSnmpInterface(iface.getIfIndex(),
                            iface.getIfName(),
                            iface.getIfAlias(),
                            iface.getIfDescr()))
                .collect(Collectors.toList());

        return new FlowNodeDetails(nodeId, ifaces);
    }

    private List<Integer> getFieldValues(LimitedCardinalityField field, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        return waitForFuture(flowQueryService.getFieldValues(field, filters))
                .stream()
                .map(Integer::valueOf)
                .sorted()
                .collect(Collectors.toList());
    }

    private FlowSummaryResponse getFieldSummaries(LimitedCardinalityField field, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);

        final FlowSummaryResponse response = new FlowSummaryResponse();
        response.setStart(timeRangeFilter.getStart());
        response.setEnd(timeRangeFilter.getEnd());

        final List<TrafficSummary<String>> summary = waitForFuture(flowQueryService.getFieldSummaries(field, filters));

        this.<String>defaultSummaryResponseConsumer(field.name(), Object::toString)
                .apply(response)
                .accept(summary);

        return response;
    }

    private FlowSeriesResponse getFieldSeries(LimitedCardinalityField field, long step, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);

        final FlowSeriesResponse response = new FlowSeriesResponse();
        response.setStart(timeRangeFilter.getStart());
        response.setEnd(timeRangeFilter.getEnd());

        final Table<Directional<String>, Long, Double> series = waitForFuture(flowQueryService.getFieldSeries(field, step, filters));

        this.<String>defaultSeriesReponseConsumer(Object::toString)
                .apply(response)
                .accept(series);

        populateResponseFromTable(series, response);

        return response;
    }

    @Override
    public List<Integer> getDscpValues(UriInfo uriInfo) {
        return getFieldValues(LimitedCardinalityField.DSCP, uriInfo);
    }

    @Override
    public FlowSummaryResponse getDscpSummaries(UriInfo uriInfo) {
        return getFieldSummaries(LimitedCardinalityField.DSCP, uriInfo);
    }

    @Override
    public FlowSeriesResponse getDscpSeries(long step, UriInfo uriInfo) {
        return getFieldSeries(LimitedCardinalityField.DSCP, step, uriInfo);
    }

    @Override
    public List<String> getApplications(String matchingPrefix, long limit, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        return waitForFuture(flowQueryService.getApplications(matchingPrefix, limit, filters));
    }

    @Override
    public FlowSummaryResponse getApplicationSummary(Integer N, Set<String> applications, boolean includeOther,
                                                     UriInfo uriInfo) {
        return getSummary(N, applications, uriInfo, "application",
                filters -> flowQueryService.getTopNApplicationSummaries(N, includeOther, filters),
                filters -> flowQueryService.getApplicationSummaries(applications, includeOther, filters),
                this.defaultSummaryResponseConsumer("Application", Function.identity()));
    }

    @Override
    public FlowSeriesResponse getApplicationSeries(long step, Integer N, Set<String> applications,
                                                   boolean includeOther, UriInfo uriInfo) {
        return getSeries(N, applications, uriInfo, "application",
                filters -> flowQueryService.getTopNApplicationSeries(N, step, includeOther, filters),
                filters -> flowQueryService.getApplicationSeries(applications, step, includeOther, filters),
                this.defaultSeriesReponseConsumer(Function.identity()));
    }

    @Override
    public List<String> getHosts(String regex, long limit, UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        return waitForFuture(flowQueryService.getHosts(regex, limit, filters));
    }

    @Override
    public FlowSummaryResponse getHostSummary(Integer N, Set<String> hosts, boolean includeOther, UriInfo uriInfo) {
        final HostnameMode hostnameMode = getHostnameModeFromQueryString(uriInfo.getQueryParameters());

        return getSummary(N, hosts, uriInfo, "host",
                filters -> flowQueryService.getTopNHostSummaries(N, includeOther, filters),
                filters -> flowQueryService.getHostSummaries(hosts, includeOther, filters),
                this.defaultSummaryResponseConsumer("Host", hostnameMode::buildDisplayName));
    }

    @Override
    public FlowSeriesResponse getHostSeries(long step, Integer N, Set<String> hosts, boolean includeOther,
                                            UriInfo uriInfo) {
        final HostnameMode hostnameMode = getHostnameModeFromQueryString(uriInfo.getQueryParameters());

        return getSeries(N, hosts, uriInfo, "host",
                filters -> flowQueryService.getTopNHostSeries(N, step, includeOther, filters),
                filters -> flowQueryService.getHostSeries(hosts, step, includeOther, filters),
                this.defaultSeriesReponseConsumer(hostnameMode::buildDisplayName));
    }

    @Override
    public List<String> getConversations(String locationPattern, String protocolPattern, String lowerIPPattern,
                                         String upperIPPattern, String applicationPattern, long limit,
                                         UriInfo uriInfo) {
        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        return waitForFuture(flowQueryService.getConversations(locationPattern, protocolPattern, lowerIPPattern,
                upperIPPattern, applicationPattern, limit, filters));
    }

    @Override
    public FlowSummaryResponse getConversationSummary(Integer N, Set<String> conversations, boolean includeOther,
                                                      UriInfo uriInfo) {
        final HostnameMode hostnameMode = getHostnameModeFromQueryString(uriInfo.getQueryParameters());

        return getSummary(N, conversations, uriInfo, "conversation",
                filters -> flowQueryService.getTopNConversationSummaries(N, includeOther, filters),
                filters -> flowQueryService.getConversationSummaries(conversations, includeOther, filters),
                response -> (summary) -> {
                    response.setHeaders(Lists.newArrayList("Location", "Protocol", "Source",
                            "Dest.", "Application", "Bytes In", "Bytes Out", "ECN"));
                    response.setRows(summary.stream()
                            .map(sum -> {
                                final Conversation conversation = sum.getEntity();
                                return Lists.newArrayList((Object) conversation.getLocation(), conversation.getProtocol(),
                                        hostnameMode.buildDisplayName(conversation.getLowerHost()),
                                        hostnameMode.buildDisplayName(conversation.getUpperHost()),
                                        conversation.getApplication(),
                                        sum.getBytesIn(), sum.getBytesOut(), sum.ecnInfo());
                            })
                            .collect(Collectors.toList()));
                });
    }

    @Override
    public FlowSeriesResponse getConversationSeries(long step, Integer N, Set<String> conversations,
                                                    boolean includeOther, UriInfo uriInfo) {
        final HostnameMode hostnameMode = getHostnameModeFromQueryString(uriInfo.getQueryParameters());

        return getSeries(N, conversations, uriInfo, "conversation",
                filters -> flowQueryService.getTopNConversationSeries(N, step, includeOther, filters),
                filters -> flowQueryService.getConversationSeries(conversations, step, includeOther, filters),
                response -> series ->
                    response.setColumns(series.rowKeySet().stream()
                            .map(d -> {
                                final Conversation conversation = d.getValue();
                                final String applicationTag = conversation.getApplication() != null ? String.format(" : %s", conversation.getApplication()) : "";
                                final FlowSeriesColumn column = new FlowSeriesColumn();
                                column.setLabel(String.format("%s <-> %s%s",
                                        hostnameMode.buildDisplayName(conversation.getLowerHost()),
                                        hostnameMode.buildDisplayName(conversation.getUpperHost()),
                                        applicationTag));
                                column.setIngress(d.isIngress());
                                return column;
                            })
                            .collect(Collectors.toList()))
                );
    }

    @Override
    public FlowGraphUrlInfo getFlowGraphUrlInfo(UriInfo uriInfo) {

        if (Strings.isNullOrEmpty(flowGraphUrl)) {
            return null;
        }

        long flowCount = waitForFuture(
                flowQueryService.getFlowCount(getFiltersFromQueryString(uriInfo.getQueryParameters())));
        FlowGraphUrlInfo graphUrlInfo = new FlowGraphUrlInfo();

        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String flowUrl = getFlowGraphUrl();
        final String formattedGraphUrl = flowUrl.replaceAll("\\$nodeId", getFirstNonNull(queryParams, "exporterNode"))
                .replaceAll("\\$ifIndex",  getFirstNonNull(queryParams, "ifIndex"))
                .replaceAll("\\$start",  getFirstNonNull(queryParams, "start"))
                .replaceAll("\\$end",  getFirstNonNull(queryParams, "end"));
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
    
    private void withValidationAndFilters(Integer N, Set<String> entities, String entitiesLabel, UriInfo uriInfo, BiConsumer<List<Filter>, TimeRangeFilter> filterConsumer) {
        validateNOrSetQueryParameters(N, entities, entitiesLabel);

        final List<Filter> filters = getFiltersFromQueryString(uriInfo.getQueryParameters());
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);
        
        filterConsumer.accept(filters, timeRangeFilter);
    }

    private <T> FlowSummaryResponse getSummary(Integer N, Set<String> entities, UriInfo uriInfo, String entitiesLabel,
                                               Function<List<Filter>, CompletableFuture<List<TrafficSummary<T>>>> topNSummaryProviderFunction,
                                               Function<List<Filter>, CompletableFuture<List<TrafficSummary<T>>>> specificEntitiesSummaryProviderFunction,
                                               Function<FlowSummaryResponse, Consumer<List<TrafficSummary<T>>>> responseConsumer) {
        final FlowSummaryResponse response = new FlowSummaryResponse();
        withValidationAndFilters(N, entities, entitiesLabel, uriInfo, (filters, timeRangeFilter) -> {
            final List<TrafficSummary<T>> summary;

            if (N != null) {
                summary = waitForFuture(topNSummaryProviderFunction.apply(filters));
            } else {
                summary = waitForFuture(specificEntitiesSummaryProviderFunction.apply(filters));
            }

            response.setStart(timeRangeFilter.getStart());
            response.setEnd(timeRangeFilter.getEnd());
            responseConsumer.apply(response).accept(summary);
        });
        return response;
    }

    private <T> FlowSeriesResponse getSeries(Integer N, Set<String> entities, UriInfo uriInfo, String entitiesLabel,
                                             Function<List<Filter>, CompletableFuture<Table<Directional<T>, Long,
                                                     Double>>> topNSeriesFutureFunction,
                                             Function<List<Filter>, CompletableFuture<Table<Directional<T>, Long,
                                                     Double>>> specificEntitiesSeriesFutureFunction,
                                             Function<FlowSeriesResponse, Consumer<Table<Directional<T>, Long, Double>>> seriesResponseConsumer) {
        final FlowSeriesResponse response = new FlowSeriesResponse();
        withValidationAndFilters(N, entities, entitiesLabel, uriInfo, (filters, timeRangeFilter) -> {
            final Table<Directional<T>, Long, Double> series;

            if (N != null) {
                series = waitForFuture(topNSeriesFutureFunction.apply(filters));
            } else {
                series = waitForFuture(specificEntitiesSeriesFutureFunction.apply(filters));
            }

            response.setStart(timeRangeFilter.getStart());
            response.setEnd(timeRangeFilter.getEnd());
            seriesResponseConsumer.apply(response).accept(series);
            populateResponseFromTable(series, response);
        });
        return response;
    }

    private <K> Function<FlowSummaryResponse, Consumer<List<TrafficSummary<K>>>> defaultSummaryResponseConsumer(
            final String entitiesHeader,
            final Function<K, String> entityLabel
    ) {
        return response -> summary -> {
            response.setHeaders(Lists.newArrayList(entitiesHeader, "Bytes In", "Bytes Out", "ECN"));
            response.setRows(
                    summary
                            .stream()
                            .map(sum -> Lists.newArrayList((Object) entityLabel.apply(sum.getEntity()), sum.getBytesIn(), sum.getBytesOut(), sum.ecnInfo())
                            )
                    .collect(Collectors.toList())
            );
        };
    }

    private <K> Function<FlowSeriesResponse, Consumer<Table<Directional<K>, Long, Double>>> defaultSeriesReponseConsumer(final Function<K, String> entityLabel) {
        return response -> series -> {
            response.setColumns(series.rowKeySet().stream()
                    .map(d -> {
                        final FlowSeriesColumn column = new FlowSeriesColumn();
                        column.setLabel(entityLabel.apply(d.getValue()));
                        column.setIngress(d.isIngress());
                        return column;
                    })
                    .collect(Collectors.toList()));
        };
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

        final List<String> dscpStr = queryParams.get("dscp");
        if (isNotEmpty(dscpStr)) {
            filters.add(new DscpFilter(dscpStr.stream().flatMap(str -> {
                Supplier<Stream<Integer>> s = DSCP_VALUE_SUPPLIER.get(str.toUpperCase());
                return s != null ? s.get() : Stream.of(Integer.parseInt(str));
            }).distinct().collect(Collectors.toList())));
        }

        return filters;
    }

    // translate symbolic DSCP names into corresponding numbers
    // (cf. https://en.wikipedia.org/wiki/Type_of_service#DSCP_and_ECN)
    // -> additionally support IP precedence groups
    //    (for P0, P5, and P6 more values are included than specified)
    private static Map<String, Supplier<Stream<Integer>>> DSCP_VALUE_SUPPLIER = new HashMap<String, Supplier<Stream<Integer>>>() {{
        for (int p = 0; p < 8; p++) {
            final int finalP = p;
            put("P" + finalP, () -> Stream.of(finalP * 8, finalP * 8 + 2, finalP * 8 + 4, finalP * 8 + 6));
        }
        for (int c = 0; c < 8; c++) {
            final int finalC = c;
            put("CS" + finalC, () -> Stream.of(finalC * 8));
            if (finalC >= 1 && finalC <= 4) {
                for (int d = 1; d <= 3; d++) {
                    final int finalD = d;
                    put(("AF" + finalC) + finalD, () -> Stream.of(finalC * 8 + finalD * 2));
                }
            }
        }
        put("LE", () -> Stream.of(1));
        put("EF", () -> Stream.of(46));
    }};

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

    enum HostnameMode {
        HIDE {
            @Override
            public String buildDisplayName(final Host host) {
                return host.getIp();
            }
        },

        APPEND {
            @Override
            public String buildDisplayName(final Host host) {
                return host.getHostname()
                        .map(name -> String.format("%s [%s]", host.getIp(), name))
                        .orElse(host.getIp());
            }
        },

        REPLACE {
            @Override
            public String buildDisplayName(final Host host) {
                return host.getHostname().orElse(host.getIp());
            }
        };

        public abstract String buildDisplayName(final Host host);
    }

    private static HostnameMode getHostnameModeFromQueryString(final MultivaluedMap<String, String> queryParams) {
        final String hostname_mode = Strings.nullToEmpty(queryParams.getFirst("hostname_mode")).toLowerCase();
        switch (hostname_mode) {
            case "hide":
                return HostnameMode.HIDE;
            case "append":
                return HostnameMode.APPEND;
            case "replace":
            default:
                return HostnameMode.REPLACE;
        }
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

    /**
     * Retrieve the first value from the map for the given key and convert this to a blank string
     * if the resulting value is null, or does not exist in the map.
     *
     * @param map multivalued map
     * @param key key to lookup
     * @return non-null string
     */
    private static String getFirstNonNull(MultivaluedMap<String, String> map, String key) {
        final String value = map.getFirst(key);
        return value != null ? value : "";
    }

    private static boolean isNullOrEmptyOrContainsNullOrEmpty(Collection<String> collection) {
        return collection == null || collection.isEmpty() || collection.contains(null) || collection.contains("");
    }

    private static void validateNOrSetQueryParameters(Integer N, Collection<String> collection, String collectionName) {
        if (N == null && isNullOrEmptyOrContainsNullOrEmpty(collection)) {
            // If neither the top N or the collection are set that is an error
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
                    .entity(String.format("One of 'N' or '%s' query parameters must be present", collectionName)).build());
        } else if (N != null && !isNullOrEmptyOrContainsNullOrEmpty(collection)) {
            // If both are set that is also an error
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
                    .entity(String.format("Only one of 'N' or '%s' query parameters should be set", collectionName)).build());
        }
    }
}
