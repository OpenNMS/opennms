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
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 *    The AS_PATHLIMIT attribute is a transitive optional BGP path
 *    attribute, with Type Code 21.  The AS_PATHLIMIT attribute has a fixed
 *    length of 5 octets.  The first octet is an unsigned number that is
 *    the upper bound on the number of ASes in the AS_PATH attribute of the
 *    associated paths.  One octet suffices because the TTL field of the IP
 *    header ensures that only one octet's worth of ASes can ever be
 *    traversed.  The second thru fifth octets are the AS number of the AS
 *    that attached the AS_PATHLIMIT attribute to the NLRI.
 */
public class AsPathLimit implements Attribute {
    public final int upperBound; // uint8
    public final long as; // uint32

    public AsPathLimit(final ByteBuf buffer, final PeerFlags flags) {
        this.upperBound = uint8(buffer);
        this.as = uint32(buffer);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("upperBound", this.upperBound)
                .add("as", this.as)
                .toString();
    }
}
