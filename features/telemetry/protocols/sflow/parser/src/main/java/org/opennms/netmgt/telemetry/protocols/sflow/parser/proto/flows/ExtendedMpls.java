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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

// struct extended_mpls { 
//    next_hop nexthop;           /* Address of the next hop */ 
//    label_stack in_stack;       /* Label stack of received packet */ 
//    label_stack out_stack;      /* Label stack for transmitted packet */ 
// };

public class ExtendedMpls implements FlowData {
    public final NextHop nexthop;
    public final LabelStack in_stack;
    public final LabelStack out_stack;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nexthop", this.nexthop)
                .add("in_stack", this.in_stack)
                .add("out_stack", this.out_stack)
                .toString();
    }

    public ExtendedMpls(final ByteBuffer buffer) throws InvalidPacketException {
        this.nexthop = new NextHop(buffer);
        this.in_stack = new LabelStack(buffer);
        this.out_stack = new LabelStack(buffer);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("nexthop");
        this.nexthop.writeBson(bsonWriter);
        bsonWriter.writeName("in_stack");
        this.in_stack.writeBson(bsonWriter);
        bsonWriter.writeName("out_stack");
        this.out_stack.writeBson(bsonWriter);
        bsonWriter.writeEndDocument();
    }
}
