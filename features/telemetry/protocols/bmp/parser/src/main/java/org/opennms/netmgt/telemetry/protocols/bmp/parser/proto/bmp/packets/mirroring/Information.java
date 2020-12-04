/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import io.netty.buffer.ByteBuf;

public class Information implements Mirroring {
    public final Code code;

    public Information(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) {
        this.code = Code.from(uint16(buffer));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public enum Code {
        ERRORED_PDU,
        MESSAGES_LOST,
        UNKNOWN;

        private static Code from(final int code) {
            switch (code) {
                case 0: return ERRORED_PDU;
                case 1: return MESSAGES_LOST;
                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Mirroring Information Code: {}", code);
                    return UNKNOWN;
            }
        }
    }
}
