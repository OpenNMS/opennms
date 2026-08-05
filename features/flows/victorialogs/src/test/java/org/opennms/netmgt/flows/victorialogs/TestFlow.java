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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.opennms.integration.api.v1.flows.Flow;

/**
 * Mutable {@link Flow} fixture.
 *
 * <p>Hand-written rather than mocked: {@link Flow} has 47 accessors and the serializer reads nearly
 * all of them, so stubbing per-test would be noisier than this.
 */
public class TestFlow implements Flow {

    /** A flow one minute long, starting at 2020-09-13T12:26:40Z. */
    public static final long START_MS = 1_600_000_000_000L;
    public static final long END_MS = 1_600_000_060_000L;

    private String application;
    private String host;
    private String location;
    private Locality srcLocality;
    private Locality dstLocality;
    private Locality flowLocality;
    private NodeInfo srcNodeInfo;
    private NodeInfo dstNodeInfo;
    private NodeInfo exporterNodeInfo;
    private Duration clockCorrection = Duration.ZERO;
    private Instant timestamp;
    private Instant firstSwitched;
    private Instant deltaSwitched;
    private Instant lastSwitched;
    private Instant receivedAt;
    private Long bytes;
    private Direction direction;
    private String dstAddr;
    private String dstAddrHostname;
    private Long dstAs;
    private Integer dstMaskLen;
    private Integer dstPort;
    private Integer engineId;
    private Integer engineType;
    private int flowRecords;
    private long flowSeqNum;
    private Integer inputSnmp;
    private Integer ipProtocolVersion;
    private String nextHop;
    private String nextHopHostname;
    private Integer outputSnmp;
    private Long packets;
    private Integer protocol;
    private SamplingAlgorithm samplingAlgorithm;
    private Double samplingInterval;
    private String srcAddr;
    private String srcAddrHostname;
    private Long srcAs;
    private Integer srcMaskLen;
    private Integer srcPort;
    private Integer tcpFlags;
    private Integer tos;
    private NetflowVersion netflowVersion;
    private Integer vlan;
    private Integer dscp;
    private Integer ecn;
    private String convoKey;

    /** A representative flow with every commonly-populated field set. */
    public static TestFlow full() {
        final TestFlow flow = new TestFlow();
        flow.timestamp = Instant.ofEpochMilli(START_MS);
        flow.firstSwitched = Instant.ofEpochMilli(START_MS);
        flow.deltaSwitched = Instant.ofEpochMilli(START_MS);
        flow.lastSwitched = Instant.ofEpochMilli(END_MS);
        flow.receivedAt = Instant.ofEpochMilli(END_MS);
        flow.clockCorrection = Duration.ofMillis(0);
        flow.application = "https";
        flow.host = "192.168.1.1";
        flow.location = "Default";
        flow.bytes = 1000L;
        flow.packets = 10L;
        flow.direction = Direction.INGRESS;
        flow.srcAddr = "192.168.1.1";
        flow.dstAddr = "10.0.0.1";
        flow.srcPort = 51_000;
        flow.dstPort = 443;
        flow.srcAs = 64_512L;
        flow.dstAs = 64_513L;
        flow.srcMaskLen = 24;
        flow.dstMaskLen = 24;
        flow.srcLocality = Locality.PRIVATE;
        flow.dstLocality = Locality.PRIVATE;
        flow.flowLocality = Locality.PRIVATE;
        flow.protocol = 6;
        flow.tcpFlags = 2;
        flow.tos = 0;
        flow.dscp = 0;
        flow.ecn = 0;
        flow.vlan = 100;
        flow.inputSnmp = 1;
        flow.outputSnmp = 2;
        flow.engineId = 1;
        flow.engineType = 1;
        flow.flowRecords = 1;
        flow.flowSeqNum = 42L;
        flow.ipProtocolVersion = 4;
        flow.nextHop = "10.0.0.254";
        flow.netflowVersion = NetflowVersion.V9;
        flow.samplingAlgorithm = SamplingAlgorithm.Unassigned;
        flow.samplingInterval = 1.0d;
        flow.convoKey = "[\"Default\",6,\"10.0.0.1\",\"192.168.1.1\",\"https\"]";
        flow.exporterNodeInfo = new TestNodeInfo(1, 1, "FS", "FID", Arrays.asList("Routers"));
        return flow;
    }

    /** Only what the serializer treats as mandatory; everything optional left null. */
    public static TestFlow minimal() {
        final TestFlow flow = new TestFlow();
        flow.timestamp = Instant.ofEpochMilli(START_MS);
        flow.direction = Direction.EGRESS;
        flow.bytes = 1L;
        return flow;
    }

    public TestFlow withTimestamp(final Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public TestFlow withSamplingInterval(final Double samplingInterval) {
        this.samplingInterval = samplingInterval;
        return this;
    }

    /**
     * Setters for the fields the fixture previously could not vary.
     *
     * <p>Their absence was not cosmetic: with no way to set an address, no test could build a flow
     * whose two endpoints are the same host, so the self-flow double-count guard was unverifiable;
     * with no way to set an application, no test could build an entity whose name collides with the
     * "Other" or "Unknown" labels; and with no way to set a hostname, the serializer's hostname
     * branches were only ever asserted in the negative.
     */
    public TestFlow withAddresses(final String srcAddr, final String dstAddr) {
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        return this;
    }

    public TestFlow withApplication(final String application) {
        this.application = application;
        return this;
    }

    public TestFlow withHostnames(final String srcAddrHostname, final String dstAddrHostname) {
        this.srcAddrHostname = srcAddrHostname;
        this.dstAddrHostname = dstAddrHostname;
        return this;
    }

    public TestFlow withPorts(final Integer srcPort, final Integer dstPort) {
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        return this;
    }

    public TestFlow withConvoKey(final String convoKey) {
        this.convoKey = convoKey;
        return this;
    }

    @Override public String getApplication() { return application; }
    @Override public String getHost() { return host; }
    @Override public String getLocation() { return location; }
    @Override public Locality getSrcLocality() { return srcLocality; }
    @Override public Locality getDstLocality() { return dstLocality; }
    @Override public Locality getFlowLocality() { return flowLocality; }
    @Override public NodeInfo getSrcNodeInfo() { return srcNodeInfo; }
    @Override public NodeInfo getDstNodeInfo() { return dstNodeInfo; }
    @Override public NodeInfo getExporterNodeInfo() { return exporterNodeInfo; }
    @Override public Duration getClockCorrection() { return clockCorrection; }
    @Override public Instant getTimestamp() { return timestamp; }
    @Override public Instant getFirstSwitched() { return firstSwitched; }
    @Override public Instant getDeltaSwitched() { return deltaSwitched; }
    @Override public Instant getLastSwitched() { return lastSwitched; }
    @Override public Instant getReceivedAt() { return receivedAt; }
    @Override public Long getBytes() { return bytes; }
    @Override public Direction getDirection() { return direction; }
    @Override public String getDstAddr() { return dstAddr; }
    @Override public Optional<String> getDstAddrHostname() { return Optional.ofNullable(dstAddrHostname); }
    @Override public Long getDstAs() { return dstAs; }
    @Override public Integer getDstMaskLen() { return dstMaskLen; }
    @Override public Integer getDstPort() { return dstPort; }
    @Override public Integer getEngineId() { return engineId; }
    @Override public Integer getEngineType() { return engineType; }
    @Override public int getFlowRecords() { return flowRecords; }
    @Override public long getFlowSeqNum() { return flowSeqNum; }
    @Override public Integer getInputSnmp() { return inputSnmp; }
    @Override public Integer getIpProtocolVersion() { return ipProtocolVersion; }
    @Override public String getNextHop() { return nextHop; }
    @Override public Optional<String> getNextHopHostname() { return Optional.ofNullable(nextHopHostname); }
    @Override public Integer getOutputSnmp() { return outputSnmp; }
    @Override public Long getPackets() { return packets; }
    @Override public Integer getProtocol() { return protocol; }
    @Override public SamplingAlgorithm getSamplingAlgorithm() { return samplingAlgorithm; }
    @Override public Double getSamplingInterval() { return samplingInterval; }
    @Override public String getSrcAddr() { return srcAddr; }
    @Override public Optional<String> getSrcAddrHostname() { return Optional.ofNullable(srcAddrHostname); }
    @Override public Long getSrcAs() { return srcAs; }
    @Override public Integer getSrcMaskLen() { return srcMaskLen; }
    @Override public Integer getSrcPort() { return srcPort; }
    @Override public Integer getTcpFlags() { return tcpFlags; }
    @Override public Integer getTos() { return tos; }
    @Override public NetflowVersion getNetflowVersion() { return netflowVersion; }
    @Override public Integer getVlan() { return vlan; }
    @Override public Integer getDscp() { return dscp; }
    @Override public Integer getEcn() { return ecn; }
    @Override public String getConvoKey() { return convoKey; }

    public static class TestNodeInfo implements NodeInfo {
        private final int interfaceId;
        private final int nodeId;
        private final String foreignSource;
        private final String foreignId;
        private final List<String> categories;

        public TestNodeInfo(final int interfaceId, final int nodeId, final String foreignSource,
                            final String foreignId, final List<String> categories) {
            this.interfaceId = interfaceId;
            this.nodeId = nodeId;
            this.foreignSource = foreignSource;
            this.foreignId = foreignId;
            this.categories = categories;
        }

        @Override public int getInterfaceId() { return interfaceId; }
        @Override public int getNodeId() { return nodeId; }
        @Override public String getForeignId() { return foreignId; }
        @Override public String getForeignSource() { return foreignSource; }
        @Override public List<String> getCategories() { return categories; }
    }
}
