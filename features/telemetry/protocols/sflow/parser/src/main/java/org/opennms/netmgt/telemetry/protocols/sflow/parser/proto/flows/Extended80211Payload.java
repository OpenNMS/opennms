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
import java.util.Optional;

import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;

import com.google.common.base.MoreObjects;

// struct extended_80211_payload {
//    cipher_suite ciphersuite; /* encryption scheme used for this packet */
//    opaque       data<>;      /* unencrypted bytes from the payload */
// };

public class Extended80211Payload implements FlowData {
    public final CipherSuite ciphersuite;
    public final Opaque<byte[]> data;

    public Extended80211Payload(final ByteBuffer buffer) throws InvalidPacketException {
        this.ciphersuite = new CipherSuite(buffer);
        this.data = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ciphersuite", this.ciphersuite)
                .add("data", this.data)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("ciphersuite");
        this.ciphersuite.writeBson(bsonWriter);
        bsonWriter.writeBinaryData("data", new BsonBinary(this.data.value));
        bsonWriter.writeEndDocument();
    }
}
