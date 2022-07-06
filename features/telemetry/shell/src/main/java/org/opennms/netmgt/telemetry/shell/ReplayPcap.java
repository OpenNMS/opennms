/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.api.TelemetryManager;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.listeners.UdpParser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.pkts.Pcap;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;

@Command(scope = "opennms", name = "telemetry-replay-pcap", description = "Replay a packet capture directly to a UDP-based parser.")
@Service
public class ReplayPcap implements Action {
    @Reference
    public TelemetryManager manager;

    @Argument(index = 0, name = "listener", description = "Listener name", required = true)
    @Completion(value = Parsers.ListenerCompleter.class)
    public String listenerName;

    @Argument(index = 1, name = "parser", description = "Parser name", required = true)
    public String parserName;

    @Argument(index = 2, name = "pcap", description = "Path to .pcap file for replay", required = true)
    public File pcapFile;

    @Option(name = "-c", aliases = "--counter", description = "Display packet counter every n-th packet. Set to <= 0 to disable.")
    public int packetCounterFrequency = 100;

    @Override
    public Object execute() throws Exception {
        final Listener listener = this.manager.getListeners().stream()
                .filter(l -> Objects.equals(l.getName(), this.listenerName))
                .min(Comparator.comparing(Listener::getName))
                .orElseThrow(() -> new RuntimeException(String.format("No listener with name '%s' found.", listenerName)));

        final Parser parser = listener.getParsers().stream()
                .filter(p -> Objects.equals(p.getName(), this.parserName))
                .min(Comparator.comparing(Parser::getName))
                .orElseThrow(() -> new RuntimeException(String.format("No parser with name '%s' found.", parserName)));

        if (!(parser instanceof UdpParser)) {
            throw new RuntimeException("Parser must implement UdpParser interface to be used to .pcap replay.");
        }
        final UdpParser udpParser = (UdpParser)parser;


        try (final InputStream in = new FileInputStream(pcapFile)) {
            System.out.printf("Processing packets from '%s'.\n", pcapFile);
            final AtomicLong packetCount = new AtomicLong();
            final Pcap pcap = Pcap.openStream(in);
            pcap.loop(packet -> {
                if (packet.hasProtocol(Protocol.UDP)) {
                    packetCount.getAndIncrement();
                    if (packetCounterFrequency > 0 && packetCount.get() % packetCounterFrequency == 0) {
                        System.out.printf("Processing packet #%d.\n", packetCount.get());
                    }
                    final UDPPacket udp = (UDPPacket) packet.getPacket(Protocol.UDP);

                    final InetSocketAddress remoteAddress = new InetSocketAddress(InetAddressUtils.getInetAddress(udp.getSourceIP()), udp.getSourcePort());
                    final InetSocketAddress localAddress = new InetSocketAddress(InetAddressUtils.getInetAddress(udp.getDestinationIP()), udp.getDestinationPort());

                    final ByteBuf buffer = Unpooled.wrappedBuffer(udp.getPayload().getArray());
                    try {
                        udpParser.parse(buffer, remoteAddress, localAddress);
                    } catch (final Exception e) {
                        System.err.printf("Failed to parse packet %s->%s@%s: %s", remoteAddress, localAddress, udp.getArrivalTime(), e.getMessage());
                    }
                }
                return true;
            });
            System.out.printf("Done processing %d packets.\n", packetCount.get());
        }
        return null;
    }
}
