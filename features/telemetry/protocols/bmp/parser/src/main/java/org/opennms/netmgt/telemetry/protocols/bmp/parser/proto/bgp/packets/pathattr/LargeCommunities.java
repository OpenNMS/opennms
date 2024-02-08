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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;

import java.util.List;

import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                      Global Administrator                     |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                       Local Data Part 1                       |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                       Local Data Part 2                       |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class LargeCommunities implements Attribute {
    public final List<LargeCommunity> largeCommunities;

    public LargeCommunities(final ByteBuf buffer, final PeerFlags flags) {
        this.largeCommunities = BufferUtils.repeatRemaining(buffer, LargeCommunity::new);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("largeCommunities", this.largeCommunities)
                .toString();
    }

    public static class LargeCommunity {
        public final long globalAdministrator; // uint32
        public final long localDataPart1; // uint32
        public final long localDataPart2; // uint32

        public LargeCommunity(final ByteBuf buffer) {
            this.globalAdministrator = uint32(buffer);
            this.localDataPart1 = uint32(buffer);
            this.localDataPart2 = uint32(buffer);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("globalAdministrator", this.globalAdministrator)
                    .add("localDataPart1", this.localDataPart1)
                    .add("localDataPart2", this.localDataPart2)
                    .toString();
        }
    }
}
