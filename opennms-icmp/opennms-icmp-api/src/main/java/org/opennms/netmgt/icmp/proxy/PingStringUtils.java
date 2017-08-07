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

package org.opennms.netmgt.icmp.proxy;

import java.util.List;
import java.util.Objects;

public class PingStringUtils {

    public static String renderSequence(PingRequest request, PingSequence sequence) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(sequence);

        final StringBuilder builder = new StringBuilder();
        if (sequence.isTimeout()) {
            builder.append(String.format("Request timeout for icmp_seq %s", sequence.getSequenceNumber()));
        }
        if (sequence.isError()) {
            builder.append(
                    String.format("Request error for icmp_seq %s: %s",
                            sequence.getSequenceNumber(),
                            sequence.getError().getMessage()));
        }
        if (sequence.isSuccess()) {
            builder.append(
                    String.format("%s bytes from %s: icmp_seq=%d time=%.3f ms",
                            request.getPacketSize(),
                            request.getInetAddress(),
                            sequence.getSequenceNumber(),
                            sequence.getResponse().getRtt()));
        }

        return builder.toString();
    }

    public static String renderHeader(PingSummary summary) {
        return String.format("PING %s: %d data bytes",  summary.getRequest().getInetAddress(), summary.getRequest().getPacketSize()-8);
    }

    public static String renderAll(PingSummary summary) {
        final StringBuilder builder = new StringBuilder();
        final List<PingSequence> sequences = summary.getSequences();
        builder.append(renderHeader(summary));
        builder.append("\n");
        for (PingSequence eachSequence : sequences) {
            builder.append(renderSequence(summary.getRequest(), eachSequence));
            builder.append("\n");
        }
        if (summary.isComplete()) {
            builder.append(renderSummary(summary));
        }
        return builder.toString();
    }

    public static String renderSummary(PingSummary summary) {
        final StringBuilder builder = new StringBuilder();
        PingSummaryCalculator calculator = new PingSummaryCalculator(summary.getSequences());
        builder.append("\n");
        builder.append(String.format("--- %s ping statistics ---", summary.getRequest().getInetAddress()));
        builder.append("\n");
        builder.append(String.format("%d packets transmitted, %d packets received, %.2f%% packet loss", calculator.getPacketsTransmitted(), calculator.getPacketsReceived(), calculator.getPacketLoss()));
        builder.append("\n");
        builder.append(String.format("round-trip min/avg/max/stddev = %.2f/%.2f/%.2f/%.2f ms", (double) calculator.getMin(), calculator.getAvg(), (double) calculator.getMax(), calculator.getStdDev()));
        builder.append("\n");
        return builder.toString();
    }
}
