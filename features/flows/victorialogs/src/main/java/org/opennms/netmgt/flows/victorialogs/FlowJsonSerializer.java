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

package org.opennms.netmgt.flows.victorialogs;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;

import org.opennms.integration.api.v1.flows.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swrve.ratelimitedlogger.RateLimitedLog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Renders {@link Flow} records as the newline-delimited JSON accepted by VictoriaLogs'
 * {@code /insert/jsonline} endpoint.
 *
 * <p><strong>Wire compatibility.</strong> The field names emitted here are exactly the ones the
 * Elasticsearch flow document uses — literal dot-separated keys such as {@code netflow.bytes}.
 * Elasticsearch interprets those dots as a path into an object; VictoriaLogs simply stores a field
 * whose name contains dots. That coincidence is what lets both backends be fed from the same
 * serialized shape, which in turn is what makes an A/B comparison between them meaningful. Keeping
 * the two in step is the point, so {@code FlowJsonSerializerTest} pins the emitted key set. The two
 * deliberate additions on top of that shape are {@link #TIME_FIELD} and {@link #MSG_FIELD}, both of
 * which exist because VictoriaLogs' data model demands them.
 *
 * <p><strong>Timestamps.</strong> {@code @timestamp} stays an epoch-milli number for parity with the
 * Elasticsearch document. Relying on VictoriaLogs to infer the unit of a bare number would be a
 * gamble, so we additionally emit {@link #TIME_FIELD} as an explicit RFC3339 string and point
 * {@code _time_field} at that instead. The redundancy costs one small field and removes the entire
 * question.
 *
 * <p>Null values are omitted rather than emitted as JSON null, matching Gson's default treatment of
 * the Elasticsearch document and avoiding a pile of empty columns in VictoriaLogs.
 */
public class FlowJsonSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(FlowJsonSerializer.class);

    /** A malformed exporter produces one failure per flow, which at exporter rates floods the log. */
    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    /** Unambiguous RFC3339 timestamp; this is what {@code _time_field} should point at. */
    public static final String TIME_FIELD = "_time";

    /**
     * VictoriaLogs' mandatory message field. Every log entry must carry one; a document without it
     * is stored with the literal placeholder {@code "missing _msg field; see ..."} as its message,
     * which is what a reader browsing the raw logs then sees for every single flow. The
     * Elasticsearch document has no message concept, so this is a VictoriaLogs-only addition rather
     * than part of the shared shape.
     */
    public static final String MSG_FIELD = "_msg";

    /** Schema version carried by the Elasticsearch flow document; mirrored for parity. */
    private static final int DOCUMENT_VERSION = 1;

    private static final DateTimeFormatter RFC3339 = DateTimeFormatter.ISO_INSTANT;

    public String toJsonLine(final Flow flow) {
        return toJsonObject(flow).toString();
    }

    /**
     * Renders a batch as a newline-delimited JSON body. A trailing newline is included: VictoriaLogs
     * parses line by line and a final unterminated line is needlessly ambiguous.
     *
     * <p>A flow that cannot be rendered is skipped rather than allowed to abort the batch. The whole
     * point of the wire format is that each line stands alone, so one unusable record has no business
     * costing the other nine hundred and ninety-nine their delivery — which is what happened when
     * serialization ran inside the caller's send: the exception surfaced as a failed send and the
     * entire chunk was counted as dropped.
     *
     * @param skipped incremented once per flow that could not be rendered, so the caller can account
     *                for the difference between what it handed over and what went out
     */
    public String toNdJson(final Collection<? extends Flow> flows, final AtomicInteger skipped) {
        final StringBuilder sb = new StringBuilder();
        for (final Flow flow : flows) {
            final String line;
            try {
                line = toJsonLine(flow);
            } catch (final RuntimeException notRenderable) {
                skipped.incrementAndGet();
                RATE_LIMITED_LOG.warn("Skipping a flow that could not be serialized; the rest of "
                        + "the batch is unaffected.", notRenderable);
                continue;
            }
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    /** @see #toNdJson(Collection, AtomicInteger) */
    public String toNdJson(final Collection<? extends Flow> flows) {
        return toNdJson(flows, new AtomicInteger());
    }

    JsonObject toJsonObject(final Flow flow) {
        final JsonObject doc = new JsonObject();

        final Instant timestamp = flow.getTimestamp();
        doc.addProperty("@timestamp", timestamp != null ? timestamp.toEpochMilli() : 0L);
        doc.addProperty(TIME_FIELD, RFC3339.format(timestamp != null ? timestamp : Instant.EPOCH));
        doc.addProperty("@clock_correction",
                flow.getClockCorrection() != null ? flow.getClockCorrection().toMillis() : 0L);
        doc.addProperty("@version", DOCUMENT_VERSION);
        doc.addProperty(MSG_FIELD, message(flow));

        addIfPresent(doc, "host", flow.getHost());
        addIfPresent(doc, "location", flow.getLocation());

        addIfPresent(doc, "netflow.application", flow.getApplication());
        addIfPresent(doc, "netflow.bytes", flow.getBytes());
        addIfPresent(doc, "netflow.convo_key", flow.getConvoKey());
        addIfPresent(doc, "netflow.direction", direction(flow.getDirection()));
        addIfPresent(doc, "netflow.dst_addr", flow.getDstAddr());
        flow.getDstAddrHostname().ifPresent(h -> doc.addProperty("netflow.dst_addr_hostname", h));
        addIfPresent(doc, "netflow.dst_as", flow.getDstAs());
        addIfPresent(doc, "netflow.dst_locality", locality(flow.getDstLocality()));
        addIfPresent(doc, "netflow.dst_mask_len", flow.getDstMaskLen());
        addIfPresent(doc, "netflow.dst_port", flow.getDstPort());
        addIfPresent(doc, "netflow.engine_id", flow.getEngineId());
        addIfPresent(doc, "netflow.engine_type", flow.getEngineType());
        doc.addProperty("netflow.first_switched", epochMilli(flow.getFirstSwitched()));
        addIfPresent(doc, "netflow.flow_locality", locality(flow.getFlowLocality()));
        doc.addProperty("netflow.flow_records", flow.getFlowRecords());
        doc.addProperty("netflow.flow_seq_num", flow.getFlowSeqNum());
        addIfPresent(doc, "netflow.input_snmp", flow.getInputSnmp());
        addIfPresent(doc, "netflow.ip_protocol_version", flow.getIpProtocolVersion());
        doc.addProperty("netflow.last_switched", epochMilli(flow.getLastSwitched()));
        addIfPresent(doc, "netflow.next_hop", flow.getNextHop());
        flow.getNextHopHostname().ifPresent(h -> doc.addProperty("netflow.next_hop_hostname", h));
        addIfPresent(doc, "netflow.output_snmp", flow.getOutputSnmp());
        addIfPresent(doc, "netflow.packets", flow.getPackets());
        addIfPresent(doc, "netflow.protocol", flow.getProtocol());
        addIfPresent(doc, "netflow.sampling_algorithm", samplingAlgorithm(flow.getSamplingAlgorithm()));
        addIfPresent(doc, "netflow.sampling_interval", flow.getSamplingInterval());
        addIfPresent(doc, "netflow.src_addr", flow.getSrcAddr());
        flow.getSrcAddrHostname().ifPresent(h -> doc.addProperty("netflow.src_addr_hostname", h));
        addIfPresent(doc, "netflow.src_as", flow.getSrcAs());
        addIfPresent(doc, "netflow.src_locality", locality(flow.getSrcLocality()));
        addIfPresent(doc, "netflow.src_mask_len", flow.getSrcMaskLen());
        addIfPresent(doc, "netflow.src_port", flow.getSrcPort());
        addIfPresent(doc, "netflow.tcp_flags", flow.getTcpFlags());
        doc.addProperty("netflow.delta_switched", epochMilli(flow.getDeltaSwitched()));
        addIfPresent(doc, "netflow.tos", flow.getTos());
        addIfPresent(doc, "netflow.ecn", flow.getEcn());
        addIfPresent(doc, "netflow.dscp", flow.getDscp());
        addIfPresent(doc, "netflow.version", netflowVersion(flow.getNetflowVersion()));
        // Rendered unsigned and as a string, matching the Elasticsearch document.
        addIfPresent(doc, "netflow.vlan",
                flow.getVlan() != null ? Integer.toUnsignedString(flow.getVlan()) : null);

        addNode(doc, "node_src", flow.getSrcNodeInfo());
        addNode(doc, "node_dst", flow.getDstNodeInfo());
        addNode(doc, "node_exporter", flow.getExporterNodeInfo());

        addHosts(doc, flow);

        return doc;
    }

    /**
     * Renders the human-readable log line for {@link #MSG_FIELD}, for example
     * {@code "Netflow v9 ingress tcp 192.168.1.1:51000 -> 10.0.0.1:443 https 1000 bytes 10 packets"}.
     *
     * <p>Every part is optional — the queries all run against the structured fields, so this line is
     * purely for a human browsing raw logs — but the result is never empty: an all-null flow still
     * says {@code "flow"} rather than reintroducing the missing-message placeholder this field exists
     * to avoid.
     */
    static String message(final Flow flow) {
        final StringBuilder sb = new StringBuilder();
        appendPart(sb, netflowVersion(flow.getNetflowVersion()));
        appendPart(sb, direction(flow.getDirection()));
        appendPart(sb, protocolName(flow.getProtocol()));
        if (flow.getSrcAddr() != null && flow.getDstAddr() != null) {
            appendPart(sb, endpoint(flow.getSrcAddr(), flow.getSrcPort()) + " -> "
                    + endpoint(flow.getDstAddr(), flow.getDstPort()));
        }
        appendPart(sb, flow.getApplication());
        if (flow.getBytes() != null) {
            appendPart(sb, flow.getBytes() + " bytes");
        }
        if (flow.getPackets() != null) {
            appendPart(sb, flow.getPackets() + " packets");
        }
        return sb.length() > 0 ? sb.toString() : "flow";
    }

    private static void appendPart(final StringBuilder sb, final String part) {
        if (part == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(part);
    }

    private static String endpoint(final String addr, final Integer port) {
        return port != null ? addr + ":" + port : addr;
    }

    /** Names for the protocols a flow reader recognises at a glance; the rest stay numeric. */
    private static String protocolName(final Integer protocol) {
        if (protocol == null) {
            return null;
        }
        switch (protocol) {
            case 1:   return "icmp";
            case 2:   return "igmp";
            case 6:   return "tcp";
            case 17:  return "udp";
            case 47:  return "gre";
            case 50:  return "esp";
            case 58:  return "icmpv6";
            case 132: return "sctp";
            default:  return "proto " + protocol;
        }
    }

    /**
     * The set of addresses involved in the flow.
     *
     * <p>Destination before source, which looks arbitrary but is not: the Elasticsearch document
     * builds this set as a side effect of its address setters, and {@code FlowDocument.from()}
     * happens to call {@code setDstAddr} before {@code setSrcAddr}. Since the set is insertion
     * ordered, matching that order is what keeps the two documents byte-comparable.
     */
    private static void addHosts(final JsonObject doc, final Flow flow) {
        final Set<String> hosts = new LinkedHashSet<>();
        if (flow.getDstAddr() != null) {
            hosts.add(flow.getDstAddr());
        }
        if (flow.getSrcAddr() != null) {
            hosts.add(flow.getSrcAddr());
        }
        if (hosts.isEmpty()) {
            return;
        }
        final JsonArray array = new JsonArray();
        hosts.forEach(array::add);
        doc.add("hosts", array);
    }

    private static void addNode(final JsonObject doc, final String key, final Flow.NodeInfo info) {
        if (info == null) {
            return;
        }
        final JsonObject node = new JsonObject();
        addIfPresent(node, "foreign_source", info.getForeignSource());
        addIfPresent(node, "foreign_id", info.getForeignId());
        node.addProperty("node_id", info.getNodeId());
        node.addProperty("interface_id", info.getInterfaceId());
        // Emitted even when empty. NodeDocument initialises its list, so Gson writes "categories":[]
        // for a node with no categories, and omitting the key here would break the byte-comparability
        // with the Elasticsearch document that the rest of this class is built around. Only a null
        // list is treated as absent.
        final List<String> categories = info.getCategories();
        if (categories != null) {
            final JsonArray array = new JsonArray();
            categories.forEach(array::add);
            node.add("categories", array);
        }
        doc.add(key, node);
    }

    private static long epochMilli(final Instant instant) {
        return instant != null ? instant.toEpochMilli() : 0L;
    }

    private static void addIfPresent(final JsonObject doc, final String key, final String value) {
        if (value != null) {
            doc.addProperty(key, value);
        }
    }

    /**
     * Adds a number, omitting it when there is nothing usable to add.
     *
     * <p>Non-finite values are omitted along with null, because Gson would emit them literally.
     * {@code JsonElement.toString()} writes through a lenient {@code JsonWriter}, so a NaN produces
     * {@code {"netflow.sampling_interval":NaN}} — a bare token that is not JSON at all. VictoriaLogs
     * cannot parse that line, skips it, and still answers 2xx, so the flow is lost silently and
     * counted as sent. {@code Flow.getSamplingInterval()} returns a boxed {@code Double} taken from
     * an exporter-supplied protobuf value, so NaN and the infinities are representable inputs rather
     * than a hypothetical.
     *
     * <p>Omitting matches how a null is treated: the field is simply absent, which is a state the
     * readers already handle.
     */
    private static void addIfPresent(final JsonObject doc, final String key, final Number value) {
        if (value == null) {
            return;
        }
        if (value instanceof Double && !Double.isFinite((Double) value)) {
            return;
        }
        if (value instanceof Float && !Float.isFinite((Float) value)) {
            return;
        }
        doc.addProperty(key, value);
    }

    // The wire values below are the ones the Elasticsearch document declares via @SerializedName.
    // They are spelled out rather than derived from Enum.name() because several of them differ from
    // the constant names on either side -- Flow.SamplingAlgorithm.RandomNOutOfNSampling is written
    // "RandomNoutOfNSampling" on the wire, for instance.

    static String direction(final Flow.Direction direction) {
        if (direction == null) {
            return null;
        }
        switch (direction) {
            case INGRESS: return "ingress";
            case EGRESS:  return "egress";
            case UNKNOWN: return "unknown";
            default:      return null;
        }
    }

    static String locality(final Flow.Locality locality) {
        if (locality == null) {
            return null;
        }
        switch (locality) {
            case PUBLIC:  return "public";
            case PRIVATE: return "private";
            default:      return null;
        }
    }

    static String netflowVersion(final Flow.NetflowVersion version) {
        if (version == null) {
            return null;
        }
        switch (version) {
            case V5:    return "Netflow v5";
            case V9:    return "Netflow v9";
            case IPFIX: return "IPFIX";
            case SFLOW: return "SFLOW";
            default:    return null;
        }
    }

    static String samplingAlgorithm(final Flow.SamplingAlgorithm algorithm) {
        if (algorithm == null) {
            return null;
        }
        switch (algorithm) {
            case Unassigned:                    return "Unassigned";
            case SystematicCountBasedSampling:  return "SystematicCountBasedSampling";
            case SystematicTimeBasedSampling:   return "SystematicTimeBasedSampling";
            case RandomNOutOfNSampling:         return "RandomNoutOfNSampling";
            case UniformProbabilisticSampling:  return "UniformProbabilisticSampling";
            case PropertyMatchFiltering:        return "PropertyMatchFiltering";
            case HashBasedFiltering:            return "HashBasedFiltering";
            case FlowStateDependentIntermediateFlowSelectionProcess:
                return "FlowStateDependentIntermediateFlowSelectionProcess";
            default:                            return null;
        }
    }
}
