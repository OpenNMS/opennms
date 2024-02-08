/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
