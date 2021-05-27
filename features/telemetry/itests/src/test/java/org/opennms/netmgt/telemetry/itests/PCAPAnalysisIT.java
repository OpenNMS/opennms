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

package org.opennms.netmgt.telemetry.itests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpListener;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Netflow9UdpParser;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.UdpPacket;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;

public class PCAPAnalysisIT implements AsyncDispatcher<TelemetryMessage> {

    // Inputs
    private static final String PCAP_FILE = "/home/fooker/files/inmco.pcap";
    private static final int INTERFACE_INDEX = 540;
    private static final ReferencePoint START = new ReferencePoint("2021-03-30 15:19:46 +0000",
                                                                   2027799084252L, 1882598179810L,
                                                                   3903596492L, 3144961499L);
    private static final ReferencePoint END = new ReferencePoint("2021-03-30 15:24:58 +0000",
                                                                 2028043029769L, 1882717216426L,
                                                                 3904013770L, 3145259617L);
    private static final long EXPECTED_SAMPLE_INTERVAL = 10L;

    private List<FlowMessage> flowMessages = new CopyOnWriteArrayList<>();

    private static List<UdpPacket> getUdpPackets(String pcapFileName, String filter) throws PcapNativeException, NotOpenException, InterruptedException {
        final PcapHandle handle = Pcaps.openOffline(pcapFileName, PcapHandle.TimestampPrecision.NANO);
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

        List<UdpPacket> packets = new LinkedList<>();
        PacketListener listener = packet -> {
            final UdpPacket udpPacket = packet.get(UdpPacket.class);
            packets.add(udpPacket);
        };

        handle.loop(0, listener);
        handle.close();
        return packets;
    }

    private static Map<String, String> flatout(final String path, final JsonElement el) {
        final Map<String, String> result = new HashMap<>();

        if (el.isJsonObject()) {
            final JsonObject object = el.getAsJsonObject();
            for (final Map.Entry<String, JsonElement> e : object.entrySet()) {
                result.putAll(flatout(path + "." + e.getKey(), e.getValue()));
            }
        } else if (el.isJsonArray()) {
            final JsonArray array = el.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                result.putAll(flatout(path + "." + i, array.get(i)));
            }
        } else if (el.isJsonNull()) {
            result.put(path, "<null>");
        } else if (el.isJsonPrimitive()) {
            result.put(path, el.getAsJsonPrimitive().toString());
        } else {
            throw new RuntimeException("Not supported type: " + el);
        }

        return result;
    }

    @Test
    public void runExperiment() throws PcapNativeException, InterruptedException, NotOpenException, IOException {
        System.out.println("START = " + START.getTimestamp());
        System.out.println("END = " + END.getTimestamp());

        String filter = "udp";
        List<UdpPacket> capture = getUdpPackets(PCAP_FILE, filter);

        EventForwarder eventForwarder = mock(EventForwarder.class);
        Identity identity = mock(Identity.class);

        // Return empty DNS results
        DnsResolver dnsResolver = mock(DnsResolver.class);
        when(dnsResolver.reverseLookup(any())).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        MetricRegistry metrics = new MetricRegistry();
        Netflow9UdpParser nf9UdpParser = new Netflow9UdpParser("parsers", this,
                                                               eventForwarder, identity, dnsResolver, metrics);
        UdpListener udpListener = new UdpListener("listener", Arrays.asList(nf9UdpParser), metrics);

        int udpListenPort = 5555;
        udpListener.setPort(udpListenPort);
        udpListener.start();

        System.out.println("Waiting for UDP listener to start...");
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // Analyze the pcap using tshark
//        final Process tshark = Runtime.getRuntime().exec("tshark -r " + PCAP_FILE + " -T json");
        final JsonArray shark = new Gson().fromJson(new FileReader("/home/fooker/files/dump.json"), JsonArray.class);

        final Stats captureBytesIn = new Stats();
        final Stats captureBytesOut = new Stats();
        final Stats captureBytesOther = new Stats();
        final Stats captureBytesIgnored = new Stats();

        final Stats capturePacketsIn = new Stats();
        final Stats capturePacketsOut = new Stats();
        final Stats capturePacketsOther = new Stats();
        final Stats capturePacketsIgnored = new Stats();

        long captureDropDueToFlowSetId = 0;
        long captureDropDueToTimerange = 0;
        long captureTotal = 0;

        final Multiset<Long> capturedDroppedDueToTs = TreeMultiset.create();

        final Map<String, JsonObject> flowHashes = new HashMap<>();

        final Multiset<Long> expectedSequences = TreeMultiset.create();
        for (final JsonElement pkg : shark) {
            final JsonObject cflow = pkg.getAsJsonObject().getAsJsonObject("_source").getAsJsonObject("layers").getAsJsonObject("cflow");
            if (cflow == null) {
                continue;
            }

            final long seqnum = Long.parseLong(cflow.get("cflow.sequence").getAsString());

            final double timestamp = cflow.getAsJsonObject("cflow.timestamp_tree").getAsJsonPrimitive("cflow.unix_secs").getAsLong() * 1000.0;
            final double uptime = cflow.getAsJsonPrimitive("cflow.sysuptime").getAsDouble() * 1000.0;

            for (final Map.Entry<String, JsonElement> flowSetEntry : Iterables.filter(cflow.entrySet(), (e) -> e.getKey().startsWith("FlowSet "))) {
                final JsonObject flowSet = flowSetEntry.getValue().getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> flows = Sets.filter(flowSet.entrySet(), (e) -> e.getKey().startsWith("Flow "));

                captureTotal += flows.size();

                if (flowSet.getAsJsonPrimitive("cflow.flowset_id").getAsInt() != 320 && flowSet.getAsJsonPrimitive("cflow.flowset_id").getAsInt() != 324) {
                    captureDropDueToFlowSetId += flows.size();
                    continue;
                }

                for (final Map.Entry<String, JsonElement> flowEntry : flows) {
                    final JsonObject flow = flowEntry.getValue().getAsJsonObject();
		    flow.add("__seqnum", pkg.getAsJsonObject().getAsJsonObject("_source").getAsJsonObject("layers").getAsJsonObject("frame").get("frame.number"));

                    final double start = flow.getAsJsonObject("cflow.timedelta_tree").getAsJsonPrimitive("cflow.timestart").getAsDouble() * 1000.0;
                    final double end = flow.getAsJsonObject("cflow.timedelta_tree").getAsJsonPrimitive("cflow.timeend").getAsDouble() * 1000.0;

                    double tsStart = timestamp - uptime + start;
                    double tsEnd = timestamp - uptime + end;

                    if (tsEnd - tsStart < 0.0) {
                        tsEnd = timestamp;
                        tsStart = timestamp - 10_000.0;
                        System.err.println(flow);
                    }

                    final double tsDelta = Math.max(tsStart, tsEnd - 10_000.0);

                    final long octets = flow.getAsJsonPrimitive("cflow.octets").getAsLong();
                    final long packets = flow.getAsJsonPrimitive("cflow.packets").getAsLong();

                    if (tsDelta > END.getTimestamp() &&
                        tsEnd < START.getTimestamp()) {
                        capturedDroppedDueToTs.add(seqnum);
                        captureDropDueToTimerange++;
                        continue;
                    }

//                    if (tsStart == tsEnd) {
//                        captureBytesIgnored.update(octets * EXPECTED_SAMPLE_INTERVAL);
//                        capturePacketsIgnored.update(packets * EXPECTED_SAMPLE_INTERVAL);
//                    } else
                    if (!Objects.equals(flow.getAsJsonPrimitive("cflow.direction").getAsString(), "0")) {
                        captureBytesIgnored.update(octets * EXPECTED_SAMPLE_INTERVAL);
                        capturePacketsIgnored.update(packets * EXPECTED_SAMPLE_INTERVAL);
                    } else
                        {
                        final String x = String.format("%s:%s@%s %s:%s@%s %s",
                                                       flow.getAsJsonPrimitive("cflow.srcaddr").getAsString(),
                                                       flow.getAsJsonPrimitive("cflow.srcport").getAsString(),
                                                       flow.getAsJsonPrimitive("cflow.inputint").getAsString(),
                                                       flow.getAsJsonPrimitive("cflow.dstaddr").getAsString(),
                                                       flow.getAsJsonPrimitive("cflow.dstport").getAsString(),
                                                       flow.getAsJsonPrimitive("cflow.outputint").getAsString(),
                                                       flow.getAsJsonPrimitive("cflow.octets").getAsString());

                        final JsonObject ex = flowHashes.put(x, flow);
                        if (ex != null) {
                            final Map<String, String> m1 = flatout("flow", ex);
                            final Map<String, String> m2 = flatout("flow", flow);

                            final MapDifference<String, String> diff = Maps.difference(m1, m2);

//                            System.out.println(String.format("%s: %s", seqnum, diff));
                            System.out.println(String.format("Duplicate: %s == %s", ex.get("__seqnum"), flow.get("__seqnum")));
                        }

                        if (flow.getAsJsonPrimitive("cflow.inputint").getAsInt() == INTERFACE_INDEX) {
                            captureBytesIn.update(octets * EXPECTED_SAMPLE_INTERVAL);
                            capturePacketsIn.update(packets * EXPECTED_SAMPLE_INTERVAL);
                            expectedSequences.add(seqnum);
                        } else if (flow.getAsJsonPrimitive("cflow.outputint").getAsInt() == INTERFACE_INDEX) {
                            captureBytesOut.update(octets * EXPECTED_SAMPLE_INTERVAL);
                            capturePacketsOut.update(packets * EXPECTED_SAMPLE_INTERVAL);
                            expectedSequences.add(seqnum);
                        } else {
                            captureBytesOther.update(octets * EXPECTED_SAMPLE_INTERVAL);
                            capturePacketsOther.update(packets * EXPECTED_SAMPLE_INTERVAL);
                        }
                    }
                }
            }
        }

        // Run through twice, so we know the engine has the templates
        for (int i = 0; i < 2; i++) {
            flowMessages.clear();
            System.out.println("Firing off packets...");
            DatagramSocket socket = new DatagramSocket();
            InetAddress localhost = InetAddress.getLocalHost();
            for (UdpPacket udpPacket : capture) {
                if (udpPacket.getHeader().getDstPort().value() != 8877) {
                    continue;
                }

                byte[] buf = udpPacket.getPayload().getRawData();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, localhost, udpListenPort);
                socket.send(packet);
                Thread.sleep(1);
            }
            System.out.println("Done firing packets.");

            System.out.println("Waiting for results...");
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            System.out.printf("Got %d messages...\n", flowMessages.size());
        }

        List<Long> seqNumbersInFilter = new LinkedList<>();

        final Stats processingBytesIn = new Stats();
        final Stats processingBytesOut = new Stats();
        final Stats processingBytesOther = new Stats();
        final Stats processingBytesIgnored = new Stats();

        final Stats processingPacketsIn = new Stats();
        final Stats processingPacketsOut = new Stats();
        final Stats processingPacketsOther = new Stats();
        final Stats processingPacketsIgnored = new Stats();

        long processingDropDueToTimerange = 0;
        long processingTotal = 0;

        final Multiset<Long> processedDroppedDueToTs = TreeMultiset.create();
        final Multiset<Long> processedSequences = TreeMultiset.create();

        for (final FlowMessage flowMessage : flowMessages) {
            processingTotal++;

            if (flowMessage.getFlowSeqNum().getValue() == 110020417) {
                System.out.println("= " + flowMessage.getNumBytes().getValue() + " " + flowMessage.getDeltaSwitched().getValue() + " - " + flowMessage.getLastSwitched().getValue());
            }

            // the start of the flow must be before the end of the range
            if (flowMessage.getDeltaSwitched().getValue() <= END.getTimestamp()
                // the end of the flow must be after the start of the range
                && flowMessage.getLastSwitched().getValue() >= START.getTimestamp()) {
                // keep
            } else {
                // skip
                processedDroppedDueToTs.add(flowMessage.getFlowSeqNum().getValue());
                processingDropDueToTimerange++;
                continue;
            }

            final long samplingInterval = (long) flowMessage.getSamplingInterval().getValue();

            // the sample interval should be consistent for our experiment, log if this is not the case
            if (samplingInterval != EXPECTED_SAMPLE_INTERVAL) {
                System.out.println("sampling interval is: " + samplingInterval);
            }

//            if (flowMessage.getFirstSwitched().getValue() == flowMessage.getLastSwitched().getValue()) {
//                processingBytesIgnored.update(flowMessage.getNumBytes().getValue() * samplingInterval);
//                processingPacketsIgnored.update(flowMessage.getNumPackets().getValue() * samplingInterval);
//            } else
            if ((flowMessage.getDirection() == Direction.INGRESS && flowMessage.getInputSnmpIfindex().getValue() == INTERFACE_INDEX)) {
                processingBytesIn.update(flowMessage.getNumBytes().getValue() * samplingInterval);
                processingPacketsIn.update(flowMessage.getNumPackets().getValue() * samplingInterval);
            } else
            if (flowMessage.getDirection() == Direction.EGRESS && flowMessage.getOutputSnmpIfindex().getValue() == INTERFACE_INDEX) {
                processingBytesOut.update(flowMessage.getNumBytes().getValue() * samplingInterval);
                processingPacketsOut.update(flowMessage.getNumPackets().getValue() * samplingInterval);
            } else {
                // skip
                processingBytesOther.update(flowMessage.getNumBytes().getValue() * samplingInterval);
                processingPacketsOther.update(flowMessage.getNumPackets().getValue() * samplingInterval);
                continue;
            }

            // track the sequence numbers for all of the flows that matches our filters and were included in the tally
            seqNumbersInFilter.add(flowMessage.getFlowSeqNum().getValue());

            if (flowMessage.getFlowSeqNum().getValue() == 110020417) {
                System.out.println("- " + flowMessage.getNumBytes().getValue() + " " + String.format("%s:%s:%s", flowMessage.getFlowSeqNum().getValue(), flowMessage.getSrcPort().getValue(), flowMessage.getDstPort().getValue()));
            }

            processedSequences.add(flowMessage.getFlowSeqNum().getValue());
        }

        final List<Long> allSeqNumbers = flowMessages.stream()
                                                     .map(f -> f.getFlowSeqNum().getValue())
                                                     .collect(Collectors.toList());

        // Find the min/max sequence numbers
        final List<Long> seqNumSorted = seqNumbersInFilter.stream().sorted().collect(Collectors.toList());
        final Long minSeqNum = seqNumSorted.get(0);
        final Long maxSeqNum = seqNumSorted.get(seqNumSorted.size() - 1);
        System.out.println("Min seq: " + minSeqNum);
        System.out.println("Max seq: " + maxSeqNum);

        // Are we missing any between these?
        for (long seqNum = minSeqNum; seqNum <= maxSeqNum; seqNum++) {
            if (!allSeqNumbers.contains(seqNum)) {
                System.out.println("Missing flows with seq: " + seqNum);
            }
        }

        // Do we have full representation of all the flow records for this sequence range?
        for (long seqNum = minSeqNum; seqNum <= maxSeqNum; seqNum++) {
            long numRecordsFound = 0;
            long numRecordsExpected = 0;
            for (FlowMessage flowMessage : flowMessages) {
                if (flowMessage.getFlowSeqNum().getValue() == seqNum) {
                    numRecordsFound++;
                    numRecordsExpected = flowMessage.getNumFlowRecords().getValue();
                }
            }
            if (numRecordsFound < 1 || numRecordsFound != numRecordsExpected) {
                System.out.println("Missing records for flow with seq: " + seqNum);
            }
        }

        // bytes from counters
        long mibBytesIn = (END.getBytesIn() - START.getBytesIn());
        long mibBytesOut = (END.getBytesOut() - START.getBytesOut());
        long mibPacketsIn = (END.getPacketsIn() - START.getPacketsIn());
        long mibPacketsOut = (END.getPacketsOut() - START.getPacketsOut());
        System.out.println("Total bytes in (reference) : " + mibBytesIn + " @ " + mibPacketsIn);
        System.out.println("Total bytes out (reference): " + mibBytesOut + " @ " + mibPacketsOut);

        // bytes from flows
        System.out.println("Total bytes in (processing): " + processingBytesIn + " @ " + processingPacketsIn);
        System.out.println("Total bytes out (processing): " + processingBytesOut + " @ " + processingPacketsOut);
        System.out.println("Total bytes other (processing): " + processingBytesOther + " @ " + processingPacketsOther);
        System.out.println("Total bytes ignored (processing): " + processingBytesIgnored + " @ " + processingPacketsIgnored);

        System.out.println("Total bytes in (capture) : " + captureBytesIn + " @ " + capturePacketsIn);
        System.out.println("Total bytes out (capture): " + captureBytesOut + " @ " + capturePacketsOut);
        System.out.println("Total bytes other (capture): " + captureBytesOther + " @ " + capturePacketsOther);
        System.out.println("Total bytes ignored (capture): " + captureBytesIgnored + " @ " + capturePacketsIgnored);

        System.out.println("Drops (capture) due to FlowSetId: " + captureDropDueToFlowSetId);
        System.out.println("Drops (capture) due to timerange: " + captureDropDueToTimerange);
        System.out.println("Total (capture):" + captureTotal);
        System.out.println("Drops (processing) due to timerange: " + processingDropDueToTimerange);
        System.out.println("Total (processing):" + processingTotal);

        // delta
        final long deltaBytesIn = (mibBytesIn - processingBytesIn.getSum());
        final long deltaBytesOut = (mibBytesOut - processingBytesOut.getSum());
        System.out.println("Delta in: " + deltaBytesIn);
        System.out.println("Delta in byt (%): " + ((1 - (double) processingBytesIn.getSum() / mibBytesIn)) * 100);
        System.out.println("Delta in pkt (%): " + ((1 - (double) processingPacketsIn.getSum() / mibPacketsIn)) * 100);
        System.out.println("Delta out: " + deltaBytesOut);
        System.out.println("Delta out byt (%): " + ((1 - (double) processingBytesOut.getSum() / mibBytesOut)) * 100);
        System.out.println("Delta out pkt (%): " + ((1 - (double) processingPacketsOut.getSum() / mibPacketsOut)) * 100);

        final Multiset<Long> missingSequences = Multisets.difference(expectedSequences, processedSequences);
        System.out.println("Missing sequences: " + missingSequences.size());
        for (final Long missingSequence : missingSequences) {
            System.out.println("  " + missingSequence);
        }

        final Multiset<Long> unexpectedSequences = Multisets.difference(processedSequences, expectedSequences);
        System.out.println("Unexpected sequences: " + unexpectedSequences.size());
//        for(Long unexpectedSequence : unexpectedSequences) {
//            System.out.println("  " + unexpectedSequence);
//        }

//        final Multiset<Long> lostSequences = Multisets.difference(capturedDroppedDueToTs, processedDroppedDueToTs);
//        System.out.println("Lost sequences: " + lostSequences.size());
//        for(Long lostSequence : lostSequences) {
//            System.out.println("  " + lostSequence);
//        }
    }

    @Override
    public CompletableFuture<DispatchStatus> send(TelemetryMessage message) {
        try {
            FlowMessage flowMessage = FlowMessage.parseFrom(message.getBuffer());
            if (flowMessage != null) {
                if (flowMessage.getFlowSeqNum().getValue() == 110020417) {
                    System.out.println("? " + flowMessage.getNumBytes().getValue());
                }

                flowMessages.add(flowMessage);
            }
        } catch (InvalidProtocolBufferException e) {
            CompletableFuture<DispatchStatus> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
        return CompletableFuture.completedFuture(DispatchStatus.DISPATCHED);
    }

    @Override
    public int getQueueSize() {
        return 0;
    }

    @Override
    public void close() {
        // pass
    }

    private static class ReferencePoint {
        private final String time;
        private final long bytes_in;
        private final long bytes_out;
        private final long packets_in;
        private final long packets_out;

        public ReferencePoint(String time, long bytes_in, long bytes_out, long packets_in, long packets_out) {
            this.time = time;
            this.bytes_in = bytes_in;
            this.bytes_out = bytes_out;
            this.packets_in = packets_in;
            this.packets_out = packets_out;
        }

        public Long getTimestamp() {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(time).getTime();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public long getBytesIn() {
            return bytes_in;
        }

        public long getBytesOut() {
            return bytes_out;
        }

        public long getPacketsIn() {
            return packets_in;
        }

        public long getPacketsOut() {
            return packets_out;
        }
    }

    private static class Stats {
        private long count = 0;

        private long sum = 0;

        private long min = Long.MAX_VALUE;
        private long max = Long.MIN_VALUE;

        public void update(final long value) {
            this.count++;

            this.sum += value;

            this.min = Math.min(this.min, value);
            this.max = Math.max(this.max, value);
        }

        public long getCount() {
            return this.count;
        }

        public long getSum() {
            return this.sum;
        }

        public long getMin() {
            return this.min;
        }

        public long getMax() {
            return this.max;
        }

        public long getAvg() {
            if (this.count == 0) {
                return 0L;
            }

            return this.sum / this.count;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("count", this.getCount())
                              .add("sum", this.getSum())
                              .add("min", this.getMin())
                              .add("max", this.getMax())
                              .add("avg", this.getAvg())
                              .toString();
        }
    }
}
