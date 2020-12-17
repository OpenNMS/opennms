package org.opennms.netmgt.telemetry.itests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
import com.google.protobuf.InvalidProtocolBufferException;

public class PCAPAnalysisIT implements AsyncDispatcher<TelemetryMessage> {

    // Inputs
    private static final String PCAP_FILE = "/Users/jwhite/labs/flow/agg003.pcap";
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
        for (FlowMessage flowMessage : flowMessages) {

            // the start of the flow must be before the end of the range
            if (flowMessage.getDeltaSwitched().getValue() <= END.getTimestamp()
                    // the end of the flow must be after the start of the range
                    && flowMessage.getLastSwitched().getValue() >= START.getTimestamp()) {
                // keep
            } else {
                // skip
                continue;
            }

            // the sample interval should be consistent for our experiment, log if this is not the case
            if (Math.abs(flowMessage.getSamplingInterval().getValue() - EXPECTED_SAMPLE_INTERVAL) > 0.01d) {
                System.out.println("sampling interval is: " + flowMessage.getSamplingInterval().getValue());
            }

            // tally the bytes if the flows meet the criteria
            if ((flowMessage.getDirection() == Direction.INGRESS && flowMessage.getInputSnmpIfindex().getValue() == INTERFACE_INDEX)) {
                totalBytesIn += flowMessage.getNumBytes().getValue() * flowMessage.getSamplingInterval().getValue();
            } else if (flowMessage.getDirection() == Direction.EGRESS && flowMessage.getOutputSnmpIfindex().getValue() == INTERFACE_INDEX) {
                totalBytesOut += flowMessage.getNumBytes().getValue() * flowMessage.getSamplingInterval().getValue();
            } else {
                // skip
                continue;
            }

            // track the sequence numbers for all of the flows that matches our filters and were included in the tally
            seqNumbersInFilter.add(flowMessage.getFlowSeqNum().getValue());
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
        System.out.println("Total bytes in: " + totalBytesIn);
        System.out.println("Total bytes out: " + totalBytesOut);

        // bytes from counters
        long mibBytesIn = (END.getBytesIn() - START.getBytesIn());
        long mibBytesOut = (END.getBytesOut() - START.getBytesOut());
        System.out.println("Total bytes in (reference) : " + mibBytesIn);
        System.out.println("Total bytes out (reference): " + mibBytesOut);

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
