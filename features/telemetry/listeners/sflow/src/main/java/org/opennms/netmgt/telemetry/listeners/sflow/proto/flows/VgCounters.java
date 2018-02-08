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

import com.google.common.primitives.UnsignedLong;

// struct vg_counters {
//   unsigned int dot12InHighPriorityFrames;
//   unsigned hyper dot12InHighPriorityOctets;
//   unsigned int dot12InNormPriorityFrames;
//   unsigned hyper dot12InNormPriorityOctets;
//   unsigned int dot12InIPMErrors;
//   unsigned int dot12InOversizeFrameErrors;
//   unsigned int dot12InDataErrors;
//   unsigned int dot12InNullAddressedFrames;
//   unsigned int dot12OutHighPriorityFrames;
//   unsigned hyper dot12OutHighPriorityOctets;
//   unsigned int dot12TransitionIntoTrainings;
//   unsigned hyper dot12HCInHighPriorityOctets;
//   unsigned hyper dot12HCInNormPriorityOctets;
//   unsigned hyper dot12HCOutHighPriorityOctets;
// };

public class VgCounters {
    public final long dot12InHighPriorityFrames;
    public final UnsignedLong dot12InHighPriorityOctets;
    public final long dot12InNormPriorityFrames;
    public final UnsignedLong dot12InNormPriorityOctets;
    public final long dot12InIPMErrors;
    public final long dot12InOversizeFrameErrors;
    public final long dot12InDataErrors;
    public final long dot12InNullAddressedFrames;
    public final long dot12OutHighPriorityFrames;
    public final UnsignedLong dot12OutHighPriorityOctets;
    public final long dot12TransitionIntoTrainings;
    public final UnsignedLong dot12HCInHighPriorityOctets;
    public final UnsignedLong dot12HCInNormPriorityOctets;
    public final UnsignedLong dot12HCOutHighPriorityOctets;

    public VgCounters(final ByteBuffer buffer) throws InvalidPacketException {
        this.dot12InHighPriorityFrames = BufferUtils.uint32(buffer);
        this.dot12InHighPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12InNormPriorityFrames = BufferUtils.uint32(buffer);
        this.dot12InNormPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12InIPMErrors = BufferUtils.uint32(buffer);
        this.dot12InOversizeFrameErrors = BufferUtils.uint32(buffer);
        this.dot12InDataErrors = BufferUtils.uint32(buffer);
        this.dot12InNullAddressedFrames = BufferUtils.uint32(buffer);
        this.dot12OutHighPriorityFrames = BufferUtils.uint32(buffer);
        this.dot12OutHighPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12TransitionIntoTrainings = BufferUtils.uint32(buffer);
        this.dot12HCInHighPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12HCInNormPriorityOctets = BufferUtils.uint64(buffer);
        this.dot12HCOutHighPriorityOctets = BufferUtils.uint64(buffer);
    }
}
