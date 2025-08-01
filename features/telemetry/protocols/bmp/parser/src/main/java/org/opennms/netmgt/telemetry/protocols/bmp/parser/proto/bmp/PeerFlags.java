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
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;

import java.net.InetAddress;
import java.util.function.Function;

import org.opennms.core.utils.InetAddressUtils;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class PeerFlags {
    public enum AddressVersion {
        IP_V4,
        IP_V6;

        public <R> R map(final Function<AddressVersion, R> mapper) {
            return mapper.apply(this);
        }
    }
    public enum Policy {
        PRE_POLICY,
        POST_POLICY;

        public <R> R map(final Function<Policy, R> mapper) {
            return mapper.apply(this);
        }
    }

    public final AddressVersion addressVersion;  // uint8 x0000000
    public final Policy policy;                  // ..... 0x000000
    public final boolean legacyASPath;           // ..... 00x00000
    public final boolean adjIn;                  // ..... 000x0000

    public PeerFlags(final int flags) {
        this.addressVersion = ((flags >> 7 & 0x01) == 1) ? AddressVersion.IP_V6 : AddressVersion.IP_V4;
        this.policy = ((flags >> 6 & 0x01) == 1) ? Policy.POST_POLICY : Policy.PRE_POLICY;
        this.legacyASPath = (flags >> 5 & 0x01) == 1;
        this.adjIn = (flags >> 4 & 0x01) == 1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("addressVersion", this.addressVersion)
                .add("policy", this.policy)
                .add("legacyASPath", this.legacyASPath)
                .add("adjIn", this.adjIn)
                .toString();
    }

    public InetAddress parsePaddedAddress(final ByteBuf buffer) {
        return InetAddressUtils.getInetAddress(this.addressVersion.<byte[]>map(v -> {switch (v) {
            case IP_V4:
                skip(buffer, 12);
                return bytes(buffer, 4);
            case IP_V6:
                return bytes(buffer, 16);
            default:
                throw new IllegalStateException();
        }}));
    }

    public InetAddress parseAddress(final ByteBuf buffer) {
        return InetAddressUtils.getInetAddress(this.addressVersion.<byte[]>map(v -> {switch (v) {
            case IP_V4:
                return bytes(buffer, 4);
            case IP_V6:
                return bytes(buffer, 16);
            default:
                throw new IllegalStateException();
        }}));
    }

    public long parseAS(final ByteBuf buffer) {
        return this.legacyASPath
                ? uint16(buffer)
                : uint32(buffer);
    }
}
