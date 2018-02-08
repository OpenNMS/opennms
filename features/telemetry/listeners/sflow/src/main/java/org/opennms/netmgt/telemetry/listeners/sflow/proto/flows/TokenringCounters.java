/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

// struct tokenring_counters {
//   unsigned int dot5StatsLineErrors;
//   unsigned int dot5StatsBurstErrors;
//   unsigned int dot5StatsACErrors;
//   unsigned int dot5StatsAbortTransErrors;
//   unsigned int dot5StatsInternalErrors;
//   unsigned int dot5StatsLostFrameErrors;
//   unsigned int dot5StatsReceiveCongestions;
//   unsigned int dot5StatsFrameCopiedErrors;
//   unsigned int dot5StatsTokenErrors;
//   unsigned int dot5StatsSoftErrors;
//   unsigned int dot5StatsHardErrors;
//   unsigned int dot5StatsSignalLoss;
//   unsigned int dot5StatsTransmitBeacons;
//   unsigned int dot5StatsRecoverys;
//   unsigned int dot5StatsLobeWires;
//   unsigned int dot5StatsRemoves;
//   unsigned int dot5StatsSingles;
//   unsigned int dot5StatsFreqErrors;
// };

public class TokenringCounters {
    public final long dot5StatsLineErrors;
    public final long dot5StatsBurstErrors;
    public final long dot5StatsACErrors;
    public final long dot5StatsAbortTransErrors;
    public final long dot5StatsInternalErrors;
    public final long dot5StatsLostFrameErrors;
    public final long dot5StatsReceiveCongestions;
    public final long dot5StatsFrameCopiedErrors;
    public final long dot5StatsTokenErrors;
    public final long dot5StatsSoftErrors;
    public final long dot5StatsHardErrors;
    public final long dot5StatsSignalLoss;
    public final long dot5StatsTransmitBeacons;
    public final long dot5StatsRecoverys;
    public final long dot5StatsLobeWires;
    public final long dot5StatsRemoves;
    public final long dot5StatsSingles;
    public final long dot5StatsFreqErrors;

    public TokenringCounters(final ByteBuffer buffer) throws InvalidPacketException {
        this.dot5StatsLineErrors = BufferUtils.uint32(buffer);
        this.dot5StatsBurstErrors = BufferUtils.uint32(buffer);
        this.dot5StatsACErrors = BufferUtils.uint32(buffer);
        this.dot5StatsAbortTransErrors = BufferUtils.uint32(buffer);
        this.dot5StatsInternalErrors = BufferUtils.uint32(buffer);
        this.dot5StatsLostFrameErrors = BufferUtils.uint32(buffer);
        this.dot5StatsReceiveCongestions = BufferUtils.uint32(buffer);
        this.dot5StatsFrameCopiedErrors = BufferUtils.uint32(buffer);
        this.dot5StatsTokenErrors = BufferUtils.uint32(buffer);
        this.dot5StatsSoftErrors = BufferUtils.uint32(buffer);
        this.dot5StatsHardErrors = BufferUtils.uint32(buffer);
        this.dot5StatsSignalLoss = BufferUtils.uint32(buffer);
        this.dot5StatsTransmitBeacons = BufferUtils.uint32(buffer);
        this.dot5StatsRecoverys = BufferUtils.uint32(buffer);
        this.dot5StatsLobeWires = BufferUtils.uint32(buffer);
        this.dot5StatsRemoves = BufferUtils.uint32(buffer);
        this.dot5StatsSingles = BufferUtils.uint32(buffer);
        this.dot5StatsFreqErrors = BufferUtils.uint32(buffer);
    }
}
