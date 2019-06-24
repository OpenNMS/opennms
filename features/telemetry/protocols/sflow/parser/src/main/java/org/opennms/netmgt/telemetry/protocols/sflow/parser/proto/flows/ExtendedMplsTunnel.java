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
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;

// struct extended_mpls_tunnel {
//    string tunnel_lsp_name<>;   /* Tunnel name */
//    unsigned int tunnel_id;     /* Tunnel ID */
//    unsigned int tunnel_cos;    /* Tunnel COS value */
// };

public class ExtendedMplsTunnel implements FlowData {
    public final AsciiString tunnel_lsp_name;
    public final long tunnel_id;
    public final long tunnel_cos;

    public ExtendedMplsTunnel(final ByteBuffer buffer) throws InvalidPacketException {
        this.tunnel_lsp_name = new AsciiString(buffer);
        this.tunnel_id = BufferUtils.uint32(buffer);
        this.tunnel_cos = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tunnel_lsp_name", this.tunnel_lsp_name)
                .add("tunnel_id", this.tunnel_id)
                .add("tunnel_cos", this.tunnel_cos)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("tunnel_lsp_name", this.tunnel_lsp_name.value);
        bsonWriter.writeInt64("tunnel_id", this.tunnel_id);
        bsonWriter.writeInt64("tunnel_cos", this.tunnel_cos);
        bsonWriter.writeEndDocument();
    }
}
