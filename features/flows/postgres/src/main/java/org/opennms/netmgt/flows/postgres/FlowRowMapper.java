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
package org.opennms.netmgt.flows.postgres;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.integration.api.v1.flows.Flow;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Maps an enriched {@link Flow} to a {@link FlowRow}: promoted hot columns for querying, plus a
 * jsonb {@code document} whose keys mirror the Elasticsearch {@code netflow.*} field names so that
 * field-based queries ({@code LimitedCardinalityField}) and ad-hoc jsonb access use the same names.
 */
public class FlowRowMapper {

    private final ObjectMapper objectMapper;

    public FlowRowMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FlowRow toRow(final Flow flow) {
        final FlowRow r = new FlowRow();
        r.flowTs = toTs(flow.getTimestamp());
        r.deltaSwitched = toEpochMilli(flow.getDeltaSwitched());
        r.lastSwitched = toEpochMilli(flow.getLastSwitched());
        r.firstSwitched = toEpochMilli(flow.getFirstSwitched());
        r.bytes = flow.getBytes();
        r.packets = flow.getPackets();
        r.samplingInterval = flow.getSamplingInterval();
        r.direction = flow.getDirection() != null ? flow.getDirection().name() : null;
        r.application = flow.getApplication();
        r.convoKey = flow.getConvoKey();
        r.srcAddr = flow.getSrcAddr();
        r.dstAddr = flow.getDstAddr();
        r.protocol = flow.getProtocol();
        r.dscp = flow.getDscp();
        r.inputSnmp = flow.getInputSnmp();
        r.outputSnmp = flow.getOutputSnmp();
        r.location = flow.getLocation();
        if (flow.getExporterNodeInfo() != null) {
            r.exporterNodeId = flow.getExporterNodeInfo().getNodeId();
        }
        r.documentJson = toDocumentJson(flow);
        return r;
    }

    /**
     * Full-fidelity document keyed on the same names the Elastic {@code FlowDocument} uses, so no
     * field is lost and jsonb access matches the documented schema. Null values are omitted.
     */
    String toDocumentJson(final Flow flow) {
        final Map<String, Object> doc = new LinkedHashMap<>();
        put(doc, "@timestamp", toEpochMilli(flow.getTimestamp()));
        put(doc, "location", flow.getLocation());
        put(doc, "host", flow.getHost());
        put(doc, "netflow.application", flow.getApplication());
        put(doc, "netflow.convo_key", flow.getConvoKey());
        put(doc, "netflow.bytes", flow.getBytes());
        put(doc, "netflow.packets", flow.getPackets());
        put(doc, "netflow.direction", flow.getDirection());
        put(doc, "netflow.first_switched", toEpochMilli(flow.getFirstSwitched()));
        put(doc, "netflow.delta_switched", toEpochMilli(flow.getDeltaSwitched()));
        put(doc, "netflow.last_switched", toEpochMilli(flow.getLastSwitched()));
        put(doc, "netflow.sampling_interval", flow.getSamplingInterval());
        put(doc, "netflow.sampling_algorithm", flow.getSamplingAlgorithm());
        put(doc, "netflow.src_addr", flow.getSrcAddr());
        put(doc, "netflow.src_addr_hostname", flow.getSrcAddrHostname().orElse(null));
        put(doc, "netflow.src_port", flow.getSrcPort());
        put(doc, "netflow.src_as", flow.getSrcAs());
        put(doc, "netflow.src_mask_len", flow.getSrcMaskLen());
        put(doc, "netflow.dst_addr", flow.getDstAddr());
        put(doc, "netflow.dst_addr_hostname", flow.getDstAddrHostname().orElse(null));
        put(doc, "netflow.dst_port", flow.getDstPort());
        put(doc, "netflow.dst_as", flow.getDstAs());
        put(doc, "netflow.dst_mask_len", flow.getDstMaskLen());
        put(doc, "netflow.next_hop", flow.getNextHop());
        put(doc, "netflow.protocol", flow.getProtocol());
        put(doc, "netflow.ip_protocol_version", flow.getIpProtocolVersion());
        put(doc, "netflow.tos", flow.getTos());
        put(doc, "netflow.dscp", flow.getDscp());
        put(doc, "netflow.ecn", flow.getEcn());
        put(doc, "netflow.tcp_flags", flow.getTcpFlags());
        put(doc, "netflow.vlan", flow.getVlan());
        put(doc, "netflow.engine_id", flow.getEngineId());
        put(doc, "netflow.engine_type", flow.getEngineType());
        put(doc, "netflow.input_snmp", flow.getInputSnmp());
        put(doc, "netflow.output_snmp", flow.getOutputSnmp());
        put(doc, "netflow.version", flow.getNetflowVersion());
        put(doc, "netflow.src_locality", flow.getSrcLocality());
        put(doc, "netflow.dst_locality", flow.getDstLocality());
        put(doc, "netflow.flow_locality", flow.getFlowLocality());
        putNode(doc, "node_src", flow.getSrcNodeInfo());
        putNode(doc, "node_dst", flow.getDstNodeInfo());
        putNode(doc, "node_exporter", flow.getExporterNodeInfo());
        try {
            return objectMapper.writeValueAsString(doc);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to serialize flow document to JSON", e);
        }
    }

    private static void put(final Map<String, Object> doc, final String key, final Object value) {
        if (value != null) {
            doc.put(key, value);
        }
    }

    private static void putNode(final Map<String, Object> doc, final String key, final Flow.NodeInfo node) {
        if (node == null) {
            return;
        }
        final Map<String, Object> n = new LinkedHashMap<>();
        n.put("node_id", node.getNodeId());
        n.put("interface_id", node.getInterfaceId());
        put(n, "foreign_source", node.getForeignSource());
        put(n, "foreign_id", node.getForeignId());
        put(n, "categories", node.getCategories());
        doc.put(key, n);
    }

    private static Long toEpochMilli(final Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    private static Timestamp toTs(final Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }
}