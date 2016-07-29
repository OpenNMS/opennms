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

import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.EchoPacket;

class DummyEchoPacket implements EchoPacket {

    private final int sequenceNumber;
    private final int identifier;
    private final long receivedTimeNanos;
    private final long sentTimeNanos;

    DummyEchoPacket(int identifier, int sequenceNumber, long receivedTimeNanos, long sentTimeNanos) {
        this.identifier = identifier;
        this.sequenceNumber = sequenceNumber;
        this.receivedTimeNanos = receivedTimeNanos;
        this.sentTimeNanos = sentTimeNanos;
    }

    @Override
    public boolean isEchoReply() {
        return true;
    }

    @Override
    public int getIdentifier() {
        return identifier;
    }

    @Override
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public long getThreadId() {
        return 1;
    }

    @Override
    public long getReceivedTimeNanos() {
        return receivedTimeNanos;
    }

    @Override
    public long getSentTimeNanos() {
        return sentTimeNanos;
    }

    @Override
    public double elapsedTime(TimeUnit timeUnit) {
        return -1;
    }
}
