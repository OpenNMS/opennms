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

import java.net.Inet6Address;
import java.net.UnknownHostException;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;

import io.netty.buffer.ByteBuf;

// typedef opaque ip_v6[16];

public class IpV6 {
    public final Inet6Address ip_v6;

    public IpV6(final ByteBuf buffer) {
        try {
            this.ip_v6 = (Inet6Address) Inet6Address.getByAddress(BufferUtils.bytes(buffer, 16));
        } catch (final UnknownHostException e) {
            // This only happens if byte array length is != 4
            throw Throwables.propagate(e);
        }
    }

    public Inet6Address getAddress() {
        return ip_v6;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ip_v6", this.ip_v6)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("address", this.ip_v6.getHostAddress());
        enr.getHostnameFor(this.ip_v6).ifPresent((hostname) -> bsonWriter.writeString("hostname", hostname));
        bsonWriter.writeEndDocument();
    }

    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
