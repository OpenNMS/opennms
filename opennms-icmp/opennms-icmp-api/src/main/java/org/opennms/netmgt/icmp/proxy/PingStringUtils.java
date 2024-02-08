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
