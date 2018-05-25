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

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.base.MoreObjects;

// enum ieee80211_version {
//   a = 1,
//   b = 2,
//   g = 3,
//   n = 4
// };

public enum Ieee80211Version {
    a(1),
    b(2),
    g(3),
    n(4);

    public final int value;

    Ieee80211Version(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", this.value)
                .toString();
    }

    public static Ieee80211Version from(final ByteBuffer buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 1:
                return a;
            case 2:
                return b;
            case 3:
                return g;
            case 4:
                return n;
            default:
                throw new InvalidPacketException(buffer, "Unknown value: {}", value);
        }
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeInt32(this.value);
    }
}
