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

package org.opennms.netmgt.telemetry.listeners.sflow.proto;

/* sFlowDataSource encoded as follows:
     The most significant byte of the source_id is used to indicate the type
     of sFlowDataSource:
        0 = ifIndex
        1 = smonVlanDataSource
        2 = entPhysicalEntry
     The lower three bytes contain the relevant index value. */

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.base.MoreObjects;

public class DataSource {

    public enum Type {
        IF_INDEX,
        SMON_VLAN_DATA_SOURCE,
        ENT_PHYSICAL_ENTRY;

        public static Type from(final ByteBuffer buffer) throws InvalidPacketException {
            final int type = BufferUtils.uint8(buffer);
            switch (type) {
                case 0:
                    return IF_INDEX;
                case 1:
                    return SMON_VLAN_DATA_SOURCE;
                case 2:
                    return ENT_PHYSICAL_ENTRY;
                default:
                    throw new InvalidPacketException(buffer, "Unknown data source type: %d", type);
            }
        }
    }

    public final Type type;
    public final long index;

    public DataSource(final ByteBuffer buffer) throws InvalidPacketException {
        this.type = Type.from(buffer);
        this.index = BufferUtils.uint24(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("index", index)
                .toString();
    }
}
