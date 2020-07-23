/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlow;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers.EthernetHeader;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers.Inet4Header;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.headers.Inet6Header;

import com.google.common.primitives.UnsignedInteger;

import io.netty.buffer.Unpooled;

public class BsonDocumentTest implements SampleDatagramEnrichment {
    private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();
    private static final IpV4 SRC_IPV4 = new IpV4(Unpooled.wrappedBuffer(new byte[]{(byte) 192, (byte) 168, (byte) 1, (byte) 1}));
    private static final String SRC_IPV4_STR = "192.168.1.1";
    private static final IpV4 DST_IPV4 = new IpV4(Unpooled.wrappedBuffer(new byte[]{(byte) 192, (byte) 168, (byte) 2, (byte) 1}));
    private static final String DST_IPV4_STR = "192.168.2.1";

    private static final IpV6 SRC_IPV6 = new IpV6(Unpooled.wrappedBuffer(new byte[]{(byte) 254, (byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1}));
    private static final String SRC_IPV6_STR = "fe80:0:0:0:0:0:0:1";
    private static final IpV6 DST_IPV6 = new IpV6(Unpooled.wrappedBuffer(new byte[]{(byte) 254, (byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 2}));
    private static final String DST_IPV6_STR = "fe80:0:0:0:0:0:0:2";

    private static final IpV4 ROUTER_IPV4 = new IpV4(Unpooled.wrappedBuffer(new byte[]{(byte) 192, (byte) 168, (byte) 3, (byte) 1}));
    private static final String ROUTER_IPV4_STR = "192.168.3.1";
    private static final IpV6 ROUTER_IPV6 = new IpV6(Unpooled.wrappedBuffer(new byte[]{(byte) 254, (byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3}));
    private static final String ROUTER_IPV6_STR = "fe80:0:0:0:0:0:0:3";
    private static final IpV4 GATEWAY_IPV4 = new IpV4(Unpooled.wrappedBuffer(new byte[]{(byte) 192, (byte) 168, (byte) 4, (byte) 1}));

    private static final int LENGTH = 500;
    private static final int PROTOCOL = 6;
    private static final int TOS = 23;
    private static final int SRC_PORT = 81;
    private static final int DST_PORT = 82;
    private static final int TCP_FLAGS = 42;
    private static final int SRC_VLAN = 111;
    private static final int SRC_PRIORITY = 11;
    private static final int DST_VLAN = 222;
    private static final int DST_PRIORITY = 22;
    private static final int INPUT = 101;
    private static final int OUTPUT = 102;
    private static final int SRC_MASK_LEN = 24;
    private static final int DST_MASK_LEN = 26;
    private static final int AS = 2222;
    private static final int SRC_AS = 3333;
    private static final int LOCALPREF = 4444;

    private static final int SUB_AGENT_ID = 901;
    private static final int SEQUENCE_NUMBER = 902;
    private static final int UPTIME = 903;

    private static final SFlow.Header SFLOW_HEADER;

    static {
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        bsonDocumentWriter.writeStartDocument();
        bsonDocumentWriter.writeName("time");
        bsonDocumentWriter.writeInt64(CURRENT_TIME_MILLIS);
        bsonDocumentWriter.writeEndDocument();
        SFLOW_HEADER = new SFlow.Header(bsonDocument);
    }

    private BsonDocument createSampledIpv4() {
        final SampledIpv4 flowData = new SampledIpv4(LENGTH, PROTOCOL, SRC_IPV4, DST_IPV4, SRC_PORT, DST_PORT, TCP_FLAGS, TOS);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 3), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    private BsonDocument createSampledIpv6() {
        final SampledIpv6 flowData = new SampledIpv6(LENGTH, PROTOCOL, SRC_IPV6, DST_IPV6, SRC_PORT, DST_PORT, TCP_FLAGS, TOS);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 4), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    private BsonDocument createSampledHeaderIpv4() {
        final Inet4Header inet4Header = new Inet4Header(TOS, LENGTH, PROTOCOL, (Inet4Address) InetAddressUtils.addr(SRC_IPV4_STR), (Inet4Address) InetAddressUtils.addr(DST_IPV4_STR), SRC_PORT, DST_PORT, TCP_FLAGS);
        final EthernetHeader ethernetHeader = new EthernetHeader(SRC_VLAN, inet4Header, null, null);
        final SampledHeader flowData = new SampledHeader(HeaderProtocol.IPv4, 1100, 1000, ethernetHeader, inet4Header, null, null);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 1), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    private BsonDocument createSampledHeaderIpv6() {
        final Inet6Header inet6Header = new Inet6Header(23, LENGTH, PROTOCOL, (Inet6Address) InetAddressUtils.addr(SRC_IPV6_STR), (Inet6Address) InetAddressUtils.addr(DST_IPV6_STR), SRC_PORT, DST_PORT, TCP_FLAGS);
        final EthernetHeader ethernetHeader = new EthernetHeader(SRC_VLAN, null, inet6Header, null);
        final SampledHeader flowData = new SampledHeader(HeaderProtocol.IPv6, 1100, 1000, ethernetHeader, null, inet6Header, null);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 1), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    private BsonDocument createExtendedSwitch() {
        final ExtendedSwitch flowData = new ExtendedSwitch(SRC_VLAN, SRC_PRIORITY, DST_VLAN, DST_PRIORITY);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 1001), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    private BsonDocument createExtendedRouterIpv4() {
        final Address address = new Address(AddressType.IP_V4, ROUTER_IPV4, null);
        final NextHop nextHop = new NextHop(address);
        final ExtendedRouter flowData = new ExtendedRouter(nextHop, SRC_MASK_LEN, DST_MASK_LEN);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 1002), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    private BsonDocument createExtendedRouterIpv6() {
        final Address address = new Address(AddressType.IP_V6, null, ROUTER_IPV6);
        final NextHop nextHop = new NextHop(address);
        final ExtendedRouter flowData = new ExtendedRouter(nextHop, SRC_MASK_LEN, DST_MASK_LEN);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 1002), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    private BsonDocument createExtendedGateway() {
        final Address address = new Address(AddressType.IP_V4, GATEWAY_IPV4, null);
        final NextHop nextHop = new NextHop(address);
        final ExtendedGateway flowData = new ExtendedGateway(nextHop, AS, SRC_AS, 3, new Array<AsPathType>(0, new ArrayList<>()), new Array<UnsignedInteger>(0, new ArrayList<>()), LOCALPREF);
        final FlowRecord flowRecord = new FlowRecord(Record.DataFormat.from(0, 1003), new Opaque<FlowData>(1, flowData));
        final FlowSample flowSample = new FlowSample(1, new SFlowDataSource(1L), 2, 3, 4, new Interface(INPUT), new Interface(OUTPUT), new Array<FlowRecord>(1, Arrays.<FlowRecord>asList(flowRecord)));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        flowSample.writeBson(bsonDocumentWriter, this);
        return bsonDocument;
    }

    /**
     * These tests assure, that the test flow data is really at the right spot in the BsonDocument's structure.
     */
    @Test
    public void testSampledIpv4() {
        final BsonDocument bsonDocument = createSampledIpv4();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Long(LENGTH), sFlow.getBytes());
        Assert.assertEquals(Flow.Direction.INGRESS, sFlow.getDirection());
        Assert.assertEquals(DST_IPV4_STR, sFlow.getDstAddr());
        Assert.assertEquals(null, sFlow.getDstAs());
        Assert.assertEquals(null, sFlow.getDstMaskLen());
        Assert.assertEquals(new Integer(DST_PORT), sFlow.getDstPort());
        Assert.assertEquals(null, sFlow.getEngineType());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getFirstSwitched());
        Assert.assertEquals(1, sFlow.getFlowRecords());
        Assert.assertEquals(0, sFlow.getFlowSeqNum());
        Assert.assertEquals(new Integer(INPUT), sFlow.getInputSnmp());
        Assert.assertEquals(new Integer(4), sFlow.getIpProtocolVersion());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getLastSwitched());
        Assert.assertEquals(Flow.NetflowVersion.SFLOW, sFlow.getNetflowVersion());
        Assert.assertEquals(null, sFlow.getNextHop());
        Assert.assertEquals(new Integer(OUTPUT), sFlow.getOutputSnmp());
        Assert.assertEquals(new Long(1), sFlow.getPackets());
        Assert.assertEquals(new Integer(PROTOCOL), sFlow.getProtocol());
        Assert.assertEquals(Flow.SamplingAlgorithm.Unassigned, sFlow.getSamplingAlgorithm());
        Assert.assertEquals(new Double(2.0), sFlow.getSamplingInterval());
        Assert.assertEquals(SRC_IPV4_STR, sFlow.getSrcAddr());
        Assert.assertEquals(null, sFlow.getSrcAs());
        Assert.assertEquals(null, sFlow.getSrcMaskLen());
        Assert.assertEquals(new Integer(SRC_PORT), sFlow.getSrcPort());
        Assert.assertEquals(new Integer(TCP_FLAGS), sFlow.getTcpFlags());
        Assert.assertEquals(CURRENT_TIME_MILLIS, sFlow.getTimestamp());
        Assert.assertEquals(new Integer(TOS), sFlow.getTos());
        Assert.assertEquals(null, sFlow.getVlan());
    }

    @Test
    public void testSampledIpv6() {
        final BsonDocument bsonDocument = createSampledIpv6();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Long(LENGTH), sFlow.getBytes());
        Assert.assertEquals(Flow.Direction.INGRESS, sFlow.getDirection());
        Assert.assertEquals(DST_IPV6_STR, sFlow.getDstAddr());
        Assert.assertEquals(null, sFlow.getDstAs());
        Assert.assertEquals(null, sFlow.getDstMaskLen());
        Assert.assertEquals(new Integer(DST_PORT), sFlow.getDstPort());
        Assert.assertEquals(null, sFlow.getEngineType());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getFirstSwitched());
        Assert.assertEquals(1, sFlow.getFlowRecords());
        Assert.assertEquals(0, sFlow.getFlowSeqNum());
        Assert.assertEquals(new Integer(INPUT), sFlow.getInputSnmp());
        Assert.assertEquals(new Integer(6), sFlow.getIpProtocolVersion());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getLastSwitched());
        Assert.assertEquals(Flow.NetflowVersion.SFLOW, sFlow.getNetflowVersion());
        Assert.assertEquals(null, sFlow.getNextHop());
        Assert.assertEquals(new Integer(OUTPUT), sFlow.getOutputSnmp());
        Assert.assertEquals(new Long(1), sFlow.getPackets());
        Assert.assertEquals(new Integer(PROTOCOL), sFlow.getProtocol());
        Assert.assertEquals(Flow.SamplingAlgorithm.Unassigned, sFlow.getSamplingAlgorithm());
        Assert.assertEquals(new Double(2.0), sFlow.getSamplingInterval());
        Assert.assertEquals(SRC_IPV6_STR, sFlow.getSrcAddr());
        Assert.assertEquals(null, sFlow.getSrcAs());
        Assert.assertEquals(null, sFlow.getSrcMaskLen());
        Assert.assertEquals(new Integer(SRC_PORT), sFlow.getSrcPort());
        Assert.assertEquals(new Integer(TCP_FLAGS), sFlow.getTcpFlags());
        Assert.assertEquals(CURRENT_TIME_MILLIS, sFlow.getTimestamp());
        Assert.assertEquals(new Integer(TOS), sFlow.getTos());
        Assert.assertEquals(null, sFlow.getVlan());
    }

    @Test
    public void testSampledHeaderIpv4() {
        final BsonDocument bsonDocument = createSampledHeaderIpv4();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Long(LENGTH), sFlow.getBytes());
        Assert.assertEquals(Flow.Direction.INGRESS, sFlow.getDirection());
        Assert.assertEquals(DST_IPV4_STR, sFlow.getDstAddr());
        Assert.assertEquals(null, sFlow.getDstAs());
        Assert.assertEquals(null, sFlow.getDstMaskLen());
        Assert.assertEquals(new Integer(DST_PORT), sFlow.getDstPort());
        Assert.assertEquals(null, sFlow.getEngineType());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getFirstSwitched());
        Assert.assertEquals(1, sFlow.getFlowRecords());
        Assert.assertEquals(0, sFlow.getFlowSeqNum());
        Assert.assertEquals(new Integer(INPUT), sFlow.getInputSnmp());
        Assert.assertEquals(new Integer(4), sFlow.getIpProtocolVersion());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getLastSwitched());
        Assert.assertEquals(Flow.NetflowVersion.SFLOW, sFlow.getNetflowVersion());
        Assert.assertEquals(null, sFlow.getNextHop());
        Assert.assertEquals(new Integer(OUTPUT), sFlow.getOutputSnmp());
        Assert.assertEquals(new Long(1), sFlow.getPackets());
        Assert.assertEquals(new Integer(PROTOCOL), sFlow.getProtocol());
        Assert.assertEquals(Flow.SamplingAlgorithm.Unassigned, sFlow.getSamplingAlgorithm());
        Assert.assertEquals(new Double(2.0), sFlow.getSamplingInterval());
        Assert.assertEquals(SRC_IPV4_STR, sFlow.getSrcAddr());
        Assert.assertEquals(null, sFlow.getSrcAs());
        Assert.assertEquals(null, sFlow.getSrcMaskLen());
        Assert.assertEquals(new Integer(SRC_PORT), sFlow.getSrcPort());
        Assert.assertEquals(new Integer(TCP_FLAGS), sFlow.getTcpFlags());
        Assert.assertEquals(CURRENT_TIME_MILLIS, sFlow.getTimestamp());
        Assert.assertEquals(new Integer(TOS), sFlow.getTos());
        Assert.assertEquals(new Integer(SRC_VLAN), sFlow.getVlan());
    }

    @Test
    public void testSampledHeaderIpv6() {
        final BsonDocument bsonDocument = createSampledHeaderIpv6();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Long(LENGTH), sFlow.getBytes());
        Assert.assertEquals(Flow.Direction.INGRESS, sFlow.getDirection());
        Assert.assertEquals(DST_IPV6_STR, sFlow.getDstAddr());
        Assert.assertEquals(null, sFlow.getDstAs());
        Assert.assertEquals(null, sFlow.getDstMaskLen());
        Assert.assertEquals(new Integer(DST_PORT), sFlow.getDstPort());
        Assert.assertEquals(null, sFlow.getEngineType());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getFirstSwitched());
        Assert.assertEquals(1, sFlow.getFlowRecords());
        Assert.assertEquals(0, sFlow.getFlowSeqNum());
        Assert.assertEquals(new Integer(INPUT), sFlow.getInputSnmp());
        Assert.assertEquals(new Integer(6), sFlow.getIpProtocolVersion());
        Assert.assertEquals(new Long(CURRENT_TIME_MILLIS), sFlow.getLastSwitched());
        Assert.assertEquals(Flow.NetflowVersion.SFLOW, sFlow.getNetflowVersion());
        Assert.assertEquals(null, sFlow.getNextHop());
        Assert.assertEquals(new Integer(OUTPUT), sFlow.getOutputSnmp());
        Assert.assertEquals(new Long(1), sFlow.getPackets());
        Assert.assertEquals(new Integer(PROTOCOL), sFlow.getProtocol());
        Assert.assertEquals(Flow.SamplingAlgorithm.Unassigned, sFlow.getSamplingAlgorithm());
        Assert.assertEquals(new Double(2.0), sFlow.getSamplingInterval());
        Assert.assertEquals(SRC_IPV6_STR, sFlow.getSrcAddr());
        Assert.assertEquals(null, sFlow.getSrcAs());
        Assert.assertEquals(null, sFlow.getSrcMaskLen());
        Assert.assertEquals(new Integer(SRC_PORT), sFlow.getSrcPort());
        Assert.assertEquals(new Integer(TCP_FLAGS), sFlow.getTcpFlags());
        Assert.assertEquals(CURRENT_TIME_MILLIS, sFlow.getTimestamp());
        Assert.assertEquals(new Integer(TOS), sFlow.getTos());
        Assert.assertEquals(new Integer(SRC_VLAN), sFlow.getVlan());
    }

    @Test
    public void testExtendedRouterIpv4() {
        final BsonDocument bsonDocument = createExtendedRouterIpv4();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Integer(SRC_MASK_LEN), sFlow.getSrcMaskLen());
        Assert.assertEquals(new Integer(DST_MASK_LEN), sFlow.getDstMaskLen());
        Assert.assertEquals(ROUTER_IPV4_STR, sFlow.getNextHop());
    }

    @Test
    public void testExtendedRouterIpv6() {
        final BsonDocument bsonDocument = createExtendedRouterIpv6();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Integer(SRC_MASK_LEN), sFlow.getSrcMaskLen());
        Assert.assertEquals(new Integer(DST_MASK_LEN), sFlow.getDstMaskLen());
        Assert.assertEquals(ROUTER_IPV6_STR, sFlow.getNextHop());
    }

    @Test
    public void testExtendedSwitch() {
        final BsonDocument bsonDocument = createExtendedSwitch();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Integer(SRC_VLAN), sFlow.getVlan());
    }

    @Test
    public void testExtendedGateway() {
        final BsonDocument bsonDocument = createExtendedGateway();
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        Assert.assertEquals(new Long(SRC_AS), sFlow.getSrcAs());
    }

    @Test
    public void testSFlowHeader() {
        final Address address = new Address(AddressType.IP_V4, SRC_IPV4, null);
        final SampleDatagramV5 sampleDatagramV5 = new SampleDatagramV5(address, SUB_AGENT_ID, SEQUENCE_NUMBER, UPTIME, new Array<SampleRecord>(0, new ArrayList<>()));
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        sampleDatagramV5.writeBson(bsonDocumentWriter, this);
        final SFlow.Header sFlow = new SFlow.Header(bsonDocument);
        Assert.assertEquals(new Integer(SUB_AGENT_ID), sFlow.getSubAgentId());
        Assert.assertEquals(new Long(SEQUENCE_NUMBER), sFlow.getSequenceNumber());
        Assert.assertEquals(null, sFlow.getTimestamp());
    }

    /**
     * This will check the generation of the SFlow document based on the different records and will fail for new
     * getter-methods without matching data type.
     */
    @Test
    public void testSFlowGetters() throws Exception {
        System.out.println("\n---\nChecking getter for SampledIpv4 (enterprise 0, format 3)\n---\n");
        testGetterForDocument(createSampledIpv4());
        System.out.println("\n---\nChecking getter for SampledIpv6 (enterprise 0, format 4)\n---\n");
        testGetterForDocument(createSampledIpv6());
        System.out.println("\n---\nChecking getter for SampledHeader Ipv4 data (enterprise 0, format 1)\n---\n");
        testGetterForDocument(createSampledHeaderIpv4());
        System.out.println("\n---\nChecking getter for SampledHeader Ipv6 data (enterprise 0, format 1)\n---\n");
        testGetterForDocument(createSampledHeaderIpv6());
        System.out.println("\n---\nChecking getter for ExtendedSwitch (enterprise 0, format 1001)\n---\n");
        testGetterForDocument(createExtendedSwitch());
        System.out.println("\n---\nChecking getter for ExtendedRouter Ipv4 data (enterprise 0, format 1002)\n---\n");
        testGetterForDocument(createExtendedRouterIpv4());
        System.out.println("\n---\nChecking getter for ExtendedRouter Ipv6 data (enterprise 0, format 1002)\n---\n");
        testGetterForDocument(createExtendedRouterIpv6());
        System.out.println("\n---\nChecking getter for ExtendedGateway (enterprise 0, format 1003)\n---\n");
        testGetterForDocument(createExtendedGateway());
    }

    private void testGetterForDocument(final BsonDocument bsonDocument) throws Exception {
        final SFlow sFlow = new SFlow(SFLOW_HEADER, bsonDocument);
        for (final PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(SFlow.class).getPropertyDescriptors()) {
            final Object object = propertyDescriptor.getReadMethod().invoke(sFlow);
            System.out.println(propertyDescriptor.getReadMethod().getName() + "() returns '" + object + "'");
        }
    }

    @Override
    public Optional<String> getHostnameFor(InetAddress srcAddress) {
        return Optional.empty();
    }
}
