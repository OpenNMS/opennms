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
package org.opennms.netmgt.flows.api;

import static org.opennms.integration.api.v1.flows.Flow.Direction;
import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;
import static org.opennms.integration.api.v1.flows.Flow.SamplingAlgorithm;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface Flow {

    /**
     * Time at which the flow was received by listener in milliseconds since epoch UTC.
     */
    Instant getReceivedAt();

    /**
     * Flow timestamp in milliseconds.
     */
    Instant getTimestamp();

    /**
     * Number of bytes transferred in the flow.
     */
    Long getBytes();

    /**
     * Direction of the flow (egress vs ingress)
     */
    Direction getDirection();

    /**
     * Destination address.
     */
    String getDstAddr();

    /**
     * Destination address hostname.
     */
    Optional<String> getDstAddrHostname();

    /**
     * Destination autonomous system (AS).
     */
    Long getDstAs();

    /**
     * The number of contiguous bits in the source address subnet mask.
     */
    Integer getDstMaskLen();

    /**
     * Destination port.
     */
    Integer getDstPort();

    /**
     * Slot number of the flow-switching engine.
     */
    Integer getEngineId();

    /**
     * Type of flow-switching engine.
     */
    Integer getEngineType();

    /**
     * Unix timestamp in ms at which the previous exported packet
     * associated with this flow was switched.
     */
    Instant getDeltaSwitched();

    /**
     * Unix timestamp in ms at which the first packet
     * associated with this flow was switched.
     */
    Instant getFirstSwitched();

    /**
     * Number of flow records in the associated packet.
     */
    int getFlowRecords();

    /**
     * Flow packet sequence number.
     */
    long getFlowSeqNum();

    /**
     * SNMP ifIndex
     */
    Integer getInputSnmp();

    /**
     * IPv4 vs IPv6
     */
    Integer getIpProtocolVersion();

    /**
     * Unix timestamp in ms at which the last packet
     * associated with this flow was switched.
     */
    Instant getLastSwitched();

    /**
     * Next hop
     */
    String getNextHop();

    /**
     * Next hop hostname
     */
    Optional<String> getNextHopHostname();

    /**
     * SNMP ifIndex
     */
    Integer getOutputSnmp();

    /**
     * Number of packets in the flow
     */
    Long getPackets();

    /**
     * IP protocol number i.e 6 for TCP, 17 for UDP
     */
    Integer getProtocol();

    /**
     * Sampling algorithm ID
     */
    SamplingAlgorithm getSamplingAlgorithm();

    /**
     * Sampling interval
     */
    Double getSamplingInterval();

    /**
     * Source address.
     */
    String getSrcAddr();

    /**
     * Source address hostname.
     */
    Optional<String> getSrcAddrHostname();

    /**
     * Source autonomous system (AS).
     */
    Long getSrcAs();

    /**
     * The number of contiguous bits in the destination address subnet mask.
     */
    Integer getSrcMaskLen();

    /**
     * Source port.
     */
    Integer getSrcPort();

    /**
     * TCP Flags.
     */
    Integer getTcpFlags();

    /**
     * TOS.
     */
    Integer getTos();

    default Integer getDscp() {
        return getTos() != null ? getTos() >>> 2 : null;
    }

    default Integer getEcn() {
        return getTos() != null ? getTos() & 0x03 : null;
    }

    /**
     * Netfow version
     */
    NetflowVersion getNetflowVersion();

    /**
     * VLAN ID.
     */
    Integer getVlan();

    /**
     * Method to get node lookup identifier.
     *
     * This field can be used as an alternate means to identify the
     * exporter node when the source address of the packets are altered
     * due to address translation.
     *
     * * @return the identifier
     */
    String getNodeIdentifier();

    default Map<String, Object> getRawMessage() {
        return Collections.EMPTY_MAP;
    }
}
