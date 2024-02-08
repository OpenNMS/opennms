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
import java.util.stream.LongStream;

class PingSummaryCalculator {

    private final List<PingSequence> sequences;

    PingSummaryCalculator(List<PingSequence> pingSequenceList) {
        this.sequences = Objects.requireNonNull(pingSequenceList);
    }

    int getPacketsTransmitted() {
        return sequences.size();
    }

    double getPacketLoss() {
        return 1.0 - (double) getPacketsReceived() / (double) getPacketsTransmitted();
    }

    int getPacketsReceived() {
        return (int) sequences.stream().filter(PingSequence::isSuccess).count();
    }

    long getMin() {
        return getDiffTimeNanos().min().orElse(0);
    }

    double getAvg() {
        return getDiffTimeNanos().average().orElse(0);
    }

    long getMax() {
        return getDiffTimeNanos().max().orElse(0);
    }

    double getStdDev() {
        return getAvg() - getMin();
    }

    private LongStream getDiffTimeNanos() {
        return sequences.stream().filter(PingSequence::isSuccess).mapToLong(eachSequence -> (long) eachSequence.getResponse().getRtt());
    }

}
