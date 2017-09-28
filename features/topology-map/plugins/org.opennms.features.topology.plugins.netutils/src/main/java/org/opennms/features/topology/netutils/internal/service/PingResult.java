/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;

import com.google.common.collect.Lists;

public class PingResult {
    private final List<PingSequence> sequences = Lists.newArrayList();

    private final PingRequest pingRequest;

    public PingResult(PingRequest pingRequest) {
        this.pingRequest = Objects.requireNonNull(pingRequest);
    }

    public void addSequence(PingSequence sequence) {
        Objects.requireNonNull(sequence);
        sequences.add(sequence);
    }

    public List<PingSequence> getSequences() {
        return sequences.stream()
                .sorted((o1, o2) -> Integer.compare(o1.getSequenceNumber(), o2.getSequenceNumber()))
                .collect(Collectors.toList());
    }

    public PingRequest getRequest() {
        return pingRequest;
    }

    public boolean hasSequence(int sequenceNumber) {
        return sequences.stream()
                .filter(eachSequence -> sequenceNumber == eachSequence.getSequenceNumber())
                .findFirst().isPresent();
    }

    public boolean isComplete() {
        return pingRequest.getNumberRequests() == sequences.size();
    }

    public PingResultSummary getSummary() {
        return new PingResultSummary(this);
    }

    public String toDetailString() {
        final String ipAddress = InetAddressUtils.toIpAddrString(getRequest().getInetAddress());
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("PING %s: %d data bytes", ipAddress, getRequest().getPackageSize()-8));
        builder.append("\n");
        for (PingSequence eachSequence : getSequences()) {
            if (eachSequence.isTimeout()) {
                builder.append(String.format("Request timeout for icmp_seq %s", eachSequence.getSequenceNumber()));
            }
            if (eachSequence.isError()) {
                builder.append(
                        String.format("Request error for icmp_seq %s: %s",
                                eachSequence.getSequenceNumber(),
                                eachSequence.getError().getMessage()));
            }
            if (eachSequence.isSuccess()) {
                builder.append(
                        String.format("%s bytes from %s: icmp_seq=%d time=%d ms",
                                getRequest().getPackageSize(),
                                ipAddress,
                                eachSequence.getResponse().getSequenceNumber(),
                                eachSequence.getResponse().getReceivedTimeNanos() - eachSequence.getResponse().getSentTimeNanos()));
            }
            builder.append("\n");
        }
        if (isComplete()) {
            PingResultSummary summary = getSummary();
            builder.append("\n");
            builder.append(String.format("--- %s ping statistics ---", ipAddress));
            builder.append("\n");
            builder.append(String.format("%d packets transmitted, %d packets received, %.2f%% packet loss", summary.getPacketsTransmitted(), summary.getPacketsReceived(), summary.getPacketLoss()));
            builder.append("\n");
            builder.append(String.format("round-trip min/avg/max/stddev = %.2f/%.2f/%.2f/%.2f ms", (double) summary.getMin(), summary.getAvg(), (double) summary.getMax(), summary.getStdDev()));
            builder.append("\n");
        }
        return builder.toString();
    }
}
