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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;

// typedef opaque ip_v6[16];

public class IpV6 {
    public final Opaque<byte[]> ip_v6;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ip_v6", this.ip_v6)
                .toString();
    }

    public IpV6(final ByteBuffer buffer) throws InvalidPacketException {
        this.ip_v6 = new Opaque(buffer, Optional.of(16), Opaque::parseBytes);
    }

    public void writeBson(final BsonWriter bsonWriter) {
        try {
            bsonWriter.writeString(Inet6Address.getByAddress(this.ip_v6.value).getHostAddress());
        } catch (UnknownHostException e) {
            Throwables.propagate(e);
        }
    }}
