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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;

import io.netty.buffer.ByteBuf;

public class InformationElement extends TLV<InformationElement.Type, String, Void> {

    public InformationElement(final ByteBuf buffer, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        super(buffer, Type::from, null, peerInfo);
    }

    public enum Type implements TLV.Type<String, Void> {
        STRING {
            @Override
            public String parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                return new String(bytes(buffer, buffer.readableBytes()), StandardCharsets.UTF_8);
            }
        },

        SYS_DESCR {
            @Override
            public String parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                return new String(bytes(buffer, buffer.readableBytes()), StandardCharsets.US_ASCII);
            }
        },

        SYS_NAME {
            @Override
            public String parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                return new String(bytes(buffer, buffer.readableBytes()), StandardCharsets.US_ASCII);
            }
        },

        VRF_TABLE_NAME {
            @Override
            public String parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                return new String(bytes(buffer, buffer.readableBytes()), StandardCharsets.US_ASCII);
            }
        },

        ADMIN_LABEL {
            @Override
            public String parse(ByteBuf buffer, Void parameter, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                return new String(bytes(buffer, buffer.readableBytes()), StandardCharsets.UTF_8);
            }
        },

        BGP_ID {
            @Override
            public String parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                return InetAddressUtils.toIpAddrString(bytes(buffer, buffer.readableBytes()));
	        }
	    },

        UNKNOWN {
            @Override
            public String parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                return "Unknown";
            }
        };

        private static Type from(final int type) {
            switch (type) {
                case 0:
                    return STRING;
                case 1:
                    return SYS_DESCR;
                case 2:
                    return SYS_NAME;
                case 3:
                    return VRF_TABLE_NAME;
                case 4:
                    return ADMIN_LABEL;
                case 65531:
                    return BGP_ID;
                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Information Element Type: {}", type);
                    return UNKNOWN;
            }
        }
    }
}
