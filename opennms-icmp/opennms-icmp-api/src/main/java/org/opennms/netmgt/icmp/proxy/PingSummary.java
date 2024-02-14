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
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class PingSummary {

    private final List<PingSequence> sequences = Lists.newArrayList();
    private final int numberRequests;
    private final PingRequest request;

    public PingSummary(PingRequest request, int numberOfRequests) {
        this.numberRequests = numberOfRequests;
        this.request = Objects.requireNonNull(request);
    }

    public void addSequence(int sequenceId, PingResponse pingResponse) {
        Objects.requireNonNull(pingResponse);
        sequences.add(new PingSequence(sequenceId, pingResponse));
    }

    public void addSequence(int sequenceId, Throwable exception) {
        Objects.requireNonNull(exception);
        sequences.add(new PingSequence(sequenceId, exception));
    }

    public boolean isComplete() {
        return numberRequests == sequences.size();
    }

    public boolean isSuccess() {
        return isComplete() && sequences.stream().filter(eachSequence -> eachSequence.isError() ||eachSequence.isTimeout()).count() == 0;
    }

    public PingSequence getSequence(int sequenceIndex) {
        return getSequences().get(sequenceIndex);
    }

    public List<PingSequence> getSequences() {
        return sequences.stream().sorted((s1, s2) -> Integer.compare(s1.getSequenceNumber(), s2.getSequenceNumber())).collect(Collectors.toList());
    }

    public PingRequest getRequest() {
        return request;
    }

    public void addSequence(PingSequence sequence) {
        sequences.add(sequence);
    }

    public int getNumberRequests() {
        return numberRequests;
    }
}
