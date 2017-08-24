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
