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

import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsSnmpInterface;
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
 * Summaries of SNMP interfaces: state from the snmpinterface row, rates
 * averaged from the collected counters.
 */
@Component
@Path("snmpinterfaces/metrics")
@Tag(name = "SnmpInterfaces", description = "SNMP Interfaces API")
@Produces(MediaType.APPLICATION_JSON)
@Transactional(readOnly = true)
public class SnmpInterfaceMetricsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpInterfaceMetricsRestService.class);

    // Counters arrive on the collection interval (300 s by default), so every
    // value is an average over the window, never an instantaneous rate.
    private static final int DEFAULT_WINDOW_SECONDS = 900;
    private static final int MIN_WINDOW_SECONDS = 300;
    private static final int MAX_WINDOW_SECONDS = 86_400;
    private static final long STEP_MS = 300_000L;

    // Counters read, not interfaces: all four groups read twelve per interface.
    private static final int MAX_SOURCES = 6_000;

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

    private static final String RESPONSE_EXAMPLE = "{\"windowSeconds\":900,\"end\":1790094081952,\"interfaces\":[{"
            + "\"nodeId\":286,\"ifIndex\":2,\"ifAdminStatus\":1,\"ifOperStatus\":1,\"speedBps\":1000000000,"
            + "\"inBitsPerSecond\":619445470.0,\"outBitsPerSecond\":39964235.3,"
            + "\"inUtilizationPercent\":61.9,\"outUtilizationPercent\":4.0,"
            + "\"inPacketsPerSecond\":77432.8,\"inUnicastPacketsPerSecond\":77432.8,"
            + "\"outPacketsPerSecond\":4995.9,\"outUnicastPacketsPerSecond\":4995.9,"
            + "\"inErrorsPerSecond\":0.0,\"outErrorsPerSecond\":0.0,\"inDiscardsPerSecond\":0.0,\"outDiscardsPerSecond\":0.0}]}";

    // Optional: without it the answer is database state only.
    @Autowired(required = false)
    private MeasurementsService m_measurementsService;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @GET
    @Path("{nodeId}/{ifIndex}")
    @Operation(summary = "Summary of one SNMP interface",
            description = "State and averaged counters for the interface with this ifIndex on this node",
            operationId = "SnmpInterfaceMetricsGet", tags = {"SnmpInterfaces"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One summary per distinct interface, in request order.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpInterfaceMetricsResponseDTO.class),
                            examples = @ExampleObject(value = RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "A metric group is unknown, or the request needs more than 6000 counters.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown metric group 'latency'; expected any of [traffic, packets, errors, discards]")))
    })
    public SnmpInterfaceMetricsResponseDTO getOne(
            @PathParam("nodeId") final int nodeId,
            @PathParam("ifIndex") final int ifIndex,
            @Parameter(description = "Seconds to average over; 300 to 86400, 900 by default")
            @QueryParam("windowSeconds") final Integer windowSeconds,
            @Parameter(description = "Counter groups to read: traffic, packets, errors, discards; all by default")
            @QueryParam("metrics") final List<String> metrics) {
        final SnmpInterfaceMetricsRequestDTO request = new SnmpInterfaceMetricsRequestDTO();
        request.setInterfaces(List.of(new SnmpInterfaceRefDTO(nodeId, ifIndex)));
        request.setWindowSeconds(windowSeconds);
        request.setMetrics(metrics);
        return getMany(request);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Summary of many SNMP interfaces",
            description = "State and averaged counters for each requested (node, ifIndex), in one request",
            operationId = "SnmpInterfaceMetricsQuery", tags = {"SnmpInterfaces"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One summary per distinct interface, in request order.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpInterfaceMetricsResponseDTO.class),
                            examples = @ExampleObject(value = RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "A metric group is unknown, or the request needs more than 6000 counters.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown metric group 'latency'; expected any of [traffic, packets, errors, discards]")))
    })
    public SnmpInterfaceMetricsResponseDTO getMany(final SnmpInterfaceMetricsRequestDTO request) {
        final int windowSeconds = windowSeconds(request == null ? null : request.getWindowSeconds());
        final long end = System.currentTimeMillis();
        if (request == null || request.getInterfaces().isEmpty()) {
            return new SnmpInterfaceMetricsResponseDTO(windowSeconds, end, List.of());
        }
        // An interface named twice is read once; request order is kept.
        final Set<String> seen = new LinkedHashSet<>();
        final List<SnmpInterfaceRefDTO> refs = new ArrayList<>();
        for (final SnmpInterfaceRefDTO ref : request.getInterfaces()) {
            if (ref.getNodeId() == null || ref.getIfIndex() == null) {
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

        final List<SnmpInterfaceMetricsDTO> results = new ArrayList<>(refs.size());
        for (final SnmpInterfaceRefDTO ref : refs) {
            results.add(fromDatabase(ref));
        }
        if (m_measurementsService != null && !refs.isEmpty()) {
            final QueryResponse response = query(refs, groups, end, windowSeconds * 1000L);
            if (response != null) {
                overlay(results, response);
            }
        }
        return new SnmpInterfaceMetricsResponseDTO(windowSeconds, end, results);
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

    /** The provisioned speed stands until the collected ifHighSpeed overrides it. */
    private SnmpInterfaceMetricsDTO fromDatabase(final SnmpInterfaceRefDTO ref) {
        final SnmpInterfaceMetricsDTO dto = new SnmpInterfaceMetricsDTO();
        dto.setNodeId(ref.getNodeId());
        dto.setIfIndex(ref.getIfIndex());
        final OnmsSnmpInterface snmpInterface =
                m_snmpInterfaceDao.findByNodeIdAndIfIndex(ref.getNodeId(), ref.getIfIndex());
        if (snmpInterface != null) {
            dto.setIfAdminStatus(snmpInterface.getIfAdminStatus());
            dto.setIfOperStatus(snmpInterface.getIfOperStatus());
            final Long ifSpeed = snmpInterface.getIfSpeed();
            // 4294967295 is the 32-bit ifSpeed ceiling: "faster than this", not a speed.
            dto.setSpeedBps(ifSpeed == null || ifSpeed <= 0 || ifSpeed == IF_SPEED_CEILING ? null : ifSpeed);
        }
        return dto;
    }

    private QueryResponse query(final List<SnmpInterfaceRefDTO> refs, final List<String> groups,
                                final long end, final long windowMs) {
        final QueryRequest query = new QueryRequest();
        // Relaxed, so one uncollected interface does not fail the whole batch.
        query.setRelaxed(true);
        query.setStart(end - windowMs);
        query.setEnd(end);
        query.setStep(STEP_MS);

        final List<Source> sources = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) {
            final String resourceId = resourceId(refs.get(i));
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
            // Identity and state still answer; only the rates are missing.
            LOG.warn("Interface metrics query failed for {} interfaces", refs.size(), e);
            return null;
        }
    }

    private static void overlay(final List<SnmpInterfaceMetricsDTO> results, final QueryResponse response) {
        final Map<String, Integer> columns = new LinkedHashMap<>();
        final String[] labels = response.getLabels();
        for (int i = 0; labels != null && i < labels.length; i++) {
            columns.put(labels[i], i);
        }
        for (int i = 0; i < results.size(); i++) {
            final SnmpInterfaceMetricsDTO dto = results.get(i);

            // Constants are keyed by source label, as MeasurementsWrapper reads them.
            final Double speedMbps = constant(response, label(i, "in") + ".ifHighSpeed",
                    label(i, "out") + ".ifHighSpeed");
            if (speedMbps != null && speedMbps > 0) {
                dto.setSpeedBps((long) (speedMbps * 1_000_000d));
            }
            final Long speedBps = dto.getSpeedBps();

            final Double inOctets = lastValue(response, columns, label(i, "in"));
            final Double outOctets = lastValue(response, columns, label(i, "out"));
            dto.setInBitsPerSecond(bits(inOctets));
            dto.setOutBitsPerSecond(bits(outOctets));
            dto.setInUtilizationPercent(percent(bits(inOctets), speedBps));
            dto.setOutUtilizationPercent(percent(bits(outOctets), speedBps));
            dto.setInUnicastPacketsPerSecond(lastValue(response, columns, label(i, "pin")));
            dto.setInMulticastPacketsPerSecond(lastValue(response, columns, label(i, "pmin")));
            dto.setInBroadcastPacketsPerSecond(lastValue(response, columns, label(i, "pbin")));
            dto.setInPacketsPerSecond(sum(dto.getInUnicastPacketsPerSecond(),
                    dto.getInMulticastPacketsPerSecond(), dto.getInBroadcastPacketsPerSecond()));
            dto.setOutUnicastPacketsPerSecond(lastValue(response, columns, label(i, "pout")));
            dto.setOutMulticastPacketsPerSecond(lastValue(response, columns, label(i, "pmout")));
            dto.setOutBroadcastPacketsPerSecond(lastValue(response, columns, label(i, "pbout")));
            dto.setOutPacketsPerSecond(sum(dto.getOutUnicastPacketsPerSecond(),
                    dto.getOutMulticastPacketsPerSecond(), dto.getOutBroadcastPacketsPerSecond()));
            dto.setInErrorsPerSecond(lastValue(response, columns, label(i, "ein")));
            dto.setOutErrorsPerSecond(lastValue(response, columns, label(i, "eout")));
            dto.setInDiscardsPerSecond(lastValue(response, columns, label(i, "din")));
            dto.setOutDiscardsPerSecond(lastValue(response, columns, label(i, "dout")));
        }
    }

    /** The most recent sample that is not NaN; RRD leaves the tail unfilled. */
    private static Double lastValue(final QueryResponse response,
                                    final Map<String, Integer> columns,
                                    final String label) {
        final Integer index = columns.get(label);
        if (index == null || response.getColumns() == null || index >= response.getColumns().length) {
            return null;
        }
        final double[] values = response.getColumns()[index].getList();
        for (int i = values.length - 1; i >= 0; i--) {
            if (!Double.isNaN(values[i])) {
                return values[i];
            }
        }
        return null;
    }

    private static Double constant(final QueryResponse response, final String... keys) {
        if (response.getConstants() == null) {
            return null;
        }
        for (final String key : keys) {
            for (final QueryResponse.QueryConstant constant : response.getConstants()) {
                if (key.equals(constant.getKey()) && constant.getValue() != null) {
                    try {
                        return Double.parseDouble(constant.getValue());
                    } catch (final NumberFormatException ignored) {
                        // try the other direction's copy
                    }
                }
            }
        }
        return null;
    }

    /** The total of whichever classes were collected; absent when none were. */
    private static Double sum(final Double... parts) {
        Double total = null;
        for (final Double part : parts) {
            if (part != null) {
                total = total == null ? part : total + part;
            }
        }
        return total;
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
        if (fallback != null) {
            // Errors and discards have no 64-bit form.
            source.setFallbackAttribute(fallback);
        }
        source.setAggregation("AVERAGE");
        source.setTransient(false);
        return source;
    }

    /** interfaceSnmp would need the ifName-and-MAC label; byIfIndex needs only the ifIndex. */
    private static String resourceId(final SnmpInterfaceRefDTO ref) {
        return "node[" + ref.getNodeId() + "].interfaceSnmpByIfIndex[" + ref.getIfIndex() + "]";
    }

    private static String label(final int index, final String counter) {
        return "i" + index + counter;
    }

    private static int windowSeconds(final Integer requested) {
        return requested == null
                ? DEFAULT_WINDOW_SECONDS
                : Math.min(Math.max(requested, MIN_WINDOW_SECONDS), MAX_WINDOW_SECONDS);
    }

    private static WebApplicationException webException(final Response.Status status, final String message) {
        return new WebApplicationException(
                Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
