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
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Array;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedInteger;

// struct extended_vlantunnel { 
//   unsigned int stack<>;  /* List of stripped 802.1Q TPID/TCI layers. Each 
//                             TPID,TCI pair is represented as a single 32 bit 
//                             integer. Layers listed from outermost to 
//                             innermost. */ 
// };

public class ExtendedVlantunnel implements FlowData {
    public final Array<UnsignedInteger> stack;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("stack", this.stack)
                .toString();
    }

    public ExtendedVlantunnel(final ByteBuffer buffer) throws InvalidPacketException {
        this.stack = new Array(buffer, Optional.empty(), BufferUtils::uint32);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartArray();
        for (final UnsignedInteger unsignedInteger : stack) {
            bsonWriter.writeInt64(unsignedInteger.longValue());
        }
        bsonWriter.writeEndArray();
    }
}
