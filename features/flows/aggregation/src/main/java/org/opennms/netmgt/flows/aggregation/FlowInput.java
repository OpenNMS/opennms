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
package org.opennms.netmgt.flows.aggregation;

import java.time.Instant;

import org.opennms.integration.api.v1.flows.Flow;

/**
 * The subset of an enriched {@link Flow} the write-time aggregator needs, extracted into a small
 * immutable holder. Keeping the engine ({@link FlowAggregator}) decoupled from the (large) {@code Flow}
 * interface makes the aggregation/windowing logic unit-testable without mocking dozens of accessors,
 * and isolates the one piece of interpretation that matters: the aggregation interface is the flow's
 * <em>egress</em> interface for EGRESS flows and its <em>ingress</em> interface otherwise. INGRESS and
 * UNKNOWN both use the ingress interface, following the OpenNMS convention (InterfaceMarkerImpl /
 * FlowThresholdingImpl) that treats an UNKNOWN direction as ingress.
 */
public final class FlowInput {

    final long deltaSwitchedMs;
    final long lastSwitchedMs;
    final long bytes;
    final Double samplingInterval;
    final boolean ingress;
    final int exporterNodeId;
    final int ifIndex;
    final String application;
    final String convoKey;
    final String srcAddr;
    final String dstAddr;
    final String srcHostname;
    final String dstHostname;
    final Integer ecn;
    final Integer dscp;

    public FlowInput(final long deltaSwitchedMs, final long lastSwitchedMs, final long bytes,
              final Double samplingInterval, final boolean ingress, final int exporterNodeId, final int ifIndex,
              final String application, final String convoKey, final String srcAddr, final String dstAddr,
              final String srcHostname, final String dstHostname, final Integer ecn, final Integer dscp) {
        this.deltaSwitchedMs = deltaSwitchedMs;
        this.lastSwitchedMs = lastSwitchedMs;
        this.bytes = bytes;
        this.samplingInterval = samplingInterval;
        this.ingress = ingress;
        this.exporterNodeId = exporterNodeId;
        this.ifIndex = ifIndex;
        this.application = application;
        this.convoKey = convoKey;
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        this.srcHostname = srcHostname;
        this.dstHostname = dstHostname;
        this.ecn = ecn;
        this.dscp = dscp;
    }

    /**
     * Extract the aggregation inputs from a {@link Flow}. Returns {@code null} when the flow lacks the
     * fields the aggregator requires (switched timestamps, exporter node, byte count, the direction's
     * interface) so the caller can count and skip it rather than aggregate garbage.
     */
    public static FlowInput from(final Flow flow) {
        final Instant delta = flow.getDeltaSwitched();
        final Instant last = flow.getLastSwitched();
        final Flow.NodeInfo exporter = flow.getExporterNodeInfo();
        final Long bytes = flow.getBytes();
        if (delta == null || last == null || exporter == null || bytes == null) {
            return null;
        }
        // Only an explicit EGRESS is egress; INGRESS, UNKNOWN, and a missing direction are all treated as
        // ingress (OpenNMS convention) and use the input (ingress) SNMP interface.
        final boolean ingress = flow.getDirection() == null || !"EGRESS".equals(flow.getDirection().name());
        final Integer ifIndex = ingress ? flow.getInputSnmp() : flow.getOutputSnmp();
        if (ifIndex == null) {
            return null;
        }
        return new FlowInput(
                delta.toEpochMilli(),
                last.toEpochMilli(),
                bytes,
                flow.getSamplingInterval(),
                ingress,
                exporter.getNodeId(),
                ifIndex,
                flow.getApplication(),
                flow.getConvoKey(),
                flow.getSrcAddr(),
                flow.getDstAddr(),
                flow.getSrcAddrHostname().orElse(null),
                flow.getDstAddrHostname().orElse(null),
                flow.getEcn(),
                flow.getDscp());
    }
}
