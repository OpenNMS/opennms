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

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;

public class PCAPAnalysisIT implements AsyncDispatcher<TelemetryMessage> {

    // Inputs
    private static final String PCAP_FILE = "/home/fooker/files/agg003.pcap";
    private static final int INTERFACE_INDEX = 731;
    private static final ReferencePoint START = new ReferencePoint("2020-12-16 15:24:56 -0000",
            8052648991542524L, 2302027755041727L,
            9870251681836L, 4013932391409L);
    private static final ReferencePoint END = new ReferencePoint("2020-12-16 15:25:56 -0000",
            8052658754853630L, 2302031573972514L,
            9870264642638L, 4013938036235L);
    private static final double EXPECTED_SAMPLE_INTERVAL = 10.0d;

    private List<FlowMessage> flowMessages = new CopyOnWriteArrayList<>();

    @Test
    public void runExperiment() throws PcapNativeException, InterruptedException, NotOpenException, IOException {
        String filter = "udp";
        List<UdpPacket> packets = getUdpPackets(PCAP_FILE, filter);

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

        long captureBytesIn = 0;
        long captureBytesOut = 0;
        long captureBytesOther = 0;

        long captureFlowsIn = 0;
        long captureFlowsOut = 0;
        long captureFlowsOther = 0;
        final Set<String> expectedSequences = Sets.newTreeSet();
        for (final JsonElement pkg : shark) {
            final JsonObject cflow = pkg.getAsJsonObject().getAsJsonObject("_source").getAsJsonObject("layers").getAsJsonObject("cflow");
            if (cflow == null) {
                continue;
            }

            final long seqnum = Long.parseLong(cflow.get("cflow.sequence").getAsString());

            final long timestamp = cflow.getAsJsonObject("cflow.timestamp_tree").getAsJsonPrimitive("cflow.unix_secs").getAsLong() * 1000L;
            final double uptime = cflow.getAsJsonPrimitive("cflow.sysuptime").getAsDouble() * 1000.0;

            for (final Map.Entry<String, JsonElement> flowSetEntry : Iterables.filter(cflow.entrySet(), (e) -> e.getKey().startsWith("FlowSet "))) {
                final JsonObject flowSet = flowSetEntry.getValue().getAsJsonObject();

                if (flowSet.getAsJsonPrimitive("cflow.flowset_id").getAsInt() != 320) {
                    continue;
                }

                for (final Map.Entry<String, JsonElement> flowEntry : Iterables.filter(flowSet.entrySet(), (e) -> e.getKey().startsWith("Flow "))) {
                    final JsonObject flow = flowEntry.getValue().getAsJsonObject();

                    final double start = flow.getAsJsonObject("cflow.timedelta_tree").getAsJsonPrimitive("cflow.timestart").getAsDouble() * 1000.0;
                    final double end = flow.getAsJsonObject("cflow.timedelta_tree").getAsJsonPrimitive("cflow.timeend").getAsDouble() * 1000.0;

                    final double tsStart = timestamp - uptime + start;
                    final double tsEnd = timestamp - uptime + end;

                    final double tsDelta = Math.max(tsStart, tsEnd - 10_000.0);

                    if (tsDelta <= END.getTimestamp() &&
                        tsEnd >= START.getTimestamp()) {

                        final long octets = flow.getAsJsonPrimitive("cflow.octets").getAsLong();

                        if (flow.getAsJsonPrimitive("cflow.direction").getAsInt() == 0 && flow.getAsJsonPrimitive("cflow.inputint").getAsInt() == INTERFACE_INDEX) {
                            captureBytesIn += octets;
                            captureFlowsIn ++;
                            expectedSequences.add(String.format("%s:%s", seqnum, octets));
                        }

                        else if (flow.getAsJsonPrimitive("cflow.direction").getAsInt() == 1 && flow.getAsJsonPrimitive("cflow.outputint").getAsInt() == INTERFACE_INDEX) {
                            captureBytesOut += octets;
                            captureFlowsOut ++;
                            expectedSequences.add(String.format("%s:%s", seqnum, octets));
                        }

                        else {
                            captureBytesOther += octets;
                            captureFlowsOther ++;
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
            for (UdpPacket udpPacket : packets) {
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

        long totalBytesIn = 0;
        long totalBytesOut = 0;
        long totalBytesOther = 0;

        long totalFlowsIn = 0;
        long totalFlowsOut = 0;
        long totalFlowsOther = 0;
        for (FlowMessage flowMessage : flowMessages) {

            if (flowMessage.getFlowSeqNum().getValue() == 570258289) {
                System.out.println("= " + flowMessage.getNumBytes().getValue() + " " + flowMessage.getDeltaSwitched().getValue() + " - " + flowMessage.getLastSwitched().getValue());
            }

            // the start of the flow must be before the end of the range
            if (flowMessage.getDeltaSwitched().getValue() <= END.getTimestamp()
                    // the end of the flow must be after the start of the range
                    && flowMessage.getLastSwitched().getValue() >= START.getTimestamp()) {
                // keep
            } else {
                // skip
                continue;
            }

            if (flowMessage.getFlowSeqNum().getValue() == 570258289) {
                System.out.println(": " + flowMessage.getNumBytes().getValue());
            }

            // the sample interval should be consistent for our experiment, log if this is not the case
            if (Math.abs(flowMessage.getSamplingInterval().getValue() - EXPECTED_SAMPLE_INTERVAL) > 0.01d) {
                System.out.println("sampling interval is: " + flowMessage.getSamplingInterval().getValue());
            }

            // tally the bytes if the flows meet the criteria
            if ((flowMessage.getDirection() == Direction.INGRESS && flowMessage.getInputSnmpIfindex().getValue() == INTERFACE_INDEX)) {
                totalBytesIn += flowMessage.getNumBytes().getValue() * flowMessage.getSamplingInterval().getValue();
                totalFlowsIn ++;
            } else if (flowMessage.getDirection() == Direction.EGRESS && flowMessage.getOutputSnmpIfindex().getValue() == INTERFACE_INDEX) {
                totalBytesOut += flowMessage.getNumBytes().getValue() * flowMessage.getSamplingInterval().getValue();
                totalFlowsOut ++;
            } else {
                // skip
                totalBytesOther += flowMessage.getNumBytes().getValue() * flowMessage.getSamplingInterval().getValue();
                totalFlowsOther ++;
                continue;
            }

            // track the sequence numbers for all of the flows that matches our filters and were included in the tally
            seqNumbersInFilter.add(flowMessage.getFlowSeqNum().getValue());


            if (flowMessage.getFlowSeqNum().getValue() == 570258289) {
                System.out.println("- " + flowMessage.getNumBytes().getValue() + " " + String.format("%s:%s", flowMessage.getFlowSeqNum().getValue(), flowMessage.getNumBytes().getValue()));
            }

            expectedSequences.remove(String.format("%s:%s", flowMessage.getFlowSeqNum().getValue(), flowMessage.getNumBytes().getValue()));
        }

        List<Long> allSeqNumbers = flowMessages.stream()
                .map(f -> f.getFlowSeqNum().getValue())
                .collect(Collectors.toList());

        // Find the min/max sequence numbers
        List<Long> seqNumSorted = seqNumbersInFilter.stream().sorted().collect(Collectors.toList());
        Long minSeqNum = seqNumSorted.get(0);
        Long maxSeqNum = seqNumSorted.get(seqNumSorted.size() - 1);
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

        // bytes from flows
        System.out.println("Total bytes in: " + totalBytesIn + " (" + totalFlowsIn + ")");
        System.out.println("Total bytes out: " + totalBytesOut + " (" + totalFlowsOut + ")");
        System.out.println("Total bytes other: " + totalBytesOther + " (" + totalFlowsOther + ")");

        // bytes from counters
        long mibBytesIn = (END.getBytesIn() - START.getBytesIn());
        long mibBytesOut = (END.getBytesOut() - START.getBytesOut());
        System.out.println("Total bytes in (reference) : " + mibBytesIn);
        System.out.println("Total bytes out (reference): " + mibBytesOut);


        System.out.println("Total bytes in (capture) : " + captureBytesIn + " (" + captureFlowsIn + ")");
        System.out.println("Total bytes out (capture): " + captureBytesOut + " (" + captureFlowsOut + ")");
        System.out.println("Total bytes other (capture): " + captureBytesOther + " (" + captureFlowsOther + ")");

        System.out.println("Missing sequences: " + expectedSequences.size());
        for (final String expectedSequence : expectedSequences) {
            System.out.println("  " + expectedSequence);
        }

        // delta
        long deltaBytesIn = (mibBytesIn - totalBytesIn);
        long deltaBytesOut = (mibBytesOut - totalBytesOut);
        System.out.println("Delta in: " + deltaBytesIn);
        System.out.println("Delta in (%): " + ((1 - (double)totalBytesIn / mibBytesIn)) * 100 );
        System.out.println("Delta out: " + deltaBytesOut);
        System.out.println("Delta out (%): " + ((1 - (double)totalBytesOut / mibBytesOut)) * 100 );
    }

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

    @Override
    public CompletableFuture<DispatchStatus> send(TelemetryMessage message) {
        try {
            FlowMessage flowMessage = FlowMessage.parseFrom(message.getBuffer());
            if (flowMessage != null) {

                if (flowMessage.getFlowSeqNum().getValue() == 570258289) {
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
}
