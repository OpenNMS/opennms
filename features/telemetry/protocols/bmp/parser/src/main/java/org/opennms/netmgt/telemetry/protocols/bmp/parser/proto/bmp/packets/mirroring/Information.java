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
