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
package org.opennms.web.rest.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsRequestDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceMetricsResponseDTO;
import org.opennms.web.rest.model.v2.SnmpInterfaceRefDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Summaries of SNMP interfaces at an instant: state from the snmpinterface
 * row, rates from the latest stored sample at or before that instant.
 */
@Component
@Path("snmpinterfaces/metrics")
@Tag(name = "SnmpInterfaces", description = "SNMP Interfaces API")
@Produces(MediaType.APPLICATION_JSON)
@Transactional(readOnly = true)
public class SnmpInterfaceMetricsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpInterfaceMetricsRestService.class);

    // How far before the instant to search for a sample. Counters arrive on the
    // collection interval (300 s by default); an hour is already a stale reading.
    private static final int DEFAULT_LOOKBACK_SECONDS = 900;
    private static final int MIN_LOOKBACK_SECONDS = 300;
    private static final int MAX_LOOKBACK_SECONDS = 3_600;
    private static final long STEP_MS = 300_000L;

    // Counters read, not interfaces: 3000 links, both ends, traffic. All four
    // groups read twelve counters per interface where traffic alone reads two.
    private static final int MAX_SOURCES = 12_000;

    // Sources per store query. The jrrd2 native xport crashes the JVM somewhere
    // above 5200 DEFs in one call, and nothing above this is faster anyway.
    private static final int SOURCES_PER_QUERY = 1_000;

    private static final long IF_SPEED_CEILING = 4_294_967_295L;

    // {label, 64-bit attribute, 32-bit fallback}. Aliases are spelled exactly as
    // the stock datacollection stores them, truncation and case included.
    private static final Map<String, String[][]> METRIC_GROUPS = Map.of(
            "traffic", new String[][] {
                    { "in", "ifHCInOctets", "ifInOctets" },
                    { "out", "ifHCOutOctets", "ifOutOctets" } },
            "packets", new String[][] {
                    { "pin", "ifHCInUcastPkts", "ifInUcastpkts" },
                    { "pmin", "ifHCInMulticastPkts", null },
                    { "pbin", "ifHCInBroadcastPkts", null },
                    { "pout", "ifHCOutUcastPkts", "ifOutUcastPkts" },
                    { "pmout", "ifHCOutMulticastPkt", null },
                    { "pbout", "ifHCOutBroadcastPkt", null } },
            "errors", new String[][] {
                    { "ein", "ifInErrors", null },
                    { "eout", "ifOutErrors", null } },
            "discards", new String[][] {
                    { "din", "ifInDiscards", null },
                    { "dout", "ifOutDiscards", null } });

    private static final List<String> ALL_GROUPS = List.of("traffic", "packets", "errors", "discards");

    private static final String RESPONSE_EXAMPLE = "{\"at\":1790094081952,\"lookbackSeconds\":900,\"stepSeconds\":300,\"interfaces\":[{"
            + "\"nodeId\":286,\"ifIndex\":2,\"ifAdminStatus\":1,\"ifOperStatus\":1,\"sampledAt\":1790093700000,\"speedBps\":1000000000,"
            + "\"inBitsPerSecond\":619445470.0,\"outBitsPerSecond\":39964235.3,"
            + "\"inUtilizationPercent\":61.9,\"outUtilizationPercent\":4.0,"
            + "\"inUnicastPacketsPerSecond\":77432.8,\"outUnicastPacketsPerSecond\":4995.9,"
            + "\"inErrorsPerSecond\":0.0,\"outErrorsPerSecond\":0.0,\"inDiscardsPerSecond\":0.0,\"outDiscardsPerSecond\":0.0}]}";

    // Optional: without it the answer is database state only.
    @Autowired(required = false)
    private MeasurementsService m_measurementsService;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @GET
    @Path("{nodeId}/{ifIndex}")
    @Operation(summary = "Summary of one SNMP interface",
            description = "State and the latest sampled counters for the interface with this ifIndex on this node",
            operationId = "SnmpInterfaceMetricsGet", tags = {"SnmpInterfaces"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One summary per distinct interface, in request order.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpInterfaceMetricsResponseDTO.class),
                            examples = @ExampleObject(value = RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "A metric group is unknown.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown metric group 'latency'; expected any of [traffic, packets, errors, discards]"))),
            @ApiResponse(responseCode = "404", description = "The node has no SNMP interface with that ifIndex.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No SNMP interface with ifIndex 99 on node 286"))),
            @ApiResponse(responseCode = "503", description = "The measurements store could not answer; nothing is returned rather than a summary with no rates.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The measurements store could not answer: rrdtool exited with status 1")))
    })
    public SnmpInterfaceMetricsResponseDTO getOne(
            @PathParam("nodeId") final int nodeId,
            @PathParam("ifIndex") final int ifIndex,
            @Parameter(description = "Instant to summarize, epoch milliseconds; now by default")
            @QueryParam("at") final Long at,
            @Parameter(description = "Seconds before the instant to search for a sample; 300 to 3600, 900 by default")
            @QueryParam("lookbackSeconds") final Integer lookbackSeconds,
            @Parameter(description = "Counter groups to read: traffic, packets, errors, discards; all by default")
            @QueryParam("metrics") final List<String> metrics) {
        final SnmpInterfaceMetricsRequestDTO request = new SnmpInterfaceMetricsRequestDTO();
        request.setInterfaces(List.of(new SnmpInterfaceRefDTO(nodeId, ifIndex)));
        request.setAt(at);
        request.setLookbackSeconds(lookbackSeconds);
        request.setMetrics(metrics);
        final SnmpInterfaceMetricsResponseDTO response = getMany(request);
        if (response.getInterfaces().isEmpty()) {
            throw webException(Response.Status.NOT_FOUND,
                    "No SNMP interface with ifIndex " + ifIndex + " on node " + nodeId);
        }
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Summary of many SNMP interfaces",
            description = "State and the latest sampled counters for each requested (node, ifIndex), in one request",
            operationId = "SnmpInterfaceMetricsQuery", tags = {"SnmpInterfaces"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One summary per distinct known interface, in request order. An interface the database does not know is left out.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpInterfaceMetricsResponseDTO.class),
                            examples = @ExampleObject(value = RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "A metric group is unknown, or the request needs more than 12000 counters.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown metric group 'latency'; expected any of [traffic, packets, errors, discards]"))),
            @ApiResponse(responseCode = "503", description = "The measurements store could not answer; nothing is returned rather than a summary with no rates.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The measurements store could not answer: rrdtool exited with status 1")))
    })
    public SnmpInterfaceMetricsResponseDTO getMany(final SnmpInterfaceMetricsRequestDTO request) {
        final long now = System.currentTimeMillis();
        // A future instant has no samples; a client clock a little ahead still gets the latest.
        final boolean historical = request != null && request.getAt() != null && request.getAt() < now;
        final long at = historical ? request.getAt() : now;
        final int lookbackSeconds = lookbackSeconds(request == null ? null : request.getLookbackSeconds());
        if (request == null || request.getInterfaces().isEmpty()) {
            return new SnmpInterfaceMetricsResponseDTO(at, lookbackSeconds, null, List.of());
        }
        // An interface named twice is read once; request order is kept.
        final Set<String> seen = new LinkedHashSet<>();
        final List<SnmpInterfaceRefDTO> refs = new ArrayList<>();
        for (final SnmpInterfaceRefDTO ref : request.getInterfaces()) {
            if (ref == null || ref.getNodeId() == null || ref.getIfIndex() == null) {
                continue;
            }
            if (seen.add(ref.getNodeId() + ":" + ref.getIfIndex())) {
                refs.add(ref);
            }
        }
        final List<String> groups = groups(request.getMetrics());
        int sources = 0;
        for (final String group : groups) {
            sources += refs.size() * METRIC_GROUPS.get(group).length;
        }
        if (sources > MAX_SOURCES) {
            throw webException(Response.Status.BAD_REQUEST,
                    "At most " + MAX_SOURCES + " counters per request; " + refs.size()
                            + " interfaces across " + groups.size() + " metric groups need " + sources
                            + ". Ask for fewer interfaces, or name only the groups you need.");
        }

        // Unknown interfaces are left out rather than answered empty, so a
        // caller can tell "does not exist" from "not collected".
        final Map<String, OnmsSnmpInterface> rows = load(refs);
        final List<OnmsSnmpInterface> known = new ArrayList<>(refs.size());
        final List<SnmpInterfaceMetricsDTO> results = new ArrayList<>(refs.size());
        for (final SnmpInterfaceRefDTO ref : refs) {
            final OnmsSnmpInterface row = rows.get(ref.getNodeId() + ":" + ref.getIfIndex());
            if (row != null) {
                known.add(row);
                results.add(fromDatabase(ref, row, historical));
            }
        }
        Long stepSeconds = null;
        long lookbackMs = lookbackSeconds * 1000L;
        if (m_measurementsService != null && !known.isEmpty()) {
            // Whole interfaces per chunk, so every counter of one lands in the same query.
            final int perInterface = sources / refs.size();
            final int perChunk = Math.max(1, SOURCES_PER_QUERY / perInterface);
            for (int from = 0; from < known.size(); from += perChunk) {
                final int to = Math.min(known.size(), from + perChunk);
                QueryResponse response = query(known.subList(from, to), groups, at, lookbackMs);
                // Consolidated history has rows wider than the lookback, and the one
                // ending at or before the instant then lies outside it: widen to a step.
                if (response.getStep() > lookbackMs) {
                    lookbackMs = response.getStep();
                    response = query(known.subList(from, to), groups, at, lookbackMs);
                }
                overlay(results.subList(from, to), response, at);
                if (stepSeconds == null && response.getStep() > 0) {
                    stepSeconds = response.getStep() / 1000L;
                }
            }
        }
        return new SnmpInterfaceMetricsResponseDTO(at, (int) (lookbackMs / 1000L), stepSeconds, results);
    }

    /** Unknown names are refused, not ignored. */
    private static List<String> groups(final List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return ALL_GROUPS;
        }
        final List<String> groups = new ArrayList<>();
        for (final String name : requested) {
            final String key = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
            if (!METRIC_GROUPS.containsKey(key)) {
                throw webException(Response.Status.BAD_REQUEST,
                        "Unknown metric group '" + name + "'; expected any of " + ALL_GROUPS);
            }
            if (!groups.contains(key)) {
                groups.add(key);
            }
        }
        return groups;
    }

    // Rows per request in a few queries rather than one each. Exact (node,
    // ifIndex) pairs, not node IN x ifIndex IN, whose product could pull in
    // every port of every named switch.
    private static final int ROWS_PER_QUERY = 1_000;

    private Map<String, OnmsSnmpInterface> load(final List<SnmpInterfaceRefDTO> refs) {
        final Map<String, OnmsSnmpInterface> rows = new HashMap<>(refs.size());
        for (int from = 0; from < refs.size(); from += ROWS_PER_QUERY) {
            final List<SnmpInterfaceRefDTO> slice = refs.subList(from, Math.min(refs.size(), from + ROWS_PER_QUERY));
            final StringBuilder pairs = new StringBuilder("({alias}.nodeId, {alias}.snmpIfIndex) in (");
            for (int i = 0; i < slice.size(); i++) {
                // Integers straight from the parsed request; nothing here is a string.
                pairs.append(i == 0 ? "(" : ",(").append(slice.get(i).getNodeId().intValue())
                        .append(',').append(slice.get(i).getIfIndex().intValue()).append(')');
            }
            pairs.append(')');
            final CriteriaBuilder criteria = new CriteriaBuilder(OnmsSnmpInterface.class).sql(pairs.toString());
            for (final OnmsSnmpInterface row : m_snmpInterfaceDao.findMatching(criteria.toCriteria())) {
                rows.put(row.getNodeId() + ":" + row.getIfIndex(), row);
            }
        }
        return rows;
    }

    /** Status is the row as it is now, so a past instant carries none. */
    private static SnmpInterfaceMetricsDTO fromDatabase(final SnmpInterfaceRefDTO ref, final OnmsSnmpInterface snmpInterface,
                                                        final boolean historical) {
        final SnmpInterfaceMetricsDTO dto = new SnmpInterfaceMetricsDTO();
        dto.setNodeId(ref.getNodeId());
        dto.setIfIndex(ref.getIfIndex());
        if (!historical) {
            dto.setIfAdminStatus(snmpInterface.getIfAdminStatus());
            dto.setIfOperStatus(snmpInterface.getIfOperStatus());
        }
        final Long ifSpeed = snmpInterface.getIfSpeed();
        // 4294967295 is the 32-bit ifSpeed ceiling: "faster than this", not a speed.
        dto.setSpeedBps(ifSpeed == null || ifSpeed <= 0 || ifSpeed == IF_SPEED_CEILING ? null : ifSpeed);
        return dto;
    }

    private QueryResponse query(final List<OnmsSnmpInterface> rows, final List<String> groups,
                                final long at, final long lookbackMs) {
        final QueryRequest query = new QueryRequest();
        // Relaxed, so one uncollected interface does not fail the whole batch.
        query.setRelaxed(true);
        query.setStart(at - lookbackMs);
        query.setEnd(at);
        query.setStep(STEP_MS);

        final List<Source> sources = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            final String resourceId = resourceId(rows.get(i));
            for (final String group : groups) {
                for (final String[] counter : METRIC_GROUPS.get(group)) {
                    sources.add(source(label(i, counter[0]), resourceId, counter[1], counter[2]));
                }
            }
        }
        query.setSources(sources);

        try {
            return m_measurementsService.query(query);
        } catch (final MeasurementException e) {
            // A store that cannot answer is not "nothing collected"; the caller
            // is told, and the cause is in the measurements layer's own log.
            LOG.warn("Interface metrics query failed for {} interfaces: {}", rows.size(), e.toString());
            throw webException(Response.Status.SERVICE_UNAVAILABLE,
                    "The measurements store could not answer: " + e.getMessage());
        }
    }

    private static void overlay(final List<SnmpInterfaceMetricsDTO> results, final QueryResponse response,
                                final long at) {
        final Map<String, Integer> columns = new LinkedHashMap<>();
        final String[] labels = response.getLabels();
        for (int i = 0; labels != null && i < labels.length; i++) {
            columns.put(labels[i], i);
        }
        for (int i = 0; i < results.size(); i++) {
            final SnmpInterfaceMetricsDTO dto = results.get(i);

            // The provisioned speed is exact; the collected ifHighSpeed is a rounded
            // Mb/s integer and fills in only when the row has no usable speed.
            if (dto.getSpeedBps() == null) {
                final Double speedMbps = collectedSpeedMbps(response, i);
                if (speedMbps != null && speedMbps > 0) {
                    dto.setSpeedBps((long) (speedMbps * 1_000_000d));
                }
            }
            final Long speedBps = dto.getSpeedBps();

            // One row per interface, so every rate is from the same sample.
            final int row = latestRow(response, columns, i, at);
            if (row < 0) {
                continue;
            }
            dto.setSampledAt(response.getTimestamps()[row]);

            final Double inOctets = value(response, columns, label(i, "in"), row);
            final Double outOctets = value(response, columns, label(i, "out"), row);
            dto.setInBitsPerSecond(bits(inOctets));
            dto.setOutBitsPerSecond(bits(outOctets));
            dto.setInUtilizationPercent(percent(bits(inOctets), speedBps));
            dto.setOutUtilizationPercent(percent(bits(outOctets), speedBps));
            dto.setInUnicastPacketsPerSecond(value(response, columns, label(i, "pin"), row));
            dto.setInMulticastPacketsPerSecond(value(response, columns, label(i, "pmin"), row));
            dto.setInBroadcastPacketsPerSecond(value(response, columns, label(i, "pbin"), row));
            dto.setOutUnicastPacketsPerSecond(value(response, columns, label(i, "pout"), row));
            dto.setOutMulticastPacketsPerSecond(value(response, columns, label(i, "pmout"), row));
            dto.setOutBroadcastPacketsPerSecond(value(response, columns, label(i, "pbout"), row));
            dto.setInErrorsPerSecond(value(response, columns, label(i, "ein"), row));
            dto.setOutErrorsPerSecond(value(response, columns, label(i, "eout"), row));
            dto.setInDiscardsPerSecond(value(response, columns, label(i, "din"), row));
            dto.setOutDiscardsPerSecond(value(response, columns, label(i, "dout"), row));
        }
    }

    /**
     * The latest row stamped at or before the instant where any of the
     * interface's counters has a value; -1 when none does. A row's stamp is the
     * end of the interval it averages, so the store's last row can lie past 'at'.
     */
    private static int latestRow(final QueryResponse response, final Map<String, Integer> columns,
                                 final int index, final long at) {
        final long[] timestamps = response.getTimestamps();
        if (timestamps == null) {
            return -1;
        }
        final String prefix = "i" + index;
        int latest = -1;
        for (final Map.Entry<String, Integer> column : columns.entrySet()) {
            if (!column.getKey().startsWith(prefix) || Character.isDigit(column.getKey().charAt(prefix.length()))) {
                continue;
            }
            final double[] values = values(response, column.getValue());
            for (int row = Math.min(values.length, timestamps.length) - 1; row > latest; row--) {
                if (timestamps[row] <= at && !Double.isNaN(values[row])) {
                    latest = row;
                    break;
                }
            }
        }
        return latest;
    }

    private static Double value(final QueryResponse response, final Map<String, Integer> columns,
                                final String label, final int row) {
        final Integer index = columns.get(label);
        if (index == null) {
            return null;
        }
        final double[] values = values(response, index);
        return row < values.length && !Double.isNaN(values[row]) ? values[row] : null;
    }

    private static double[] values(final QueryResponse response, final int index) {
        if (response.getColumns() == null || index >= response.getColumns().length) {
            return new double[0];
        }
        return response.getColumns()[index].getList();
    }

    /**
     * ifHighSpeed arrives as a response constant keyed "<label>.ifHighSpeed" for
     * every source read from the interface, so any of the request's groups will do.
     */
    private static Double collectedSpeedMbps(final QueryResponse response, final int index) {
        if (response.getConstants() == null) {
            return null;
        }
        final String prefix = "i" + index;
        for (final QueryResponse.QueryConstant constant : response.getConstants()) {
            final String key = constant.getKey();
            if (key == null || !key.startsWith(prefix) || !key.endsWith(".ifHighSpeed")
                    || Character.isDigit(key.charAt(prefix.length())) || constant.getValue() == null) {
                continue;
            }
            try {
                return Double.parseDouble(constant.getValue());
            } catch (final NumberFormatException ignored) {
                // try another source's copy
            }
        }
        return null;
    }

    private static Double bits(final Double octetsPerSecond) {
        return octetsPerSecond == null ? null : octetsPerSecond * 8d;
    }

    /** Null rather than zero when the speed is unknown: zero would read as idle. */
    private static Double percent(final Double bitsPerSecond, final Long speedBps) {
        if (bitsPerSecond == null || speedBps == null || speedBps <= 0) {
            return null;
        }
        return bitsPerSecond / speedBps * 100d;
    }

    private static Source source(final String label, final String resourceId,
                                 final String attribute, final String fallback) {
        final Source source = new Source();
        source.setLabel(label);
        source.setResourceId(resourceId);
        source.setAttribute(attribute);
        // Only the octet and unicast counters have a 32-bit form to fall back to.
        if (fallback != null) {
            source.setFallbackAttribute(fallback);
        }
        source.setAggregation("AVERAGE");
        source.setTransient(false);
        return source;
    }

    // The label the collector filed the RRDs under, computed from the row as the
    // info panel does; byIfIndex would list the node's directory to find it.
    private static String resourceId(final OnmsSnmpInterface row) {
        final String label = row.computeLabelForRRD();
        return ResourceId.get("node", String.valueOf(row.getNodeId()))
                .resolve("interfaceSnmp", label == null ? "" : label).toString();
    }

    private static String label(final int index, final String counter) {
        return "i" + index + counter;
    }

    private static int lookbackSeconds(final Integer requested) {
        return requested == null
                ? DEFAULT_LOOKBACK_SECONDS
                : Math.min(Math.max(requested, MIN_LOOKBACK_SECONDS), MAX_LOOKBACK_SECONDS);
    }

    private static WebApplicationException webException(final Response.Status status, final String message) {
        return new WebApplicationException(
                Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
