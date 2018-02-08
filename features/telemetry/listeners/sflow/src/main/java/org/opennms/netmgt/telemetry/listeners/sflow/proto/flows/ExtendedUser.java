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

import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;

// struct extended_user {
//    charset src_charset;        /* Character set for src_user */
//    opaque src_user<>;          /* User ID associated with packet source */
//    charset dst_charset;        /* Character set for dst_user */
//    opaque dst_user<>;          /* User ID associated with packet destination */
// };

public class ExtendedUser {
    public final Charset src_charset;
    public final Opaque<byte[]> src_user;
    public final Charset dst_charset;
    public final Opaque<byte[]> dst_user;

    public ExtendedUser(final ByteBuffer buffer) throws InvalidPacketException {
        this.src_charset = new Charset(buffer);
        this.src_user = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
        this.dst_charset = new Charset(buffer);
        this.dst_user = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
    }
}
